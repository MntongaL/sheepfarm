-- V14: one planned administration of one plan item for one sheep of that plan.
CREATE TABLE planned_sheep_treatment (
    id                      uuid          PRIMARY KEY DEFAULT gen_random_uuid(),
    farm_id                 uuid          NOT NULL,
    treatment_plan_id       uuid          NOT NULL,   -- redundant on purpose: forces sheep link and item link into the SAME plan
    treatment_plan_sheep_id uuid          NOT NULL,
    treatment_plan_item_id  uuid          NOT NULL,
    planned_date            date,                      -- NULL = inherit from the plan item
    dosage                  numeric(12,3),             -- NULL = inherit from the plan item
    dose_unit               varchar(30),
    status                  varchar(20)   NOT NULL DEFAULT 'PLANNED',
    completed_treatment_id  uuid,                      -- the ACTUAL treatment record, if one was created
    completed_at            timestamptz,
    notes                   text,
    created_at              timestamptz   NOT NULL DEFAULT now(),
    updated_at              timestamptz   NOT NULL DEFAULT now(),

    CONSTRAINT fk_pst_plan      FOREIGN KEY (farm_id, treatment_plan_id)
        REFERENCES treatment_plan (farm_id, id) ON DELETE RESTRICT,
    CONSTRAINT fk_pst_plan_sheep FOREIGN KEY (treatment_plan_id, treatment_plan_sheep_id)
        REFERENCES treatment_plan_sheep (treatment_plan_id, id) ON DELETE RESTRICT,
    CONSTRAINT fk_pst_plan_item  FOREIGN KEY (treatment_plan_id, treatment_plan_item_id)
        REFERENCES treatment_plan_item (treatment_plan_id, id) ON DELETE RESTRICT,
    CONSTRAINT fk_pst_treatment  FOREIGN KEY (farm_id, completed_treatment_id)
        REFERENCES treatments (farm_id, id) ON DELETE RESTRICT,

    -- one planned administration per sheep per plan item (repeats = separate plan items)
    CONSTRAINT uq_pst_sheep_item UNIQUE (treatment_plan_sheep_id, treatment_plan_item_id),
    CONSTRAINT ck_pst_status    CHECK (status IN ('PLANNED','COMPLETED','SKIPPED','CANCELLED')),
    CONSTRAINT ck_pst_completed_ref CHECK (completed_treatment_id IS NULL OR status = 'COMPLETED'),
    CONSTRAINT ck_pst_completed_at  CHECK (completed_at IS NULL OR status = 'COMPLETED'),
    CONSTRAINT ck_pst_dosage        CHECK (dosage IS NULL OR dosage > 0)
);

-- An actual treatment can be linked to at most one planned administration.
CREATE UNIQUE INDEX uq_pst_completed_treatment ON planned_sheep_treatment (completed_treatment_id)
    WHERE completed_treatment_id IS NOT NULL;

CREATE INDEX ix_pst_plan_sheep_status ON planned_sheep_treatment (treatment_plan_sheep_id, status);
CREATE INDEX ix_pst_date_status       ON planned_sheep_treatment (planned_date, status);
CREATE INDEX ix_pst_item              ON planned_sheep_treatment (treatment_plan_item_id);

-- A linked actual treatment must be for the same sheep and the same medication as the plan says.
-- (Plain foreign keys cannot express this, so a trigger does. It only reads; it never creates or deletes treatments.)
CREATE OR REPLACE FUNCTION check_planned_treatment_matches_actual() RETURNS trigger AS $$
DECLARE
    plan_sheep_id uuid;
    item_medication_id uuid;
    actual_sheep_id uuid;
    actual_medication_id uuid;
BEGIN
    IF NEW.completed_treatment_id IS NULL THEN
        RETURN NEW;
    END IF;
    SELECT sheep_id INTO plan_sheep_id FROM treatment_plan_sheep WHERE id = NEW.treatment_plan_sheep_id;
    SELECT medication_id INTO item_medication_id FROM treatment_plan_item WHERE id = NEW.treatment_plan_item_id;
    SELECT sheep_id, medication_id INTO actual_sheep_id, actual_medication_id
      FROM treatments WHERE id = NEW.completed_treatment_id;
    IF actual_sheep_id IS DISTINCT FROM plan_sheep_id THEN
        RAISE EXCEPTION 'Linked treatment is for a different sheep than the planned administration';
    END IF;
    IF actual_medication_id IS DISTINCT FROM item_medication_id THEN
        RAISE EXCEPTION 'Linked treatment used a different medication than the plan item';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_pst_matches_actual
    BEFORE INSERT OR UPDATE OF completed_treatment_id, treatment_plan_sheep_id, treatment_plan_item_id
    ON planned_sheep_treatment
    FOR EACH ROW EXECUTE FUNCTION check_planned_treatment_matches_actual();
