# M11 — Premium Expected Lineups Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add the SportMonks Premium Expected Lineups feature (pre-match predicted lineups by team and by player) to the football module, exposed on `FootballClient`.

**Architecture:** Continuation of M4/M5/M8/M9/M10. One new record `ExpectedLineup` in `football.model`; one collection-only endpoint `ExpectedLineupsEndpoint` (clone of `PreMatchOddsEndpoint`) with `byTeam`/`byPlayer`, on the existing football base URL (`/v3/football`) — no new executor. One accessor on `FootballClient`, placed before `core` (which stays LAST).

**Tech Stack:** Java 25, Gradle, JDK HttpClient, Jackson (snake_case), JUnit 5 + WireMock + AssertJ.

---

### Task 1: `ExpectedLineup` model

**Files:**
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/model/ExpectedLineup.java`
- Test: `football/src/test/java/io/github/miro93/sportmonks/football/model/ExpectedLineupDecodingTest.java`

- [ ] **Step 1: Write the failing test**

Create `ExpectedLineupDecodingTest.java`:

```java
package io.github.miro93.sportmonks.football.model;

import io.github.miro93.sportmonks.core.json.JacksonCodec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExpectedLineupDecodingTest {

    private final JacksonCodec codec = new JacksonCodec();

    @Test
    void decodesExpectedLineupWithAllScalars() {
        String json = """
                {
                  "data": {
                    "id": 1,
                    "sport_id": 1,
                    "fixture_id": 18533878,
                    "player_id": 172,
                    "team_id": 1,
                    "type_id": 11,
                    "player_name": "Virgil van Dijk",
                    "jersey_number": 4,
                    "position_id": 148,
                    "detailed_position_id": 153,
                    "formation_field": "1",
                    "formation_position": "4"
                  }
                }
                """;

        ExpectedLineup lineup = codec.decode(json, codec.type(ExpectedLineup.class)).data();

        assertThat(lineup.id()).isEqualTo(1L);
        assertThat(lineup.sportId()).isEqualTo(1L);
        assertThat(lineup.fixtureId()).isEqualTo(18533878L);
        assertThat(lineup.playerId()).isEqualTo(172L);
        assertThat(lineup.teamId()).isEqualTo(1L);
        assertThat(lineup.typeId()).isEqualTo(11L);
        assertThat(lineup.playerName()).isEqualTo("Virgil van Dijk");
        assertThat(lineup.jerseyNumber()).isEqualTo(4);
        assertThat(lineup.positionId()).isEqualTo(148L);
        assertThat(lineup.detailedPositionId()).isEqualTo(153L);
        assertThat(lineup.formationField()).isEqualTo("1");
        assertThat(lineup.formationPosition()).isEqualTo("4");
    }

    @Test
    void decodesExpectedLineupWithOptionalFieldsAbsent() {
        String json = """
                { "data": { "id": 1 } }
                """;

        ExpectedLineup lineup = codec.decode(json, codec.type(ExpectedLineup.class)).data();

        assertThat(lineup.id()).isEqualTo(1L);
        assertThat(lineup.sportId()).isNull();
        assertThat(lineup.fixtureId()).isNull();
        assertThat(lineup.playerId()).isNull();
        assertThat(lineup.teamId()).isNull();
        assertThat(lineup.typeId()).isNull();
        assertThat(lineup.playerName()).isNull();
        assertThat(lineup.jerseyNumber()).isNull();
        assertThat(lineup.positionId()).isNull();
        assertThat(lineup.detailedPositionId()).isNull();
        assertThat(lineup.formationField()).isNull();
        assertThat(lineup.formationPosition()).isNull();
    }
}
```

Note on `formation_field`/`formation_position`: the SportMonks docs label these "integer",
but real payloads frequently return them as strings. The model types them as `String`
(project numbers-as-strings convention). If, when run against a real payload, they prove to
be stable plain integers, they can be retyped to `Integer` — but this test (which feeds them
as JSON strings `"1"`/`"4"`) defines the contract for now.

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :football:test --tests "*ExpectedLineupDecodingTest"`
Expected: FAIL — `ExpectedLineup` does not exist (compilation error).

- [ ] **Step 3: Write minimal implementation**

Create `ExpectedLineup.java`:

```java
package io.github.miro93.sportmonks.football.model;

/// A single premium expected-lineup entry from the SportMonks football API: the
/// predicted placement of one player in a fixture's lineup, available before the
/// official lineup is published. {@code id} is always present; every other field
/// may be {@code null}. {@code formationField} and {@code formationPosition} are
/// {@code String} because the API returns these placement markers as strings.
public record ExpectedLineup(
        long id,
        Long sportId,
        Long fixtureId,
        Long playerId,
        Long teamId,
        Long typeId,
        String playerName,
        Integer jerseyNumber,
        Long positionId,
        Long detailedPositionId,
        String formationField,
        String formationPosition) {
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :football:test --tests "*ExpectedLineupDecodingTest"`
Expected: PASS (2 tests).

- [ ] **Step 5: Commit**

```bash
git add football/src/main/java/io/github/miro93/sportmonks/football/model/ExpectedLineup.java \
        football/src/test/java/io/github/miro93/sportmonks/football/model/ExpectedLineupDecodingTest.java
git commit -m "feat: add ExpectedLineup model"
```

---

### Task 2: `ExpectedLineupsEndpoint`

**Files:**
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/endpoint/ExpectedLineupsEndpoint.java`
- Test: `football/src/test/java/io/github/miro93/sportmonks/football/endpoint/ExpectedLineupsEndpointTest.java`

- [ ] **Step 1: Write the failing test**

Create `ExpectedLineupsEndpointTest.java`:

```java
package io.github.miro93.sportmonks.football.endpoint;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class ExpectedLineupsEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private ExpectedLineupsEndpoint lineups(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new ExpectedLineupsEndpoint(executor, codec);
    }

    @Test
    void byTeamHitsTeamsPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/expected-lineups/teams/1")).willReturn(okJson("""
                { "data": [ { "id": 1, "team_id": 1, "player_name": "Virgil van Dijk" } ] }
                """)));

        var response = lineups(wm.getHttpBaseUrl()).byTeam(1L).get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().teamId()).isEqualTo(1L);
        assertThat(response.data().getFirst().playerName()).isEqualTo("Virgil van Dijk");
    }

    @Test
    void byPlayerHitsPlayersPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/expected-lineups/players/172")).willReturn(okJson("""
                { "data": [ { "id": 1, "player_id": 172, "player_name": "Virgil van Dijk" } ] }
                """)));

        var response = lineups(wm.getHttpBaseUrl()).byPlayer(172L).get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().playerId()).isEqualTo(172L);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :football:test --tests "*ExpectedLineupsEndpointTest"`
Expected: FAIL — `ExpectedLineupsEndpoint` does not exist (compilation error).

- [ ] **Step 3: Write minimal implementation**

Create `ExpectedLineupsEndpoint.java`:

```java
package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.football.model.ExpectedLineup;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks premium expected lineups endpoints
/// ({@code /expected-lineups}): pre-match predicted lineups by team and by player.
public final class ExpectedLineupsEndpoint {

    private final ApiExecutor executor;
    private final DataType<List<ExpectedLineup>> list;

    /// Creates the endpoint, building the {@link ExpectedLineup} list decoder from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the list response type
    public ExpectedLineupsEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.list = codec.listType(ExpectedLineup.class);
    }

    /// Requests the expected lineup entries for a given team.
    ///
    /// @param teamId the team id
    /// @return a collection request for the matching expected lineup entries
    public CollectionRequest<ExpectedLineup> byTeam(long teamId) {
        return collection("expected-lineups/teams/" + teamId);
    }

    /// Requests the expected lineup entries for a given player.
    ///
    /// @param playerId the player id
    /// @return a collection request for the matching expected lineup entries
    public CollectionRequest<ExpectedLineup> byPlayer(long playerId) {
        return collection("expected-lineups/players/" + playerId);
    }

    private CollectionRequest<ExpectedLineup> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :football:test --tests "*ExpectedLineupsEndpointTest"`
Expected: PASS (2 tests).

- [ ] **Step 5: Commit**

```bash
git add football/src/main/java/io/github/miro93/sportmonks/football/endpoint/ExpectedLineupsEndpoint.java \
        football/src/test/java/io/github/miro93/sportmonks/football/endpoint/ExpectedLineupsEndpointTest.java
git commit -m "feat: add ExpectedLineupsEndpoint"
```

---

### Task 3: Wire `ExpectedLineupsEndpoint` into `FootballClient`

**Files:**
- Modify: `football/src/main/java/io/github/miro93/sportmonks/football/FootballClient.java`
- Test: `football/src/test/java/io/github/miro93/sportmonks/football/FootballClientM11Test.java`

The new field / constructor param / assignment / accessor / build() argument go **after
`premiumBookmakers` and before `core`** (core stays LAST). No new executor — uses the
existing football `executor`.

- [ ] **Step 1: Write the failing test**

Create `FootballClientM11Test.java`:

```java
package io.github.miro93.sportmonks.football;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class FootballClientM11Test {

    private FootballClient client(String baseUrl) {
        return FootballClient.builder()
                .apiToken(ApiToken.of("tok"))
                .baseUrl(baseUrl)
                .build();
    }

    @Test
    void exposesExpectedLineups(WireMockRuntimeInfo wm) {
        assertThat(client(wm.getHttpBaseUrl()).expectedLineups()).isNotNull();
    }

    @Test
    void expectedLineupsByTeamDecodesAndSendsAuth(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/expected-lineups/teams/1")).willReturn(okJson("""
                { "data": [{ "id": 1, "team_id": 1, "player_name": "Virgil van Dijk" }] }
                """)));

        var lineups = client(wm.getHttpBaseUrl()).expectedLineups().byTeam(1L).get().data();

        assertThat(lineups).hasSize(1);
        assertThat(lineups.getFirst().playerName()).isEqualTo("Virgil van Dijk");
        verify(getRequestedFor(urlPathEqualTo("/expected-lineups/teams/1"))
                .withHeader("Authorization", equalTo("tok")));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :football:test --tests "*FootballClientM11Test"`
Expected: FAIL — `expectedLineups()` does not exist (compilation error).

- [ ] **Step 3: Add the import**

In `FootballClient.java`, add this import alongside the existing `endpoint.*` imports
(alphabetical order — it sorts right after the `CommentariesEndpoint` import and before
`FixturesEndpoint`):

```java
import io.github.miro93.sportmonks.football.endpoint.ExpectedLineupsEndpoint;
```

- [ ] **Step 4: Add the field**

In the private field block, between `private final PremiumBookmakersEndpoint premiumBookmakers;`
and `private final CoreClient core;`, add:

```java
    private final ExpectedLineupsEndpoint expectedLineups;
```

- [ ] **Step 5: Add the constructor param and assignment**

In the private constructor signature, between `PremiumBookmakersEndpoint premiumBookmakers,`
and `CoreClient core)`, add:

```java
            ExpectedLineupsEndpoint expectedLineups,
```

In the constructor body, between `this.premiumBookmakers = premiumBookmakers;` and
`this.core = core;`, add:

```java
        this.expectedLineups = expectedLineups;
```

- [ ] **Step 6: Add the accessor**

Between the `premiumBookmakers()` accessor and the `core()` accessor, add:

```java
    /// Returns the premium expected lineups endpoint.
    ///
    /// @return the {@code /expected-lineups} endpoint accessor
    public ExpectedLineupsEndpoint expectedLineups() {
        return expectedLineups;
    }
```

- [ ] **Step 7: Wire the endpoint in `build()`**

In the `return new FootballClient(...)` argument list, between the
`new PremiumBookmakersEndpoint(oddsExecutor, codec),` argument and the trailing `core)`
argument, add:

```java
                    new ExpectedLineupsEndpoint(executor, codec),
```

- [ ] **Step 8: Run test to verify it passes**

Run: `./gradlew :football:test --tests "*FootballClientM11Test"`
Expected: PASS (2 tests).

- [ ] **Step 9: Run the full football test suite**

Run: `./gradlew :football:test`
Expected: PASS (all existing tests + the new M11 tests).

- [ ] **Step 10: Commit**

```bash
git add football/src/main/java/io/github/miro93/sportmonks/football/FootballClient.java \
        football/src/test/java/io/github/miro93/sportmonks/football/FootballClientM11Test.java
git commit -m "feat: expose expected lineups on FootballClient"
```

---

### Task 4: README documentation

**Files:**
- Modify: `README.md`

- [ ] **Step 1: Locate the football endpoints table**

Run: `grep -n "premiumBookmakers\|premiumOdds" README.md`
Expected: shows the M10 premium rows in the football endpoints table.

- [ ] **Step 2: Add the expectedLineups accessor row**

The football endpoints table uses three columns: `| accessor | EndpointClass | methods |`.
After the `premiumBookmakers()` row (the last premium row), add this row verbatim:

```markdown
| `expectedLineups()` | `ExpectedLineupsEndpoint` | `byTeam(teamId)`, `byPlayer(playerId)` |
```

- [ ] **Step 3: Verify the row renders**

Run: `grep -n "expectedLineups" README.md`
Expected: shows the new row.

- [ ] **Step 4: Commit**

```bash
git add README.md
git commit -m "docs: document expected lineups endpoint"
```

---

### Task 5: Final verification

- [ ] **Step 1: Run the complete build and test suite**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL — all modules compile, all tests pass (M10 total + 6 new M11 tests).

---

## Self-Review Notes

- **Spec coverage:** `ExpectedLineup` model with all 12 fields (Task 1), `ExpectedLineupsEndpoint`
  with `byTeam`/`byPlayer` (Task 2), `FootballClient` accessor placed before `core` with no new
  executor (Task 3), README row (Task 4), full build (Task 5). All spec sections map to a task.
- **Type consistency:** record component names (`sportId`, `fixtureId`, `playerId`, `teamId`,
  `typeId`, `playerName`, `jerseyNumber`, `positionId`, `detailedPositionId`, `formationField`,
  `formationPosition`), accessor name (`expectedLineups`), and method names (`byTeam`,
  `byPlayer`) match across all tasks and tests.
- **Placeholders:** none — every code step contains complete code.
- **Note carried from spec:** `formationField`/`formationPosition` typed as `String`; the
  decoding test feeds them as JSON strings and pins that contract. Flagged for re-typing only
  if real payloads prove otherwise.
