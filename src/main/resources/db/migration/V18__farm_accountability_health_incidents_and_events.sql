-- V16: user accountability, farm health incidents, farm event history.
-- Starting schema (verified from V1, V10-V15 and the entities): Flyway version 15, PostgreSQL 17.6.
--   users.id, farm.id, sheep.id, treatments.id, treatment_plan.id, planned_sheep_treatment.id are uuid.
--   treatments.administered_by is varchar(160) (free text) and is preserved in administered_by_legacy.
--   uq_sheep_farm_id (V11), uq_treatment_farm_id (V11) and set_updated_at() (V10) already exist.
-- Never edit this file after it has been applied.

-- ---------------------------------------------------------------------------
-- Preflight: fail visibly if the schema is not what this migration assumes.
-- ---------------------------------------------------------------------------
DO $$
DECLARE
    req       text[][] := ARRAY[
        ['users','id'], ['farm','id'], ['sheep','id'], ['sheep','farm_id'],
        ['treatments','id'], ['treatments','farm_id'], ['treatments','administered_by'],
        ['treatment_plan','id'], ['treatment_plan','status'],
        ['planned_sheep_treatment','id'], ['planned_sheep_treatment','farm_id'], ['planned_sheep_treatment','status']];
    conflicts text[][] := ARRAY[
        ['farm','created_by'], ['farm','updated_by'],
        ['treatment_plan','created_by'], ['treatment_plan','updated_by'], ['treatment_plan','completed_by'],
        ['planned_sheep_treatment','completed_by'],
        ['treatments','recorded_by'], ['treatments','administered_by_legacy']];
    i int;
BEGIN
    FOR i IN 1 .. array_length(req, 1) LOOP
        IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                       WHERE table_schema = 'public' AND table_name = req[i][1] AND column_name = req[i][2]) THEN
            RAISE EXCEPTION 'Preflight failed: missing %.%', req[i][1], req[i][2];
        END IF;
    END LOOP;

    FOR i IN 1 .. array_length(conflicts, 1) LOOP
        IF EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema = 'public' AND table_name = conflicts[i][1] AND column_name = conflicts[i][2]) THEN
            RAISE EXCEPTION 'Preflight failed: %.% already exists', conflicts[i][1], conflicts[i][2];
        END IF;
    END LOOP;

    IF (SELECT data_type FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'users' AND column_name = 'id') <> 'uuid' THEN
        RAISE EXCEPTION 'Preflight failed: users.id is not uuid; change the user reference columns below to match.';
    END IF;

    IF (SELECT data_type FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'treatments' AND column_name = 'administered_by') <> 'character varying' THEN
        RAISE EXCEPTION 'Preflight failed: treatments.administered_by is not varchar (already migrated?).';
    END IF;

    IF to_regclass('public.farm_health_incident') IS NOT NULL OR to_regclass('public.farm_event') IS NOT NULL THEN
        RAISE EXCEPTION 'Preflight failed: farm_health_incident or farm_event already exists.';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uq_sheep_farm_id' AND conrelid = 'public.sheep'::regclass)
       OR NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uq_treatment_farm_id' AND conrelid = 'public.treatments'::regclass) THEN
        RAISE EXCEPTION 'Preflight failed: V11 farm-scoped unique keys (uq_sheep_farm_id / uq_treatment_farm_id) are missing.';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_proc p JOIN pg_namespace n ON n.oid = p.pronamespace
                   WHERE n.nspname = 'public' AND p.proname = 'set_updated_at') THEN
        RAISE EXCEPTION 'Preflight failed: function set_updated_at() (V10) is missing.';
    END IF;
END $$;

-- ---------------------------------------------------------------------------
-- A. farm: who created / last updated it.
-- Nullable + ON DELETE SET NULL: historical rows have no author, and deleting a user must not delete or block farms.
-- ---------------------------------------------------------------------------
ALTER TABLE farm
    ADD COLUMN created_by uuid,
    ADD COLUMN updated_by uuid;

ALTER TABLE farm
    ADD CONSTRAINT fk_farm_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_farm_updated_by FOREIGN KEY (updated_by) REFERENCES users (id) ON DELETE SET NULL;

-- ---------------------------------------------------------------------------
-- B. treatments: administered_by becomes a user reference; the old free text is kept, not discarded.
-- Nothing is cast or guessed: existing rows get NULL in the new columns and keep their text in administered_by_legacy.
-- ---------------------------------------------------------------------------
ALTER TABLE treatments RENAME COLUMN administered_by TO administered_by_legacy;
COMMENT ON COLUMN treatments.administered_by_legacy IS
    'Free-text administrator name captured before V16. Kept for history; new rows use administered_by (users.id).';

ALTER TABLE treatments
    ALTER COLUMN administered_at
    TYPE TIMESTAMP(6) WITH TIME ZONE
    USING (administered_at::TIMESTAMP AT TIME ZONE 'UTC');

ALTER TABLE treatments
    ADD COLUMN administered_by uuid,   -- person who administered the medication
    ADD COLUMN recorded_by     uuid;   -- person who entered the record

ALTER TABLE treatments
    ADD CONSTRAINT fk_treatments_administered_by FOREIGN KEY (administered_by) REFERENCES users (id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_treatments_recorded_by     FOREIGN KEY (recorded_by)     REFERENCES users (id) ON DELETE SET NULL;

CREATE INDEX ix_treatments_administered_by ON treatments (administered_by) WHERE administered_by IS NOT NULL;
CREATE INDEX ix_treatments_recorded_by     ON treatments (recorded_by)     WHERE recorded_by IS NOT NULL;

-- ---------------------------------------------------------------------------
-- C. treatment_plan and planned_sheep_treatment accountability.
-- completed_treatment_id and all existing plan columns are untouched.
-- completed_by follows the existing pattern (ck_tps_completed, ck_pst_completed_ref): only set when COMPLETED.
-- ---------------------------------------------------------------------------
ALTER TABLE treatment_plan
    ADD COLUMN created_by   uuid,
    ADD COLUMN updated_by   uuid,
    ADD COLUMN completed_by uuid;

ALTER TABLE treatment_plan
    ADD CONSTRAINT fk_treatment_plan_created_by   FOREIGN KEY (created_by)   REFERENCES users (id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_treatment_plan_updated_by   FOREIGN KEY (updated_by)   REFERENCES users (id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_treatment_plan_completed_by FOREIGN KEY (completed_by) REFERENCES users (id) ON DELETE SET NULL,
    ADD CONSTRAINT ck_treatment_plan_completed_by CHECK (completed_by IS NULL OR status = 'COMPLETED');

ALTER TABLE planned_sheep_treatment
    ADD COLUMN completed_by uuid;

ALTER TABLE planned_sheep_treatment
    ADD CONSTRAINT fk_pst_completed_by FOREIGN KEY (completed_by) REFERENCES users (id) ON DELETE SET NULL,
    ADD CONSTRAINT ck_pst_completed_by CHECK (completed_by IS NULL OR status = 'COMPLETED'),
    -- target for the farm-consistent composite key used by farm_event
    ADD CONSTRAINT uq_pst_farm_id UNIQUE (farm_id, id);

-- ---------------------------------------------------------------------------
-- D. farm_health_incident
-- ---------------------------------------------------------------------------
CREATE TABLE farm_health_incident (
    id              uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    farm_id         uuid         NOT NULL,
    sheep_id        uuid         NOT NULL,
    incident_number varchar(50),                                   -- optional, unique per farm when present
    title           varchar(200) NOT NULL,
    incident_type   varchar(40)  NOT NULL,                         -- open vocabulary, UPPER_SNAKE_CASE
    severity        varchar(20)  NOT NULL DEFAULT 'MEDIUM',
    status          varchar(30)  NOT NULL DEFAULT 'OPEN',
    observed_at     timestamptz  NOT NULL DEFAULT now(),
    reported_by     uuid,
    assigned_to     uuid,
    symptoms        text,
    diagnosis       text,                                          -- NULL until known
    notes           text,
    follow_up_at    timestamptz,
    resolved_at     timestamptz,
    resolved_by     uuid,
    outcome         text,
    created_at      timestamptz  NOT NULL DEFAULT now(),
    updated_at      timestamptz  NOT NULL DEFAULT now(),

    CONSTRAINT fk_fhi_farm        FOREIGN KEY (farm_id)           REFERENCES farm (id)           ON DELETE RESTRICT,
    -- composite key: the sheep must belong to the same farm as the incident
    CONSTRAINT fk_fhi_sheep       FOREIGN KEY (farm_id, sheep_id) REFERENCES sheep (farm_id, id) ON DELETE RESTRICT,
    CONSTRAINT fk_fhi_reported_by FOREIGN KEY (reported_by)       REFERENCES users (id)          ON DELETE SET NULL,
    CONSTRAINT fk_fhi_assigned_to FOREIGN KEY (assigned_to)       REFERENCES users (id)          ON DELETE SET NULL,
    CONSTRAINT fk_fhi_resolved_by FOREIGN KEY (resolved_by)       REFERENCES users (id)          ON DELETE SET NULL,
    CONSTRAINT uq_fhi_farm_id     UNIQUE (farm_id, id),            -- target for farm_event composite key

    CONSTRAINT ck_fhi_title        CHECK (length(btrim(title)) > 0),
    CONSTRAINT ck_fhi_number       CHECK (incident_number IS NULL OR length(btrim(incident_number)) > 0),
    CONSTRAINT ck_fhi_type         CHECK (incident_type ~ '^[A-Z][A-Z0-9_]*$'),
    CONSTRAINT ck_fhi_severity     CHECK (severity IN ('LOW','MEDIUM','HIGH','CRITICAL')),
    CONSTRAINT ck_fhi_status       CHECK (status IN ('OPEN','UNDER_OBSERVATION','TREATMENT_IN_PROGRESS',
                                                     'FOLLOW_UP_REQUIRED','RESOLVED','CLOSED')),
    -- resolution fields only on RESOLVED/CLOSED (same pattern as ck_tps_completed); RESOLVED must carry a time
    CONSTRAINT ck_fhi_resolved_at  CHECK (resolved_at IS NULL OR status IN ('RESOLVED','CLOSED')),
    CONSTRAINT ck_fhi_resolved_req CHECK (status <> 'RESOLVED' OR resolved_at IS NOT NULL),
    CONSTRAINT ck_fhi_resolved_by  CHECK (resolved_by IS NULL OR resolved_at IS NOT NULL),
    CONSTRAINT ck_fhi_outcome      CHECK (outcome IS NULL OR status IN ('RESOLVED','CLOSED')),
    CONSTRAINT ck_fhi_resolved_dt  CHECK (resolved_at IS NULL OR resolved_at >= observed_at),
    CONSTRAINT ck_fhi_followup_dt  CHECK (follow_up_at IS NULL OR follow_up_at >= observed_at),
    CONSTRAINT ck_fhi_followup_req CHECK (status <> 'FOLLOW_UP_REQUIRED' OR follow_up_at IS NOT NULL)
);

CREATE UNIQUE INDEX uq_fhi_farm_incident_number ON farm_health_incident (farm_id, incident_number)
    WHERE incident_number IS NOT NULL;
CREATE INDEX ix_fhi_farm_status   ON farm_health_incident (farm_id, status);
CREATE INDEX ix_fhi_farm_severity ON farm_health_incident (farm_id, severity);
CREATE INDEX ix_fhi_farm_observed ON farm_health_incident (farm_id, observed_at DESC);
CREATE INDEX ix_fhi_sheep         ON farm_health_incident (sheep_id, observed_at DESC);
CREATE INDEX ix_fhi_follow_up     ON farm_health_incident (farm_id, follow_up_at)
    WHERE follow_up_at IS NOT NULL AND status NOT IN ('RESOLVED','CLOSED');
CREATE INDEX ix_fhi_assigned_to   ON farm_health_incident (assigned_to) WHERE assigned_to IS NOT NULL;

-- Same updated_at mechanism the farm table already uses (V10).
CREATE TRIGGER trg_fhi_updated_at
    BEFORE UPDATE ON farm_health_incident
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ---------------------------------------------------------------------------
-- E. farm_event: append-style chronological history.
-- Every optional reference is a composite key with farm_id, so an event can never point at another farm's record.
-- All record references are ON DELETE RESTRICT: history blocks deletion instead of vanishing or being nulled.
-- Only user references use SET NULL.
-- ---------------------------------------------------------------------------
CREATE TABLE farm_event (
    id                   uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    farm_id              uuid         NOT NULL,
    sheep_id             uuid,
    health_incident_id   uuid,
    treatment_id         uuid,
    planned_treatment_id uuid,
    event_type           varchar(40)  NOT NULL,
    title                varchar(200) NOT NULL,
    description          text,
    occurred_at          timestamptz  NOT NULL DEFAULT now(),
    recorded_by          uuid,
    metadata             jsonb        NOT NULL DEFAULT '{}'::jsonb,
    created_at           timestamptz  NOT NULL DEFAULT now(),

    CONSTRAINT fk_fe_farm        FOREIGN KEY (farm_id)                       REFERENCES farm (id)                        ON DELETE RESTRICT,
    CONSTRAINT fk_fe_sheep       FOREIGN KEY (farm_id, sheep_id)             REFERENCES sheep (farm_id, id)              ON DELETE RESTRICT,
    CONSTRAINT fk_fe_incident    FOREIGN KEY (farm_id, health_incident_id)   REFERENCES farm_health_incident (farm_id, id) ON DELETE RESTRICT,
    CONSTRAINT fk_fe_treatment   FOREIGN KEY (farm_id, treatment_id)         REFERENCES treatments (farm_id, id)         ON DELETE RESTRICT,
    CONSTRAINT fk_fe_planned     FOREIGN KEY (farm_id, planned_treatment_id) REFERENCES planned_sheep_treatment (farm_id, id) ON DELETE RESTRICT,
    CONSTRAINT fk_fe_recorded_by FOREIGN KEY (recorded_by)                   REFERENCES users (id)                       ON DELETE SET NULL,

    CONSTRAINT ck_fe_title    CHECK (length(btrim(title)) > 0),
    CONSTRAINT ck_fe_metadata CHECK (jsonb_typeof(metadata) = 'object'),
    CONSTRAINT ck_fe_type     CHECK (event_type IN ('HEALTH_OBSERVED','HEALTH_INCIDENT_REPORTED','DIAGNOSIS_RECORDED',
                                                    'TREATMENT_ADMINISTERED','TREATMENT_COMPLETED','FOLLOW_UP_RECORDED',
                                                    'INCIDENT_RESOLVED','SHEEP_MOVED','VACCINATION','WEIGHT_RECORDED',
                                                    'GENERAL_NOTE')),
    -- events that are about an incident / a treatment must say which one
    CONSTRAINT ck_fe_incident_ref  CHECK (event_type NOT IN ('HEALTH_INCIDENT_REPORTED','DIAGNOSIS_RECORDED',
                                                              'FOLLOW_UP_RECORDED','INCIDENT_RESOLVED')
                                          OR health_incident_id IS NOT NULL),
    CONSTRAINT ck_fe_treatment_ref CHECK ((event_type <> 'TREATMENT_ADMINISTERED' OR treatment_id IS NOT NULL)
                                          AND (event_type <> 'TREATMENT_COMPLETED'
                                               OR treatment_id IS NOT NULL OR planned_treatment_id IS NOT NULL))
);

-- Duplicate guards for automatic events. Keyed on the record id, so several medications for the same sheep on the
-- same day are still allowed (each treatment has its own id). Follow-ups, notes and observations are never limited.
CREATE UNIQUE INDEX uq_fe_treatment_administered ON farm_event (treatment_id)
    WHERE event_type = 'TREATMENT_ADMINISTERED' AND treatment_id IS NOT NULL;
CREATE UNIQUE INDEX uq_fe_planned_completed ON farm_event (planned_treatment_id)
    WHERE event_type = 'TREATMENT_COMPLETED' AND planned_treatment_id IS NOT NULL;
CREATE UNIQUE INDEX uq_fe_incident_reported ON farm_event (health_incident_id)
    WHERE event_type = 'HEALTH_INCIDENT_REPORTED' AND health_incident_id IS NOT NULL;

CREATE INDEX ix_fe_farm_time      ON farm_event (farm_id, occurred_at DESC, id DESC);
CREATE INDEX ix_fe_farm_type_time ON farm_event (farm_id, event_type, occurred_at DESC);
CREATE INDEX ix_fe_sheep_time     ON farm_event (sheep_id, occurred_at DESC) WHERE sheep_id IS NOT NULL;
CREATE INDEX ix_fe_incident_time  ON farm_event (health_incident_id, occurred_at DESC) WHERE health_incident_id IS NOT NULL;
CREATE INDEX ix_fe_treatment      ON farm_event (treatment_id) WHERE treatment_id IS NOT NULL;
CREATE INDEX ix_fe_planned        ON farm_event (planned_treatment_id) WHERE planned_treatment_id IS NOT NULL;
CREATE INDEX ix_fe_recorded_by    ON farm_event (recorded_by) WHERE recorded_by IS NOT NULL;
