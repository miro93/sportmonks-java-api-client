# M3 — Football: Leagues / Seasons / Stages Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add the competition-structure endpoints — Leagues, Seasons, Stages, Rounds, Schedules — as strongly-typed model records plus endpoint classes wired into `FootballClient`. No `core` changes: the M2 request plumbing (`SingleResourceRequest<T>`, `CollectionRequest<T>`, `RequestSpec`, `ApiExecutor`) is reused as-is.

**Architecture:** Mirror the M2 pattern exactly. Each resource is an immutable record in `football/.../model/`; each endpoint class in `football/.../endpoint/` turns calls into `SingleResourceRequest<T>` / `CollectionRequest<T>` via the shared `ApiExecutor` + `JacksonCodec`. `FootballClient.Builder.build()` constructs each endpoint and exposes `leagues()`, `seasons()`, `stages()`, `rounds()`, `schedules()`. Schedules reuse `Stage` (a schedule is a stage tree with nested `rounds` → `fixtures`), so there is no separate `Schedule` model; instead `Stage` gains a nullable `rounds` relation and `Round` gains a nullable `fixtures` relation (reusing the M2 `Fixture`).

**Tech Stack:** Java 25, Gradle (existing multi-module), Jackson (Blackbird, snake_case), JUnit 5 + WireMock + AssertJ. Builds on merged M1 `core` + M2 `football`.

**Source of truth for paths & fields:** SportMonks docs are fetchable as Markdown by appending `.md` to the page URL (see `sitemap.md`). Endpoint paths and entity fields below were taken from the `get-all-*` detail pages. Implementers MUST consult the relevant `.md` page's example JSON to confirm the exact runtime type of each field before typing it (the "Field Description" tables sometimes say `integer` where the live API returns a boolean).

---

## Confirmed endpoint paths (base `https://api.sportmonks.com/v3/football`)

**Leagues** (issue #15):
- `all` → `leagues`
- `byId(id)` → `leagues/{id}`
- `byCountry(countryId)` → `leagues/countries/{countryId}`
- `search(name)` → `leagues/search/{name}`
- `live()` → `leagues/live`
- `byDate(date)` → `leagues/fixtures/{date}`

**Seasons** (issue #16):
- `all` → `seasons`
- `byId(id)` → `seasons/{id}`
- `byTeam(teamId)` → `seasons/teams/{teamId}`
- `search(name)` → `seasons/search/{name}`
- ⚠ NO native `byLeague` path — dropped. League→seasons is obtained via the leagues endpoint with `include=seasons`. Update issue #16 to note this.

**Stages** (issue #17):
- `all` → `stages`
- `byId(id)` → `stages/{id}`
- `bySeason(seasonId)` → `stages/seasons/{seasonId}`
- `search(name)` → `stages/search/{name}`

**Rounds** (issue #17):
- `all` → `rounds`
- `byId(id)` → `rounds/{id}`
- `bySeason(seasonId)` → `rounds/seasons/{seasonId}`
- `search(name)` → `rounds/search/{name}`

**Schedules** (issue #17) — return `List<Stage>`:
- `bySeason(seasonId)` → `schedules/seasons/{seasonId}`
- `byTeam(teamId)` → `schedules/teams/{teamId}`
- `bySeasonAndTeam(seasonId, teamId)` → `schedules/seasons/{seasonId}/teams/{teamId}`

---

## Model fields (confirmed from docs `get-all-*.md`)

**League:** `id`(long), `sport_id`(Long), `country_id`(Long), `name`(String), `active`(Boolean), `short_code`(String, nullable), `image_path`(String), `type`(String), `sub_type`(String), `last_played_at`(String), `category`(Integer), `has_jerseys`(Boolean). Relation (nullable): `seasons` (List<Season>).

**Season:** `id`(long), `sport_id`(Long), `league_id`(Long), `tie_breaker_rule_id`(Long), `name`(String), `finished`(Boolean), `pending`(Boolean), `is_current`(Boolean), `starting_at`(String), `ending_at`(String), `standings_recalculated_at`(String), `games_in_current_week`(Boolean), `standing_method`(String). Relation (nullable): `league`(League), `stages`(List<Stage>).

**Stage:** `id`(long), `sport_id`(Long), `league_id`(Long), `season_id`(Long), `type_id`(Long), `name`(String), `sort_order`(Integer), `finished`(Boolean), `pending`(Boolean), `is_current`(Boolean), `starting_at`(String), `ending_at`(String), `games_in_current_week`(Boolean). Relations (nullable): `rounds`(List<Round>) — populated by schedules and `include=rounds`.

**Round:** `id`(long), `sport_id`(Long), `league_id`(Long), `season_id`(Long), `stage_id`(Long), `group_id`(Long), `name`(String), `finished`(Boolean), `pending`(Boolean), `is_current`(Boolean), `starting_at`(String), `ending_at`(String), `games_in_current_week`(Boolean). Relations (nullable): `fixtures`(List<Fixture>) — populated by schedules and `include=fixtures`.

> Use boxed types for every scalar except `id` (always present → `long`). Boolean flags (`finished`/`pending`/`is_current`/`games_in_current_week`/`active`/`has_jerseys`) are `Boolean` (nullable) — confirm against the example JSON. Relations are always nullable (only present when included). Every record carries full JavaDoc (`///`) on the type AND each public accessor is covered by the type-level doc; add per-component prose where it aids clarity. Keep coverage ≥ 80% (CodeRabbit gate) — see M2 commit `69c6284`.

---

## File Structure

```
football/src/main/java/io/github/miro93/sportmonks/football/
├── FootballClient.java                 # MODIFY — wire 5 new endpoints + accessors
├── endpoint/
│   ├── LeaguesEndpoint.java            # NEW
│   ├── SeasonsEndpoint.java            # NEW
│   ├── StagesEndpoint.java             # NEW
│   ├── RoundsEndpoint.java             # NEW
│   └── SchedulesEndpoint.java          # NEW
└── model/
    ├── League.java                     # NEW
    ├── Season.java                     # NEW
    ├── Stage.java                      # NEW
    └── Round.java                      # NEW
football/src/test/java/io/github/miro93/sportmonks/football/
├── model/CompetitionDecodingTest.java  # NEW (WireMock-free pure decode)
├── endpoint/LeaguesEndpointTest.java   # NEW (WireMock)
├── endpoint/SeasonsEndpointTest.java   # NEW (WireMock)
├── endpoint/StagesEndpointTest.java    # NEW (WireMock)
├── endpoint/RoundsEndpointTest.java    # NEW (WireMock)
├── endpoint/SchedulesEndpointTest.java # NEW (WireMock)
└── FootballClientM3Test.java           # NEW — client exposes/wires new endpoints
```

Maps to M3 issues: models (#18) → Task 1; LeaguesEndpoint (#15) → Task 2; SeasonsEndpoint (#16) → Task 3; Stages/Rounds/Schedules (#17) → Task 4; FootballClient wiring → Task 5. WireMock tests (#19) are produced across Tasks 1–5.

**Implementation order:** 1 (models) → 2 (leagues) → 3 (seasons) → 4 (stages/rounds/schedules) → 5 (client wiring). Endpoint tests construct endpoints directly (`new LeaguesEndpoint(executor, codec)`); the client is wired and tested last.

---

## Task 1: Model records (League, Season, Stage, Round) + decoding test

**Files:**
- Create: `football/.../model/League.java`, `Season.java`, `Stage.java`, `Round.java`
- Test: `football/.../model/CompetitionDecodingTest.java`

Model each record per the field tables above, mirroring the M2 `Fixture`/`Participant` style (boxed nullable scalars, `long id`, nullable relation lists). `Stage.rounds` is `List<Round>`; `Round.fixtures` is `List<Fixture>` (import the M2 `Fixture`); `Season.stages` is `List<Stage>`; `Season.league`/`League.seasons` round out the relations.

The decoding test feeds representative envelope JSON (copy field shapes from the docs example responses, including a nested schedule-style `stage → rounds → fixtures` payload) straight through `JacksonCodec` and asserts every scalar and relation maps correctly, including `null` relations when not included.

**Verification:** `./gradlew :football:test --tests '*CompetitionDecodingTest'` green; `./gradlew build` green.

---

## Task 2: LeaguesEndpoint (#15)

**Files:**
- Create: `football/.../endpoint/LeaguesEndpoint.java`
- Test: `football/.../endpoint/LeaguesEndpointTest.java`

Ctor `(ApiExecutor, JacksonCodec)` building `DataType<League>` + `DataType<List<League>>`. Methods: `all()`, `byId(long)` (→ `SingleResourceRequest<League>`), `byCountry(long)`, `search(String)` (guard null), `live()`, `byDate(LocalDate)` — all `CollectionRequest<League>`. WireMock test asserts the exact path/query for each method and that a sample envelope decodes.

**Verification:** `./gradlew :football:test --tests '*LeaguesEndpointTest'` green.

---

## Task 3: SeasonsEndpoint (#16)

**Files:**
- Create: `football/.../endpoint/SeasonsEndpoint.java`
- Test: `football/.../endpoint/SeasonsEndpointTest.java`

Ctor `(ApiExecutor, JacksonCodec)`. Methods: `all()`, `byId(long)` (→ `SingleResourceRequest<Season>`), `byTeam(long)`, `search(String)` (guard null) — `CollectionRequest<Season>`. NO `byLeague`. WireMock test per method.

**Verification:** `./gradlew :football:test --tests '*SeasonsEndpointTest'` green.

---

## Task 4: StagesEndpoint + RoundsEndpoint + SchedulesEndpoint (#17)

**Files:**
- Create: `football/.../endpoint/StagesEndpoint.java`, `RoundsEndpoint.java`, `SchedulesEndpoint.java`
- Test: `football/.../endpoint/StagesEndpointTest.java`, `RoundsEndpointTest.java`, `SchedulesEndpointTest.java`

`StagesEndpoint` / `RoundsEndpoint`: ctor `(ApiExecutor, JacksonCodec)`; methods `all()`, `byId(long)` (→ `SingleResourceRequest`), `bySeason(long)`, `search(String)` (guard null) → `CollectionRequest`. `SchedulesEndpoint`: ctor `(ApiExecutor, JacksonCodec)` building `DataType<List<Stage>>`; methods `bySeason(long)`, `byTeam(long)`, `bySeasonAndTeam(long, long)` → `CollectionRequest<Stage>`. WireMock tests assert exact paths (note schedules' nested `seasons/{id}/teams/{id}`).

**Verification:** `./gradlew :football:test --tests '*StagesEndpointTest' --tests '*RoundsEndpointTest' --tests '*SchedulesEndpointTest'` green.

---

## Task 5: Wire endpoints into FootballClient

**Files:**
- Modify: `football/.../FootballClient.java`
- Test: `football/.../FootballClientM3Test.java`

In `build()`, construct the 5 new endpoints (sharing the existing `executor` + `codec`) and pass them to the private ctor; add `leagues()`, `seasons()`, `stages()`, `rounds()`, `schedules()` accessors (with `///` JavaDoc). Test builds a client (placeholder token, WireMock base URL) and asserts each accessor returns a working endpoint that hits the expected path. Never use a real token — env `SPORTMONKS_API_TOKEN` or a placeholder.

**Verification:** `./gradlew build` green (all M1+M2+M3 tests).

---

## Definition of done

- [ ] All tasks complete; `./gradlew build` SUCCESSFUL.
- [ ] JavaDoc coverage ≥ 80% on new public API (types, ctors, methods).
- [ ] No personal data committed (no real email/token/local path).
- [ ] PR opened against `main` closing #15, #16, #17, #18, #19.
- [ ] Issue #16 updated to note `byLeague` is not a native endpoint.
