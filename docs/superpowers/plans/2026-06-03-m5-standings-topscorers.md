# M5 — Football: Standings / Topscorers Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax.

**Goal:** Add the ranking endpoints — Standings (+ live, + corrections) and Topscorers — as strongly-typed model records plus endpoint classes wired into `FootballClient`. No `core` changes: reuse the M2 request plumbing (`CollectionRequest<T>`, `RequestSpec`, `ApiExecutor`, `DataType<T>`).

**Architecture:** Identical to M2/M3/M4. Immutable records in `football/.../model/`; endpoint classes in `football/.../endpoint/` turning calls into `CollectionRequest<T>` via the shared `ApiExecutor` + `JacksonCodec`. `FootballClient.Builder.build()` constructs each endpoint and exposes `standings()`, `topscorers()`.

**Tech Stack:** Java 25, Gradle (existing multi-module), Jackson (Blackbird, snake_case), JUnit 5 + WireMock + AssertJ. Builds on merged M1 `core` + M2/M3/M4 `football`.

**Source of truth:** SportMonks docs fetched as Markdown (`.md` URLs) — paths and fields below were confirmed from the live example JSON (not just the field tables). Both resources are **collection-only**: there is no `byId` single-resource path for either standings or topscorers.

---

## Confirmed endpoint paths (base `https://api.sportmonks.com/v3/football`)

**Standings** (issue #25) → `Standing`, all return `CollectionRequest<Standing>`:
- `all()` → `standings`
- `bySeason(seasonId)` → `standings/seasons/{seasonId}`
- `byRound(roundId)` → `standings/rounds/{roundId}`
- `correctionsBySeason(seasonId)` → `standings/corrections/seasons/{seasonId}`
- `liveByLeague(leagueId)` → `standings/leagues/{leagueId}/live`

**Topscorers** (issue #26) → `Topscorer`, all return `CollectionRequest<Topscorer>`:
- `bySeason(seasonId)` → `topscorers/seasons/{seasonId}`
- `byStage(stageId)` → `topscorers/stages/{stageId}`

> No `search`, no `byId`, no `byMultipleIds` for either resource. So no null-guard / empty-guard methods are needed this milestone (all inputs are primitive `long`).

---

## Model fields (confirmed from live example JSON)

**Standing:** `id`(long), `participant_id`(Long), `sport_id`(Long), `league_id`(Long), `season_id`(Long), `stage_id`(Long), `group_id`(Long — nullable), `round_id`(Long — nullable), `standing_rule_id`(Long), `position`(Integer), `result`(String — movement indicator e.g. `"equal"`/`"up"`/`"down"`, may be null), `points`(Integer). Relations (nullable, present only via includes): `participant`(Team), `details`(List&lt;StandingDetail&gt;).

**StandingDetail** (the per-row stat breakdown — the useful payload of a standings table): `id`(long), `standing_type`(String — e.g. `"normal"`/`"live"`), `standing_id`(Long), `type_id`(Long), `value`(Integer). No typed relations this milestone.

**Topscorer:** `id`(long), `season_id`(Long), `stage_id`(Long), `player_id`(Long), `participant_id`(Long), `type_id`(Long — metric type: goals/assists/cards), `position`(Integer), `total`(Integer). Relations (nullable, present only via includes): `player`(Player), `participant`(Team).

> Conventions (same as M2/M3/M4): only `id` is primitive `long`; every other scalar is a BOXED nullable type. Relations are nullable and present only when the API `include` requested them. **Only relations whose target type exists in this milestone or earlier are typed** — here `Standing.participant`→Team (M4), `Standing.details`→List&lt;StandingDetail&gt; (this milestone), `Topscorer.player`→Player (M4), `Topscorer.participant`→Team (M4). Other includes (`season`, `league`, `stage`, `round`, `group`, `rule`, `form`, `sport`) are intentionally NOT typed — their FK ids are already on the record, and modeling `rule`/`form`/`group`/`sport` is out of scope (no stub models). Each record carries `///` JavaDoc (JEP 467) on the type; keep ≥80% coverage (CodeRabbit gate). The type-level doc must accurately state which fields are always-present (`id`) vs nullable (everything else, including `result`, `group_id`, `round_id` and all relations) — don't claim nullable FK fields are "always present" (that was an M3 review finding).

---

## File Structure

```
football/src/main/java/io/github/miro93/sportmonks/football/
├── FootballClient.java                  # MODIFY — wire 2 new endpoints + accessors
├── endpoint/
│   ├── StandingsEndpoint.java           # NEW
│   └── TopscorersEndpoint.java          # NEW
└── model/
    ├── Standing.java                    # NEW
    ├── StandingDetail.java              # NEW
    └── Topscorer.java                   # NEW
football/src/test/java/io/github/miro93/sportmonks/football/
├── model/RankingsDecodingTest.java      # NEW (pure decode)
├── endpoint/StandingsEndpointTest.java  # NEW (WireMock)
├── endpoint/TopscorersEndpointTest.java # NEW
└── FootballClientM5Test.java            # NEW
```

Maps to M5 issues: models (#27) → Task 1; StandingsEndpoint (#25) → Task 2; TopscorersEndpoint (#26) → Task 3; FootballClient wiring → Task 4. WireMock tests (#28) across Tasks 1–4.

**Implementation order:** 1 (models) → 2 (standings) → 3 (topscorers) → 4 (client wiring). Endpoint tests construct endpoints directly; the client is wired and tested last.

---

## Task 1: Model records (Standing, StandingDetail, Topscorer) + decoding test

**Files:** create the three records + `model/RankingsDecodingTest.java`.

Mirror the M4 record style (`football/.../model/Squad.java`, `Team.java`). Use the field tables above. Wire the relations: `Standing.participant`(Team), `Standing.details`(List&lt;StandingDetail&gt;), `Topscorer.player`(Player), `Topscorer.participant`(Team). The decoding test feeds representative envelope JSON (**snake_case keys** — the codec is configured `SNAKE_CASE`; camelCase keys silently decode to a null `data`) through `JacksonCodec` and asserts scalars + relations, including: a Standing with `details` (each StandingDetail decoded) and a `participant` Team; a Standing with null relations / null `group_id`/`round_id`/`result`; a Topscorer with `player` + `participant`; and a Topscorer with null relations.

**Verification:** `./gradlew :football:test --tests '*RankingsDecodingTest'` green; `./gradlew build` green.

---

## Task 2: StandingsEndpoint (#25)

**Files:** `endpoint/StandingsEndpoint.java` + `endpoint/StandingsEndpointTest.java`.

Ctor `(ApiExecutor, JacksonCodec)` building `DataType<List<Standing>>` only (no single-resource type — there is no `byId`). Methods `all`, `bySeason`, `byRound`, `correctionsBySeason`, `liveByLeague` → `CollectionRequest<Standing>`, each via a private `collection(String path)` helper (mirror `SquadsEndpoint`). WireMock test per method asserting the exact path + decode; exercise `.getAsync()` on at least one; assert the live path is exactly `standings/leagues/{id}/live` and corrections is `standings/corrections/seasons/{id}`.

**Verification:** `./gradlew :football:test --tests '*StandingsEndpointTest'` green.

---

## Task 3: TopscorersEndpoint (#26)

**Files:** `endpoint/TopscorersEndpoint.java` + `endpoint/TopscorersEndpointTest.java`.

Ctor `(ApiExecutor, JacksonCodec)` building `DataType<List<Topscorer>>`. Methods `bySeason`, `byStage` → `CollectionRequest<Topscorer>` via a private `collection` helper. WireMock test per method asserting exact path + decode; `.getAsync()` exercised.

**Verification:** `./gradlew :football:test --tests '*TopscorersEndpointTest'` green.

---

## Task 4: Wire endpoints into FootballClient

**Files:** modify `FootballClient.java` + `FootballClientM5Test.java`.

In `build()`, construct the 2 new endpoints (sharing the existing `executor` + `codec`) and pass to the private ctor; add `standings()`, `topscorers()` accessors + fields + ctor params, each with `///` JavaDoc. Test builds a client (placeholder token, WireMock base URL) and asserts each accessor returns a working endpoint hitting the expected path. **Never use a real token** (placeholder `ApiToken.of("tok")` or the `SPORTMONKS_API_TOKEN` env var).

**Verification:** `./gradlew build` green (all M1–M5 tests).

---

## Definition of done

- [ ] All tasks complete; `./gradlew build` SUCCESSFUL.
- [ ] JavaDoc coverage ≥ 80% on new public API.
- [ ] No personal data committed (no real email/token/local path).
- [ ] `core/` untouched (verify `git diff <base> HEAD -- core/` is empty).
- [ ] PR opened against `main` closing #25, #26, #27, #28.
