-- V11: preflight, sheep name/breed, farm-scoped unique keys (needed for composite foreign keys), indexes.
-- Never edit this file after it has been applied.

-- ---- Preflight: fail fast with a clear message if the schema is not what these migrations assume ----
DO $$
DECLARE
    req text[][] := ARRAY[
        ['farm','id'],
        ['sheep','id'], ['sheep','farm_id'], ['sheep','birth_year'], ['sheep','birth_month'],
        ['medications','id'],
        ['treatments','id'], ['treatments','farm_id'], ['treatments','sheep_id'], ['treatments','medication_id']];
    i int;
    missing text := '';
BEGIN
    FOR i IN 1 .. array_length(req, 1) LOOP
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema = 'public' AND table_name = req[i][1] AND column_name = req[i][2]) THEN
            missing := missing || format(' %s.%s;', req[i][1], req[i][2]);
        END IF;
    END LOOP;
    IF missing <> '' THEN
        RAISE EXCEPTION 'Preflight failed, missing columns:% Adjust table/column names in V11-V15 to your schema.', missing;
    END IF;

    -- New foreign-key columns are uuid, so the referenced ids must be uuid too.
    FOR i IN 1 .. 4 LOOP
        IF (SELECT data_type FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = (ARRAY['farm','sheep','medications','treatments'])[i]
              AND column_name = 'id') <> 'uuid' THEN
            RAISE EXCEPTION 'Table % has a non-uuid id; change the uuid columns in V12-V14 to match.',
                (ARRAY['farm','sheep','medications','treatments'])[i];
        END IF;
    END LOOP;
END $$;

-- ---- Sheep: nullable name and breed. Existing animals stay unnamed; nothing is invented.
-- IF NOT EXISTS makes each a no-op if the column already exists. ----
ALTER TABLE sheep ADD COLUMN IF NOT EXISTS name  varchar(100);
ALTER TABLE sheep ADD COLUMN IF NOT EXISTS breed varchar(100);

-- ---- Farm-scoped unique keys. (farm_id, id) is trivially unique because id is already unique;
-- they exist only so other tables can reference "this sheep AND this farm" together.
-- medications has no farm_id (global catalogue), so it gets none. ----
ALTER TABLE sheep      ADD CONSTRAINT uq_sheep_farm_id      UNIQUE (farm_id, id);
ALTER TABLE treatments ADD CONSTRAINT uq_treatment_farm_id  UNIQUE (farm_id, id);

-- ---- Indexes. Existing: ix_sheep_farm (farm_id), ix_sheep_birth (birth_year, birth_month).
-- Added: farm-scoped birth lookup for the "sheep of farm X born in year/month Y" selection. ----
CREATE INDEX IF NOT EXISTS ix_sheep_farm_birth ON sheep (farm_id, birth_year, birth_month);
