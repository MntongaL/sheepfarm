-- V15: farm ownership constraints on the existing tables.
-- Your schema had NO foreign key from sheep.farm_id or treatments.farm_id to farm(id), and no rule that a treatment's
-- sheep belongs to the treatment's farm. All three are added NOT VALID: enforced for every new or changed row immediately,
-- WITHOUT scanning or rejecting historical rows, so this migration cannot fail on legacy data.
-- Validate later (query 6 and 7 in 00_inspect_schema.sql must return 0 first):
--     ALTER TABLE sheep      VALIDATE CONSTRAINT fk_sheep_farm;
--     ALTER TABLE treatments VALIDATE CONSTRAINT fk_treatments_farm;
--     ALTER TABLE treatments VALIDATE CONSTRAINT fk_treatment_sheep_farm;
-- (medications is a global catalogue without farm_id, so there is no medication/farm rule to enforce.)
ALTER TABLE sheep
    ADD CONSTRAINT fk_sheep_farm FOREIGN KEY (farm_id) REFERENCES farm (id) NOT VALID;
ALTER TABLE treatments
    ADD CONSTRAINT fk_treatments_farm FOREIGN KEY (farm_id) REFERENCES farm (id) NOT VALID;
ALTER TABLE treatments
    ADD CONSTRAINT fk_treatment_sheep_farm FOREIGN KEY (farm_id, sheep_id)
    REFERENCES sheep (farm_id, id) NOT VALID;
