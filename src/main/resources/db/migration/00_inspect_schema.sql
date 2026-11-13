-- Run these against a COPY of your database and send me the output (no secrets in it).
-- They show exactly what the migrations below must match.

-- 1. Tables and columns, types, nullability, defaults
SELECT table_name, column_name, data_type, character_maximum_length, numeric_precision, numeric_scale,
       is_nullable, column_default
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name IN ('farm','sheep','medication','medication_category','administration_type','treatments')
ORDER BY table_name, ordinal_position;

-- 2. Constraints (PK, FK, unique, check)
SELECT conrelid::regclass AS table_name, conname, contype, pg_get_constraintdef(oid) AS definition
FROM pg_constraint
WHERE connamespace = 'public'::regnamespace
  AND conrelid::regclass::text IN ('farm','sheep','medication','medication_category','administration_type','treatments')
ORDER BY 1, 3, 2;

-- 3. Indexes
SELECT tablename, indexname, indexdef FROM pg_indexes
WHERE schemaname = 'public'
  AND tablename IN ('farm','sheep','medication','medication_category','administration_type','treatments')
ORDER BY 1, 2;

-- 4. Flyway history (last applied version -> pick the next version number)
SELECT installed_rank, version, description, success FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 10;

-- 5. Does medication belong to a farm? (decides the medication farm-consistency constraint)
SELECT EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema='public' AND table_name='medication' AND column_name='farm_id') AS medication_has_farm_id;

-- 6. Existing treatments whose sheep/medication belong to a DIFFERENT farm (must be 0 before validating V15)
SELECT count(*) AS treatments_with_sheep_in_other_farm
FROM treatments t JOIN sheep s ON s.id = t.sheep_id WHERE s.farm_id IS DISTINCT FROM t.farm_id;
