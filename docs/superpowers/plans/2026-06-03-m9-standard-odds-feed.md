# M9 — Standard Odds Feed Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add the SportMonks Standard Odds Feed (`/v3/football`) — Bookmakers, Markets, Pre-match Odds, Inplay Odds — as model records + endpoint classes wired into `FootballClient`.

**Architecture:** Continuation of M4/M5/M8. Immutable records in `football/.../model/`; endpoint classes in `football/.../endpoint/` cloning the `CoachesEndpoint` pattern (`CollectionRequest`/`SingleResourceRequest` via the shared `ApiExecutor` + `JacksonCodec`). `FootballClient.Builder.build()` wires 4 new endpoints onto the existing football `executor` and exposes accessors (`core` stays LAST). No `core` module changes.

**Tech Stack:** Java 25, Gradle multi-module, Jackson Blackbird (snake_case), JUnit 5 + WireMock + AssertJ. Builds on merged M1–M8 (v0.4.0).

**Source of truth:** `docs/superpowers/specs/2026-06-03-m9-standard-odds-feed-design.md`. Paths confirmed from docs. The `Odd` entity's numeric-looking fields (`value`, `probability`, `dp3`, `fractional`, `american`, `total`, `handicap`) are **Strings** — the API returns them as strings (e.g. `"value":"1.48"`, `"probability":"67.57%"`), consistent with the project's numbers-as-strings convention.

**Conventions (M4/M5/M8):** only `id` is primitive `long`; every other scalar is a BOXED nullable type. Each type carries `///` JavaDoc (JEP 467), ≥80% coverage (CodeRabbit gate). The pre-match and inplay endpoints SHARE the single `Odd` model.

---

## File Structure

```
football/src/main/java/io/github/miro93/sportmonks/football/
├── FootballClient.java                  # MODIFY (Task 6) — wire 4 endpoints + accessors
├── endpoint/
│   ├── BookmakersEndpoint.java          # NEW (Task 1)
│   ├── MarketsEndpoint.java             # NEW (Task 2)
│   ├── PreMatchOddsEndpoint.java        # NEW (Task 3)
│   └── InplayOddsEndpoint.java          # NEW (Task 4)
└── model/
    ├── Bookmaker.java                   # NEW (Task 1)
    ├── Market.java                      # NEW (Task 2)
    └── Odd.java                         # NEW (Task 3)
football/src/test/java/io/github/miro93/sportmonks/football/
├── endpoint/BookmakersEndpointTest.java     # NEW (Task 1)
├── endpoint/MarketsEndpointTest.java        # NEW (Task 2)
├── endpoint/PreMatchOddsEndpointTest.java   # NEW (Task 3)
├── endpoint/InplayOddsEndpointTest.java     # NEW (Task 4)
├── model/OddDecodingTest.java               # NEW (Task 5)
├── model/BookmakerMarketDecodingTest.java   # NEW (Task 5)
└── FootballClientM9Test.java                # NEW (Task 6)
README.md                                    # MODIFY (Task 7)
```

**Build/test commands** (from repo root):
- One class: `./gradlew :football:test --tests "io.github.miro93.sportmonks.football.endpoint.BookmakersEndpointTest"`
- Whole suite: `./gradlew test`

---

## Task 1: Bookmakers (model + endpoint)

**Files:**
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/model/Bookmaker.java`
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/endpoint/BookmakersEndpoint.java`
- Test: `football/src/test/java/io/github/miro93/sportmonks/football/endpoint/BookmakersEndpointTest.java`

- [ ] **Step 1: Write the failing test**

```java
package io.github.miro93.sportmonks.football.endpoint;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.football.model.Bookmaker;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest
class BookmakersEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private BookmakersEndpoint bookmakers(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new BookmakersEndpoint(executor, codec);
    }

    @Test
    void allHitsBookmakersRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/bookmakers")).willReturn(okJson("""
                { "data": [ { "id": 34, "legacy_id": 2, "name": "bet365" } ] }
                """)));

        var response = bookmakers(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().name()).isEqualTo("bet365");
    }

    @Test
    void byIdHitsTheCorrectPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlEqualTo("/bookmakers/34")).willReturn(okJson("""
                { "data": { "id": 34, "legacy_id": 2, "name": "bet365" } }
                """)));

        Bookmaker bookmaker = bookmakers(wm.getHttpBaseUrl()).byId(34L).get().data();

        assertThat(bookmaker.id()).isEqualTo(34L);
        assertThat(bookmaker.legacyId()).isEqualTo(2L);
    }

    @Test
    void searchHitsSearchPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/bookmakers/search/bet")).willReturn(okJson("""
                { "data": [ { "id": 34, "name": "bet365" } ] }
                """)));

        var response = bookmakers(wm.getHttpBaseUrl()).search("bet").get();

        assertThat(response.data()).hasSize(1);
    }

    @Test
    void byFixtureHitsFixturesPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/bookmakers/fixtures/18535517")).willReturn(okJson("""
                { "data": [ { "id": 34, "name": "bet365" } ] }
                """)));

        var response = bookmakers(wm.getHttpBaseUrl()).byFixture(18535517L).get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().id()).isEqualTo(34L);
    }

    @Test
    void searchRejectsNull(WireMockRuntimeInfo wm) {
        assertThatThrownBy(() -> bookmakers(wm.getHttpBaseUrl()).search(null))
                .isInstanceOf(NullPointerException.class);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :football:test --tests "io.github.miro93.sportmonks.football.endpoint.BookmakersEndpointTest"`
Expected: COMPILE FAILURE — `Bookmaker` / `BookmakersEndpoint` do not exist.

- [ ] **Step 3: Create the `Bookmaker` model**

```java
package io.github.miro93.sportmonks.football.model;

/// A betting bookmaker from the SportMonks football API. {@code id} is always
/// present; {@code legacyId} and {@code name} may be {@code null}.
public record Bookmaker(
        long id,
        Long legacyId,
        String name) {
}
```

- [ ] **Step 4: Create the `BookmakersEndpoint`**

```java
package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.core.request.SingleResourceRequest;
import io.github.miro93.sportmonks.football.model.Bookmaker;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks {@code /bookmakers} endpoints.
public final class BookmakersEndpoint {

    private final ApiExecutor executor;
    private final DataType<Bookmaker> single;
    private final DataType<List<Bookmaker>> list;

    /// Creates the endpoint, building the {@link Bookmaker} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the single/list response types
    public BookmakersEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.single = codec.type(Bookmaker.class);
        this.list = codec.listType(Bookmaker.class);
    }

    /// Requests every bookmaker, paginated.
    ///
    /// @return a collection request for all bookmakers
    public CollectionRequest<Bookmaker> all() {
        return collection("bookmakers");
    }

    /// Requests a single bookmaker by its id.
    ///
    /// @param id the bookmaker id
    /// @return a single-resource request for that bookmaker
    public SingleResourceRequest<Bookmaker> byId(long id) {
        return new SingleResourceRequest<>(executor, RequestSpec.builder("bookmakers/" + id), single);
    }

    /// Searches bookmakers by name.
    ///
    /// @param name the search term (must not be {@code null})
    /// @return a collection request for the matching bookmakers
    /// @throws NullPointerException if {@code name} is {@code null}
    public CollectionRequest<Bookmaker> search(String name) {
        Objects.requireNonNull(name, "name");
        return collection("bookmakers/search/" + name);
    }

    /// Requests all bookmakers offering odds for a given fixture.
    ///
    /// @param fixtureId the fixture id to filter by
    /// @return a collection request for the matching bookmakers
    public CollectionRequest<Bookmaker> byFixture(long fixtureId) {
        return collection("bookmakers/fixtures/" + fixtureId);
    }

    private CollectionRequest<Bookmaker> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `./gradlew :football:test --tests "io.github.miro93.sportmonks.football.endpoint.BookmakersEndpointTest"`
Expected: PASS (5 tests).

- [ ] **Step 6: Commit**

```bash
git add football/src/main/java/io/github/miro93/sportmonks/football/model/Bookmaker.java \
        football/src/main/java/io/github/miro93/sportmonks/football/endpoint/BookmakersEndpoint.java \
        football/src/test/java/io/github/miro93/sportmonks/football/endpoint/BookmakersEndpointTest.java
git commit -m "feat(football): add Bookmakers endpoint and model"
```

---

## Task 2: Markets (model + endpoint)

**Files:**
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/model/Market.java`
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/endpoint/MarketsEndpoint.java`
- Test: `football/src/test/java/io/github/miro93/sportmonks/football/endpoint/MarketsEndpointTest.java`

- [ ] **Step 1: Write the failing test**

```java
package io.github.miro93.sportmonks.football.endpoint;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.football.model.Market;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest
class MarketsEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private MarketsEndpoint markets(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new MarketsEndpoint(executor, codec);
    }

    @Test
    void allHitsMarketsRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/markets")).willReturn(okJson("""
                { "data": [ { "id": 1, "legacy_id": 1, "name": "Fulltime Result", "developer_name": "FULLTIME_RESULT", "has_winning_calculations": true } ] }
                """)));

        var response = markets(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().developerName()).isEqualTo("FULLTIME_RESULT");
        assertThat(response.data().getFirst().hasWinningCalculations()).isTrue();
    }

    @Test
    void byIdHitsTheCorrectPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlEqualTo("/markets/1")).willReturn(okJson("""
                { "data": { "id": 1, "name": "Fulltime Result" } }
                """)));

        Market market = markets(wm.getHttpBaseUrl()).byId(1L).get().data();

        assertThat(market.id()).isEqualTo(1L);
        assertThat(market.name()).isEqualTo("Fulltime Result");
    }

    @Test
    void searchHitsSearchPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/markets/search/Result")).willReturn(okJson("""
                { "data": [ { "id": 1, "name": "Fulltime Result" } ] }
                """)));

        var response = markets(wm.getHttpBaseUrl()).search("Result").get();

        assertThat(response.data()).hasSize(1);
    }

    @Test
    void searchRejectsNull(WireMockRuntimeInfo wm) {
        assertThatThrownBy(() -> markets(wm.getHttpBaseUrl()).search(null))
                .isInstanceOf(NullPointerException.class);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :football:test --tests "io.github.miro93.sportmonks.football.endpoint.MarketsEndpointTest"`
Expected: COMPILE FAILURE — `Market` / `MarketsEndpoint` do not exist.

- [ ] **Step 3: Create the `Market` model**

```java
package io.github.miro93.sportmonks.football.model;

/// A betting market (e.g. Fulltime Result) from the SportMonks football API.
/// {@code id} is always present; {@code legacyId}, {@code name},
/// {@code developerName} and {@code hasWinningCalculations} may be {@code null}.
public record Market(
        long id,
        Long legacyId,
        String name,
        String developerName,
        Boolean hasWinningCalculations) {
}
```

- [ ] **Step 4: Create the `MarketsEndpoint`**

```java
package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.core.request.SingleResourceRequest;
import io.github.miro93.sportmonks.football.model.Market;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks {@code /markets} endpoints.
public final class MarketsEndpoint {

    private final ApiExecutor executor;
    private final DataType<Market> single;
    private final DataType<List<Market>> list;

    /// Creates the endpoint, building the {@link Market} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the single/list response types
    public MarketsEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.single = codec.type(Market.class);
        this.list = codec.listType(Market.class);
    }

    /// Requests every market, paginated.
    ///
    /// @return a collection request for all markets
    public CollectionRequest<Market> all() {
        return collection("markets");
    }

    /// Requests a single market by its id.
    ///
    /// @param id the market id
    /// @return a single-resource request for that market
    public SingleResourceRequest<Market> byId(long id) {
        return new SingleResourceRequest<>(executor, RequestSpec.builder("markets/" + id), single);
    }

    /// Searches markets by name.
    ///
    /// @param name the search term (must not be {@code null})
    /// @return a collection request for the matching markets
    /// @throws NullPointerException if {@code name} is {@code null}
    public CollectionRequest<Market> search(String name) {
        Objects.requireNonNull(name, "name");
        return collection("markets/search/" + name);
    }

    private CollectionRequest<Market> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `./gradlew :football:test --tests "io.github.miro93.sportmonks.football.endpoint.MarketsEndpointTest"`
Expected: PASS (4 tests).

- [ ] **Step 6: Commit**

```bash
git add football/src/main/java/io/github/miro93/sportmonks/football/model/Market.java \
        football/src/main/java/io/github/miro93/sportmonks/football/endpoint/MarketsEndpoint.java \
        football/src/test/java/io/github/miro93/sportmonks/football/endpoint/MarketsEndpointTest.java
git commit -m "feat(football): add Markets endpoint and model"
```

---

## Task 3: Odd model + Pre-match Odds endpoint

**Files:**
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/model/Odd.java`
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/endpoint/PreMatchOddsEndpoint.java`
- Test: `football/src/test/java/io/github/miro93/sportmonks/football/endpoint/PreMatchOddsEndpointTest.java`

- [ ] **Step 1: Write the failing test**

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
class PreMatchOddsEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private PreMatchOddsEndpoint odds(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new PreMatchOddsEndpoint(executor, codec);
    }

    @Test
    void allHitsPreMatchRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/odds/pre-match")).willReturn(okJson("""
                { "data": [ { "id": 1, "fixture_id": 18533878, "market_id": 1, "bookmaker_id": 34, "label": "Home", "value": "1.48" } ] }
                """)));

        var response = odds(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().value()).isEqualTo("1.48");
    }

    @Test
    void byFixtureHitsFixturesPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/odds/pre-match/fixtures/18533878")).willReturn(okJson("""
                { "data": [ { "id": 1, "fixture_id": 18533878, "label": "Home", "value": "1.48" } ] }
                """)));

        var response = odds(wm.getHttpBaseUrl()).byFixture(18533878L).get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().fixtureId()).isEqualTo(18533878L);
    }

    @Test
    void byFixtureAndBookmakerHitsCompositePath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/odds/pre-match/fixtures/18533878/bookmakers/34")).willReturn(okJson("""
                { "data": [ { "id": 1, "bookmaker_id": 34, "value": "1.48" } ] }
                """)));

        var response = odds(wm.getHttpBaseUrl()).byFixtureAndBookmaker(18533878L, 34L).get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().bookmakerId()).isEqualTo(34L);
    }

    @Test
    void byFixtureAndMarketHitsCompositePath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/odds/pre-match/fixtures/18533878/markets/1")).willReturn(okJson("""
                { "data": [ { "id": 1, "market_id": 1, "value": "1.48" } ] }
                """)));

        var response = odds(wm.getHttpBaseUrl()).byFixtureAndMarket(18533878L, 1L).get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().marketId()).isEqualTo(1L);
    }

    @Test
    void latestHitsLatestPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/odds/pre-match/latest")).willReturn(okJson("""
                { "data": [ { "id": 1, "value": "1.48" } ] }
                """)));

        var response = odds(wm.getHttpBaseUrl()).latest().get();

        assertThat(response.data()).hasSize(1);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :football:test --tests "io.github.miro93.sportmonks.football.endpoint.PreMatchOddsEndpointTest"`
Expected: COMPILE FAILURE — `Odd` / `PreMatchOddsEndpoint` do not exist.

- [ ] **Step 3: Create the `Odd` model**

```java
package io.github.miro93.sportmonks.football.model;

/// A single betting odd from the SportMonks football API (shared by the
/// pre-match and in-play feeds). {@code id} is always present; every other field
/// may be {@code null}. The numeric-looking fields — {@code value},
/// {@code probability}, {@code dp3}, {@code fractional}, {@code american},
/// {@code total} and {@code handicap} — are {@code String} because the API
/// returns them as strings (e.g. {@code "1.48"}, {@code "67.57%"}).
public record Odd(
        long id,
        Long fixtureId,
        Long marketId,
        Long bookmakerId,
        String label,
        String value,
        String name,
        Integer sortOrder,
        String marketDescription,
        String probability,
        String dp3,
        String fractional,
        String american,
        Boolean winning,
        Boolean stopped,
        String total,
        String handicap,
        String participants,
        String createdAt,
        String updatedAt,
        String originalLabel,
        String latestBookmakerUpdate) {
}
```

- [ ] **Step 4: Create the `PreMatchOddsEndpoint`**

```java
package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.football.model.Odd;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks pre-match odds endpoints ({@code /odds/pre-match}).
public final class PreMatchOddsEndpoint {

    private final ApiExecutor executor;
    private final DataType<List<Odd>> list;

    /// Creates the endpoint, building the {@link Odd} list decoder from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the list response type
    public PreMatchOddsEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.list = codec.listType(Odd.class);
    }

    /// Requests every pre-match odd, paginated.
    ///
    /// @return a collection request for all pre-match odds
    public CollectionRequest<Odd> all() {
        return collection("odds/pre-match");
    }

    /// Requests all pre-match odds for a given fixture.
    ///
    /// @param fixtureId the fixture id
    /// @return a collection request for the matching odds
    public CollectionRequest<Odd> byFixture(long fixtureId) {
        return collection("odds/pre-match/fixtures/" + fixtureId);
    }

    /// Requests pre-match odds for a fixture from a specific bookmaker.
    ///
    /// @param fixtureId   the fixture id
    /// @param bookmakerId the bookmaker id
    /// @return a collection request for the matching odds
    public CollectionRequest<Odd> byFixtureAndBookmaker(long fixtureId, long bookmakerId) {
        return collection("odds/pre-match/fixtures/" + fixtureId + "/bookmakers/" + bookmakerId);
    }

    /// Requests pre-match odds for a fixture in a specific market.
    ///
    /// @param fixtureId the fixture id
    /// @param marketId  the market id
    /// @return a collection request for the matching odds
    public CollectionRequest<Odd> byFixtureAndMarket(long fixtureId, long marketId) {
        return collection("odds/pre-match/fixtures/" + fixtureId + "/markets/" + marketId);
    }

    /// Requests the most recently updated pre-match odds.
    ///
    /// @return a collection request for the latest pre-match odds
    public CollectionRequest<Odd> latest() {
        return collection("odds/pre-match/latest");
    }

    private CollectionRequest<Odd> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `./gradlew :football:test --tests "io.github.miro93.sportmonks.football.endpoint.PreMatchOddsEndpointTest"`
Expected: PASS (5 tests).

- [ ] **Step 6: Commit**

```bash
git add football/src/main/java/io/github/miro93/sportmonks/football/model/Odd.java \
        football/src/main/java/io/github/miro93/sportmonks/football/endpoint/PreMatchOddsEndpoint.java \
        football/src/test/java/io/github/miro93/sportmonks/football/endpoint/PreMatchOddsEndpointTest.java
git commit -m "feat(football): add Pre-match Odds endpoint and Odd model"
```

---

## Task 4: Inplay Odds endpoint (reuses `Odd`)

**Files:**
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/endpoint/InplayOddsEndpoint.java`
- Test: `football/src/test/java/io/github/miro93/sportmonks/football/endpoint/InplayOddsEndpointTest.java`

- [ ] **Step 1: Write the failing test**

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
class InplayOddsEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private InplayOddsEndpoint odds(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new InplayOddsEndpoint(executor, codec);
    }

    @Test
    void allHitsInplayRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/odds/inplay")).willReturn(okJson("""
                { "data": [ { "id": 1, "fixture_id": 18533878, "value": "2.10" } ] }
                """)));

        var response = odds(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().value()).isEqualTo("2.10");
    }

    @Test
    void byFixtureHitsFixturesPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/odds/inplay/fixtures/18533878")).willReturn(okJson("""
                { "data": [ { "id": 1, "fixture_id": 18533878 } ] }
                """)));

        var response = odds(wm.getHttpBaseUrl()).byFixture(18533878L).get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().fixtureId()).isEqualTo(18533878L);
    }

    @Test
    void byFixtureAndBookmakerHitsCompositePath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/odds/inplay/fixtures/18533878/bookmakers/34")).willReturn(okJson("""
                { "data": [ { "id": 1, "bookmaker_id": 34 } ] }
                """)));

        var response = odds(wm.getHttpBaseUrl()).byFixtureAndBookmaker(18533878L, 34L).get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().bookmakerId()).isEqualTo(34L);
    }

    @Test
    void byFixtureAndMarketHitsCompositePath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/odds/inplay/fixtures/18533878/markets/1")).willReturn(okJson("""
                { "data": [ { "id": 1, "market_id": 1 } ] }
                """)));

        var response = odds(wm.getHttpBaseUrl()).byFixtureAndMarket(18533878L, 1L).get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().marketId()).isEqualTo(1L);
    }

    @Test
    void latestHitsLatestPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/odds/inplay/latest")).willReturn(okJson("""
                { "data": [ { "id": 1, "value": "2.10" } ] }
                """)));

        var response = odds(wm.getHttpBaseUrl()).latest().get();

        assertThat(response.data()).hasSize(1);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :football:test --tests "io.github.miro93.sportmonks.football.endpoint.InplayOddsEndpointTest"`
Expected: COMPILE FAILURE — `InplayOddsEndpoint` does not exist.

- [ ] **Step 3: Create the `InplayOddsEndpoint`** (identical to `PreMatchOddsEndpoint` but with the `odds/inplay` path prefix)

```java
package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.football.model.Odd;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks in-play (live) odds endpoints ({@code /odds/inplay}).
public final class InplayOddsEndpoint {

    private final ApiExecutor executor;
    private final DataType<List<Odd>> list;

    /// Creates the endpoint, building the {@link Odd} list decoder from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the list response type
    public InplayOddsEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.list = codec.listType(Odd.class);
    }

    /// Requests every in-play odd, paginated.
    ///
    /// @return a collection request for all in-play odds
    public CollectionRequest<Odd> all() {
        return collection("odds/inplay");
    }

    /// Requests all in-play odds for a given fixture.
    ///
    /// @param fixtureId the fixture id
    /// @return a collection request for the matching odds
    public CollectionRequest<Odd> byFixture(long fixtureId) {
        return collection("odds/inplay/fixtures/" + fixtureId);
    }

    /// Requests in-play odds for a fixture from a specific bookmaker.
    ///
    /// @param fixtureId   the fixture id
    /// @param bookmakerId the bookmaker id
    /// @return a collection request for the matching odds
    public CollectionRequest<Odd> byFixtureAndBookmaker(long fixtureId, long bookmakerId) {
        return collection("odds/inplay/fixtures/" + fixtureId + "/bookmakers/" + bookmakerId);
    }

    /// Requests in-play odds for a fixture in a specific market.
    ///
    /// @param fixtureId the fixture id
    /// @param marketId  the market id
    /// @return a collection request for the matching odds
    public CollectionRequest<Odd> byFixtureAndMarket(long fixtureId, long marketId) {
        return collection("odds/inplay/fixtures/" + fixtureId + "/markets/" + marketId);
    }

    /// Requests the most recently updated in-play odds.
    ///
    /// @return a collection request for the latest in-play odds
    public CollectionRequest<Odd> latest() {
        return collection("odds/inplay/latest");
    }

    private CollectionRequest<Odd> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :football:test --tests "io.github.miro93.sportmonks.football.endpoint.InplayOddsEndpointTest"`
Expected: PASS (5 tests).

- [ ] **Step 5: Commit**

```bash
git add football/src/main/java/io/github/miro93/sportmonks/football/endpoint/InplayOddsEndpoint.java \
        football/src/test/java/io/github/miro93/sportmonks/football/endpoint/InplayOddsEndpointTest.java
git commit -m "feat(football): add Inplay Odds endpoint"
```

---

## Task 5: Decoding tests (rich Odd + nullable coverage)

Pure-decode tests pinning field mappings — full `Odd` payload (with String `value`/`probability`) + all-optional-absent, and Bookmaker/Market. **If a field assertion fails, fix the model + the matching endpoint test, then update the spec's "confirm in TDD" note.**

**Files:**
- Test: `football/src/test/java/io/github/miro93/sportmonks/football/model/OddDecodingTest.java`
- Test: `football/src/test/java/io/github/miro93/sportmonks/football/model/BookmakerMarketDecodingTest.java`

- [ ] **Step 1: Write `OddDecodingTest`**

```java
package io.github.miro93.sportmonks.football.model;

import io.github.miro93.sportmonks.core.json.JacksonCodec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OddDecodingTest {

    private final JacksonCodec codec = new JacksonCodec();

    @Test
    void decodesOddWithAllScalars() {
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
                    "sort_order": 0,
                    "market_description": "Match Winner",
                    "probability": "67.57%",
                    "dp3": "1.480",
                    "fractional": "37/25",
                    "american": "-209",
                    "winning": false,
                    "stopped": false,
                    "total": null,
                    "handicap": null,
                    "participants": null,
                    "created_at": "2023-01-11T14:40:25.000000Z",
                    "updated_at": "2023-01-11T14:47:50.000000Z",
                    "original_label": null,
                    "latest_bookmaker_update": "2023-01-11 14:40:25"
                  }
                }
                """;

        Odd odd = codec.decode(json, codec.type(Odd.class)).data();

        assertThat(odd.id()).isEqualTo(1L);
        assertThat(odd.fixtureId()).isEqualTo(18533878L);
        assertThat(odd.marketId()).isEqualTo(1L);
        assertThat(odd.bookmakerId()).isEqualTo(34L);
        assertThat(odd.label()).isEqualTo("Home");
        assertThat(odd.value()).isEqualTo("1.48");
        assertThat(odd.name()).isEqualTo("Home");
        assertThat(odd.sortOrder()).isEqualTo(0);
        assertThat(odd.marketDescription()).isEqualTo("Match Winner");
        assertThat(odd.probability()).isEqualTo("67.57%");
        assertThat(odd.dp3()).isEqualTo("1.480");
        assertThat(odd.fractional()).isEqualTo("37/25");
        assertThat(odd.american()).isEqualTo("-209");
        assertThat(odd.winning()).isFalse();
        assertThat(odd.stopped()).isFalse();
        assertThat(odd.total()).isNull();
        assertThat(odd.handicap()).isNull();
        assertThat(odd.participants()).isNull();
        assertThat(odd.createdAt()).isEqualTo("2023-01-11T14:40:25.000000Z");
        assertThat(odd.updatedAt()).isEqualTo("2023-01-11T14:47:50.000000Z");
        assertThat(odd.originalLabel()).isNull();
        assertThat(odd.latestBookmakerUpdate()).isEqualTo("2023-01-11 14:40:25");
    }

    @Test
    void decodesOddWithOptionalFieldsAbsent() {
        String json = """
                { "data": { "id": 1 } }
                """;

        Odd odd = codec.decode(json, codec.type(Odd.class)).data();

        assertThat(odd.id()).isEqualTo(1L);
        assertThat(odd.fixtureId()).isNull();
        assertThat(odd.marketId()).isNull();
        assertThat(odd.bookmakerId()).isNull();
        assertThat(odd.label()).isNull();
        assertThat(odd.value()).isNull();
        assertThat(odd.name()).isNull();
        assertThat(odd.sortOrder()).isNull();
        assertThat(odd.marketDescription()).isNull();
        assertThat(odd.probability()).isNull();
        assertThat(odd.dp3()).isNull();
        assertThat(odd.fractional()).isNull();
        assertThat(odd.american()).isNull();
        assertThat(odd.winning()).isNull();
        assertThat(odd.stopped()).isNull();
        assertThat(odd.total()).isNull();
        assertThat(odd.handicap()).isNull();
        assertThat(odd.participants()).isNull();
        assertThat(odd.createdAt()).isNull();
        assertThat(odd.updatedAt()).isNull();
        assertThat(odd.originalLabel()).isNull();
        assertThat(odd.latestBookmakerUpdate()).isNull();
    }
}
```

- [ ] **Step 2: Write `BookmakerMarketDecodingTest`**

```java
package io.github.miro93.sportmonks.football.model;

import io.github.miro93.sportmonks.core.json.JacksonCodec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BookmakerMarketDecodingTest {

    private final JacksonCodec codec = new JacksonCodec();

    @Test
    void decodesBookmaker() {
        String json = """
                { "data": { "id": 34, "legacy_id": 2, "name": "bet365" } }
                """;

        Bookmaker bookmaker = codec.decode(json, codec.type(Bookmaker.class)).data();

        assertThat(bookmaker.id()).isEqualTo(34L);
        assertThat(bookmaker.legacyId()).isEqualTo(2L);
        assertThat(bookmaker.name()).isEqualTo("bet365");
    }

    @Test
    void decodesBookmakerWithOptionalFieldsAbsent() {
        String json = """
                { "data": { "id": 34 } }
                """;

        Bookmaker bookmaker = codec.decode(json, codec.type(Bookmaker.class)).data();

        assertThat(bookmaker.id()).isEqualTo(34L);
        assertThat(bookmaker.legacyId()).isNull();
        assertThat(bookmaker.name()).isNull();
    }

    @Test
    void decodesMarketWithBooleanFlag() {
        String json = """
                {
                  "data": {
                    "id": 1,
                    "legacy_id": 1,
                    "name": "Fulltime Result",
                    "developer_name": "FULLTIME_RESULT",
                    "has_winning_calculations": true
                  }
                }
                """;

        Market market = codec.decode(json, codec.type(Market.class)).data();

        assertThat(market.id()).isEqualTo(1L);
        assertThat(market.legacyId()).isEqualTo(1L);
        assertThat(market.name()).isEqualTo("Fulltime Result");
        assertThat(market.developerName()).isEqualTo("FULLTIME_RESULT");
        assertThat(market.hasWinningCalculations()).isTrue();
    }

    @Test
    void decodesMarketWithOptionalFieldsAbsent() {
        String json = """
                { "data": { "id": 1, "name": "Fulltime Result" } }
                """;

        Market market = codec.decode(json, codec.type(Market.class)).data();

        assertThat(market.id()).isEqualTo(1L);
        assertThat(market.name()).isEqualTo("Fulltime Result");
        assertThat(market.legacyId()).isNull();
        assertThat(market.developerName()).isNull();
        assertThat(market.hasWinningCalculations()).isNull();
    }
}
```

- [ ] **Step 3: Run both tests**

Run: `./gradlew :football:test --tests "io.github.miro93.sportmonks.football.model.OddDecodingTest" --tests "io.github.miro93.sportmonks.football.model.BookmakerMarketDecodingTest"`
Expected: PASS (6 tests). If `value`/`probability` don't decode as String, or `winning`/`stopped`/`hasWinningCalculations` don't decode as Boolean, fix the model and re-run.

- [ ] **Step 4: Commit**

```bash
git add football/src/test/java/io/github/miro93/sportmonks/football/model/OddDecodingTest.java \
        football/src/test/java/io/github/miro93/sportmonks/football/model/BookmakerMarketDecodingTest.java
git commit -m "test(football): add odds/bookmaker/market decoding tests"
```

---

## Task 6: Wire 4 endpoints into FootballClient

Add the 4 new endpoints to `FootballClient`, keeping the `core` field/accessor LAST.

**Files:**
- Modify: `football/src/main/java/io/github/miro93/sportmonks/football/FootballClient.java`
- Test: `football/src/test/java/io/github/miro93/sportmonks/football/FootballClientM9Test.java`

- [ ] **Step 1: Write the failing `FootballClientM9Test`**

```java
package io.github.miro93.sportmonks.football;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class FootballClientM9Test {

    private FootballClient client(String baseUrl) {
        return FootballClient.builder()
                .apiToken(ApiToken.of("tok"))
                .baseUrl(baseUrl)
                .build();
    }

    @Test
    void exposesM9Endpoints(WireMockRuntimeInfo wm) {
        var client = client(wm.getHttpBaseUrl());
        assertThat(client.bookmakers()).isNotNull();
        assertThat(client.markets()).isNotNull();
        assertThat(client.preMatchOdds()).isNotNull();
        assertThat(client.inplayOdds()).isNotNull();
    }

    @Test
    void preMatchOddsByFixtureDecodesAndSendsAuth(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/odds/pre-match/fixtures/18533878")).willReturn(okJson("""
                { "data": [{ "id": 1, "fixture_id": 18533878, "value": "1.48" }] }
                """)));

        var odds = client(wm.getHttpBaseUrl()).preMatchOdds().byFixture(18533878L).get().data();

        assertThat(odds).hasSize(1);
        assertThat(odds.getFirst().value()).isEqualTo("1.48");
        verify(getRequestedFor(urlPathEqualTo("/odds/pre-match/fixtures/18533878"))
                .withHeader("Authorization", equalTo("tok")));
    }

    @Test
    void bookmakersByFixtureDecodesAndSendsAuth(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/bookmakers/fixtures/18533878")).willReturn(okJson("""
                { "data": [{ "id": 34, "name": "bet365" }] }
                """)));

        var books = client(wm.getHttpBaseUrl()).bookmakers().byFixture(18533878L).get().data();

        assertThat(books).hasSize(1);
        assertThat(books.getFirst().id()).isEqualTo(34L);
        verify(getRequestedFor(urlPathEqualTo("/bookmakers/fixtures/18533878"))
                .withHeader("Authorization", equalTo("tok")));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :football:test --tests "io.github.miro93.sportmonks.football.FootballClientM9Test"`
Expected: COMPILE FAILURE — `bookmakers()`/`markets()`/`preMatchOdds()`/`inplayOdds()` do not exist.

- [ ] **Step 3: Add 4 imports** alongside the other `football.endpoint.*` imports:

```java
import io.github.miro93.sportmonks.football.endpoint.BookmakersEndpoint;
import io.github.miro93.sportmonks.football.endpoint.InplayOddsEndpoint;
import io.github.miro93.sportmonks.football.endpoint.MarketsEndpoint;
import io.github.miro93.sportmonks.football.endpoint.PreMatchOddsEndpoint;
```

(4 imports — one per new endpoint.)

- [ ] **Step 4: Add 4 fields** AFTER the `commentaries` field and BEFORE `private final CoreClient core;`:

```java
    private final BookmakersEndpoint bookmakers;
    private final MarketsEndpoint markets;
    private final PreMatchOddsEndpoint preMatchOdds;
    private final InplayOddsEndpoint inplayOdds;
```

- [ ] **Step 5: Add 4 constructor params + assignments**

In the private constructor, add 4 params after `CommentariesEndpoint commentaries,` and before `CoreClient core)`:

```java
            CommentariesEndpoint commentaries,
            BookmakersEndpoint bookmakers,
            MarketsEndpoint markets,
            PreMatchOddsEndpoint preMatchOdds,
            InplayOddsEndpoint inplayOdds,
            CoreClient core) {
```

And assignments after `this.commentaries = commentaries;` and before `this.core = core;`:

```java
        this.bookmakers = bookmakers;
        this.markets = markets;
        this.preMatchOdds = preMatchOdds;
        this.inplayOdds = inplayOdds;
```

- [ ] **Step 6: Add 4 accessors** AFTER the `commentaries()` accessor and BEFORE the `core()` accessor:

```java
    /// Returns the bookmakers endpoint.
    ///
    /// @return the {@code /bookmakers} endpoint accessor
    public BookmakersEndpoint bookmakers() {
        return bookmakers;
    }

    /// Returns the markets endpoint.
    ///
    /// @return the {@code /markets} endpoint accessor
    public MarketsEndpoint markets() {
        return markets;
    }

    /// Returns the pre-match odds endpoint.
    ///
    /// @return the {@code /odds/pre-match} endpoint accessor
    public PreMatchOddsEndpoint preMatchOdds() {
        return preMatchOdds;
    }

    /// Returns the in-play odds endpoint.
    ///
    /// @return the {@code /odds/inplay} endpoint accessor
    public InplayOddsEndpoint inplayOdds() {
        return inplayOdds;
    }
```

- [ ] **Step 7: Wire the endpoints in `build()`**

In the `return new FootballClient(...)` call, after `new CommentariesEndpoint(executor, codec),` and BEFORE `core)`, add:

```java
                    new CommentariesEndpoint(executor, codec),
                    new BookmakersEndpoint(executor, codec),
                    new MarketsEndpoint(executor, codec),
                    new PreMatchOddsEndpoint(executor, codec),
                    new InplayOddsEndpoint(executor, codec),
                    core);
```

- [ ] **Step 8: Run test to verify it passes**

Run: `./gradlew :football:test --tests "io.github.miro93.sportmonks.football.FootballClientM9Test"`
Expected: PASS (3 tests). Then run full `./gradlew :football:test`:
Expected: BUILD SUCCESSFUL.

- [ ] **Step 9: Commit**

```bash
git add football/src/main/java/io/github/miro93/sportmonks/football/FootballClient.java \
        football/src/test/java/io/github/miro93/sportmonks/football/FootballClientM9Test.java
git commit -m "feat(football): wire Bookmakers/Markets/PreMatchOdds/InplayOdds into FootballClient"
```

---

## Task 7: README + full suite verification

**Files:**
- Modify: `README.md`

- [ ] **Step 1: Add 4 rows to the football endpoints table in README**

Locate the football "Available Endpoints" table (the one with "All endpoints are accessed via `FootballClient`"). Add 4 rows matching the table's existing column format (inspect it first; argument names like `fixtureId`/`bookmakerId`/`marketId` per the surrounding convention):

```
| `bookmakers()`   | `BookmakersEndpoint`   | `all()`, `byId(id)`, `search(name)`, `byFixture(fixtureId)` |
| `markets()`      | `MarketsEndpoint`      | `all()`, `byId(id)`, `search(name)` |
| `preMatchOdds()` | `PreMatchOddsEndpoint` | `all()`, `byFixture(fixtureId)`, `byFixtureAndBookmaker(fixtureId, bookmakerId)`, `byFixtureAndMarket(fixtureId, marketId)`, `latest()` |
| `inplayOdds()`   | `InplayOddsEndpoint`   | `all()`, `byFixture(fixtureId)`, `byFixtureAndBookmaker(fixtureId, bookmakerId)`, `byFixtureAndMarket(fixtureId, marketId)`, `latest()` |
```

(Match the exact column layout/separators of the existing table.)

- [ ] **Step 2: Run the full test suite**

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL. New M9 tests: Bookmakers 5, Markets 4, PreMatchOdds 5, InplayOdds 5 (19 endpoint), 6 decoding, 3 `FootballClientM9Test` — all in `:football`. No failures.

- [ ] **Step 3: Commit**

```bash
git add README.md
git commit -m "docs: document standard odds feed endpoints (M9)"
```

---

## Definition of Done

- [ ] 3 models + 4 endpoints created under `football.{model,endpoint}`, each with `///` JavaDoc.
- [ ] `Odd` shared by `PreMatchOddsEndpoint` and `InplayOddsEndpoint`; numeric-looking fields are `String`.
- [ ] `FootballClient` exposes `bookmakers()`/`markets()`/`preMatchOdds()`/`inplayOdds()`; `core()` + all prior endpoints unchanged; `core` stays LAST in the constructor.
- [ ] All endpoint paths verified by WireMock tests (incl. composite `fixtures/{id}/bookmakers/{id}` and `fixtures/{id}/markets/{id}`); nullable decoding verified.
- [ ] `./gradlew test` green; README documents the 4 odds-feed endpoints.
- [ ] Conventional `feat:`/`test:`/`docs:` commits so release-please updates the CHANGELOG.
- [ ] Out of scope (deferred): Premium Odds Feed (M10, `/v3/odds`), separate Odds API product, bookmaker mappings/event-ids, `havingOdds` filter.
```
