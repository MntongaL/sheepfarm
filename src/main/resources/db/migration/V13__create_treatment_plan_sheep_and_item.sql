-- V13: plan membership (explicit association) and medication entries of a plan.
-- farm_id is repeated on these tables ON PURPOSE: the composite foreign keys below make the database
-- reject a sheep or plan from another farm. The composite keys themselves keep the copies consistent.

CREATE TABLE treatment_plan_sheep (
    id                uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    farm_id           uuid        NOT NULL,
    treatment_plan_id uuid        NOT NULL,
    sheep_id          uuid        NOT NULL,
    status            varchar(20) NOT NULL DEFAULT 'PENDING',
    added_at          timestamptz NOT NULL DEFAULT now(),
    completed_at      timestamptz,
    notes             text,

    CONSTRAINT fk_tps_plan  FOREIGN KEY (farm_id, treatment_plan_id) REFERENCES treatment_plan (farm_id, id) ON DELETE RESTRICT,
    CONSTRAINT fk_tps_sheep FOREIGN KEY (farm_id, sheep_id)          REFERENCES sheep (farm_id, id)          ON DELETE RESTRICT,
    CONSTRAINT uq_tps_plan_sheep UNIQUE (treatment_plan_id, sheep_id),   -- a sheep is in a plan at most once
    CONSTRAINT uq_tps_plan_id    UNIQUE (treatment_plan_id, id),         -- target for "same plan" composite keys
    CONSTRAINT ck_tps_status    CHECK (status IN ('PENDING','IN_PROGRESS','COMPLETED','REMOVED')),
    CONSTRAINT ck_tps_completed CHECK (completed_at IS NULL OR status = 'COMPLETED')
);
CREATE INDEX ix_tps_sheep ON treatment_plan_sheep (sheep_id);

CREATE TABLE treatment_plan_item (
    id                uuid          PRIMARY KEY DEFAULT gen_random_uuid(),
    farm_id           uuid          NOT NULL,
    treatment_plan_id uuid          NOT NULL,
    medication_id     uuid          NOT NULL,
    planned_date      date          NOT NULL,
    dosage            numeric(12,3) NOT NULL,   -- same type as treatments.dose_amount
    dose_unit         varchar(30)   NOT NULL,   -- treatments.dose_unit is NOT NULL too
    notes             text,
    status            varchar(20)   NOT NULL DEFAULT 'PLANNED',
    created_at        timestamptz   NOT NULL DEFAULT now(),
    updated_at        timestamptz   NOT NULL DEFAULT now(),

    CONSTRAINT fk_tpi_plan FOREIGN KEY (farm_id, treatment_plan_id) REFERENCES treatment_plan (farm_id, id) ON DELETE RESTRICT,
    CONSTRAINT uq_tpi_plan_id UNIQUE (treatment_plan_id, id),
    -- deliberately NO unique (treatment_plan_id, medication_id): repeat administrations are allowed
    CONSTRAINT ck_tpi_dosage CHECK (dosage > 0),
    CONSTRAINT ck_tpi_status CHECK (status IN ('PLANNED','COMPLETED','SKIPPED','CANCELLED'))
);
CREATE INDEX ix_tpi_plan_date  ON treatment_plan_item (treatment_plan_id, planned_date);
CREATE INDEX ix_tpi_medication ON treatment_plan_item (medication_id);

-- medications is a global catalogue (no farm_id), so every farm may use every medication: plain foreign key.
ALTER TABLE treatment_plan_item
    ADD CONSTRAINT fk_tpi_medication FOREIGN KEY (medication_id) REFERENCES medications (id) ON DELETE RESTRICT;
