-- Verification of V11-V15. Run on a DISPOSABLE copy of the populated database after Flyway has migrated it.
--   psql -v ON_ERROR_STOP=1 -f 99_verification_tests.sql
-- Everything runs in one transaction that is ROLLED BACK at the end, so no data is kept.
-- It reuses existing farms/sheep/medication rows; it needs one farm with >= 2 sheep and one medication usable by it.
-- Cross-farm tests run only if a second farm with sheep exists (otherwise they print a NOTICE and are skipped).
BEGIN;

CREATE FUNCTION pg_temp.expect_fail(label text, stmt text) RETURNS void AS $f$
BEGIN
    BEGIN
        EXECUTE stmt;
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'PASS (rejected): % -> %', label, SQLERRM;
        RETURN;
    END;
    RAISE EXCEPTION 'FAIL (was accepted): %', label;
END;
$f$ LANGUAGE plpgsql;

DO $$
DECLARE
    farm_a uuid; farm_b uuid;
    s1 uuid; s2 uuid; s_other uuid;
    med uuid; plan1 uuid; plan2 uuid; plan_other uuid;
    tps1 uuid; tps2 uuid; tps_p2 uuid;
    item1 uuid; item2 uuid; item_p2 uuid;
    pst1 uuid; pst2 uuid; tr uuid;
    med_farm_specific boolean;
BEGIN
    SELECT farm_id INTO farm_a FROM sheep WHERE farm_id IS NOT NULL GROUP BY farm_id HAVING count(*) >= 2 LIMIT 1;
    IF farm_a IS NULL THEN RAISE EXCEPTION 'Need a farm with at least 2 sheep'; END IF;
    SELECT id INTO s1 FROM sheep WHERE farm_id = farm_a ORDER BY id LIMIT 1;
    SELECT id INTO s2 FROM sheep WHERE farm_id = farm_a AND id <> s1 ORDER BY id LIMIT 1;

    SELECT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema='public' AND table_name='medication' AND column_name='farm_id') INTO med_farm_specific;
    IF med_farm_specific THEN
        EXECUTE 'SELECT id FROM medication WHERE farm_id = $1 LIMIT 1' INTO med USING farm_a;
    ELSE
        SELECT id INTO med FROM medication LIMIT 1;
    END IF;
    IF med IS NULL THEN RAISE EXCEPTION 'Need a medication usable by farm %', farm_a; END IF;

    -- 6. one plan, multiple sheep
    INSERT INTO treatment_plan (farm_id, name, start_date) VALUES (farm_a, 'T-plan 1', current_date) RETURNING id INTO plan1;
    INSERT INTO treatment_plan_sheep (farm_id, treatment_plan_id, sheep_id) VALUES (farm_a, plan1, s1) RETURNING id INTO tps1;
    INSERT INTO treatment_plan_sheep (farm_id, treatment_plan_id, sheep_id) VALUES (farm_a, plan1, s2) RETURNING id INTO tps2;
    RAISE NOTICE 'PASS: one plan with two sheep';

    -- 7. one sheep in several plans
    INSERT INTO treatment_plan (farm_id, name, start_date) VALUES (farm_a, 'T-plan 2', current_date) RETURNING id INTO plan2;
    INSERT INTO treatment_plan_sheep (farm_id, treatment_plan_id, sheep_id) VALUES (farm_a, plan2, s1) RETURNING id INTO tps_p2;
    RAISE NOTICE 'PASS: sheep in two plans';

    PERFORM pg_temp.expect_fail('duplicate sheep in same plan',
        format('INSERT INTO treatment_plan_sheep (farm_id, treatment_plan_id, sheep_id) VALUES (%L,%L,%L)', farm_a, plan1, s1));

    -- 8. repeated medication in one plan on different dates
    INSERT INTO treatment_plan_item (farm_id, treatment_plan_id, medication_id, planned_date, dosage)
        VALUES (farm_a, plan1, med, current_date, 2) RETURNING id INTO item1;
    INSERT INTO treatment_plan_item (farm_id, treatment_plan_id, medication_id, planned_date, dosage)
        VALUES (farm_a, plan1, med, current_date + 14, 2) RETURNING id INTO item2;
    INSERT INTO treatment_plan_item (farm_id, treatment_plan_id, medication_id, planned_date, dosage)
        VALUES (farm_a, plan2, med, current_date, 1) RETURNING id INTO item_p2;
    RAISE NOTICE 'PASS: same medication twice in a plan';

    -- 9. independent planned administrations per sheep (different dosage/date allowed)
    INSERT INTO planned_sheep_treatment (farm_id, treatment_plan_id, treatment_plan_sheep_id, treatment_plan_item_id)
        VALUES (farm_a, plan1, tps1, item1) RETURNING id INTO pst1;
    INSERT INTO planned_sheep_treatment (farm_id, treatment_plan_id, treatment_plan_sheep_id, treatment_plan_item_id, dosage, planned_date)
        VALUES (farm_a, plan1, tps2, item1, 3, current_date + 1) RETURNING id INTO pst2;
    RAISE NOTICE 'PASS: independent planned administrations';

    PERFORM pg_temp.expect_fail('duplicate sheep + plan item',
        format('INSERT INTO planned_sheep_treatment (farm_id, treatment_plan_id, treatment_plan_sheep_id, treatment_plan_item_id) VALUES (%L,%L,%L,%L)', farm_a, plan1, tps1, item1));

    -- 10. cross-plan references rejected (sheep of plan 2 with an item of plan 1, and the reverse)
    PERFORM pg_temp.expect_fail('plan-sheep from plan 2 with item from plan 1',
        format('INSERT INTO planned_sheep_treatment (farm_id, treatment_plan_id, treatment_plan_sheep_id, treatment_plan_item_id) VALUES (%L,%L,%L,%L)', farm_a, plan1, tps_p2, item2));
    PERFORM pg_temp.expect_fail('item from plan 2 with plan-sheep from plan 1',
        format('INSERT INTO planned_sheep_treatment (farm_id, treatment_plan_id, treatment_plan_sheep_id, treatment_plan_item_id) VALUES (%L,%L,%L,%L)', farm_a, plan1, tps2, item_p2));

    -- 11. cross-farm references rejected
    SELECT farm_id INTO farm_b FROM sheep WHERE farm_id IS NOT NULL AND farm_id <> farm_a LIMIT 1;
    IF farm_b IS NULL THEN
        RAISE NOTICE 'SKIPPED: cross-farm tests (no second farm with sheep)';
    ELSE
        SELECT id INTO s_other FROM sheep WHERE farm_id = farm_b LIMIT 1;
        PERFORM pg_temp.expect_fail('sheep of farm B in plan of farm A (farm_id = A)',
            format('INSERT INTO treatment_plan_sheep (farm_id, treatment_plan_id, sheep_id) VALUES (%L,%L,%L)', farm_a, plan1, s_other));
        PERFORM pg_temp.expect_fail('sheep of farm B in plan of farm A (farm_id = B)',
            format('INSERT INTO treatment_plan_sheep (farm_id, treatment_plan_id, sheep_id) VALUES (%L,%L,%L)', farm_b, plan1, s_other));
    END IF;

    -- status / selection checks
    PERFORM pg_temp.expect_fail('invalid plan status',
        format('INSERT INTO treatment_plan (farm_id, name, start_date, status) VALUES (%L,%L,current_date,%L)', farm_a, 'x', 'DONE'));
    PERFORM pg_temp.expect_fail('birth_month without birth_year',
        format('INSERT INTO treatment_plan (farm_id, name, start_date, birth_month) VALUES (%L,%L,current_date,3)', farm_a, 'x'));

    -- 12. link a planned administration to an actual treatment (needs an existing treatment of s1 + med)
    SELECT id INTO tr FROM treatments WHERE farm_id = farm_a AND sheep_id = s1 AND medication_id = med LIMIT 1;
    IF tr IS NULL THEN
        RAISE NOTICE 'SKIPPED: completed-treatment link test (no existing treatment for that sheep+medication)';
    ELSE
        UPDATE planned_sheep_treatment SET status = 'COMPLETED', completed_treatment_id = tr, completed_at = now() WHERE id = pst1;
        RAISE NOTICE 'PASS: planned administration linked to an actual treatment';
        PERFORM pg_temp.expect_fail('same actual treatment linked twice',
            format('UPDATE planned_sheep_treatment SET status=%L, completed_treatment_id=%L WHERE id=%L', 'COMPLETED', tr, pst2));
        PERFORM pg_temp.expect_fail('deleting a linked actual treatment',
            format('DELETE FROM treatments WHERE id = %L', tr));
        UPDATE treatment_plan SET status = 'CANCELLED' WHERE id = plan1;
        IF NOT EXISTS (SELECT 1 FROM treatments WHERE id = tr) THEN RAISE EXCEPTION 'FAIL: cancelling a plan removed a treatment'; END IF;
        RAISE NOTICE 'PASS: cancelling a plan leaves the actual treatment intact';
    END IF;

    PERFORM pg_temp.expect_fail('deleting a plan that has members',
        format('DELETE FROM treatment_plan WHERE id = %L', plan1));
END $$;

ROLLBACK;
