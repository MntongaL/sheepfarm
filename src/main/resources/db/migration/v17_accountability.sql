
-- ============================================================
-- Farm accountability, health incidents and event history
-- PostgreSQL / Flyway
-- ============================================================

-- 1. FARM ACCOUNTABILITY

ALTER TABLE farm
    ADD COLUMN created_by UUID,
    ADD COLUMN updated_by UUID;

ALTER TABLE farm
    ADD CONSTRAINT fk_farm_created_by
        FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL;

ALTER TABLE farm
    ADD CONSTRAINT fk_farm_updated_by
        FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL;


-- 2. TREATMENT ACCOUNTABILITY
-- Preserve existing free-text administering information.
-- Assumes treatments.administered_by is currently VARCHAR/TEXT.

ALTER TABLE treatments
    RENAME COLUMN administered_by TO administered_by_legacy;

ALTER TABLE treatments
    ADD COLUMN administered_by UUID,
    ADD COLUMN recorded_by UUID;

ALTER TABLE treatments
    ADD CONSTRAINT fk_treatments_administered_by
        FOREIGN KEY (administered_by) REFERENCES users(id) ON DELETE SET NULL;

ALTER TABLE treatments
    ADD CONSTRAINT fk_treatments_recorded_by
        FOREIGN KEY (recorded_by) REFERENCES users(id) ON DELETE SET NULL;

CREATE INDEX idx_treatments_administered_by
    ON treatments (administered_by);

CREATE INDEX idx_treatments_recorded_by
    ON treatments (recorded_by);


-- 3. TREATMENT PLAN ACCOUNTABILITY

ALTER TABLE treatment_plan
    ADD COLUMN created_by UUID,
    ADD COLUMN updated_by UUID,
    ADD COLUMN completed_by UUID;

ALTER TABLE treatment_plan
    ADD CONSTRAINT fk_treatment_plan_created_by
        FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL;

ALTER TABLE treatment_plan
    ADD CONSTRAINT fk_treatment_plan_updated_by
        FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL;

ALTER TABLE treatment_plan
    ADD CONSTRAINT fk_treatment_plan_completed_by
        FOREIGN KEY (completed_by) REFERENCES users(id) ON DELETE SET NULL;


-- 4. PLANNED TREATMENT COMPLETION ACCOUNTABILITY

ALTER TABLE planned_sheep_treatment
    ADD COLUMN completed_by UUID;

ALTER TABLE planned_sheep_treatment
    ADD CONSTRAINT fk_planned_treatment_completed_by
        FOREIGN KEY (completed_by) REFERENCES users(id) ON DELETE SET NULL;


-- 5. FARM HEALTH INCIDENTS

CREATE TABLE farm_health_incident (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    farm_id UUID NOT NULL,
    sheep_id UUID NOT NULL,

    incident_number VARCHAR(50),
    title VARCHAR(200) NOT NULL,
    incident_type VARCHAR(50) NOT NULL,

    severity VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',

    observed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    reported_by UUID,
    assigned_to UUID,

    symptoms TEXT,
    diagnosis TEXT,
    notes TEXT,

    follow_up_at TIMESTAMPTZ,

    resolved_at TIMESTAMPTZ,
    resolved_by UUID,
    outcome TEXT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_health_incident_farm
        FOREIGN KEY (farm_id) REFERENCES farm(id) ON DELETE RESTRICT,

    CONSTRAINT fk_health_incident_sheep
        FOREIGN KEY (sheep_id) REFERENCES sheep(id) ON DELETE RESTRICT,

    CONSTRAINT fk_health_incident_reported_by
        FOREIGN KEY (reported_by) REFERENCES users(id) ON DELETE SET NULL,

    CONSTRAINT fk_health_incident_assigned_to
        FOREIGN KEY (assigned_to) REFERENCES users(id) ON DELETE SET NULL,

    CONSTRAINT fk_health_incident_resolved_by
        FOREIGN KEY (resolved_by) REFERENCES users(id) ON DELETE SET NULL,

    CONSTRAINT chk_health_incident_severity
        CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),

    CONSTRAINT chk_health_incident_status
        CHECK (status IN (
            'OPEN',
            'UNDER_OBSERVATION',
            'TREATMENT_IN_PROGRESS',
            'FOLLOW_UP_REQUIRED',
            'RESOLVED',
            'CLOSED'
        )),

    CONSTRAINT chk_health_incident_resolution
        CHECK (
            status NOT IN ('RESOLVED', 'CLOSED')
            OR resolved_at IS NOT NULL
        )
);

CREATE UNIQUE INDEX uq_health_incident_number
    ON farm_health_incident (farm_id, incident_number)
    WHERE incident_number IS NOT NULL;

CREATE INDEX idx_health_incident_farm_status
    ON farm_health_incident (farm_id, status);

CREATE INDEX idx_health_incident_sheep_observed
    ON farm_health_incident (sheep_id, observed_at DESC);

CREATE INDEX idx_health_incident_follow_up
    ON farm_health_incident (farm_id, follow_up_at)
    WHERE follow_up_at IS NOT NULL
      AND status NOT IN ('RESOLVED', 'CLOSED');

CREATE INDEX idx_health_incident_assigned_to
    ON farm_health_incident (assigned_to);


-- 6. FARM EVENT HISTORY

CREATE TABLE farm_event (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    farm_id UUID NOT NULL,
    sheep_id UUID,
    health_incident_id UUID,
    treatment_id UUID,
    planned_treatment_id UUID,

    event_type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,

    occurred_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    recorded_by UUID,

    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_farm_event_farm
        FOREIGN KEY (farm_id) REFERENCES farm(id) ON DELETE RESTRICT,

    CONSTRAINT fk_farm_event_sheep
        FOREIGN KEY (sheep_id) REFERENCES sheep(id) ON DELETE SET NULL,

    CONSTRAINT fk_farm_event_health_incident
        FOREIGN KEY (health_incident_id)
        REFERENCES farm_health_incident(id) ON DELETE SET NULL,

    CONSTRAINT fk_farm_event_treatment
        FOREIGN KEY (treatment_id) REFERENCES treatments(id) ON DELETE SET NULL,

    CONSTRAINT fk_farm_event_planned_treatment
        FOREIGN KEY (planned_treatment_id)
        REFERENCES planned_sheep_treatment(id) ON DELETE SET NULL,

    CONSTRAINT fk_farm_event_recorded_by
        FOREIGN KEY (recorded_by) REFERENCES users(id) ON DELETE SET NULL,

    CONSTRAINT chk_farm_event_type
        CHECK (event_type IN (
            'HEALTH_OBSERVED',
            'HEALTH_INCIDENT_REPORTED',
            'DIAGNOSIS_RECORDED',
            'TREATMENT_ADMINISTERED',
            'TREATMENT_COMPLETED',
            'FOLLOW_UP_RECORDED',
            'INCIDENT_RESOLVED',
            'SHEEP_MOVED',
            'VACCINATION',
            'WEIGHT_RECORDED',
            'GENERAL_NOTE'
        )),

    CONSTRAINT chk_farm_event_metadata
        CHECK (jsonb_typeof(metadata) = 'object')
);

CREATE INDEX idx_farm_event_timeline
    ON farm_event (farm_id, occurred_at DESC);

CREATE INDEX idx_farm_event_sheep_timeline
    ON farm_event (sheep_id, occurred_at DESC);

CREATE INDEX idx_farm_event_incident
    ON farm_event (health_incident_id);

CREATE INDEX idx_farm_event_treatment
    ON farm_event (treatment_id);

CREATE INDEX idx_farm_event_planned_treatment
    ON farm_event (planned_treatment_id);

CREATE INDEX idx_farm_event_recorded_by
    ON farm_event (recorded_by);


-- Prevent duplicate automatic treatment events while allowing
-- separate treatments for the same sheep on the same day.

CREATE UNIQUE INDEX uq_farm_event_treatment_type
    ON farm_event (treatment_id, event_type)
    WHERE treatment_id IS NOT NULL
      AND event_type IN (
          'TREATMENT_ADMINISTERED',
          'TREATMENT_COMPLETED'
      );

-- ============================================================
-- END OF MIGRATION
-- ============================================================
