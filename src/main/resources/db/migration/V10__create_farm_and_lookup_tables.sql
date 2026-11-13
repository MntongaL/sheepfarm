-- =====================================================================
-- Flyway migration (PostgreSQL 13+): farm + lookup tables
-- Rename/renumber the file to fit your migration history (V10 is a placeholder).
-- Uses CREATE TABLE IF NOT EXISTS because your sheep records already carry a
-- farm id; if a farm table exists, review it before running.
-- gen_random_uuid() is built in from PostgreSQL 13; on older versions run
--   CREATE EXTENSION IF NOT EXISTS pgcrypto;
-- =====================================================================

-- ---------- helper: keep updated_at current ---------------------------
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS trigger AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ---------- farm -------------------------------------------------------
CREATE TABLE IF NOT EXISTS farm (
    id                  uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    name                varchar(150) NOT NULL,
    registration_number varchar(60),
    address             varchar(255),
    region              varchar(100),
    latitude            numeric(9,6),
    longitude           numeric(9,6),
    contact_name        varchar(120),
    contact_phone       varchar(40),
    contact_email       varchar(150),
    is_active           boolean      NOT NULL DEFAULT true,
    created_at          timestamptz  NOT NULL DEFAULT now(),
    updated_at          timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT chk_farm_name_not_blank CHECK (length(btrim(name)) > 0),
    CONSTRAINT chk_farm_latitude  CHECK (latitude  IS NULL OR latitude  BETWEEN -90  AND 90),
    CONSTRAINT chk_farm_longitude CHECK (longitude IS NULL OR longitude BETWEEN -180 AND 180)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_farm_registration_number
    ON farm (registration_number) WHERE registration_number IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_farm_name ON farm (lower(name));

DROP TRIGGER IF EXISTS trg_farm_updated_at ON farm;
CREATE TRIGGER trg_farm_updated_at
    BEFORE UPDATE ON farm
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ---------- lookup: month (fixed 1..12) -------------------------------
CREATE TABLE IF NOT EXISTS month (
    id         smallint    PRIMARY KEY,
    name       varchar(20) NOT NULL UNIQUE,
    short_name varchar(3)  NOT NULL UNIQUE,
    CONSTRAINT chk_month_id CHECK (id BETWEEN 1 AND 12)
);

INSERT INTO month (id, name, short_name) VALUES
    (1,  'January',   'Jan'), (2,  'February', 'Feb'), (3,  'March',    'Mar'),
    (4,  'April',     'Apr'), (5,  'May',      'May'), (6,  'June',     'Jun'),
    (7,  'July',      'Jul'), (8,  'August',   'Aug'), (9,  'September','Sep'),
    (10, 'October',   'Oct'), (11, 'November', 'Nov'), (12, 'December', 'Dec')
ON CONFLICT (id) DO NOTHING;

-- ---------- lookup: gender ---------------------------------------------
CREATE TABLE IF NOT EXISTS gender (
    id         smallint    GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code       varchar(30) NOT NULL UNIQUE,
    name       varchar(60) NOT NULL,
    sort_order smallint    NOT NULL DEFAULT 0,
    is_active  boolean     NOT NULL DEFAULT true,
    CONSTRAINT chk_gender_code CHECK (code = upper(code))
);

INSERT INTO gender (code, name, sort_order) VALUES
    ('FEMALE', 'Female', 1),
    ('MALE',   'Male',   2)
ON CONFLICT (code) DO NOTHING;

-- ---------- lookup: administration_type (route) ------------------------
CREATE TABLE IF NOT EXISTS administration_type (
    id         smallint    GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code       varchar(30) NOT NULL UNIQUE,
    name       varchar(60) NOT NULL,
    sort_order smallint    NOT NULL DEFAULT 0,
    is_active  boolean     NOT NULL DEFAULT true,
    CONSTRAINT chk_administration_type_code CHECK (code = upper(code))
);

INSERT INTO administration_type (code, name, sort_order) VALUES
    ('ORAL',           'By mouth (drench or feed)',  1),
    ('INTRAMUSCULAR',  'Into the muscle (IM)',    2),
    ('SUBCUTANEOUS',   'Under the skin (SC)',     3),
    ('INTRAVENOUS',    'Into a vein (IV)',      4),
    ('TOPICAL',        'On the skin (pour-on)',     5),
    ('INTRAMAMMARY',   'Into the udder (teat)',          6),
    ('INTRANASAL',     'Through the nose',            7),
    ('OTHER',          'Other',                 99)
ON CONFLICT (code) DO NOTHING;

-- ---------- lookup: medicine_category ----------------------------------
CREATE TABLE IF NOT EXISTS medicine_category (
    id         smallint    GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code       varchar(30) NOT NULL UNIQUE,
    name       varchar(80) NOT NULL,
    sort_order smallint    NOT NULL DEFAULT 0,
    is_active  boolean     NOT NULL DEFAULT true,
    CONSTRAINT chk_medicine_category_code CHECK (code = upper(code))
);

INSERT INTO medicine_category (code, name, sort_order) VALUES
    ('ANTIBIOTIC',        'Antibiotic',                    1),
    ('ANTHELMINTIC',      'Dewormer (internal parasites)',       2),
    ('ECTOPARASITICIDE',  'Treats external parasites (dip / pour-on)', 3),
    ('ANTIPROTOZOAL',     'Treats protozoan infections (e.g. coccidiosis)', 4),
    ('VACCINE',           'Vaccine',                       5),
    ('ANTI_INFLAMMATORY', 'Reduces swelling and relieves pain', 6),
    ('VITAMIN_MINERAL',   'Vitamin / mineral supplement',  7),
    ('HORMONAL',          'Controls reproduction and hormone activity', 8),
    ('OTHER',             'Other',                         99)
ON CONFLICT (code) DO NOTHING;
