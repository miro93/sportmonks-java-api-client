# M10 — Premium Odds Feed Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add the SportMonks Premium Odds Feed — premium pre-match odds, their history, and premium markets/bookmakers — to the football module, exposed on `FootballClient`.

**Architecture:** Continuation of M4/M5/M8/M9. New records `PremiumOdd` + `HistoricalOdd` in `football.model`; four collection-only endpoints cloning `PreMatchOddsEndpoint`. The feed core lives under `/v3/football` (existing executor); premium markets/bookmakers live under `/v3/odds`, requiring a second `ApiExecutor` on `FootballClient` exactly like M7's `coreExecutor` for `/v3/core`. `Market`/`Bookmaker` (M9) are reused.

**Tech Stack:** Java 25, Gradle, JDK HttpClient, Jackson (snake_case), JUnit 5 + WireMock + AssertJ.

---

### Task 1: `PremiumOdd` model

**Files:**
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/model/PremiumOdd.java`
- Test: `football/src/test/java/io/github/miro93/sportmonks/football/model/PremiumOddDecodingTest.java`

- [ ] **Step 1: Write the failing test**

Create `PremiumOddDecodingTest.java`:

```java
package io.github.miro93.sportmonks.football.model;

import io.github.miro93.sportmonks.core.json.JacksonCodec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PremiumOddDecodingTest {

    private final JacksonCodec codec = new JacksonCodec();

    @Test
    void decodesPremiumOddWithAllScalars() {
        String json = """
                {
                  "data": {
                    "id": 1,
                    "fixture_id": 18533878,
                    "market_id": 1,
                    "bookmaker_id": 34,
                    "label": "Home",
                    "value": "1.48",
                    "name": "Home",
                    "market_description": "Match Winner",
                    "probability": "67.57%",
                    "dp3": "1.480",
                    "fractional": "37/25",
                    "american": "-209",
                    "stopped": false,
                    "total": null,
                    "handicap": null,
                    "participants": null,
                    "latest_bookmaker_update": "2023-01-11 14:40:25"
                  }
                }
                """;

        PremiumOdd odd = codec.decode(json, codec.type(PremiumOdd.class)).data();

        assertThat(odd.id()).isEqualTo(1L);
        assertThat(odd.fixtureId()).isEqualTo(18533878L);
        assertThat(odd.marketId()).isEqualTo(1L);
        assertThat(odd.bookmakerId()).isEqualTo(34L);
        assertThat(odd.label()).isEqualTo("Home");
        assertThat(odd.value()).isEqualTo("1.48");
        assertThat(odd.name()).isEqualTo("Home");
        assertThat(odd.marketDescription()).isEqualTo("Match Winner");
        assertThat(odd.probability()).isEqualTo("67.57%");
        assertThat(odd.dp3()).isEqualTo("1.480");
        assertThat(odd.fractional()).isEqualTo("37/25");
        assertThat(odd.american()).isEqualTo("-209");
        assertThat(odd.stopped()).isFalse();
        assertThat(odd.total()).isNull();
        assertThat(odd.handicap()).isNull();
        assertThat(odd.participants()).isNull();
        assertThat(odd.latestBookmakerUpdate()).isEqualTo("2023-01-11 14:40:25");
    }

    @Test
    void decodesPremiumOddWithOptionalFieldsAbsent() {
        String json = """
                { "data": { "id": 1 } }
                """;

        PremiumOdd odd = codec.decode(json, codec.type(PremiumOdd.class)).data();

        assertThat(odd.id()).isEqualTo(1L);
        assertThat(odd.fixtureId()).isNull();
        assertThat(odd.marketId()).isNull();
        assertThat(odd.bookmakerId()).isNull();
        assertThat(odd.label()).isNull();
        assertThat(odd.value()).isNull();
        assertThat(odd.name()).isNull();
        assertThat(odd.marketDescription()).isNull();
        assertThat(odd.probability()).isNull();
        assertThat(odd.dp3()).isNull();
        assertThat(odd.fractional()).isNull();
        assertThat(odd.american()).isNull();
        assertThat(odd.stopped()).isNull();
        assertThat(odd.total()).isNull();
        assertThat(odd.handicap()).isNull();
        assertThat(odd.participants()).isNull();
        assertThat(odd.latestBookmakerUpdate()).isNull();
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :football:test --tests "*PremiumOddDecodingTest"`
Expected: FAIL — `PremiumOdd` does not exist (compilation error).

- [ ] **Step 3: Write minimal implementation**

Create `PremiumOdd.java`:

```java
package io.github.miro93.sportmonks.football.model;

/// A single premium betting odd from the SportMonks Premium Odds Feed. Mirrors
/// {@link Odd} but, per the API, premium odds have no {@code winning} or
/// {@code original_label} fields. {@code id} is always present; every other
/// field may be {@code null}. The numeric-looking fields — {@code value},
/// {@code probability}, {@code dp3}, {@code fractional}, {@code american},
/// {@code total} and {@code handicap} — are {@code String} because the API
/// returns them as strings (e.g. {@code "1.48"}, {@code "67.57%"}).
public record PremiumOdd(
        long id,
        Long fixtureId,
        Long marketId,
        Long bookmakerId,
        String label,
        String value,
        String name,
        String marketDescription,
        String probability,
        String dp3,
        String fractional,
        String american,
        Boolean stopped,
        String total,
        String handicap,
        String participants,
        String latestBookmakerUpdate) {
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :football:test --tests "*PremiumOddDecodingTest"`
Expected: PASS (2 tests).

- [ ] **Step 5: Commit**

```bash
git add football/src/main/java/io/github/miro93/sportmonks/football/model/PremiumOdd.java \
        football/src/test/java/io/github/miro93/sportmonks/football/model/PremiumOddDecodingTest.java
git commit -m "feat: add PremiumOdd model"
```

---

### Task 2: `HistoricalOdd` model

**Files:**
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/model/HistoricalOdd.java`
- Test: `football/src/test/java/io/github/miro93/sportmonks/football/model/HistoricalOddDecodingTest.java`

- [ ] **Step 1: Write the failing test**

Create `HistoricalOddDecodingTest.java`:

```java
package io.github.miro93.sportmonks.football.model;

import io.github.miro93.sportmonks.core.json.JacksonCodec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HistoricalOddDecodingTest {

    private final JacksonCodec codec = new JacksonCodec();

    @Test
    void decodesHistoricalOddWithAllScalars() {
        String json = """
                {
                  "data": {
                    "id": 100,
                    "odd_id": 1,
                    "value": "1.48",
                    "probability": "67.57%",
                    "dp3": "1.480",
                    "fractional": "37/25",
                    "american": "-209",
                    "bookmaker_update": "2023-01-11 14:40:25"
                  }
                }
                """;

        HistoricalOdd odd = codec.decode(json, codec.type(HistoricalOdd.class)).data();

        assertThat(odd.id()).isEqualTo(100L);
        assertThat(odd.oddId()).isEqualTo(1L);
        assertThat(odd.value()).isEqualTo("1.48");
        assertThat(odd.probability()).isEqualTo("67.57%");
        assertThat(odd.dp3()).isEqualTo("1.480");
        assertThat(odd.fractional()).isEqualTo("37/25");
        assertThat(odd.american()).isEqualTo("-209");
        assertThat(odd.bookmakerUpdate()).isEqualTo("2023-01-11 14:40:25");
    }

    @Test
    void decodesHistoricalOddWithOptionalFieldsAbsent() {
        String json = """
                { "data": { "id": 100 } }
                """;

        HistoricalOdd odd = codec.decode(json, codec.type(HistoricalOdd.class)).data();

        assertThat(odd.id()).isEqualTo(100L);
        assertThat(odd.oddId()).isNull();
        assertThat(odd.value()).isNull();
        assertThat(odd.probability()).isNull();
        assertThat(odd.dp3()).isNull();
        assertThat(odd.fractional()).isNull();
        assertThat(odd.american()).isNull();
        assertThat(odd.bookmakerUpdate()).isNull();
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :football:test --tests "*HistoricalOddDecodingTest"`
Expected: FAIL — `HistoricalOdd` does not exist (compilation error).

- [ ] **Step 3: Write minimal implementation**

Create `HistoricalOdd.java`:

```java
package io.github.miro93.sportmonks.football.model;

/// A historical premium odd record from the SportMonks Premium Odds Feed
/// (entity {@code PremiumOddHistory}): one snapshot of a premium odd's value
/// over time. {@code id} is always present; every other field may be
/// {@code null}. {@code oddId} references the parent premium odd. The
/// numeric-looking fields — {@code value}, {@code probability}, {@code dp3},
/// {@code fractional} and {@code american} — are {@code String} because the API
/// returns them as strings.
public record HistoricalOdd(
        long id,
        Long oddId,
        String value,
        String probability,
        String dp3,
        String fractional,
        String american,
        String bookmakerUpdate) {
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :football:test --tests "*HistoricalOddDecodingTest"`
Expected: PASS (2 tests).

- [ ] **Step 5: Commit**

```bash
git add football/src/main/java/io/github/miro93/sportmonks/football/model/HistoricalOdd.java \
        football/src/test/java/io/github/miro93/sportmonks/football/model/HistoricalOddDecodingTest.java
git commit -m "feat: add HistoricalOdd model"
```

---

### Task 3: `PremiumOddsEndpoint`

**Files:**
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/endpoint/PremiumOddsEndpoint.java`
- Test: `football/src/test/java/io/github/miro93/sportmonks/football/endpoint/PremiumOddsEndpointTest.java`

- [ ] **Step 1: Write the failing test**

Create `PremiumOddsEndpointTest.java`:

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
class PremiumOddsEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private PremiumOddsEndpoint odds(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new PremiumOddsEndpoint(executor, codec);
    }

    @Test
    void allHitsPremiumRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/odds/premium")).willReturn(okJson("""
                { "data": [ { "id": 1, "fixture_id": 18533878, "value": "1.48" } ] }
                """)));

        var response = odds(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().value()).isEqualTo("1.48");
    }

    @Test
    void byFixtureHitsFixturesPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/odds/premium/fixtures/18533878")).willReturn(okJson("""
                { "data": [ { "id": 1, "fixture_id": 18533878, "value": "1.48" } ] }
                """)));

        var response = odds(wm.getHttpBaseUrl()).byFixture(18533878L).get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().fixtureId()).isEqualTo(18533878L);
    }

    @Test
    void byFixtureAndBookmakerHitsCompositePath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/odds/premium/fixtures/18533878/bookmakers/34")).willReturn(okJson("""
                { "data": [ { "id": 1, "bookmaker_id": 34, "value": "1.48" } ] }
                """)));

        var response = odds(wm.getHttpBaseUrl()).byFixtureAndBookmaker(18533878L, 34L).get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().bookmakerId()).isEqualTo(34L);
    }

    @Test
    void byFixtureAndMarketHitsCompositePath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/odds/premium/fixtures/18533878/markets/1")).willReturn(okJson("""
                { "data": [ { "id": 1, "market_id": 1, "value": "1.48" } ] }
                """)));

        var response = odds(wm.getHttpBaseUrl()).byFixtureAndMarket(18533878L, 1L).get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().marketId()).isEqualTo(1L);
    }

    @Test
    void updatedBetweenHitsTimeRangePath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/odds/premium/updated/between/1767225600/1767225900")).willReturn(okJson("""
                { "data": [ { "id": 1, "value": "1.48" } ] }
                """)));

        var response = odds(wm.getHttpBaseUrl()).updatedBetween(1767225600L, 1767225900L).get();

        assertThat(response.data()).hasSize(1);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :football:test --tests "*PremiumOddsEndpointTest"`
Expected: FAIL — `PremiumOddsEndpoint` does not exist (compilation error).

- [ ] **Step 3: Write minimal implementation**

Create `PremiumOddsEndpoint.java`:

```java
package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.football.model.PremiumOdd;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks premium pre-match odds endpoints
/// ({@code /odds/premium}).
public final class PremiumOddsEndpoint {

    private final ApiExecutor executor;
    private final DataType<List<PremiumOdd>> list;

    /// Creates the endpoint, building the {@link PremiumOdd} list decoder from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the list response type
    public PremiumOddsEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.list = codec.listType(PremiumOdd.class);
    }

    /// Requests every premium pre-match odd, paginated.
    ///
    /// @return a collection request for all premium odds
    public CollectionRequest<PremiumOdd> all() {
        return collection("odds/premium");
    }

    /// Requests all premium odds for a given fixture.
    ///
    /// @param fixtureId the fixture id
    /// @return a collection request for the matching premium odds
    public CollectionRequest<PremiumOdd> byFixture(long fixtureId) {
        return collection("odds/premium/fixtures/" + fixtureId);
    }

    /// Requests premium odds for a fixture from a specific bookmaker.
    ///
    /// @param fixtureId   the fixture id
    /// @param bookmakerId the bookmaker id
    /// @return a collection request for the matching premium odds
    public CollectionRequest<PremiumOdd> byFixtureAndBookmaker(long fixtureId, long bookmakerId) {
        return collection("odds/premium/fixtures/" + fixtureId + "/bookmakers/" + bookmakerId);
    }

    /// Requests premium odds for a fixture in a specific market.
    ///
    /// @param fixtureId the fixture id
    /// @param marketId  the market id
    /// @return a collection request for the matching premium odds
    public CollectionRequest<PremiumOdd> byFixtureAndMarket(long fixtureId, long marketId) {
        return collection("odds/premium/fixtures/" + fixtureId + "/markets/" + marketId);
    }

    /// Requests premium odds updated within a time range, expressed as UNIX
    /// timestamps (seconds).
    ///
    /// @param fromEpochSeconds start of the range, as a UNIX timestamp in seconds
    /// @param toEpochSeconds   end of the range, as a UNIX timestamp in seconds
    /// @return a collection request for the premium odds updated in the range
    public CollectionRequest<PremiumOdd> updatedBetween(long fromEpochSeconds, long toEpochSeconds) {
        return collection("odds/premium/updated/between/" + fromEpochSeconds + "/" + toEpochSeconds);
    }

    private CollectionRequest<PremiumOdd> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :football:test --tests "*PremiumOddsEndpointTest"`
Expected: PASS (5 tests).

- [ ] **Step 5: Commit**

```bash
git add football/src/main/java/io/github/miro93/sportmonks/football/endpoint/PremiumOddsEndpoint.java \
        football/src/test/java/io/github/miro93/sportmonks/football/endpoint/PremiumOddsEndpointTest.java
git commit -m "feat: add PremiumOddsEndpoint"
```

---

### Task 4: `PremiumOddsHistoryEndpoint`

**Files:**
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/endpoint/PremiumOddsHistoryEndpoint.java`
- Test: `football/src/test/java/io/github/miro93/sportmonks/football/endpoint/PremiumOddsHistoryEndpointTest.java`

- [ ] **Step 1: Write the failing test**

Create `PremiumOddsHistoryEndpointTest.java`:

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
class PremiumOddsHistoryEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private PremiumOddsHistoryEndpoint history(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new PremiumOddsHistoryEndpoint(executor, codec);
    }

    @Test
    void allHitsHistoryRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/odds/premium/history")).willReturn(okJson("""
                { "data": [ { "id": 100, "odd_id": 1, "value": "1.48" } ] }
                """)));

        var response = history(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().oddId()).isEqualTo(1L);
    }

    @Test
    void updatedBetweenHitsTimeRangePath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/odds/premium/history/updated/between/1767225600/1767225900")).willReturn(okJson("""
                { "data": [ { "id": 100, "value": "1.48" } ] }
                """)));

        var response = history(wm.getHttpBaseUrl()).updatedBetween(1767225600L, 1767225900L).get();

        assertThat(response.data()).hasSize(1);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :football:test --tests "*PremiumOddsHistoryEndpointTest"`
Expected: FAIL — `PremiumOddsHistoryEndpoint` does not exist (compilation error).

- [ ] **Step 3: Write minimal implementation**

Create `PremiumOddsHistoryEndpoint.java`:

```java
package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.football.model.HistoricalOdd;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks premium pre-match odds history endpoints
/// ({@code /odds/premium/history}).
public final class PremiumOddsHistoryEndpoint {

    private final ApiExecutor executor;
    private final DataType<List<HistoricalOdd>> list;

    /// Creates the endpoint, building the {@link HistoricalOdd} list decoder from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the list response type
    public PremiumOddsHistoryEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.list = codec.listType(HistoricalOdd.class);
    }

    /// Requests the full history of premium pre-match odds, paginated.
    ///
    /// @return a collection request for all historical premium odds
    public CollectionRequest<HistoricalOdd> all() {
        return collection("odds/premium/history");
    }

    /// Requests historical premium odds updated within a time range, expressed
    /// as UNIX timestamps (seconds).
    ///
    /// @param fromEpochSeconds start of the range, as a UNIX timestamp in seconds
    /// @param toEpochSeconds   end of the range, as a UNIX timestamp in seconds
    /// @return a collection request for the historical premium odds updated in the range
    public CollectionRequest<HistoricalOdd> updatedBetween(long fromEpochSeconds, long toEpochSeconds) {
        return collection("odds/premium/history/updated/between/" + fromEpochSeconds + "/" + toEpochSeconds);
    }

    private CollectionRequest<HistoricalOdd> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :football:test --tests "*PremiumOddsHistoryEndpointTest"`
Expected: PASS (2 tests).

- [ ] **Step 5: Commit**

```bash
git add football/src/main/java/io/github/miro93/sportmonks/football/endpoint/PremiumOddsHistoryEndpoint.java \
        football/src/test/java/io/github/miro93/sportmonks/football/endpoint/PremiumOddsHistoryEndpointTest.java
git commit -m "feat: add PremiumOddsHistoryEndpoint"
```

---

### Task 5: `PremiumMarketsEndpoint`

**Files:**
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/endpoint/PremiumMarketsEndpoint.java`
- Test: `football/src/test/java/io/github/miro93/sportmonks/football/endpoint/PremiumMarketsEndpointTest.java`

Note: this endpoint targets the `/v3/odds` base URL (path `markets/premium`). The endpoint
class itself is base-URL-agnostic — it just calls `collection("markets/premium")`; the base
URL is supplied by whichever `ApiExecutor` is passed in. The test passes the WireMock URL
directly.

- [ ] **Step 1: Write the failing test**

Create `PremiumMarketsEndpointTest.java`:

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
class PremiumMarketsEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private PremiumMarketsEndpoint markets(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new PremiumMarketsEndpoint(executor, codec);
    }

    @Test
    void allHitsPremiumMarketsPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/markets/premium")).willReturn(okJson("""
                { "data": [ { "id": 1, "legacy_id": 9, "name": "Fulltime Result", "developer_name": "FULLTIME_RESULT" } ] }
                """)));

        var response = markets(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().name()).isEqualTo("Fulltime Result");
        assertThat(response.data().getFirst().developerName()).isEqualTo("FULLTIME_RESULT");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :football:test --tests "*PremiumMarketsEndpointTest"`
Expected: FAIL — `PremiumMarketsEndpoint` does not exist (compilation error).

- [ ] **Step 3: Write minimal implementation**

Create `PremiumMarketsEndpoint.java`:

```java
package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.football.model.Market;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks premium markets endpoint ({@code /markets/premium}
/// on the odds base URL).
public final class PremiumMarketsEndpoint {

    private final ApiExecutor executor;
    private final DataType<List<Market>> list;

    /// Creates the endpoint, building the {@link Market} list decoder from {@code codec}.
    ///
    /// @param executor the executor used to run requests (configured with the odds base URL)
    /// @param codec    the codec used to derive the list response type
    public PremiumMarketsEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.list = codec.listType(Market.class);
    }

    /// Requests every premium market, paginated.
    ///
    /// @return a collection request for all premium markets
    public CollectionRequest<Market> all() {
        return new CollectionRequest<>(executor, RequestSpec.builder("markets/premium"), list);
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :football:test --tests "*PremiumMarketsEndpointTest"`
Expected: PASS (1 test).

- [ ] **Step 5: Commit**

```bash
git add football/src/main/java/io/github/miro93/sportmonks/football/endpoint/PremiumMarketsEndpoint.java \
        football/src/test/java/io/github/miro93/sportmonks/football/endpoint/PremiumMarketsEndpointTest.java
git commit -m "feat: add PremiumMarketsEndpoint"
```

---

### Task 6: `PremiumBookmakersEndpoint`

**Files:**
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/endpoint/PremiumBookmakersEndpoint.java`
- Test: `football/src/test/java/io/github/miro93/sportmonks/football/endpoint/PremiumBookmakersEndpointTest.java`

Note: targets the `/v3/odds` base URL (path `bookmakers/premium`); same base-URL-agnostic
pattern as Task 5.

- [ ] **Step 1: Write the failing test**

Create `PremiumBookmakersEndpointTest.java`:

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
class PremiumBookmakersEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private PremiumBookmakersEndpoint bookmakers(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new PremiumBookmakersEndpoint(executor, codec);
    }

    @Test
    void allHitsPremiumBookmakersPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/bookmakers/premium")).willReturn(okJson("""
                { "data": [ { "id": 34, "legacy_id": 2, "name": "bet365" } ] }
                """)));

        var response = bookmakers(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().id()).isEqualTo(34L);
        assertThat(response.data().getFirst().name()).isEqualTo("bet365");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :football:test --tests "*PremiumBookmakersEndpointTest"`
Expected: FAIL — `PremiumBookmakersEndpoint` does not exist (compilation error).

- [ ] **Step 3: Write minimal implementation**

Create `PremiumBookmakersEndpoint.java`:

```java
package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.football.model.Bookmaker;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks premium bookmakers endpoint
/// ({@code /bookmakers/premium} on the odds base URL).
public final class PremiumBookmakersEndpoint {

    private final ApiExecutor executor;
    private final DataType<List<Bookmaker>> list;

    /// Creates the endpoint, building the {@link Bookmaker} list decoder from {@code codec}.
    ///
    /// @param executor the executor used to run requests (configured with the odds base URL)
    /// @param codec    the codec used to derive the list response type
    public PremiumBookmakersEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.list = codec.listType(Bookmaker.class);
    }

    /// Requests every premium bookmaker, paginated.
    ///
    /// @return a collection request for all premium bookmakers
    public CollectionRequest<Bookmaker> all() {
        return new CollectionRequest<>(executor, RequestSpec.builder("bookmakers/premium"), list);
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :football:test --tests "*PremiumBookmakersEndpointTest"`
Expected: PASS (1 test).

- [ ] **Step 5: Commit**

```bash
git add football/src/main/java/io/github/miro93/sportmonks/football/endpoint/PremiumBookmakersEndpoint.java \
        football/src/test/java/io/github/miro93/sportmonks/football/endpoint/PremiumBookmakersEndpointTest.java
git commit -m "feat: add PremiumBookmakersEndpoint"
```

---

### Task 7: Wire endpoints into `FootballClient` (with `/v3/odds` executor)

**Files:**
- Modify: `football/src/main/java/io/github/miro93/sportmonks/football/FootballClient.java`
- Test: `football/src/test/java/io/github/miro93/sportmonks/football/FootballClientM10Test.java`

This task adds a second `ApiExecutor` (`oddsExecutor`, base `/v3/odds`) mirroring the
existing `coreExecutor`, wires the four new endpoints, and adds their accessors. The four
new fields / constructor params / `build()` arguments go **after `inplayOdds` and before
`core`** (core stays LAST).

- [ ] **Step 1: Write the failing test**

Create `FootballClientM10Test.java`:

```java
package io.github.miro93.sportmonks.football;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class FootballClientM10Test {

    private FootballClient client(String baseUrl) {
        return FootballClient.builder()
                .apiToken(ApiToken.of("tok"))
                .baseUrl(baseUrl)
                .build();
    }

    @Test
    void exposesM10Endpoints(WireMockRuntimeInfo wm) {
        var client = client(wm.getHttpBaseUrl());
        assertThat(client.premiumOdds()).isNotNull();
        assertThat(client.premiumOddsHistory()).isNotNull();
        assertThat(client.premiumMarkets()).isNotNull();
        assertThat(client.premiumBookmakers()).isNotNull();
    }

    @Test
    void premiumOddsByFixtureUsesFootballExecutorAndSendsAuth(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/odds/premium/fixtures/18533878")).willReturn(okJson("""
                { "data": [{ "id": 1, "fixture_id": 18533878, "value": "1.48" }] }
                """)));

        var odds = client(wm.getHttpBaseUrl()).premiumOdds().byFixture(18533878L).get().data();

        assertThat(odds).hasSize(1);
        assertThat(odds.getFirst().value()).isEqualTo("1.48");
        verify(getRequestedFor(urlPathEqualTo("/odds/premium/fixtures/18533878"))
                .withHeader("Authorization", equalTo("tok")));
    }

    @Test
    void premiumMarketsUsesConfiguredOddsBaseUrlAndSendsAuth(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/markets/premium")).willReturn(okJson("""
                { "data": [{ "id": 1, "name": "Fulltime Result" }] }
                """)));

        var client = FootballClient.builder()
                .apiToken(ApiToken.of("tok"))
                .baseUrl(wm.getHttpBaseUrl())
                .oddsBaseUrl(wm.getHttpBaseUrl())
                .build();

        var markets = client.premiumMarkets().all().get().data();

        assertThat(markets).hasSize(1);
        assertThat(markets.getFirst().name()).isEqualTo("Fulltime Result");
        verify(getRequestedFor(urlPathEqualTo("/markets/premium"))
                .withHeader("Authorization", equalTo("tok")));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :football:test --tests "*FootballClientM10Test"`
Expected: FAIL — `premiumOdds()`/`oddsBaseUrl()` etc. do not exist (compilation error).

- [ ] **Step 3: Add the imports**

In `FootballClient.java`, add these imports alongside the existing `endpoint.*` imports
(keep alphabetical order within the group):

```java
import io.github.miro93.sportmonks.football.endpoint.PremiumBookmakersEndpoint;
import io.github.miro93.sportmonks.football.endpoint.PremiumMarketsEndpoint;
import io.github.miro93.sportmonks.football.endpoint.PremiumOddsEndpoint;
import io.github.miro93.sportmonks.football.endpoint.PremiumOddsHistoryEndpoint;
```

- [ ] **Step 4: Add the `ODDS_BASE_URL` constant and fields**

Below the existing `DEFAULT_BASE_URL` constant, add:

```java
    /// The default base URL for the SportMonks odds resources (premium markets
    /// and bookmakers), which live under {@code /v3/odds} rather than
    /// {@code /v3/football}.
    public static final String ODDS_BASE_URL = "https://api.sportmonks.com/v3/odds";
```

In the field block, between `private final InplayOddsEndpoint inplayOdds;` and
`private final CoreClient core;`, add:

```java
    private final PremiumOddsEndpoint premiumOdds;
    private final PremiumOddsHistoryEndpoint premiumOddsHistory;
    private final PremiumMarketsEndpoint premiumMarkets;
    private final PremiumBookmakersEndpoint premiumBookmakers;
```

- [ ] **Step 5: Add the constructor params and assignments**

In the private constructor signature, between `InplayOddsEndpoint inplayOdds,` and
`CoreClient core)`, add:

```java
            PremiumOddsEndpoint premiumOdds,
            PremiumOddsHistoryEndpoint premiumOddsHistory,
            PremiumMarketsEndpoint premiumMarkets,
            PremiumBookmakersEndpoint premiumBookmakers,
```

In the constructor body, between `this.inplayOdds = inplayOdds;` and `this.core = core;`, add:

```java
        this.premiumOdds = premiumOdds;
        this.premiumOddsHistory = premiumOddsHistory;
        this.premiumMarkets = premiumMarkets;
        this.premiumBookmakers = premiumBookmakers;
```

- [ ] **Step 6: Add the accessors**

Between the `inplayOdds()` accessor and the `core()` accessor, add:

```java
    /// Returns the premium pre-match odds endpoint.
    ///
    /// @return the {@code /odds/premium} endpoint accessor
    public PremiumOddsEndpoint premiumOdds() {
        return premiumOdds;
    }

    /// Returns the premium pre-match odds history endpoint.
    ///
    /// @return the {@code /odds/premium/history} endpoint accessor
    public PremiumOddsHistoryEndpoint premiumOddsHistory() {
        return premiumOddsHistory;
    }

    /// Returns the premium markets endpoint (served from the odds base URL).
    ///
    /// @return the {@code /markets/premium} endpoint accessor
    public PremiumMarketsEndpoint premiumMarkets() {
        return premiumMarkets;
    }

    /// Returns the premium bookmakers endpoint (served from the odds base URL).
    ///
    /// @return the {@code /bookmakers/premium} endpoint accessor
    public PremiumBookmakersEndpoint premiumBookmakers() {
        return premiumBookmakers;
    }
```

- [ ] **Step 7: Add the `oddsBaseUrl` builder field and method**

In the `Builder` class, below `private String coreBaseUrl = CoreClient.DEFAULT_BASE_URL;`, add:

```java
        private String oddsBaseUrl = ODDS_BASE_URL;
```

Below the `coreBaseUrl(String)` builder method, add:

```java
        /// Overrides the base URL for premium markets and bookmakers, which are
        /// served from {@link #ODDS_BASE_URL} rather than the football base URL.
        ///
        /// @param oddsBaseUrl the odds base URL
        /// @return this builder
        public Builder oddsBaseUrl(String oddsBaseUrl) {
            this.oddsBaseUrl = Objects.requireNonNull(oddsBaseUrl, "oddsBaseUrl");
            return this;
        }
```

- [ ] **Step 8: Wire the executor and endpoints in `build()`**

In `build()`, after the line `ApiExecutor coreExecutor = new ApiExecutor(transport, codec, apiToken, coreBaseUrl);`, add:

```java
            ApiExecutor oddsExecutor = new ApiExecutor(transport, codec, apiToken, oddsBaseUrl);
```

In the `return new FootballClient(...)` call, between the `new InplayOddsEndpoint(executor, codec),`
argument and the trailing `core)` argument, add:

```java
                    new PremiumOddsEndpoint(executor, codec),
                    new PremiumOddsHistoryEndpoint(executor, codec),
                    new PremiumMarketsEndpoint(oddsExecutor, codec),
                    new PremiumBookmakersEndpoint(oddsExecutor, codec),
```

(Note: `premiumOdds` and `premiumOddsHistory` use `executor` (football base URL);
`premiumMarkets` and `premiumBookmakers` use `oddsExecutor` (odds base URL).)

- [ ] **Step 9: Run test to verify it passes**

Run: `./gradlew :football:test --tests "*FootballClientM10Test"`
Expected: PASS (3 tests).

- [ ] **Step 10: Run the full football test suite**

Run: `./gradlew :football:test`
Expected: PASS (all existing tests + the new M10 tests).

- [ ] **Step 11: Commit**

```bash
git add football/src/main/java/io/github/miro93/sportmonks/football/FootballClient.java \
        football/src/test/java/io/github/miro93/sportmonks/football/FootballClientM10Test.java
git commit -m "feat: expose premium odds feed on FootballClient"
```

---

### Task 8: README documentation

**Files:**
- Modify: `README.md`

- [ ] **Step 1: Locate the football endpoints table**

Run: `grep -n "inplayOdds\|preMatchOdds\|Odds" README.md`
Expected: shows the M9 odds rows in the football endpoints table.

- [ ] **Step 2: Add the four premium accessors to the football endpoints table**

The football endpoints table uses three columns: `| accessor | EndpointClass | methods |`.
After the `inplayOdds()` row (the last row of the table, currently `README.md:251`), add
these four rows verbatim:

```markdown
| `premiumOdds()` | `PremiumOddsEndpoint` | `all()`, `byFixture(fixtureId)`, `byFixtureAndBookmaker(fixtureId, bookmakerId)`, `byFixtureAndMarket(fixtureId, marketId)`, `updatedBetween(fromEpochSeconds, toEpochSeconds)` |
| `premiumOddsHistory()` | `PremiumOddsHistoryEndpoint` | `all()`, `updatedBetween(fromEpochSeconds, toEpochSeconds)` |
| `premiumMarkets()` | `PremiumMarketsEndpoint` | `all()` (served from the `/v3/odds` base URL) |
| `premiumBookmakers()` | `PremiumBookmakersEndpoint` | `all()` (served from the `/v3/odds` base URL) |
```

- [ ] **Step 2b: Add a base-URL note after the table**

The README already explains the Core API `/v3/core` base URL (around `README.md:125` and
`README.md:253`). After the football endpoints table (and before the Core API paragraph at
line 253), add this sentence so the second base URL is documented:

```markdown
Premium markets and bookmakers (`premiumMarkets()`, `premiumBookmakers()`) are served from a
separate odds base URL (`/v3/odds`), configurable via `oddsBaseUrl(...)` on the builder; all
other football endpoints — including the premium odds feed itself — use `/v3/football`.
```

- [ ] **Step 3: Verify the table renders**

Run: `grep -n "premium" README.md`
Expected: shows the four new rows.

- [ ] **Step 4: Commit**

```bash
git add README.md
git commit -m "docs: document premium odds feed endpoints"
```

---

### Task 9: Final verification

- [ ] **Step 1: Run the complete build and test suite**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL — all modules compile, all tests pass (292 prior + 13 new
M10 tests = 305).

- [ ] **Step 2: Confirm no release is cut**

Do NOT merge release-please PR #70. M10 lands on `main`; release-please updates #70; the user
merges it to release M9 + M10 together.

---

## Self-Review Notes

- **Spec coverage:** `PremiumOdd` (Task 1), `HistoricalOdd` (Task 2), `PremiumOddsEndpoint`
  with all 5 methods incl. `updatedBetween` (Task 3), `PremiumOddsHistoryEndpoint` (Task 4),
  `PremiumMarketsEndpoint` (Task 5), `PremiumBookmakersEndpoint` (Task 6), `oddsExecutor` +
  `oddsBaseUrl` + 4 accessors with `core` last (Task 7), README + base-URL note (Task 8),
  routing tests across both executors (Task 7 `FootballClientM10Test`). All spec sections map
  to a task.
- **Type consistency:** accessor names (`premiumOdds`, `premiumOddsHistory`, `premiumMarkets`,
  `premiumBookmakers`), method names (`all`, `byFixture`, `byFixtureAndBookmaker`,
  `byFixtureAndMarket`, `updatedBetween`), and record component names match across all tasks
  and tests.
- **Placeholders:** none — every code step contains complete code.
