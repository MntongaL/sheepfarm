-- V12: treatment_plan (belongs to exactly one farm)
CREATE TABLE treatment_plan (
    id             uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    farm_id        uuid         NOT NULL,
    name           varchar(150) NOT NULL,
    description    text,
    start_date     date         NOT NULL,
    end_date       date,
    status         varchar(20)  NOT NULL DEFAULT 'DRAFT',
    selection_type varchar(20)  NOT NULL DEFAULT 'INDIVIDUAL',
    -- Original selection criteria only. Membership is ALWAYS taken from treatment_plan_sheep.
    -- Same types as sheep.birth_year (integer) and sheep.birth_month (varchar) so a criterion compares directly.
    birth_year     integer,
    birth_month    varchar(20),
    created_at     timestamptz  NOT NULL DEFAULT now(),
    updated_at     timestamptz  NOT NULL DEFAULT now(),

    CONSTRAINT fk_treatment_plan_farm     FOREIGN KEY (farm_id) REFERENCES farm (id) ON DELETE RESTRICT,
    CONSTRAINT uq_treatment_plan_farm_id  UNIQUE (farm_id, id),
    CONSTRAINT ck_treatment_plan_name    CHECK (length(btrim(name)) > 0),
    CONSTRAINT ck_treatment_plan_status  CHECK (status IN ('DRAFT','ACTIVE','COMPLETED','CANCELLED')),
    CONSTRAINT ck_treatment_plan_selection CHECK (selection_type IN ('INDIVIDUAL','BIRTH_MONTH','MIXED')),
    CONSTRAINT ck_treatment_plan_dates   CHECK (end_date IS NULL OR end_date >= start_date),
    CONSTRAINT ck_treatment_plan_birth_year  CHECK (birth_year IS NULL OR birth_year BETWEEN 1900 AND 2200),
    CONSTRAINT ck_treatment_plan_month_needs_year CHECK (birth_month IS NULL OR birth_year IS NOT NULL)
);

CREATE INDEX ix_treatment_plan_farm_status ON treatment_plan (farm_id, status);
CREATE INDEX ix_treatment_plan_farm_start  ON treatment_plan (farm_id, start_date);
