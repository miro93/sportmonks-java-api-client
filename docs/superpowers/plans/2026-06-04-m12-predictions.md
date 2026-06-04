# M12 — Predictions Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add the SportMonks Predictions feature (probabilities, value bets, predictability) to the football module, exposed on `FootballClient`.

**Architecture:** Continuation of M4–M11. Three new records in `football.model` — `Prediction` (probabilities, free-form `Map<String,Object>` payload), `ValueBet` + nested `ValueBetPrediction` (typed stable payload), `Predictability` (league-scoped, `data` map). One collection-only endpoint `PredictionsEndpoint` (clone of `PreMatchOddsEndpoint`) holding three list decoders, with five methods, on the football base URL (`/v3/football`) — no new executor. One accessor on `FootballClient`, placed before `core` (which stays LAST).

**Tech Stack:** Java 25, Gradle, JDK HttpClient, Jackson (snake_case; decodes free-form objects into `Map<String,Object>`), JUnit 5 + WireMock + AssertJ.

---

### Task 1: `Prediction` model

**Files:**
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/model/Prediction.java`
- Test: `football/src/test/java/io/github/miro93/sportmonks/football/model/PredictionDecodingTest.java`

- [ ] **Step 1: Write the failing test**

Create `PredictionDecodingTest.java`:

```java
package io.github.miro93.sportmonks.football.model;

import io.github.miro93.sportmonks.core.json.JacksonCodec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PredictionDecodingTest {

    private final JacksonCodec codec = new JacksonCodec();

    @Test
    void decodesYesNoProbability() {
        String json = """
                {
                  "data": {
                    "id": 100,
                    "fixture_id": 18533878,
                    "type_id": 231,
                    "predictions": { "yes": 0.67, "no": 0.33 }
                  }
                }
                """;

        Prediction prediction = codec.decode(json, codec.type(Prediction.class)).data();

        assertThat(prediction.id()).isEqualTo(100L);
        assertThat(prediction.fixtureId()).isEqualTo(18533878L);
        assertThat(prediction.typeId()).isEqualTo(231L);
        assertThat(prediction.predictions())
                .containsEntry("yes", 0.67)
                .containsEntry("no", 0.33);
    }

    @Test
    void decodesHomeDrawAwayProbability() {
        String json = """
                {
                  "data": {
                    "id": 101,
                    "fixture_id": 18533878,
                    "type_id": 237,
                    "predictions": { "home": 0.5, "draw": 0.3, "away": 0.2 }
                  }
                }
                """;

        Prediction prediction = codec.decode(json, codec.type(Prediction.class)).data();

        assertThat(prediction.predictions())
                .containsEntry("home", 0.5)
                .containsEntry("draw", 0.3)
                .containsEntry("away", 0.2);
    }

    @Test
    void decodesPredictionWithOptionalFieldsAbsent() {
        String json = """
                { "data": { "id": 100 } }
                """;

        Prediction prediction = codec.decode(json, codec.type(Prediction.class)).data();

        assertThat(prediction.id()).isEqualTo(100L);
        assertThat(prediction.fixtureId()).isNull();
        assertThat(prediction.typeId()).isNull();
        assertThat(prediction.predictions()).isNull();
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :football:test --tests "*PredictionDecodingTest"`
Expected: FAIL — `Prediction` does not exist (compilation error).

- [ ] **Step 3: Write minimal implementation**

Create `Prediction.java`:

```java
package io.github.miro93.sportmonks.football.model;

import java.util.Map;

/// A predicted-probability record from the SportMonks predictions feed
/// (probabilities endpoints). {@code id} is always present; every other field
/// may be {@code null}. {@code predictions} is a free-form object whose keys
/// depend on {@code typeId} (e.g. {@code {yes,no}}, {@code {home,draw,away}},
/// correct-score maps), so it is exposed as a raw {@code Map<String, Object>};
/// numeric values decode as {@code Double}/{@code Integer}.
public record Prediction(
        long id,
        Long fixtureId,
        Long typeId,
        Map<String, Object> predictions) {
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :football:test --tests "*PredictionDecodingTest"`
Expected: PASS (3 tests).

- [ ] **Step 5: Commit**

```bash
git add football/src/main/java/io/github/miro93/sportmonks/football/model/Prediction.java \
        football/src/test/java/io/github/miro93/sportmonks/football/model/PredictionDecodingTest.java
git commit -m "feat: add Prediction model"
```

---

### Task 2: `ValueBet` + `ValueBetPrediction` models

**Files:**
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/model/ValueBet.java`
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/model/ValueBetPrediction.java`
- Test: `football/src/test/java/io/github/miro93/sportmonks/football/model/ValueBetDecodingTest.java`

- [ ] **Step 1: Write the failing test**

Create `ValueBetDecodingTest.java`:

```java
package io.github.miro93.sportmonks.football.model;

import io.github.miro93.sportmonks.core.json.JacksonCodec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValueBetDecodingTest {

    private final JacksonCodec codec = new JacksonCodec();

    @Test
    void decodesValueBetWithTypedPredictions() {
        String json = """
                {
                  "data": {
                    "id": 200,
                    "fixture_id": 18533878,
                    "type_id": 1685,
                    "predictions": {
                      "bet": "Home",
                      "bookmaker": "bet365",
                      "odd": "2.10",
                      "is_value": true,
                      "stake": "1.0",
                      "fair_odd": "1.95"
                    }
                  }
                }
                """;

        ValueBet valueBet = codec.decode(json, codec.type(ValueBet.class)).data();

        assertThat(valueBet.id()).isEqualTo(200L);
        assertThat(valueBet.fixtureId()).isEqualTo(18533878L);
        assertThat(valueBet.typeId()).isEqualTo(1685L);
        assertThat(valueBet.predictions()).isNotNull();
        assertThat(valueBet.predictions().bet()).isEqualTo("Home");
        assertThat(valueBet.predictions().bookmaker()).isEqualTo("bet365");
        assertThat(valueBet.predictions().odd()).isEqualTo("2.10");
        assertThat(valueBet.predictions().isValue()).isTrue();
        assertThat(valueBet.predictions().stake()).isEqualTo("1.0");
        assertThat(valueBet.predictions().fairOdd()).isEqualTo("1.95");
    }

    @Test
    void decodesValueBetWithOptionalFieldsAbsent() {
        String json = """
                { "data": { "id": 200 } }
                """;

        ValueBet valueBet = codec.decode(json, codec.type(ValueBet.class)).data();

        assertThat(valueBet.id()).isEqualTo(200L);
        assertThat(valueBet.fixtureId()).isNull();
        assertThat(valueBet.typeId()).isNull();
        assertThat(valueBet.predictions()).isNull();
    }

    @Test
    void decodesValueBetPredictionWithAbsentInnerFields() {
        String json = """
                { "data": { "id": 200, "predictions": { "bet": "Away" } } }
                """;

        ValueBet valueBet = codec.decode(json, codec.type(ValueBet.class)).data();

        assertThat(valueBet.predictions().bet()).isEqualTo("Away");
        assertThat(valueBet.predictions().bookmaker()).isNull();
        assertThat(valueBet.predictions().odd()).isNull();
        assertThat(valueBet.predictions().isValue()).isNull();
        assertThat(valueBet.predictions().stake()).isNull();
        assertThat(valueBet.predictions().fairOdd()).isNull();
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :football:test --tests "*ValueBetDecodingTest"`
Expected: FAIL — `ValueBet` / `ValueBetPrediction` do not exist (compilation error).

- [ ] **Step 3: Write minimal implementation**

Create `ValueBetPrediction.java`:

```java
package io.github.miro93.sportmonks.football.model;

/// The typed payload of a {@link ValueBet}: a single value-bet recommendation.
/// Every field may be {@code null}. {@code odd}, {@code stake} and
/// {@code fairOdd} are {@code String} because the API returns these
/// numeric-looking values as strings (project numbers-as-strings convention).
public record ValueBetPrediction(
        String bet,
        String bookmaker,
        String odd,
        Boolean isValue,
        String stake,
        String fairOdd) {
}
```

Create `ValueBet.java`:

```java
package io.github.miro93.sportmonks.football.model;

/// A value-bet record from the SportMonks predictions feed (value-bets
/// endpoints). {@code id} is always present; every other field may be
/// {@code null}. Unlike {@link Prediction}, the {@code predictions} payload has
/// a stable shape and is typed as {@link ValueBetPrediction}.
public record ValueBet(
        long id,
        Long fixtureId,
        Long typeId,
        ValueBetPrediction predictions) {
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :football:test --tests "*ValueBetDecodingTest"`
Expected: PASS (3 tests).

- [ ] **Step 5: Commit**

```bash
git add football/src/main/java/io/github/miro93/sportmonks/football/model/ValueBet.java \
        football/src/main/java/io/github/miro93/sportmonks/football/model/ValueBetPrediction.java \
        football/src/test/java/io/github/miro93/sportmonks/football/model/ValueBetDecodingTest.java
git commit -m "feat: add ValueBet and ValueBetPrediction models"
```

---

### Task 3: `Predictability` model

**Files:**
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/model/Predictability.java`
- Test: `football/src/test/java/io/github/miro93/sportmonks/football/model/PredictabilityDecodingTest.java`

- [ ] **Step 1: Write the failing test**

Create `PredictabilityDecodingTest.java`:

```java
package io.github.miro93.sportmonks.football.model;

import io.github.miro93.sportmonks.core.json.JacksonCodec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PredictabilityDecodingTest {

    private final JacksonCodec codec = new JacksonCodec();

    @Test
    void decodesPredictabilityWithDataMap() {
        String json = """
                {
                  "data": {
                    "id": 300,
                    "league_id": 8,
                    "type_id": 1683,
                    "data": { "fulltime_result": 0.75, "both_teams_to_score": 0.5 }
                  }
                }
                """;

        Predictability predictability = codec.decode(json, codec.type(Predictability.class)).data();

        assertThat(predictability.id()).isEqualTo(300L);
        assertThat(predictability.leagueId()).isEqualTo(8L);
        assertThat(predictability.typeId()).isEqualTo(1683L);
        assertThat(predictability.data())
                .containsEntry("fulltime_result", 0.75)
                .containsEntry("both_teams_to_score", 0.5);
    }

    @Test
    void decodesPredictabilityWithOptionalFieldsAbsent() {
        String json = """
                { "data": { "id": 300 } }
                """;

        Predictability predictability = codec.decode(json, codec.type(Predictability.class)).data();

        assertThat(predictability.id()).isEqualTo(300L);
        assertThat(predictability.leagueId()).isNull();
        assertThat(predictability.typeId()).isNull();
        assertThat(predictability.data()).isNull();
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :football:test --tests "*PredictabilityDecodingTest"`
Expected: FAIL — `Predictability` does not exist (compilation error).

- [ ] **Step 3: Write minimal implementation**

Create `Predictability.java`:

```java
package io.github.miro93.sportmonks.football.model;

import java.util.Map;

/// A league-level predictability record from the SportMonks predictions feed
/// (predictability endpoint). {@code id} is always present; every other field
/// may be {@code null}. The variable payload is league-scoped and lives under
/// the {@code data} key (not {@code predictions}); it maps market names to
/// reliability metrics, exposed as a raw {@code Map<String, Object>}.
public record Predictability(
        long id,
        Long leagueId,
        Long typeId,
        Map<String, Object> data) {
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :football:test --tests "*PredictabilityDecodingTest"`
Expected: PASS (2 tests).

- [ ] **Step 5: Commit**

```bash
git add football/src/main/java/io/github/miro93/sportmonks/football/model/Predictability.java \
        football/src/test/java/io/github/miro93/sportmonks/football/model/PredictabilityDecodingTest.java
git commit -m "feat: add Predictability model"
```

---

### Task 4: `PredictionsEndpoint`

**Files:**
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/endpoint/PredictionsEndpoint.java`
- Test: `football/src/test/java/io/github/miro93/sportmonks/football/endpoint/PredictionsEndpointTest.java`

- [ ] **Step 1: Write the failing test**

Create `PredictionsEndpointTest.java`:

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
class PredictionsEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private PredictionsEndpoint predictions(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new PredictionsEndpoint(executor, codec);
    }

    @Test
    void probabilitiesHitsProbabilitiesRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/predictions/probabilities")).willReturn(okJson("""
                { "data": [ { "id": 1, "fixture_id": 18533878, "predictions": { "yes": 0.6 } } ] }
                """)));

        var response = predictions(wm.getHttpBaseUrl()).probabilities().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().predictions()).containsEntry("yes", 0.6);
    }

    @Test
    void probabilitiesByFixtureHitsFixturesPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/predictions/probabilities/fixtures/18533878")).willReturn(okJson("""
                { "data": [ { "id": 1, "fixture_id": 18533878 } ] }
                """)));

        var response = predictions(wm.getHttpBaseUrl()).probabilitiesByFixture(18533878L).get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().fixtureId()).isEqualTo(18533878L);
    }

    @Test
    void valueBetsHitsValueBetsRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/predictions/value-bets")).willReturn(okJson("""
                { "data": [ { "id": 2, "fixture_id": 18533878, "predictions": { "bet": "Home", "is_value": true } } ] }
                """)));

        var response = predictions(wm.getHttpBaseUrl()).valueBets().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().predictions().bet()).isEqualTo("Home");
    }

    @Test
    void valueBetsByFixtureHitsFixturesPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/predictions/value-bets/fixtures/18533878")).willReturn(okJson("""
                { "data": [ { "id": 2, "fixture_id": 18533878 } ] }
                """)));

        var response = predictions(wm.getHttpBaseUrl()).valueBetsByFixture(18533878L).get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().fixtureId()).isEqualTo(18533878L);
    }

    @Test
    void predictabilityByLeagueHitsLeaguesPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/predictions/predictability/leagues/8")).willReturn(okJson("""
                { "data": [ { "id": 3, "league_id": 8, "data": { "fulltime_result": 0.75 } } ] }
                """)));

        var response = predictions(wm.getHttpBaseUrl()).predictabilityByLeague(8L).get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().leagueId()).isEqualTo(8L);
        assertThat(response.data().getFirst().data()).containsEntry("fulltime_result", 0.75);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :football:test --tests "*PredictionsEndpointTest"`
Expected: FAIL — `PredictionsEndpoint` does not exist (compilation error).

- [ ] **Step 3: Write minimal implementation**

Create `PredictionsEndpoint.java`:

```java
package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.football.model.Predictability;
import io.github.miro93.sportmonks.football.model.Prediction;
import io.github.miro93.sportmonks.football.model.ValueBet;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks predictions endpoints ({@code /predictions}):
/// probabilities and value bets by fixture, plus predictability by league.
public final class PredictionsEndpoint {

    private final ApiExecutor executor;
    private final DataType<List<Prediction>> predictionList;
    private final DataType<List<ValueBet>> valueBetList;
    private final DataType<List<Predictability>> predictabilityList;

    /// Creates the endpoint, building the list decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the list response types
    public PredictionsEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.predictionList = codec.listType(Prediction.class);
        this.valueBetList = codec.listType(ValueBet.class);
        this.predictabilityList = codec.listType(Predictability.class);
    }

    /// Requests every predicted-probability record, paginated.
    ///
    /// @return a collection request for all probability predictions
    public CollectionRequest<Prediction> probabilities() {
        return new CollectionRequest<>(executor, RequestSpec.builder("predictions/probabilities"), predictionList);
    }

    /// Requests the probability predictions for a given fixture.
    ///
    /// @param fixtureId the fixture id
    /// @return a collection request for the matching probability predictions
    public CollectionRequest<Prediction> probabilitiesByFixture(long fixtureId) {
        return new CollectionRequest<>(executor, RequestSpec.builder("predictions/probabilities/fixtures/" + fixtureId), predictionList);
    }

    /// Requests every value-bet record, paginated.
    ///
    /// @return a collection request for all value bets
    public CollectionRequest<ValueBet> valueBets() {
        return new CollectionRequest<>(executor, RequestSpec.builder("predictions/value-bets"), valueBetList);
    }

    /// Requests the value bets for a given fixture.
    ///
    /// @param fixtureId the fixture id
    /// @return a collection request for the matching value bets
    public CollectionRequest<ValueBet> valueBetsByFixture(long fixtureId) {
        return new CollectionRequest<>(executor, RequestSpec.builder("predictions/value-bets/fixtures/" + fixtureId), valueBetList);
    }

    /// Requests the predictability records for a given league.
    ///
    /// @param leagueId the league id
    /// @return a collection request for the matching predictability records
    public CollectionRequest<Predictability> predictabilityByLeague(long leagueId) {
        return new CollectionRequest<>(executor, RequestSpec.builder("predictions/predictability/leagues/" + leagueId), predictabilityList);
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :football:test --tests "*PredictionsEndpointTest"`
Expected: PASS (5 tests).

- [ ] **Step 5: Commit**

```bash
git add football/src/main/java/io/github/miro93/sportmonks/football/endpoint/PredictionsEndpoint.java \
        football/src/test/java/io/github/miro93/sportmonks/football/endpoint/PredictionsEndpointTest.java
git commit -m "feat: add PredictionsEndpoint"
```

---

### Task 5: Wire `PredictionsEndpoint` into `FootballClient`

**Files:**
- Modify: `football/src/main/java/io/github/miro93/sportmonks/football/FootballClient.java`
- Test: `football/src/test/java/io/github/miro93/sportmonks/football/FootballClientM12Test.java`

The new field / constructor param / assignment / accessor / build() argument go **after
`expectedLineups` and before `core`** (core stays LAST). No new executor — uses the existing
football `executor`.

- [ ] **Step 1: Write the failing test**

Create `FootballClientM12Test.java`:

```java
package io.github.miro93.sportmonks.football;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class FootballClientM12Test {

    private FootballClient client(String baseUrl) {
        return FootballClient.builder()
                .apiToken(ApiToken.of("tok"))
                .baseUrl(baseUrl)
                .build();
    }

    @Test
    void exposesPredictions(WireMockRuntimeInfo wm) {
        assertThat(client(wm.getHttpBaseUrl()).predictions()).isNotNull();
    }

    @Test
    void valueBetsByFixtureDecodesAndSendsAuth(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/predictions/value-bets/fixtures/18533878")).willReturn(okJson("""
                { "data": [{ "id": 2, "fixture_id": 18533878, "predictions": { "bet": "Home" } }] }
                """)));

        var bets = client(wm.getHttpBaseUrl()).predictions().valueBetsByFixture(18533878L).get().data();

        assertThat(bets).hasSize(1);
        assertThat(bets.getFirst().predictions().bet()).isEqualTo("Home");
        verify(getRequestedFor(urlPathEqualTo("/predictions/value-bets/fixtures/18533878"))
                .withHeader("Authorization", equalTo("tok")));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :football:test --tests "*FootballClientM12Test"`
Expected: FAIL — `predictions()` does not exist (compilation error).

- [ ] **Step 3: Add the import**

In `FootballClient.java`, add this import alongside the existing `endpoint.*` imports
(alphabetical order — it sorts right after the `PlayersEndpoint` import and before
`PreMatchOddsEndpoint`):

```java
import io.github.miro93.sportmonks.football.endpoint.PredictionsEndpoint;
```

- [ ] **Step 4: Add the field**

In the private field block, between `private final ExpectedLineupsEndpoint expectedLineups;`
and `private final CoreClient core;`, add:

```java
    private final PredictionsEndpoint predictions;
```

- [ ] **Step 5: Add the constructor param and assignment**

In the private constructor signature, between `ExpectedLineupsEndpoint expectedLineups,`
and `CoreClient core)`, add:

```java
            PredictionsEndpoint predictions,
```

In the constructor body, between `this.expectedLineups = expectedLineups;` and
`this.core = core;`, add:

```java
        this.predictions = predictions;
```

- [ ] **Step 6: Add the accessor**

Between the `expectedLineups()` accessor and the `core()` accessor, add:

```java
    /// Returns the predictions endpoint.
    ///
    /// @return the {@code /predictions} endpoint accessor
    public PredictionsEndpoint predictions() {
        return predictions;
    }
```

- [ ] **Step 7: Wire the endpoint in `build()`**

In the `return new FootballClient(...)` argument list, between the
`new ExpectedLineupsEndpoint(executor, codec),` argument and the trailing `core)` argument,
add:

```java
                    new PredictionsEndpoint(executor, codec),
```

- [ ] **Step 8: Run test to verify it passes**

Run: `./gradlew :football:test --tests "*FootballClientM12Test"`
Expected: PASS (2 tests).

- [ ] **Step 9: Run the full football test suite**

Run: `./gradlew :football:test`
Expected: PASS (all existing tests + the new M12 tests).

- [ ] **Step 10: Commit**

```bash
git add football/src/main/java/io/github/miro93/sportmonks/football/FootballClient.java \
        football/src/test/java/io/github/miro93/sportmonks/football/FootballClientM12Test.java
git commit -m "feat: expose predictions on FootballClient"
```

---

### Task 6: README documentation

**Files:**
- Modify: `README.md`

- [ ] **Step 1: Locate the football endpoints table**

Run: `grep -n "expectedLineups\|premiumBookmakers" README.md`
Expected: shows the M10/M11 rows in the football endpoints table.

- [ ] **Step 2: Add the predictions accessor row**

The football endpoints table uses three columns: `| accessor | EndpointClass | methods |`.
After the `expectedLineups()` row (the last current row of the table), add this row verbatim:

```markdown
| `predictions()` | `PredictionsEndpoint` | `probabilities()`, `probabilitiesByFixture(fixtureId)`, `valueBets()`, `valueBetsByFixture(fixtureId)`, `predictabilityByLeague(leagueId)` |
```

- [ ] **Step 3: Verify the row renders**

Run: `grep -n "predictions()" README.md`
Expected: shows the new row.

- [ ] **Step 4: Commit**

```bash
git add README.md
git commit -m "docs: document predictions endpoint"
```

---

### Task 7: Final verification

- [ ] **Step 1: Run the complete build and test suite**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL — all modules compile, all tests pass (M11 total + 15 new M12 tests).

---

## Self-Review Notes

- **Spec coverage:** `Prediction` (Task 1), `ValueBet` + `ValueBetPrediction` (Task 2),
  `Predictability` (Task 3), `PredictionsEndpoint` with all 5 methods (Task 4),
  `FootballClient` accessor placed before `core` with no new executor (Task 5), README row
  (Task 6), full build (Task 7). All spec sections map to a task.
- **Type consistency:** record component names (`fixtureId`, `leagueId`, `typeId`,
  `predictions`, `data`; `ValueBetPrediction` fields `bet`/`bookmaker`/`odd`/`isValue`/
  `stake`/`fairOdd`), accessor name (`predictions`), and method names (`probabilities`,
  `probabilitiesByFixture`, `valueBets`, `valueBetsByFixture`, `predictabilityByLeague`)
  match across all tasks and tests. `Prediction.predictions` is `Map<String,Object>`;
  `ValueBet.predictions` is `ValueBetPrediction`; `Predictability.data` is `Map<String,Object>`.
- **Placeholders:** none — every code step contains complete code.
- **Note carried from spec:** `odd`/`stake`/`fairOdd` typed as `String`; the decoding test
  feeds them as JSON strings and pins that contract. Map values decode as `Double`/`Integer`;
  the tests use decimal literals (`0.67`) that decode to `Double` and assert via
  `containsEntry`.
