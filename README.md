# US Equity Universe Pipeline

[![CI](https://github.com/damian1000/stocks-analysis-us/actions/workflows/ci.yml/badge.svg)](https://github.com/damian1000/stocks-analysis-us/actions/workflows/ci.yml)
[![JDK](https://img.shields.io/badge/jdk-25-orange)](https://openjdk.org/projects/jdk/25/)
[![Spring Boot](https://img.shields.io/badge/spring--boot-4.0.6-brightgreen)](https://spring.io/projects/spring-boot)

A Spring Boot 4 / Java 25 service that builds a **ranked US equity universe from public fundamentals**. It walks Zacks sector/industry classifications and Yahoo Finance fundamentals, applies a configurable PEG-style fundamental ranking, and exports the result as a desk-friendly Excel report.

The pipeline is structured so the signal (currently PEG) is one stage of six — swap `_5analysis` for a different scoring rule (e.g. EV/EBITDA decile rank, momentum, quality) and the rest of the pipeline doesn't change.

## Pipeline architecture

```
   ┌─────────────────┐   ┌───────────────────┐   ┌──────────────────┐
   │ 1. Sector       │──▶│ 2. Zacks industry │──▶│ 3. Zacks-code    │
   │    mapping      │   │    universe       │   │    enrichment    │
   └─────────────────┘   └───────────────────┘   └────────┬─────────┘
                                                            │
                                                            ▼
   ┌──────────────────┐   ┌───────────────────┐   ┌──────────────────┐
   │ 6. Excel export  │◀──│ 5. Ranking        │◀──│ 4. Fundamentals  │
   │ (JXLS template)  │   │ (PEG today)       │   │ (Yahoo + FX)     │
   └──────────────────┘   └───────────────────┘   └──────────────────┘
```

Stages talk via Spring `ApplicationEvent`s — never direct method calls. Each numbered package in `src/main/java/com/dfh/stock/analysis/us/` owns one stage, its DTOs, its repository, and its event publisher. Adding a stage is a new package, not a rewrite.

## What it demonstrates

- **Event-driven Spring** — `ApplicationEventPublisher` between stages, so any stage can be skipped, re-run, or replaced without touching the others
- **External-data discipline** — throttled HTTP, retry-on-failure, Tika-based HTML parsing for Zacks pages, all behind a single boundary that's mocked in tests
- **No live calls in CI** — the test suite mocks the HTTP boundary; the same tests run deterministically on every push
- **Configurable ranking stage** — the scoring rule (currently PEG) is one Spring bean. Replacing it doesn't disturb the universe-build or the export
- **Schema-managed DB** — Flyway migrations against PostgreSQL 17; the schema is the source of truth, not the JPA entities
- **Externalised config** — per-profile YAML + env-var placeholders; nothing sensitive committed

## Prerequisites

- JDK 25 (Gradle toolchain will fetch one if missing)
- Docker (local Postgres via `docker compose`)

## Quick start

```bash
cp .env.example .env             # tweak if you want
docker compose up -d             # Postgres on 5432
./gradlew bootRun                # defaults to `dev` profile
```

App listens on `http://localhost:9000` (override with `SERVER_PORT`).

## Configuration

| Variable | Default | Purpose |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/trading` | Postgres JDBC URL |
| `DB_USERNAME` | `postgres` | |
| `DB_PASSWORD` | `postgres` | |
| `FX_PROVIDER_URL` | `https://data.fixer.io/api/latest` | FX rate source |
| `FX_API_KEY` | _empty_ | Leave blank to skip FX conversion |
| `SERVER_PORT` | `9000` | HTTP port |
| `SPRING_PROFILES_ACTIVE` | `dev` | Spring profile |

## Tests

```bash
./gradlew test
```

All tests deterministic, HTTP boundary mocked.

## What it would need to be a real signal-gen pipeline

This is a demo of the pipeline shape, not a production alpha factory. To take it further:

- A point-in-time fundamentals store (Yahoo gives latest values; backtests need as-of dates to avoid look-ahead)
- A separate scoring service per signal, with the universe stage publishing once and N rankers consuming
- Survivorship-bias handling — the current universe is "what Zacks lists today"
- Per-symbol data quality flags (stale prices, missing fundamentals, halted tickers) feeding into the export

These are the things that turn "screener" into "investable universe."

## Stack

- Java 25, Spring Boot 4.0.6 (Web, JPA, Reactive WebFlux)
- Flyway 11.x, PostgreSQL 17 via Docker
- Apache POI + JXLS (Excel export)
- Apache Tika (HTML parsing)
- Lombok, Slf4j
- JUnit Jupiter 6 + Mockito + Hamcrest
- Gradle 9.5.1

## Notes

- The fixer.io key is empty in `.env.example` — sign up for the free tier if you want FX conversion
- Zacks data is parsed from public pages; be respectful with throttling (`stocks.analysis.us.stocklookup.sleeptime`)
- Historical Excel templates live under `src/main/resources/template/`

## License

Apache 2.0 — see [LICENSE](LICENSE) and [NOTICE](NOTICE).
