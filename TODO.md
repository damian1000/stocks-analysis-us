# TODO

## Coverage gap to 80% (Highest Leverage)

Current line coverage is **~36%** (after Amount/HtmlParser/EventManager/NumberUtils/EmailExport tests + JaCoCo exclusions for `Application`, `HtmlRetriever`, and `*Repository` classes). Lifting it to 80% needs fixture-based tests on six classes, each ~30-60 minutes of focused work:

- **YahooStockLookup** (~92 lines) — nested JSON parse via `quoteSummaryStore` → `summaryDetail` / `financialData` / `earningsTrend` / `earningsHistory`. Save a sample Yahoo response JSON as a test fixture (no live network), mock `HtmlRetriever` to return it, assert on each `stockLookup.set*` path including the null-guards. Cover the half-populated case too — most real responses are missing some sections.
- **ZacksSectorMappingService** (~63 lines) — fetch and parse the Zacks sector list. Same pattern: saved HTML fixture, mocked retriever, assert the parsed entities and the `repository.save(...)` calls.
- **AnalysisService** (~63 lines) — applies PEG ranking over the stock lookup table; mock the repository, assert on the computed `AnalysisStock` rows for representative input.
- **ZacksListRetrieverService** (~60 lines) — Zacks industry list parsing.
- **StockLookupService** (~53 lines) — orchestrates per-symbol Yahoo lookups; mostly delegation but worth testing the retry-and-skip-on-error path.
- **ZacksBasicRetrieverService** (~40 lines) — Zacks code extraction per symbol.

Each test file should follow the pattern: `@ExtendWith(MockitoExtension)`, mock `HtmlRetriever` + repository + `ApplicationEventPublisher`, drive the service with a saved fixture under `src/test/resources/fixtures/<service>/`, and verify both the parsed output and the event published on completion.

Smaller cleanups that also help:
- `ExportService` (18 lines) — mock the repository, assert excel + email export are invoked.
- `ExcelExport` (14 lines) — write to a temp file, read back and assert headers and one row.
- POJO tests for `PEGStock`, `AnalysisStock`, `ZacksList`, `IdGenerator`, `Formatter` (~15 lines combined).

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

- Add PostgreSQL integration tests, preferably using Testcontainers.
- Re-enable external parser tests using saved HTML fixtures instead of live network calls.
- Add fixture-based tests for Zacks sector mapping, Zacks industry list, Zacks code extraction, and Yahoo quote summary parsing.
- Add tests for pipeline event ordering and failure propagation.
- Add tests for email export disabled/enabled configuration paths.
- Add tests for stock lookup error-row persistence.
- Add tests for report generation using a small deterministic dataset.

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
- Evaluate whether Java 25 is necessary, or whether an LTS runtime would reduce operational friction.
- Add formatting/linting rules so style stays consistent.
- Add CI artifacts for test reports and migration validation.

## Documentation

- Document the exact pipeline commands for running one stage or the full pipeline.
- Document required environment variables for database, FX, and email.
- Document expected external data source limitations and throttling behavior.
- Add a recovery guide for failed or partial runs.
- Add a schema overview explaining the five pipeline tables and report output.
