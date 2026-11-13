# Sheep Medication Tracking Backend (starter implementation)

Java 17 + Spring Boot + PostgreSQL backend starter based on the supplied workbook. Includes initial sheep, medication catalogue and individual sheep treatment APIs, Flyway schema, validation, OpenAPI UI and PostgreSQL Docker Compose.

## Workbook mapping observed
- **Medication Master**: Medication Name, Category, Type, Standard Dosage, Withdrawal Days, Dosing frequency, Treats, Notes.
- **Sheep Register**: Sheep ID / Tag, Birth Month, Birth Year, Age (Months formula), Type, Notes. Age is derived and should be computed in the app rather than persisted as an Excel formula.
- **Medication Log**: Medication Name, Type, Dosage Given, Medication Date, Next Medication Date, Administered By, Notes. Some log rows describe groups (e.g. “all adult females”) rather than named individual sheep; import must preserve these as group/history notes and must not fabricate individual animal links.
- The workbook includes blank cells, “Done/Pending/???” next-date values, and doses represented as text (including decimal commas). These require import preview and human confirmation.

## Run on Windows
1. Install JDK 17, Maven 3.9+, and Docker Desktop.
2. In this directory run `docker compose up -d`.
3. Set `DB_PASSWORD=change_me` in your terminal or update `application.yml` for local development.
4. Run `mvn spring-boot:run`.
5. Open Swagger at http://localhost:8080/swagger-ui.html.
6. From a physical phone, configure Expo to use your PC's LAN IP, e.g. `http://192.168.x.x:8080`, not `localhost`.

## Endpoints implemented
- `POST/GET /api/v1/sheep`; `GET /api/v1/sheep/{id}`; `PATCH /api/v1/sheep/{id}/status`
- `POST/GET /api/v1/medications`; `GET/PUT /api/v1/medications/{id}`; `PATCH /api/v1/medications/{id}/status`
- `POST/GET /api/v1/treatments`; `GET /api/v1/treatments/{id}`

## Example sheep payload
```json
{"farmId":"11111111-1111-1111-1111-111111111111","tagNumber":"MN01","birthMonth":"March","birthYear":2024,"sex":"Ewe","notes":""}
```

## Example medication payload
```json
{"name":"Endo+Lint","category":"Dewormer","administrationType":"Oral Drench","standardDosage":"2ml/10kg","withdrawalDays":7,"dosingFrequency":"Workbook value; verify before use","treats":"Roundworms; tapeworms; lungworms","notes":"Imported from workbook; verify product label."}
```

## Example treatment payload
```json
{"farmId":"11111111-1111-1111-1111-111111111111","sheepId":"22222222-2222-2222-2222-222222222222","medicationId":"33333333-3333-3333-3333-333333333333","administeredAt":"2026-10-09","doseAmount":2.000,"doseUnit":"ml","administeredBy":"Farm worker","notes":"Recorded dose; not a dosage recommendation."}
```

## Important implementation boundary
This is a runnable foundation, not yet the complete production scope from the specification. JWT authentication/farm memberships, farm CRUD, multi-animal treatment events, inventory, reminders, workbook import preview/confirm, reports/exports, and comprehensive integration tests remain to be implemented. The workbook contains product guidance and historical values; these are not independently verified veterinary directions. Do not use this system to prescribe doses; confirm product labels/veterinary guidance and withdrawal periods before use. Authentication must be added before exposing the API outside a trusted local development network.
