-- V21: remove the legacy free-text category and administration_type columns from medications.
-- Run after V20. Medications now use category_id and administration_type_id (foreign keys to the lookups).
-- Destructive: the dropped text cannot be recovered. The preflight stops the migration if any medication still
-- has legacy text that V20 could not map to a lookup row, so nothing is lost silently.
-- Never edit this file after it has been applied.

DO $$
DECLARE
    unmapped_categories int;
    unmapped_admin_types int;
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema = 'public' AND table_name = 'medications' AND column_name = 'category_id')
       OR NOT EXISTS (SELECT 1 FROM information_schema.columns
                      WHERE table_schema = 'public' AND table_name = 'medications' AND column_name = 'administration_type_id') THEN
        RAISE EXCEPTION 'Preflight failed: medications.category_id / administration_type_id are missing (V20 not applied).';
    END IF;

    SELECT count(*) INTO unmapped_categories
    FROM medications
    WHERE category IS NOT NULL AND btrim(category) <> '' AND category_id IS NULL;

    SELECT count(*) INTO unmapped_admin_types
    FROM medications
    WHERE administration_type IS NOT NULL AND btrim(administration_type) <> '' AND administration_type_id IS NULL;

    IF unmapped_categories > 0 OR unmapped_admin_types > 0 THEN
        RAISE EXCEPTION 'Preflight failed: % medication(s) have a category and % have an administration type that were not mapped to a lookup. Set category_id / administration_type_id for them first (see: SELECT id, name, category, administration_type FROM medications WHERE (category_id IS NULL AND category IS NOT NULL) OR (administration_type_id IS NULL AND administration_type IS NOT NULL)).',
            unmapped_categories, unmapped_admin_types;
    END IF;
END $$;

ALTER TABLE medications
    DROP COLUMN category,
    DROP COLUMN administration_type;