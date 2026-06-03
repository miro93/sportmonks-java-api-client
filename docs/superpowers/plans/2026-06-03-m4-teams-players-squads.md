# M4 — Football: Teams / Players / Squads Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax.

**Goal:** Add the participant endpoints — Teams, Players, Coaches, Squads, Transfers — as strongly-typed model records plus endpoint classes wired into `FootballClient`. No `core` changes: reuse the M2 request plumbing (`SingleResourceRequest<T>`, `CollectionRequest<T>`, `RequestSpec`, `ApiExecutor`).

**Architecture:** Identical to M2/M3. Immutable records in `football/.../model/`; endpoint classes in `football/.../endpoint/` turning calls into `SingleResourceRequest<T>` / `CollectionRequest<T>` via the shared `ApiExecutor` + `JacksonCodec`. `FootballClient.Builder.build()` constructs each endpoint and exposes `teams()`, `players()`, `coaches()`, `squads()`, `transfers()`.

**Tech Stack:** Java 25, Gradle (existing multi-module), Jackson (Blackbird, snake_case), JUnit 5 + WireMock + AssertJ. Builds on merged M1 `core` + M2/M3 `football`.

**Source of truth:** SportMonks docs fetched as Markdown (`.md` URLs). Paths/fields below were confirmed from the `get-all-*.md` detail pages. Implementers should glance at the example JSON to confirm a field's real runtime type when in doubt (the field tables sometimes say `integer` where the live API returns a boolean, or `string` for an id like `city_id`).

---

## Confirmed endpoint paths (base `https://api.sportmonks.com/v3/football`)

**Teams** (issue #20) → `Team`:
- `all()` → `teams`
- `byId(id)` → `teams/{id}`
- `byMultipleIds(ids...)` → `teams/multi/{csv}` (comma-joined; guard empty, mirror `FixturesEndpoint.byMultipleIds`)
- `bySeason(seasonId)` → `teams/seasons/{seasonId}`
- `byCountry(countryId)` → `teams/countries/{countryId}`
- `search(name)` → `teams/search/{name}` (guard null)

**Players** (issue #21) → `Player`:
- `all()` → `players`
- `byId(id)` → `players/{id}`
- `byCountry(countryId)` → `players/countries/{countryId}`
- `search(name)` → `players/search/{name}` (guard null)
- `latest()` → `players/latest`

**Coaches** (issue #21, split into its own endpoint class) → `Coach`:
- `all()` → `coaches`
- `byId(id)` → `coaches/{id}`
- `byCountry(countryId)` → `coaches/countries/{countryId}`
- `search(name)` → `coaches/search/{name}` (guard null)
- `latest()` → `coaches/latest`

**Squads** (issue #22) → `Squad`:
- `byTeam(teamId)` → `squads/teams/{teamId}` (current domestic squad)
- `bySeasonAndTeam(seasonId, teamId)` → `squads/seasons/{seasonId}/teams/{teamId}` (historical)

**Transfers** (issue #22, split into its own endpoint class) → `Transfer`:
- `all()` → `transfers`
- `byId(id)` → `transfers/{id}`
- `latest()` → `transfers/latest`
- `byDateRange(start, end)` → `transfers/between/{start}/{end}` (guard nulls)
- `byTeam(teamId)` → `transfers/teams/{teamId}`
- `byPlayer(playerId)` → `transfers/players/{playerId}`

---

## Model fields (confirmed from docs)

**Team:** `id`(long), `sport_id`(Long), `country_id`(Long), `venue_id`(Long), `gender`(String), `name`(String), `short_code`(String), `image_path`(String), `founded`(Integer), `type`(String), `placeholder`(boolean — always present, primitive, mirror `Fixture.placeholder`), `last_played_at`(String). Relation (nullable): `squad`(List&lt;Squad&gt;).

**Player:** `id`(long), `sport_id`(Long), `country_id`(Long), `nationality_id`(Long), `city_id`(String — yes, the API returns this as a string), `position_id`(Long), `detailed_position_id`(Long), `type_id`(Long), `common_name`(String), `firstname`(String), `lastname`(String), `name`(String), `display_name`(String), `image_path`(String), `height`(Integer), `weight`(Integer), `date_of_birth`(String), `gender`(String). No typed relations this milestone.

**Coach:** `id`(long), `player_id`(Long), `sport_id`(Long), `country_id`(Long), `nationality_id`(Long), `city_id`(String), `common_name`(String), `firstname`(String), `lastname`(String), `name`(String), `display_name`(String), `image_path`(String), `height`(Integer), `weight`(Integer), `date_of_birth`(String), `gender`(String). No typed relations this milestone.

**Squad** (squad member):** `id`(long), `transfer_id`(Long), `player_id`(Long), `team_id`(Long), `position_id`(Long), `detailed_position_id`(Long), `jersey_number`(Integer), `start`(String), `end`(String). Relation (nullable): `player`(Player).

**Transfer:** `id`(long), `sport_id`(Long), `player_id`(Long), `type_id`(Long), `from_team_id`(Long), `to_team_id`(Long), `position_id`(Long), `detailed_position_id`(Long), `date`(String), `career_ended`(Boolean), `completed`(Boolean), `amount`(String — may be null when fee undisclosed), `completed_at`(String). Relations (nullable): `player`(Player), `fromTeam`(Team), `toTeam`(Team).

> Conventions (same as M2/M3): only `id` is primitive `long`; `Team.placeholder` is primitive `boolean` (mirrors `Fixture`); every other scalar is a BOXED nullable type. Relations are nullable and present only when the API `include` requested them. **Only relations whose target type exists in this milestone or earlier are typed** (`Squad.player`→Player, `Team.squad`→List&lt;Squad&gt;, `Transfer.player/fromTeam/toTeam`). Includes whose targets aren't modeled yet (venue, country, position, nationality) are intentionally NOT added — no stub models. Each record carries `///` JavaDoc (JEP 467) on the type; keep ≥80% coverage (CodeRabbit gate). The type-level doc must accurately state which fields are always-present vs nullable (don't claim nullable FK fields are "always present" — that was an M3 review finding).

---

## File Structure

```
football/src/main/java/io/github/miro93/sportmonks/football/
├── FootballClient.java                 # MODIFY — wire 5 new endpoints + accessors
├── endpoint/
│   ├── TeamsEndpoint.java              # NEW
│   ├── PlayersEndpoint.java            # NEW
│   ├── CoachesEndpoint.java            # NEW
│   ├── SquadsEndpoint.java             # NEW
│   └── TransfersEndpoint.java          # NEW
└── model/
    ├── Team.java                       # NEW
    ├── Player.java                     # NEW
    ├── Coach.java                      # NEW
    ├── Squad.java                      # NEW
    └── Transfer.java                   # NEW
football/src/test/java/io/github/miro93/sportmonks/football/
├── model/ParticipantsDecodingTest.java # NEW (pure decode)
├── endpoint/TeamsEndpointTest.java     # NEW (WireMock)
├── endpoint/PlayersEndpointTest.java   # NEW
├── endpoint/CoachesEndpointTest.java   # NEW
├── endpoint/SquadsEndpointTest.java    # NEW
├── endpoint/TransfersEndpointTest.java # NEW
└── FootballClientM4Test.java           # NEW
```

Maps to M4 issues: models (#23) → Task 1; TeamsEndpoint (#20) → Task 2; Players+Coaches (#21) → Task 3; Squads+Transfers (#22) → Task 4; FootballClient wiring → Task 5. WireMock tests (#24) across Tasks 1–5.

**Implementation order:** 1 (models) → 2 (teams) → 3 (players+coaches) → 4 (squads+transfers) → 5 (client wiring). Endpoint tests construct endpoints directly; the client is wired and tested last.

> NOTE the existing `Participant` record (M2) is a different thing (a fixture participant) — do NOT confuse it with `Team`. They are separate types; do not merge or rename.

---

## Task 1: Model records (Team, Player, Coach, Squad, Transfer) + decoding test

**Files:** create the five records + `model/ParticipantsDecodingTest.java`.

Mirror the M3 record style (`football/.../model/Season.java`, `Stage.java`). Use the field tables above. Wire the relations: `Team.squad`(List&lt;Squad&gt;), `Squad.player`(Player), `Transfer.player`(Player)/`fromTeam`(Team)/`toTeam`(Team). The decoding test feeds representative envelope JSON through `JacksonCodec` and asserts scalars + relations, including null relations when not included, and at least one nested case (e.g. a Team with `squad` → each Squad with `player`, and a Transfer with `player`/`fromTeam`/`toTeam`).

**Verification:** `./gradlew :football:test --tests '*ParticipantsDecodingTest'` green; `./gradlew build` green.

---

## Task 2: TeamsEndpoint (#20)

**Files:** `endpoint/TeamsEndpoint.java` + `endpoint/TeamsEndpointTest.java`.

Ctor `(ApiExecutor, JacksonCodec)` building `DataType<Team>` + `DataType<List<Team>>`. Methods per the Teams path list (mirror `FixturesEndpoint` for `byMultipleIds` empty-guard and `LeaguesEndpoint`/`SeasonsEndpoint` for the rest). WireMock test per method asserting exact path + decode; exercise `.getAsync()`; test `search(null)` and `byMultipleIds()` (empty) throw.

**Verification:** `./gradlew :football:test --tests '*TeamsEndpointTest'` green.

---

## Task 3: PlayersEndpoint + CoachesEndpoint (#21)

**Files:** `endpoint/PlayersEndpoint.java`, `endpoint/CoachesEndpoint.java` + their tests.

Both ctor `(ApiExecutor, JacksonCodec)`. Methods per the Players/Coaches path lists (`all`, `byId`→Single, `byCountry`, `search` null-guarded, `latest`). WireMock tests per method; `.getAsync()` exercised; `search(null)` throws.

**Verification:** `./gradlew :football:test --tests '*PlayersEndpointTest' --tests '*CoachesEndpointTest'` green.

---

## Task 4: SquadsEndpoint + TransfersEndpoint (#22)

**Files:** `endpoint/SquadsEndpoint.java`, `endpoint/TransfersEndpoint.java` + their tests.

`SquadsEndpoint` ctor builds `DataType<List<Squad>>`; methods `byTeam`, `bySeasonAndTeam` → `CollectionRequest<Squad>`. `TransfersEndpoint` builds `DataType<Transfer>` + `DataType<List<Transfer>>`; methods `all`, `byId`→Single, `latest`, `byDateRange` (guard null start/end), `byTeam`, `byPlayer` → collections. WireMock tests per method (note the nested `squads/seasons/{id}/teams/{id}` path and `transfers/between/{start}/{end}`); `.getAsync()` exercised; `byDateRange(null,...)` throws.

**Verification:** `./gradlew :football:test --tests '*SquadsEndpointTest' --tests '*TransfersEndpointTest'` green.

---

## Task 5: Wire endpoints into FootballClient

**Files:** modify `FootballClient.java` + `FootballClientM4Test.java`.

In `build()`, construct the 5 new endpoints (sharing the existing `executor` + `codec`) and pass to the private ctor; add `teams()`, `players()`, `coaches()`, `squads()`, `transfers()` accessors with `///` JavaDoc. Test builds a client (placeholder token, WireMock base URL) and asserts each accessor returns a working endpoint hitting the expected path. Never use a real token.

**Verification:** `./gradlew build` green (all M1–M4 tests).

---

## Definition of done

- [ ] All tasks complete; `./gradlew build` SUCCESSFUL.
- [ ] JavaDoc coverage ≥ 80% on new public API.
- [ ] No personal data committed (no real email/token/local path).
- [ ] PR opened against `main` closing #20, #21, #22, #23, #24.
