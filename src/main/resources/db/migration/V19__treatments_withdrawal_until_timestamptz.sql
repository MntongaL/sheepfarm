-- V19: treatments.withdrawal_until becomes timestamptz so it matches Treatment.withdrawalUntil (OffsetDateTime).
-- V18 already converted administered_at; this finishes the pair. Existing dates become midnight UTC of the same day.

DO $$
BEGIN
    IF (SELECT data_type FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'treatments' AND column_name = 'withdrawal_until')
       IS DISTINCT FROM 'date' THEN
        RAISE EXCEPTION 'Preflight failed: treatments.withdrawal_until is missing or not date (already converted?).';
    END IF;
END $$;

ALTER TABLE treatments
    ALTER COLUMN withdrawal_until
    TYPE TIMESTAMP(6) WITH TIME ZONE
    USING (withdrawal_until::TIMESTAMP AT TIME ZONE 'UTC');
