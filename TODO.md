# TODO

## Remaining test gaps

Overall line coverage is **~91%**. The remaining unit-level gaps are infra-bound:

- **`ExcelExport`** — needs a JXLS template fixture in `src/test/resources/template/`, then write to a temp file and assert headers + one row. The production template under `src/main/resources/template/` could be reused as a fixture.
- **`EmailExport`** — the disabled path is covered; the enabled SMTP path needs a fake SMTP server (e.g. GreenMail or Wiser) to assert host/port/auth without sending real mail.

## Security

- Verify nothing sensitive remains in git history (current `application-*.yml` uses `${DB_PASSWORD:postgres}` / `${EMAIL_PASSWORD:}` env vars, not literal credentials — so the live tree is clean).
- Consider adding secret scanning to CI, for example GitHub secret scanning or a pre-commit hook.

## Bug Fixes

- Validate the new Flyway migration against a real PostgreSQL database.
- Add a migration-backed repository test so JPA mappings are checked against Flyway-created tables.
- Review all entity column names against migrations, not only `target_price` and `recommendation_rating`.
- Revisit `AnalysisRepository extends JpaRepository<AnalysisStock, String>` because `AnalysisStock.id` is `Long`.
- Review date columns currently mapped as `LocalDate` while migrations define `TIMESTAMP`.
- Check all parsing paths for empty substring/index failures when external HTML changes.
- Make Yahoo parsing fail with clear errors when required JSON sections are missing.
- Ensure partial stage failures cannot delete existing good data before replacement data is confirmed.

## Reliability

- Add structured retry configuration for HTTP calls: retry count, delay, timeout, and user-agent.
- Add rate limiting tests around `StockLookupService`.
- Persist per-symbol lookup failures with enough context to debug source/parser changes.
- Add stage-level summary metrics: attempted, succeeded, failed, skipped.
- Add a dry-run mode that fetches/parses without deleting or writing database rows.
- Consider staging writes into temporary tables, then swapping only after successful completion.
- Add explicit health/startup checks for database and Flyway migration state.

## Tests

- Add PostgreSQL integration tests, preferably using Testcontainers, so JPA mappings are validated against the Flyway-created schema (closest to production behaviour today).
- Re-enable the live network parser tests (`YahooStockLookupTest`'s `@Disabled` methods) as opt-in tests gated by a system property — so they can be run manually but don't break CI.
- Add tests for pipeline event ordering and failure propagation across stages.
- Enable-path test for `EmailExport` using a fake SMTP server.
- Add tests for report generation using a small deterministic dataset (end-to-end through the export stage).

## Design Review

- Replace ad hoc string parsing with structured JSON parsing where the source response is JSON.
- Separate external data clients from pipeline stage services.
- Introduce explicit DTOs for source responses and mapping logic.
- Replace system properties such as `date`, `event`, and `zacksDate` with typed Spring configuration or command-line arguments.
- Rename legacy `Reuters` log/config wording now that the source is Yahoo.
- Avoid placing generated reports or operational notes under `src/main/resources`.
- Decide whether this app is a batch job or web service; remove server concerns if it is batch-only.
- Consider Spring Batch if restartability, stage tracking, and retry behavior become more important.

## Build And Maintenance

- Remove generated `build/`, `.gradle/`, and IDE files from working copies before commits.
- Add dependency vulnerability scanning.
- Review deprecated APIs reported by Gradle and update replacements.
- Add formatting/linting rules so style stays consistent.
- Add CI artifacts for test reports and migration validation.

## Documentation

- Document the exact pipeline commands for running one stage or the full pipeline.
- Document required environment variables for database, FX, and email.
- Document expected external data source limitations and throttling behavior.
- Add a recovery guide for failed or partial runs.
- Add a schema overview explaining the five pipeline tables and report output.
