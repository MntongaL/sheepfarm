-- V20: link medications to the administration_type and medicine_category lookups and to a farm, and track stock.
-- Starting schema (V1, V10, V13): medications(id uuid, name varchar(160) UNIQUE, category varchar(100),
--   administration_type varchar(100), ...) is a global catalogue; the lookups have smallint ids with unique codes.
-- Expects V19 to be applied. Never edit this file after it has been applied.

-- ---------------------------------------------------------------------------
-- Preflight
-- ---------------------------------------------------------------------------
DO $$
DECLARE
    col text;
BEGIN
    FOREACH col IN ARRAY ARRAY['id','name','category','administration_type'] LOOP
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema = 'public' AND table_name = 'medications' AND column_name = col) THEN
            RAISE EXCEPTION 'Preflight failed: medications.% is missing', col;
        END IF;
    END LOOP;

    FOREACH col IN ARRAY ARRAY['category_id','administration_type_id','farm_id',
                               'quantity_purchased','quantity_remaining','quantity_unit'] LOOP
        IF EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema = 'public' AND table_name = 'medications' AND column_name = col) THEN
            RAISE EXCEPTION 'Preflight failed: medications.% already exists', col;
        END IF;
    END LOOP;

    IF to_regclass('public.administration_type') IS NULL OR to_regclass('public.medicine_category') IS NULL
       OR to_regclass('public.farm') IS NULL THEN
        RAISE EXCEPTION 'Preflight failed: administration_type, medicine_category or farm table is missing (V10).';
    END IF;

    IF (SELECT data_type FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'administration_type' AND column_name = 'id') <> 'smallint'
       OR (SELECT data_type FROM information_schema.columns
           WHERE table_schema = 'public' AND table_name = 'medicine_category' AND column_name = 'id') <> 'smallint' THEN
        RAISE EXCEPTION 'Preflight failed: lookup ids are not smallint; change category_id / administration_type_id to match.';
    END IF;

    IF (SELECT data_type FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'farm' AND column_name = 'id') <> 'uuid' THEN
        RAISE EXCEPTION 'Preflight failed: farm.id is not uuid.';
    END IF;

    -- The case-insensitive name indexes below would fail on names that differ only by case.
    IF EXISTS (SELECT 1 FROM medications GROUP BY lower(btrim(name)) HAVING count(*) > 1) THEN
        RAISE EXCEPTION 'Preflight failed: medications has names that differ only by case/whitespace; resolve them first.';
    END IF;
END $$;

-- ---------------------------------------------------------------------------
-- 1. Lookup links. The old text columns stay untouched (nothing is discarded); the new columns hold the real
--    foreign keys. Existing text is matched to a lookup row by id, code or name, and only when it matches
--    exactly one row. Anything else stays NULL and keeps its legacy text for manual review.
-- ---------------------------------------------------------------------------
ALTER TABLE medications
    ADD COLUMN category_id            smallint,
    ADD COLUMN administration_type_id smallint;

WITH hits AS (
    SELECT m.id AS medication_id, c.id AS lookup_id, count(*) OVER (PARTITION BY m.id) AS n
    FROM medications m
    JOIN medicine_category c
      ON btrim(m.category) = c.id::text
      OR upper(btrim(m.category)) = c.code
      OR lower(btrim(m.category)) = lower(c.name)
    WHERE m.category IS NOT NULL AND btrim(m.category) <> ''
)
UPDATE medications m SET category_id = hits.lookup_id
FROM hits WHERE hits.medication_id = m.id AND hits.n = 1;

WITH hits AS (
    SELECT m.id AS medication_id, a.id AS lookup_id, count(*) OVER (PARTITION BY m.id) AS n
    FROM medications m
    JOIN administration_type a
      ON btrim(m.administration_type) = a.id::text
      OR upper(btrim(m.administration_type)) = a.code
      OR lower(btrim(m.administration_type)) = lower(a.name)
    WHERE m.administration_type IS NOT NULL AND btrim(m.administration_type) <> ''
)
UPDATE medications m SET administration_type_id = hits.lookup_id
FROM hits WHERE hits.medication_id = m.id AND hits.n = 1;

ALTER TABLE medications
    ADD CONSTRAINT fk_medications_category
        FOREIGN KEY (category_id) REFERENCES medicine_category (id) ON DELETE RESTRICT,
    ADD CONSTRAINT fk_medications_administration_type
        FOREIGN KEY (administration_type_id) REFERENCES administration_type (id) ON DELETE RESTRICT;

CREATE INDEX ix_medications_category            ON medications (category_id)            WHERE category_id IS NOT NULL;
CREATE INDEX ix_medications_administration_type ON medications (administration_type_id) WHERE administration_type_id IS NOT NULL;

COMMENT ON COLUMN medications.category IS 'Legacy free text from before V20. Use category_id.';
COMMENT ON COLUMN medications.administration_type IS 'Legacy free text from before V20. Use administration_type_id.';

-- ---------------------------------------------------------------------------
-- 2. Farm link. NULL farm_id = shared catalogue entry (every existing row, since none has a farm and none is
--    guessed); a value makes the medication belong to that farm.
-- ---------------------------------------------------------------------------
ALTER TABLE medications
    ADD COLUMN farm_id uuid;

ALTER TABLE medications
    ADD CONSTRAINT fk_medications_farm FOREIGN KEY (farm_id) REFERENCES farm (id) ON DELETE RESTRICT;

CREATE INDEX ix_medications_farm ON medications (farm_id) WHERE farm_id IS NOT NULL;

-- Names were globally unique (V1). With farm ownership they must be unique per farm instead, and among the shared
-- entries. The old constraint is looked up by definition because V1 did not name it.
DO $$
DECLARE
    old_name text;
BEGIN
    SELECT c.conname INTO old_name
    FROM pg_constraint c
    WHERE c.conrelid = 'public.medications'::regclass AND c.contype = 'u'
      AND c.conkey = ARRAY[(SELECT a.attnum FROM pg_attribute a
                            WHERE a.attrelid = 'public.medications'::regclass AND a.attname = 'name')];
    IF old_name IS NULL THEN
        RAISE EXCEPTION 'Preflight failed: the unique constraint on medications.name was not found.';
    END IF;
    EXECUTE format('ALTER TABLE public.medications DROP CONSTRAINT %I', old_name);
END $$;

CREATE UNIQUE INDEX uq_medications_farm_name   ON medications (farm_id, lower(btrim(name))) WHERE farm_id IS NOT NULL;
CREATE UNIQUE INDEX uq_medications_shared_name ON medications (lower(btrim(name)))          WHERE farm_id IS NULL;

-- ---------------------------------------------------------------------------
-- 3. Stock. Unit is required once a quantity is recorded, so the numbers always mean something.
-- ---------------------------------------------------------------------------
ALTER TABLE medications
    ADD COLUMN quantity_purchased numeric(12,3),   -- quantity when bought
    ADD COLUMN quantity_remaining numeric(12,3),
    ADD COLUMN quantity_unit      varchar(30);

ALTER TABLE medications
    ADD CONSTRAINT ck_medications_qty_purchased CHECK (quantity_purchased IS NULL OR quantity_purchased > 0),
    ADD CONSTRAINT ck_medications_qty_remaining CHECK (quantity_remaining IS NULL OR quantity_remaining >= 0),
    ADD CONSTRAINT ck_medications_qty_balance   CHECK (quantity_remaining IS NULL OR quantity_purchased IS NULL
                                                       OR quantity_remaining <= quantity_purchased),
    ADD CONSTRAINT ck_medications_qty_unit      CHECK ((quantity_purchased IS NULL AND quantity_remaining IS NULL)
                                                       OR (quantity_unit IS NOT NULL AND length(btrim(quantity_unit)) > 0));
