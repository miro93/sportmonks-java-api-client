# M13 — Statistics + Expected (xG) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add the SportMonks dedicated Statistics endpoints (season-by-participant, stage, round) and Expected/xG endpoints (per fixture, per lineup) to the football module, exposed on `FootballClient`.

**Architecture:** Continuation of M4–M12. New records `Statistic` + nested `StatisticDetail` (variable `value` as `Map<String,Object>`) and `Expected` (variable `data` as `Map<String,Object>`). Two collection-only endpoints `StatisticsEndpoint` and `ExpectedEndpoint` cloning the `PreMatchOddsEndpoint` pattern, on the football base URL (`/v3/football`) — no new executor. Two accessors on `FootballClient`, placed before `core` (which stays LAST).

**Tech Stack:** Java 25, Gradle, JDK HttpClient, Jackson (snake_case; free-form objects → `Map<String,Object>`, `details` array → `List<StatisticDetail>`), JUnit 5 + WireMock + AssertJ.

---

### Task 1: `StatisticDetail` + `Statistic` models

**Files:**
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/model/StatisticDetail.java`
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/model/Statistic.java`
- Test: `football/src/test/java/io/github/miro93/sportmonks/football/model/StatisticDecodingTest.java`

- [ ] **Step 1: Write the failing test**

Create `StatisticDecodingTest.java`:

```java
package io.github.miro93.sportmonks.football.model;

import io.github.miro93.sportmonks.core.json.JacksonCodec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StatisticDecodingTest {

    private final JacksonCodec codec = new JacksonCodec();

    @Test
    void decodesStatisticWithDetails() {
        String json = """
                {
                  "data": {
                    "id": 5001,
                    "team_id": 1,
                    "season_id": 19735,
                    "has_values": true,
                    "position_id": 1,
                    "jersey_number": 4,
                    "details": [
                      { "id": 9001, "type_id": 52, "value": { "total": 12 } },
                      { "id": 9002, "type_id": 80, "value": { "home": 5, "away": 7 } }
                    ]
                  }
                }
                """;

        Statistic statistic = codec.decode(json, codec.type(Statistic.class)).data();

        assertThat(statistic.id()).isEqualTo(5001L);
        assertThat(statistic.teamId()).isEqualTo(1L);
        assertThat(statistic.seasonId()).isEqualTo(19735L);
        assertThat(statistic.hasValues()).isTrue();
        assertThat(statistic.positionId()).isEqualTo(1L);
        assertThat(statistic.jerseyNumber()).isEqualTo(4);
        assertThat(statistic.details()).hasSize(2);
        assertThat(statistic.details().getFirst().id()).isEqualTo(9001L);
        assertThat(statistic.details().getFirst().typeId()).isEqualTo(52L);
        assertThat(statistic.details().getFirst().value()).containsEntry("total", 12);
        assertThat(statistic.details().get(1).value())
                .containsEntry("home", 5)
                .containsEntry("away", 7);
    }

    @Test
    void decodesStatisticWithOptionalFieldsAbsent() {
        String json = """
                { "data": { "id": 5001 } }
                """;

        Statistic statistic = codec.decode(json, codec.type(Statistic.class)).data();

        assertThat(statistic.id()).isEqualTo(5001L);
        assertThat(statistic.playerId()).isNull();
        assertThat(statistic.coachId()).isNull();
        assertThat(statistic.teamId()).isNull();
        assertThat(statistic.refereeId()).isNull();
        assertThat(statistic.seasonId()).isNull();
        assertThat(statistic.stageId()).isNull();
        assertThat(statistic.roundId()).isNull();
        assertThat(statistic.hasValues()).isNull();
        assertThat(statistic.positionId()).isNull();
        assertThat(statistic.jerseyNumber()).isNull();
        assertThat(statistic.details()).isNull();
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :football:test --tests "*StatisticDecodingTest"`
Expected: FAIL — `Statistic` / `StatisticDetail` do not exist (compilation error).

- [ ] **Step 3: Write minimal implementation**

Create `StatisticDetail.java`:

```java
package io.github.miro93.sportmonks.football.model;

import java.util.Map;

/// A single detail entry inside a {@link Statistic}. {@code id} is always
/// present; every other field may be {@code null}. {@code value} is a free-form
/// object whose keys depend on {@code typeId} (e.g. {@code {total}},
/// {@code {home,away}}, {@code {average,highest,lowest}}), so it is exposed as a
/// raw {@code Map<String, Object>}; numeric values decode as
/// {@code Double}/{@code Integer}.
public record StatisticDetail(
        long id,
        Long typeId,
        Map<String, Object> value) {
}
```

Create `Statistic.java`:

```java
package io.github.miro93.sportmonks.football.model;

import java.util.List;

/// A statistics record from the SportMonks statistics endpoints (season by
/// participant, stage, round). A single unified envelope: the participant ids
/// ({@code playerId}/{@code coachId}/{@code teamId}/{@code refereeId}) and scope
/// ids ({@code seasonId}/{@code stageId}/{@code roundId}) that do not apply to
/// the requested context are {@code null}. {@code id} is always present; every
/// other field may be {@code null}. {@code details} carries the individual
/// statistic values.
public record Statistic(
        long id,
        Long playerId,
        Long coachId,
        Long teamId,
        Long refereeId,
        Long seasonId,
        Long stageId,
        Long roundId,
        Boolean hasValues,
        Long positionId,
        Integer jerseyNumber,
        List<StatisticDetail> details) {
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :football:test --tests "*StatisticDecodingTest"`
Expected: PASS (2 tests).

- [ ] **Step 5: Commit**

```bash
git add football/src/main/java/io/github/miro93/sportmonks/football/model/StatisticDetail.java \
        football/src/main/java/io/github/miro93/sportmonks/football/model/Statistic.java \
        football/src/test/java/io/github/miro93/sportmonks/football/model/StatisticDecodingTest.java
git commit -m "feat: add Statistic and StatisticDetail models"
```

---

### Task 2: `Expected` model

**Files:**
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/model/Expected.java`
- Test: `football/src/test/java/io/github/miro93/sportmonks/football/model/ExpectedDecodingTest.java`

- [ ] **Step 1: Write the failing test**

Create `ExpectedDecodingTest.java`:

```java
package io.github.miro93.sportmonks.football.model;

import io.github.miro93.sportmonks.core.json.JacksonCodec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExpectedDecodingTest {

    private final JacksonCodec codec = new JacksonCodec();

    @Test
    void decodesExpectedWithDataMap() {
        String json = """
                {
                  "data": {
                    "id": 7001,
                    "fixture_id": 18533878,
                    "type_id": 5304,
                    "participant_id": 1,
                    "location": "home",
                    "data": { "value": 1.85 }
                  }
                }
                """;

        Expected expected = codec.decode(json, codec.type(Expected.class)).data();

        assertThat(expected.id()).isEqualTo(7001L);
        assertThat(expected.fixtureId()).isEqualTo(18533878L);
        assertThat(expected.typeId()).isEqualTo(5304L);
        assertThat(expected.participantId()).isEqualTo(1L);
        assertThat(expected.location()).isEqualTo("home");
        assertThat(expected.data()).containsEntry("value", 1.85);
    }

    @Test
    void decodesExpectedWithOptionalFieldsAbsent() {
        String json = """
                { "data": { "id": 7001 } }
                """;

        Expected expected = codec.decode(json, codec.type(Expected.class)).data();

        assertThat(expected.id()).isEqualTo(7001L);
        assertThat(expected.fixtureId()).isNull();
        assertThat(expected.typeId()).isNull();
        assertThat(expected.participantId()).isNull();
        assertThat(expected.location()).isNull();
        assertThat(expected.data()).isNull();
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :football:test --tests "*ExpectedDecodingTest"`
Expected: FAIL — `Expected` does not exist (compilation error).

- [ ] **Step 3: Write minimal implementation**

Create `Expected.java`:

```java
package io.github.miro93.sportmonks.football.model;

import java.util.Map;

/// An expected-goals (xG) record from the SportMonks expected endpoints
/// ({@code /expected/fixtures} for team-level, {@code /expected/lineups} for
/// player-level). {@code id} is always present; every other field may be
/// {@code null}. {@code location} is {@code "home"} or {@code "away"}.
/// {@code data} is a free-form object (typically {@code {value}}) whose shape
/// depends on {@code typeId}, exposed as a raw {@code Map<String, Object>};
/// numeric values decode as {@code Double}/{@code Integer}.
public record Expected(
        long id,
        Long fixtureId,
        Long typeId,
        Long participantId,
        String location,
        Map<String, Object> data) {
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :football:test --tests "*ExpectedDecodingTest"`
Expected: PASS (2 tests).

- [ ] **Step 5: Commit**

```bash
git add football/src/main/java/io/github/miro93/sportmonks/football/model/Expected.java \
        football/src/test/java/io/github/miro93/sportmonks/football/model/ExpectedDecodingTest.java
git commit -m "feat: add Expected model"
```

---

### Task 3: `StatisticsEndpoint`

**Files:**
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/endpoint/StatisticsEndpoint.java`
- Test: `football/src/test/java/io/github/miro93/sportmonks/football/endpoint/StatisticsEndpointTest.java`

- [ ] **Step 1: Write the failing test**

Create `StatisticsEndpointTest.java`:

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
class StatisticsEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private StatisticsEndpoint statistics(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new StatisticsEndpoint(executor, codec);
    }

    @Test
    void seasonByTeamHitsTeamsPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/statistics/seasons/teams/1")).willReturn(okJson("""
                { "data": [ { "id": 5001, "team_id": 1 } ] }
                """)));

        var response = statistics(wm.getHttpBaseUrl()).seasonByTeam(1L).get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().teamId()).isEqualTo(1L);
    }

    @Test
    void seasonByPlayerHitsPlayersPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/statistics/seasons/players/172")).willReturn(okJson("""
                { "data": [ { "id": 5002, "player_id": 172 } ] }
                """)));

        var response = statistics(wm.getHttpBaseUrl()).seasonByPlayer(172L).get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().playerId()).isEqualTo(172L);
    }

    @Test
    void seasonByCoachHitsCoachesPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/statistics/seasons/coaches/455")).willReturn(okJson("""
                { "data": [ { "id": 5003, "coach_id": 455 } ] }
                """)));

        var response = statistics(wm.getHttpBaseUrl()).seasonByCoach(455L).get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().coachId()).isEqualTo(455L);
    }

    @Test
    void seasonByRefereeHitsRefereesPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/statistics/seasons/referees/12")).willReturn(okJson("""
                { "data": [ { "id": 5004, "referee_id": 12 } ] }
                """)));

        var response = statistics(wm.getHttpBaseUrl()).seasonByReferee(12L).get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().refereeId()).isEqualTo(12L);
    }

    @Test
    void byStageHitsStagesPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/statistics/stages/77")).willReturn(okJson("""
                { "data": [ { "id": 5005, "stage_id": 77 } ] }
                """)));

        var response = statistics(wm.getHttpBaseUrl()).byStage(77L).get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().stageId()).isEqualTo(77L);
    }

    @Test
    void byRoundHitsRoundsPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/statistics/rounds/33")).willReturn(okJson("""
                { "data": [ { "id": 5006, "round_id": 33 } ] }
                """)));

        var response = statistics(wm.getHttpBaseUrl()).byRound(33L).get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().roundId()).isEqualTo(33L);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :football:test --tests "*StatisticsEndpointTest"`
Expected: FAIL — `StatisticsEndpoint` does not exist (compilation error).

- [ ] **Step 3: Write minimal implementation**

Create `StatisticsEndpoint.java`:

```java
package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.football.model.Statistic;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks statistics endpoints ({@code /statistics}): season
/// statistics by participant (team, player, coach, referee), plus stage and
/// round statistics.
public final class StatisticsEndpoint {

    private final ApiExecutor executor;
    private final DataType<List<Statistic>> list;

    /// Creates the endpoint, building the {@link Statistic} list decoder from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the list response type
    public StatisticsEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.list = codec.listType(Statistic.class);
    }

    /// Requests the season statistics for a given team.
    ///
    /// @param teamId the team id
    /// @return a collection request for the matching statistics
    public CollectionRequest<Statistic> seasonByTeam(long teamId) {
        return collection("statistics/seasons/teams/" + teamId);
    }

    /// Requests the season statistics for a given player.
    ///
    /// @param playerId the player id
    /// @return a collection request for the matching statistics
    public CollectionRequest<Statistic> seasonByPlayer(long playerId) {
        return collection("statistics/seasons/players/" + playerId);
    }

    /// Requests the season statistics for a given coach.
    ///
    /// @param coachId the coach id
    /// @return a collection request for the matching statistics
    public CollectionRequest<Statistic> seasonByCoach(long coachId) {
        return collection("statistics/seasons/coaches/" + coachId);
    }

    /// Requests the season statistics for a given referee.
    ///
    /// @param refereeId the referee id
    /// @return a collection request for the matching statistics
    public CollectionRequest<Statistic> seasonByReferee(long refereeId) {
        return collection("statistics/seasons/referees/" + refereeId);
    }

    /// Requests the statistics for a given stage.
    ///
    /// @param stageId the stage id
    /// @return a collection request for the matching statistics
    public CollectionRequest<Statistic> byStage(long stageId) {
        return collection("statistics/stages/" + stageId);
    }

    /// Requests the statistics for a given round.
    ///
    /// @param roundId the round id
    /// @return a collection request for the matching statistics
    public CollectionRequest<Statistic> byRound(long roundId) {
        return collection("statistics/rounds/" + roundId);
    }

    private CollectionRequest<Statistic> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :football:test --tests "*StatisticsEndpointTest"`
Expected: PASS (6 tests).

- [ ] **Step 5: Commit**

```bash
git add football/src/main/java/io/github/miro93/sportmonks/football/endpoint/StatisticsEndpoint.java \
        football/src/test/java/io/github/miro93/sportmonks/football/endpoint/StatisticsEndpointTest.java
git commit -m "feat: add StatisticsEndpoint"
```

---

### Task 4: `ExpectedEndpoint`

**Files:**
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/endpoint/ExpectedEndpoint.java`
- Test: `football/src/test/java/io/github/miro93/sportmonks/football/endpoint/ExpectedEndpointTest.java`

- [ ] **Step 1: Write the failing test**

Create `ExpectedEndpointTest.java`:

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
class ExpectedEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private ExpectedEndpoint expected(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new ExpectedEndpoint(executor, codec);
    }

    @Test
    void fixturesHitsExpectedFixturesPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/expected/fixtures")).willReturn(okJson("""
                { "data": [ { "id": 7001, "fixture_id": 18533878, "location": "home", "data": { "value": 1.85 } } ] }
                """)));

        var response = expected(wm.getHttpBaseUrl()).fixtures().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().location()).isEqualTo("home");
        assertThat(response.data().getFirst().data()).containsEntry("value", 1.85);
    }

    @Test
    void lineupsHitsExpectedLineupsPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/expected/lineups")).willReturn(okJson("""
                { "data": [ { "id": 7002, "fixture_id": 18533878, "participant_id": 172 } ] }
                """)));

        var response = expected(wm.getHttpBaseUrl()).lineups().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().participantId()).isEqualTo(172L);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :football:test --tests "*ExpectedEndpointTest"`
Expected: FAIL — `ExpectedEndpoint` does not exist (compilation error).

- [ ] **Step 3: Write minimal implementation**

Create `ExpectedEndpoint.java`:

```java
package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.football.model.Expected;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks expected-goals (xG) endpoints ({@code /expected}):
/// team-level xG per fixture and player-level xG per lineup.
public final class ExpectedEndpoint {

    private final ApiExecutor executor;
    private final DataType<List<Expected>> list;

    /// Creates the endpoint, building the {@link Expected} list decoder from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the list response type
    public ExpectedEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.list = codec.listType(Expected.class);
    }

    /// Requests the team-level expected-goals records aggregated per fixture.
    ///
    /// @return a collection request for the fixture-level xG records
    public CollectionRequest<Expected> fixtures() {
        return collection("expected/fixtures");
    }

    /// Requests the player-level expected-goals records per lineup.
    ///
    /// @return a collection request for the lineup-level xG records
    public CollectionRequest<Expected> lineups() {
        return collection("expected/lineups");
    }

    private CollectionRequest<Expected> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :football:test --tests "*ExpectedEndpointTest"`
Expected: PASS (2 tests).

- [ ] **Step 5: Commit**

```bash
git add football/src/main/java/io/github/miro93/sportmonks/football/endpoint/ExpectedEndpoint.java \
        football/src/test/java/io/github/miro93/sportmonks/football/endpoint/ExpectedEndpointTest.java
git commit -m "feat: add ExpectedEndpoint"
```

---

### Task 5: Wire `StatisticsEndpoint` and `ExpectedEndpoint` into `FootballClient`

**Files:**
- Modify: `football/src/main/java/io/github/miro93/sportmonks/football/FootballClient.java`
- Test: `football/src/test/java/io/github/miro93/sportmonks/football/FootballClientM13Test.java`

The two new fields / constructor params / assignments / accessors / build() arguments go
**after `predictions` and before `core`** (core stays LAST), in the order `statistics` then
`expected`. No new executor — uses the existing football `executor`.

- [ ] **Step 1: Write the failing test**

Create `FootballClientM13Test.java`:

```java
package io.github.miro93.sportmonks.football;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class FootballClientM13Test {

    private FootballClient client(String baseUrl) {
        return FootballClient.builder()
                .apiToken(ApiToken.of("tok"))
                .baseUrl(baseUrl)
                .build();
    }

    @Test
    void exposesM13Endpoints(WireMockRuntimeInfo wm) {
        var client = client(wm.getHttpBaseUrl());
        assertThat(client.statistics()).isNotNull();
        assertThat(client.expected()).isNotNull();
    }

    @Test
    void seasonByTeamDecodesAndSendsAuth(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/statistics/seasons/teams/1")).willReturn(okJson("""
                { "data": [{ "id": 5001, "team_id": 1 }] }
                """)));

        var stats = client(wm.getHttpBaseUrl()).statistics().seasonByTeam(1L).get().data();

        assertThat(stats).hasSize(1);
        assertThat(stats.getFirst().teamId()).isEqualTo(1L);
        verify(getRequestedFor(urlPathEqualTo("/statistics/seasons/teams/1"))
                .withHeader("Authorization", equalTo("tok")));
    }

    @Test
    void expectedFixturesDecodesAndSendsAuth(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/expected/fixtures")).willReturn(okJson("""
                { "data": [{ "id": 7001, "location": "home" }] }
                """)));

        var xg = client(wm.getHttpBaseUrl()).expected().fixtures().get().data();

        assertThat(xg).hasSize(1);
        assertThat(xg.getFirst().location()).isEqualTo("home");
        verify(getRequestedFor(urlPathEqualTo("/expected/fixtures"))
                .withHeader("Authorization", equalTo("tok")));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :football:test --tests "*FootballClientM13Test"`
Expected: FAIL — `statistics()`/`expected()` do not exist (compilation error).

- [ ] **Step 3: Add the imports**

In `FootballClient.java`, add these imports alongside the existing `endpoint.*` imports
(alphabetical order — `ExpectedEndpoint` sorts right after the `CommentariesEndpoint` import
and before `ExpectedLineupsEndpoint`; `StatisticsEndpoint` sorts right after the
`SquadsEndpoint` import and before `StatesEndpoint` — note `StagesEndpoint`/`StandingsEndpoint`/
`StatesEndpoint` ordering, place `StatisticsEndpoint` so the block stays alphabetical):

```java
import io.github.miro93.sportmonks.football.endpoint.ExpectedEndpoint;
import io.github.miro93.sportmonks.football.endpoint.StatisticsEndpoint;
```

(If unsure of exact slot, the imports compile regardless of order; keep the block tidy.)

- [ ] **Step 4: Add the fields**

In the private field block, between `private final PredictionsEndpoint predictions;`
and `private final CoreClient core;`, add:

```java
    private final StatisticsEndpoint statistics;
    private final ExpectedEndpoint expected;
```

- [ ] **Step 5: Add the constructor params and assignments**

In the private constructor signature, between `PredictionsEndpoint predictions,`
and `CoreClient core)`, add:

```java
            StatisticsEndpoint statistics,
            ExpectedEndpoint expected,
```

In the constructor body, between `this.predictions = predictions;` and `this.core = core;`,
add:

```java
        this.statistics = statistics;
        this.expected = expected;
```

- [ ] **Step 6: Add the accessors**

Between the `predictions()` accessor and the `core()` accessor, add:

```java
    /// Returns the statistics endpoint.
    ///
    /// @return the {@code /statistics} endpoint accessor
    public StatisticsEndpoint statistics() {
        return statistics;
    }

    /// Returns the expected-goals (xG) endpoint.
    ///
    /// @return the {@code /expected} endpoint accessor
    public ExpectedEndpoint expected() {
        return expected;
    }
```

- [ ] **Step 7: Wire the endpoints in `build()`**

In the `return new FootballClient(...)` argument list, between the
`new PredictionsEndpoint(executor, codec),` argument and the trailing `core)` argument, add:

```java
                    new StatisticsEndpoint(executor, codec),
                    new ExpectedEndpoint(executor, codec),
```

- [ ] **Step 8: Run test to verify it passes**

Run: `./gradlew :football:test --tests "*FootballClientM13Test"`
Expected: PASS (3 tests).

- [ ] **Step 9: Run the full football test suite**

Run: `./gradlew :football:test`
Expected: PASS (all existing tests + the new M13 tests).

- [ ] **Step 10: Commit**

```bash
git add football/src/main/java/io/github/miro93/sportmonks/football/FootballClient.java \
        football/src/test/java/io/github/miro93/sportmonks/football/FootballClientM13Test.java
git commit -m "feat: expose statistics and expected (xG) on FootballClient"
```

---

### Task 6: README documentation

**Files:**
- Modify: `README.md`

- [ ] **Step 1: Locate the football endpoints table**

Run: `grep -n "predictions()\|expectedLineups" README.md`
Expected: shows the M11/M12 rows in the football endpoints table.

- [ ] **Step 2: Add the statistics and expected accessor rows**

The football endpoints table uses three columns: `| accessor | EndpointClass | methods |`.
After the `predictions()` row (the last current row of the table), add these two rows verbatim:

```markdown
| `statistics()` | `StatisticsEndpoint` | `seasonByTeam(teamId)`, `seasonByPlayer(playerId)`, `seasonByCoach(coachId)`, `seasonByReferee(refereeId)`, `byStage(stageId)`, `byRound(roundId)` |
| `expected()` | `ExpectedEndpoint` | `fixtures()`, `lineups()` |
```

- [ ] **Step 3: Verify the rows render**

Run: `grep -n "statistics()\|expected()" README.md`
Expected: shows the two new rows.

- [ ] **Step 4: Commit**

```bash
git add README.md
git commit -m "docs: document statistics and expected (xG) endpoints"
```

---

### Task 7: Final verification

- [ ] **Step 1: Run the complete build and test suite**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL — all modules compile, all tests pass (M12 total + 17 new M13 tests).

- [ ] **Step 2: Release note (no action in this task)**

This milestone is intended to ship as **1.0.0**. The release itself is handled after merge
(via a `Release-As: 1.0.0` footer on `main` or by editing the release-please PR). Do NOT cut
or merge any release as part of implementing this plan.

---

## Self-Review Notes

- **Spec coverage:** `Statistic` + `StatisticDetail` (Task 1), `Expected` (Task 2),
  `StatisticsEndpoint` with all 6 methods (Task 3), `ExpectedEndpoint` with `fixtures`/`lineups`
  (Task 4), `FootballClient` accessors placed before `core` with no new executor (Task 5),
  README rows (Task 6), full build + release note (Task 7). All spec sections map to a task.
- **Type consistency:** record component names (`playerId`/`coachId`/`teamId`/`refereeId`/
  `seasonId`/`stageId`/`roundId`/`hasValues`/`positionId`/`jerseyNumber`/`details`;
  `StatisticDetail` `typeId`/`value`; `Expected` `fixtureId`/`typeId`/`participantId`/
  `location`/`data`), accessor names (`statistics`, `expected`), and method names
  (`seasonByTeam`/`seasonByPlayer`/`seasonByCoach`/`seasonByReferee`/`byStage`/`byRound`;
  `fixtures`/`lineups`) match across all tasks and tests.
- **Placeholders:** none — every code step contains complete code.
- **Note carried from spec:** `StatisticDetail.value` and `Expected.data` are
  `Map<String,Object>`; map number values decode as `Double`/`Integer` — tests assert via
  `containsEntry` with `12`/`5`/`7` (Integer) and `1.85` (Double). `StatisticDetail.id` is
  `long` (TDD-confirm against a real payload).
