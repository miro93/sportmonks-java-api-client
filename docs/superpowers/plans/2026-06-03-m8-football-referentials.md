# M8 — Football Referentials Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add the SportMonks football referential resources (`/v3/football`) — States, Venues, Referees, TV Stations, Commentaries — as model records + endpoint classes wired into `FootballClient`.

**Architecture:** Continuation of M4/M5. Immutable records in `football/.../model/`; endpoint classes in `football/.../endpoint/` cloning the `CoachesEndpoint` pattern (`CollectionRequest`/`SingleResourceRequest` via the shared `ApiExecutor` + `JacksonCodec`). `FootballClient.Builder.build()` wires 5 new endpoints onto the EXISTING football `executor` and exposes accessors. No `core` module changes.

**Tech Stack:** Java 25, Gradle multi-module, Jackson Blackbird (snake_case), JUnit 5 + WireMock + AssertJ. Builds on merged M1–M7.

**Source of truth:** `docs/superpowers/specs/2026-06-03-m8-football-referentials-design.md`. Endpoint paths confirmed from the docs. Field names/types from the docs `entities` pages, **with two flagged convention deviations confirmed in TDD (Task 6):**
- **`Commentary.id` is a `String`** (the only non-`long` id in the project). Commentaries has NO `byId`.
- **`cityId` is a `String`** on `Venue` and `Referee` (the API returns `city_id` as a string — same as the existing `Coach.cityId`).

**Conventions (M4/M5):** only `id` is primitive `long` (except `Commentary.id` = `String`); every other scalar is a BOXED nullable type. Each type carries `///` JavaDoc (JEP 467), ≥80% coverage (CodeRabbit gate). Boolean record components `isGoal`/`isImportant` map to JSON `is_goal`/`is_important` via the SNAKE_CASE strategy.

---

## File Structure

```
football/src/main/java/io/github/miro93/sportmonks/football/
├── FootballClient.java                  # MODIFY (Task 7) — wire 5 endpoints + accessors
├── endpoint/
│   ├── StatesEndpoint.java              # NEW (Task 1)
│   ├── VenuesEndpoint.java              # NEW (Task 2)
│   ├── RefereesEndpoint.java            # NEW (Task 3)
│   ├── TvStationsEndpoint.java          # NEW (Task 4)
│   └── CommentariesEndpoint.java        # NEW (Task 5)
└── model/
    ├── State.java                       # NEW (Task 1)
    ├── Venue.java                       # NEW (Task 2)
    ├── Referee.java                     # NEW (Task 3)
    ├── TvStation.java                   # NEW (Task 4)
    └── Commentary.java                  # NEW (Task 5)
football/src/test/java/io/github/miro93/sportmonks/football/
├── endpoint/StatesEndpointTest.java         # NEW (Task 1)
├── endpoint/VenuesEndpointTest.java         # NEW (Task 2)
├── endpoint/RefereesEndpointTest.java       # NEW (Task 3)
├── endpoint/TvStationsEndpointTest.java     # NEW (Task 4)
├── endpoint/CommentariesEndpointTest.java   # NEW (Task 5)
├── model/VenueRefereeDecodingTest.java      # NEW (Task 6)
├── model/StateTvCommentaryDecodingTest.java # NEW (Task 6)
└── FootballClientM8Test.java                # NEW (Task 7)
README.md                                    # MODIFY (Task 8)
```

**Build/test commands** (from repo root):
- One class: `./gradlew :football:test --tests "io.github.miro93.sportmonks.football.endpoint.StatesEndpointTest"`
- Whole suite: `./gradlew test`

---

## Task 1: States (model + endpoint)

**Files:**
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/model/State.java`
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/endpoint/StatesEndpoint.java`
- Test: `football/src/test/java/io/github/miro93/sportmonks/football/endpoint/StatesEndpointTest.java`

- [ ] **Step 1: Write the failing test**

```java
package io.github.miro93.sportmonks.football.endpoint;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.football.model.State;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class StatesEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private StatesEndpoint states(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new StatesEndpoint(executor, codec);
    }

    @Test
    void allHitsStatesRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/states")).willReturn(okJson("""
                { "data": [ { "id": 1, "state": "NS", "name": "Not Started", "short_name": "NS", "developer_name": "NOT_STARTED" } ] }
                """)));

        var response = states(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().developerName()).isEqualTo("NOT_STARTED");
    }

    @Test
    void byIdHitsTheCorrectPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlEqualTo("/states/1")).willReturn(okJson("""
                { "data": { "id": 1, "state": "NS", "name": "Not Started", "short_name": "NS" } }
                """)));

        State state = states(wm.getHttpBaseUrl()).byId(1L).get().data();

        assertThat(state.id()).isEqualTo(1L);
        assertThat(state.shortName()).isEqualTo("NS");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :football:test --tests "io.github.miro93.sportmonks.football.endpoint.StatesEndpointTest"`
Expected: COMPILE FAILURE — `State` / `StatesEndpoint` do not exist.

- [ ] **Step 3: Create the `State` model**

```java
package io.github.miro93.sportmonks.football.model;

/// A fixture state (e.g. NS, INPLAY, FT) from the SportMonks football API.
/// {@code id} is always present; {@code state}, {@code name}, {@code shortName}
/// and {@code developerName} may be {@code null}.
public record State(
        long id,
        String state,
        String name,
        String shortName,
        String developerName) {
}
```

- [ ] **Step 4: Create the `StatesEndpoint`**

```java
package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.core.request.SingleResourceRequest;
import io.github.miro93.sportmonks.football.model.State;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks {@code /states} endpoints.
public final class StatesEndpoint {

    private final ApiExecutor executor;
    private final DataType<State> single;
    private final DataType<List<State>> list;

    /// Creates the endpoint, building the {@link State} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the single/list response types
    public StatesEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.single = codec.type(State.class);
        this.list = codec.listType(State.class);
    }

    /// Requests every fixture state, paginated.
    ///
    /// @return a collection request for all states
    public CollectionRequest<State> all() {
        return new CollectionRequest<>(executor, RequestSpec.builder("states"), list);
    }

    /// Requests a single state by its id.
    ///
    /// @param id the state id
    /// @return a single-resource request for that state
    public SingleResourceRequest<State> byId(long id) {
        return new SingleResourceRequest<>(executor, RequestSpec.builder("states/" + id), single);
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `./gradlew :football:test --tests "io.github.miro93.sportmonks.football.endpoint.StatesEndpointTest"`
Expected: PASS (2 tests).

- [ ] **Step 6: Commit**

```bash
git add football/src/main/java/io/github/miro93/sportmonks/football/model/State.java \
        football/src/main/java/io/github/miro93/sportmonks/football/endpoint/StatesEndpoint.java \
        football/src/test/java/io/github/miro93/sportmonks/football/endpoint/StatesEndpointTest.java
git commit -m "feat(football): add States endpoint and model"
```

---

## Task 2: Venues (model + endpoint, with bySeason + search)

**Files:**
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/model/Venue.java`
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/endpoint/VenuesEndpoint.java`
- Test: `football/src/test/java/io/github/miro93/sportmonks/football/endpoint/VenuesEndpointTest.java`

- [ ] **Step 1: Write the failing test**

```java
package io.github.miro93.sportmonks.football.endpoint;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.football.model.Venue;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest
class VenuesEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private VenuesEndpoint venues(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new VenuesEndpoint(executor, codec);
    }

    @Test
    void allHitsVenuesRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/venues")).willReturn(okJson("""
                { "data": [ { "id": 8909, "name": "Celtic Park", "capacity": 60411 } ] }
                """)));

        var response = venues(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().capacity()).isEqualTo(60411);
    }

    @Test
    void byIdHitsTheCorrectPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlEqualTo("/venues/8909")).willReturn(okJson("""
                { "data": { "id": 8909, "name": "Celtic Park", "city_name": "Glasgow", "surface": "grass", "national_team": false } }
                """)));

        Venue venue = venues(wm.getHttpBaseUrl()).byId(8909L).get().data();

        assertThat(venue.id()).isEqualTo(8909L);
        assertThat(venue.cityName()).isEqualTo("Glasgow");
        assertThat(venue.nationalTeam()).isFalse();
    }

    @Test
    void bySeasonHitsSeasonsPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/venues/seasons/19735")).willReturn(okJson("""
                { "data": [ { "id": 8909, "name": "Celtic Park" } ] }
                """)));

        var response = venues(wm.getHttpBaseUrl()).bySeason(19735L).get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().id()).isEqualTo(8909L);
    }

    @Test
    void searchHitsSearchPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/venues/search/Celtic")).willReturn(okJson("""
                { "data": [ { "id": 8909, "name": "Celtic Park" } ] }
                """)));

        var response = venues(wm.getHttpBaseUrl()).search("Celtic").get();

        assertThat(response.data()).hasSize(1);
    }

    @Test
    void searchRejectsNull(WireMockRuntimeInfo wm) {
        assertThatThrownBy(() -> venues(wm.getHttpBaseUrl()).search(null))
                .isInstanceOf(NullPointerException.class);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :football:test --tests "io.github.miro93.sportmonks.football.endpoint.VenuesEndpointTest"`
Expected: COMPILE FAILURE — `Venue` / `VenuesEndpoint` do not exist.

- [ ] **Step 3: Create the `Venue` model**

```java
package io.github.miro93.sportmonks.football.model;

/// A venue (stadium) from the SportMonks football API. {@code id} is always
/// present; every other field may be {@code null}. {@code cityId} is typed as
/// {@code String} because the API returns {@code city_id} as a string value
/// (same as {@link Coach}); {@code latitude}/{@code longitude} are also strings.
public record Venue(
        long id,
        Long countryId,
        String cityId,
        String name,
        String address,
        String zipcode,
        String latitude,
        String longitude,
        Integer capacity,
        String imagePath,
        String cityName,
        String surface,
        Boolean nationalTeam) {
}
```

- [ ] **Step 4: Create the `VenuesEndpoint`**

```java
package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.core.request.SingleResourceRequest;
import io.github.miro93.sportmonks.football.model.Venue;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks {@code /venues} endpoints.
public final class VenuesEndpoint {

    private final ApiExecutor executor;
    private final DataType<Venue> single;
    private final DataType<List<Venue>> list;

    /// Creates the endpoint, building the {@link Venue} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the single/list response types
    public VenuesEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.single = codec.type(Venue.class);
        this.list = codec.listType(Venue.class);
    }

    /// Requests every venue, paginated.
    ///
    /// @return a collection request for all venues
    public CollectionRequest<Venue> all() {
        return collection("venues");
    }

    /// Requests a single venue by its id.
    ///
    /// @param id the venue id
    /// @return a single-resource request for that venue
    public SingleResourceRequest<Venue> byId(long id) {
        return new SingleResourceRequest<>(executor, RequestSpec.builder("venues/" + id), single);
    }

    /// Requests all venues for a given season.
    ///
    /// @param seasonId the season id to filter by
    /// @return a collection request for the matching venues
    public CollectionRequest<Venue> bySeason(long seasonId) {
        return collection("venues/seasons/" + seasonId);
    }

    /// Searches venues by name.
    ///
    /// @param name the search term (must not be {@code null})
    /// @return a collection request for the matching venues
    /// @throws NullPointerException if {@code name} is {@code null}
    public CollectionRequest<Venue> search(String name) {
        Objects.requireNonNull(name, "name");
        return collection("venues/search/" + name);
    }

    private CollectionRequest<Venue> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `./gradlew :football:test --tests "io.github.miro93.sportmonks.football.endpoint.VenuesEndpointTest"`
Expected: PASS (5 tests).

- [ ] **Step 6: Commit**

```bash
git add football/src/main/java/io/github/miro93/sportmonks/football/model/Venue.java \
        football/src/main/java/io/github/miro93/sportmonks/football/endpoint/VenuesEndpoint.java \
        football/src/test/java/io/github/miro93/sportmonks/football/endpoint/VenuesEndpointTest.java
git commit -m "feat(football): add Venues endpoint and model"
```

---

## Task 3: Referees (model + endpoint, clone of Coach, with byCountry + bySeason + search)

**Files:**
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/model/Referee.java`
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/endpoint/RefereesEndpoint.java`
- Test: `football/src/test/java/io/github/miro93/sportmonks/football/endpoint/RefereesEndpointTest.java`

- [ ] **Step 1: Write the failing test**

```java
package io.github.miro93.sportmonks.football.endpoint;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.football.model.Referee;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest
class RefereesEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private RefereesEndpoint referees(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new RefereesEndpoint(executor, codec);
    }

    @Test
    void allHitsRefereesRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/referees")).willReturn(okJson("""
                { "data": [ { "id": 14, "name": "John Beaton" } ] }
                """)));

        var response = referees(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().name()).isEqualTo("John Beaton");
    }

    @Test
    void byIdHitsTheCorrectPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlEqualTo("/referees/14")).willReturn(okJson("""
                { "data": { "id": 14, "name": "John Beaton", "country_id": 1161 } }
                """)));

        Referee referee = referees(wm.getHttpBaseUrl()).byId(14L).get().data();

        assertThat(referee.id()).isEqualTo(14L);
        assertThat(referee.countryId()).isEqualTo(1161L);
    }

    @Test
    void byCountryHitsCountriesPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/referees/countries/1161")).willReturn(okJson("""
                { "data": [ { "id": 14, "name": "John Beaton" } ] }
                """)));

        var response = referees(wm.getHttpBaseUrl()).byCountry(1161L).get();

        assertThat(response.data()).hasSize(1);
    }

    @Test
    void bySeasonHitsSeasonsPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/referees/seasons/19735")).willReturn(okJson("""
                { "data": [ { "id": 14, "name": "John Beaton" } ] }
                """)));

        var response = referees(wm.getHttpBaseUrl()).bySeason(19735L).get();

        assertThat(response.data()).hasSize(1);
    }

    @Test
    void searchHitsSearchPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/referees/search/Beaton")).willReturn(okJson("""
                { "data": [ { "id": 14, "name": "John Beaton" } ] }
                """)));

        var response = referees(wm.getHttpBaseUrl()).search("Beaton").get();

        assertThat(response.data()).hasSize(1);
    }

    @Test
    void searchRejectsNull(WireMockRuntimeInfo wm) {
        assertThatThrownBy(() -> referees(wm.getHttpBaseUrl()).search(null))
                .isInstanceOf(NullPointerException.class);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :football:test --tests "io.github.miro93.sportmonks.football.endpoint.RefereesEndpointTest"`
Expected: COMPILE FAILURE — `Referee` / `RefereesEndpoint` do not exist.

- [ ] **Step 3: Create the `Referee` model** (mirror of `Coach`)

```java
package io.github.miro93.sportmonks.football.model;

/// A match referee from the SportMonks football API. {@code id} is always
/// present; every other field may be {@code null}. {@code cityId} is typed as
/// {@code String} because the API returns {@code city_id} as a string value
/// (same as {@link Coach}).
public record Referee(
        long id,
        Long sportId,
        Long countryId,
        String cityId,
        String commonName,
        String firstname,
        String lastname,
        String name,
        String displayName,
        String imagePath,
        Integer height,
        Integer weight,
        String dateOfBirth,
        String gender) {
}
```

- [ ] **Step 4: Create the `RefereesEndpoint`**

```java
package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.core.request.SingleResourceRequest;
import io.github.miro93.sportmonks.football.model.Referee;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks {@code /referees} endpoints.
public final class RefereesEndpoint {

    private final ApiExecutor executor;
    private final DataType<Referee> single;
    private final DataType<List<Referee>> list;

    /// Creates the endpoint, building the {@link Referee} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the single/list response types
    public RefereesEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.single = codec.type(Referee.class);
        this.list = codec.listType(Referee.class);
    }

    /// Requests every referee, paginated.
    ///
    /// @return a collection request for all referees
    public CollectionRequest<Referee> all() {
        return collection("referees");
    }

    /// Requests a single referee by its id.
    ///
    /// @param id the referee id
    /// @return a single-resource request for that referee
    public SingleResourceRequest<Referee> byId(long id) {
        return new SingleResourceRequest<>(executor, RequestSpec.builder("referees/" + id), single);
    }

    /// Requests all referees for a given country.
    ///
    /// @param countryId the country id to filter by
    /// @return a collection request for the matching referees
    public CollectionRequest<Referee> byCountry(long countryId) {
        return collection("referees/countries/" + countryId);
    }

    /// Requests all referees for a given season.
    ///
    /// @param seasonId the season id to filter by
    /// @return a collection request for the matching referees
    public CollectionRequest<Referee> bySeason(long seasonId) {
        return collection("referees/seasons/" + seasonId);
    }

    /// Searches referees by name.
    ///
    /// @param name the search term (must not be {@code null})
    /// @return a collection request for the matching referees
    /// @throws NullPointerException if {@code name} is {@code null}
    public CollectionRequest<Referee> search(String name) {
        Objects.requireNonNull(name, "name");
        return collection("referees/search/" + name);
    }

    private CollectionRequest<Referee> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `./gradlew :football:test --tests "io.github.miro93.sportmonks.football.endpoint.RefereesEndpointTest"`
Expected: PASS (6 tests).

- [ ] **Step 6: Commit**

```bash
git add football/src/main/java/io/github/miro93/sportmonks/football/model/Referee.java \
        football/src/main/java/io/github/miro93/sportmonks/football/endpoint/RefereesEndpoint.java \
        football/src/test/java/io/github/miro93/sportmonks/football/endpoint/RefereesEndpointTest.java
git commit -m "feat(football): add Referees endpoint and model"
```

---

## Task 4: TV Stations (model + endpoint, with byFixture)

**Files:**
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/model/TvStation.java`
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/endpoint/TvStationsEndpoint.java`
- Test: `football/src/test/java/io/github/miro93/sportmonks/football/endpoint/TvStationsEndpointTest.java`

- [ ] **Step 1: Write the failing test**

```java
package io.github.miro93.sportmonks.football.endpoint;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.football.model.TvStation;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class TvStationsEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private TvStationsEndpoint tvStations(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new TvStationsEndpoint(executor, codec);
    }

    @Test
    void allHitsTvStationsRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/tv-stations")).willReturn(okJson("""
                { "data": [ { "id": 5, "name": "Sky Sports", "url": "https://sky.com", "image_path": "https://cdn/sky.png" } ] }
                """)));

        var response = tvStations(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().name()).isEqualTo("Sky Sports");
    }

    @Test
    void byIdHitsTheCorrectPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlEqualTo("/tv-stations/5")).willReturn(okJson("""
                { "data": { "id": 5, "name": "Sky Sports" } }
                """)));

        TvStation station = tvStations(wm.getHttpBaseUrl()).byId(5L).get().data();

        assertThat(station.id()).isEqualTo(5L);
        assertThat(station.name()).isEqualTo("Sky Sports");
    }

    @Test
    void byFixtureHitsFixturesPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/tv-stations/fixtures/18535517")).willReturn(okJson("""
                { "data": [ { "id": 5, "name": "Sky Sports" } ] }
                """)));

        var response = tvStations(wm.getHttpBaseUrl()).byFixture(18535517L).get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().id()).isEqualTo(5L);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :football:test --tests "io.github.miro93.sportmonks.football.endpoint.TvStationsEndpointTest"`
Expected: COMPILE FAILURE — `TvStation` / `TvStationsEndpoint` do not exist.

- [ ] **Step 3: Create the `TvStation` model**

```java
package io.github.miro93.sportmonks.football.model;

/// A TV station from the SportMonks football API. {@code id} is always present;
/// {@code name}, {@code url} and {@code imagePath} may be {@code null}.
public record TvStation(
        long id,
        String name,
        String url,
        String imagePath) {
}
```

- [ ] **Step 4: Create the `TvStationsEndpoint`**

```java
package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.core.request.SingleResourceRequest;
import io.github.miro93.sportmonks.football.model.TvStation;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks {@code /tv-stations} endpoints.
public final class TvStationsEndpoint {

    private final ApiExecutor executor;
    private final DataType<TvStation> single;
    private final DataType<List<TvStation>> list;

    /// Creates the endpoint, building the {@link TvStation} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the single/list response types
    public TvStationsEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.single = codec.type(TvStation.class);
        this.list = codec.listType(TvStation.class);
    }

    /// Requests every TV station, paginated.
    ///
    /// @return a collection request for all TV stations
    public CollectionRequest<TvStation> all() {
        return collection("tv-stations");
    }

    /// Requests a single TV station by its id.
    ///
    /// @param id the TV station id
    /// @return a single-resource request for that TV station
    public SingleResourceRequest<TvStation> byId(long id) {
        return new SingleResourceRequest<>(executor, RequestSpec.builder("tv-stations/" + id), single);
    }

    /// Requests all TV stations broadcasting a given fixture.
    ///
    /// @param fixtureId the fixture id to filter by
    /// @return a collection request for the matching TV stations
    public CollectionRequest<TvStation> byFixture(long fixtureId) {
        return collection("tv-stations/fixtures/" + fixtureId);
    }

    private CollectionRequest<TvStation> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `./gradlew :football:test --tests "io.github.miro93.sportmonks.football.endpoint.TvStationsEndpointTest"`
Expected: PASS (3 tests).

- [ ] **Step 6: Commit**

```bash
git add football/src/main/java/io/github/miro93/sportmonks/football/model/TvStation.java \
        football/src/main/java/io/github/miro93/sportmonks/football/endpoint/TvStationsEndpoint.java \
        football/src/test/java/io/github/miro93/sportmonks/football/endpoint/TvStationsEndpointTest.java
git commit -m "feat(football): add TV Stations endpoint and model"
```

---

## Task 5: Commentaries (model + endpoint, String id, byFixture, NO byId)

**Files:**
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/model/Commentary.java`
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/endpoint/CommentariesEndpoint.java`
- Test: `football/src/test/java/io/github/miro93/sportmonks/football/endpoint/CommentariesEndpointTest.java`

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
class CommentariesEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private CommentariesEndpoint commentaries(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new CommentariesEndpoint(executor, codec);
    }

    @Test
    void allHitsCommentariesRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/commentaries")).willReturn(okJson("""
                { "data": [ { "id": "abc1", "fixture_id": 18535517, "comment": "Kick-off", "minute": 1, "is_goal": false } ] }
                """)));

        var response = commentaries(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().id()).isEqualTo("abc1");
        assertThat(response.data().getFirst().fixtureId()).isEqualTo(18535517L);
    }

    @Test
    void byFixtureHitsFixturesPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/commentaries/fixtures/18535517")).willReturn(okJson("""
                { "data": [ { "id": "abc1", "fixture_id": 18535517, "comment": "Goal!", "is_goal": true, "minute": 23 } ] }
                """)));

        var response = commentaries(wm.getHttpBaseUrl()).byFixture(18535517L).get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().isGoal()).isTrue();
        assertThat(response.data().getFirst().minute()).isEqualTo(23);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :football:test --tests "io.github.miro93.sportmonks.football.endpoint.CommentariesEndpointTest"`
Expected: COMPILE FAILURE — `Commentary` / `CommentariesEndpoint` do not exist.

- [ ] **Step 3: Create the `Commentary` model** (note: `id` is `String`)

```java
package io.github.miro93.sportmonks.football.model;

/// A textual match commentary line from the SportMonks football API. Unlike
/// every other resource in this client, {@code id} is a {@code String} (the API
/// types the commentary id as a string). Aside from {@code id}, every field may
/// be {@code null}: {@code fixtureId}, {@code comment}, {@code minute},
/// {@code extraMinute}, {@code isGoal}, {@code isImportant} and {@code order}.
public record Commentary(
        String id,
        Long fixtureId,
        String comment,
        Integer minute,
        Integer extraMinute,
        Boolean isGoal,
        Boolean isImportant,
        Integer order) {
}
```

- [ ] **Step 4: Create the `CommentariesEndpoint`** (no `byId`)

```java
package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.football.model.Commentary;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks {@code /commentaries} endpoints.
public final class CommentariesEndpoint {

    private final ApiExecutor executor;
    private final DataType<List<Commentary>> list;

    /// Creates the endpoint, building the {@link Commentary} list decoder from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the list response type
    public CommentariesEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.list = codec.listType(Commentary.class);
    }

    /// Requests every commentary, paginated.
    ///
    /// @return a collection request for all commentaries
    public CollectionRequest<Commentary> all() {
        return collection("commentaries");
    }

    /// Requests all commentaries for a given fixture.
    ///
    /// @param fixtureId the fixture id to filter by
    /// @return a collection request for the matching commentaries
    public CollectionRequest<Commentary> byFixture(long fixtureId) {
        return collection("commentaries/fixtures/" + fixtureId);
    }

    private CollectionRequest<Commentary> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `./gradlew :football:test --tests "io.github.miro93.sportmonks.football.endpoint.CommentariesEndpointTest"`
Expected: PASS (2 tests).

- [ ] **Step 6: Commit**

```bash
git add football/src/main/java/io/github/miro93/sportmonks/football/model/Commentary.java \
        football/src/main/java/io/github/miro93/sportmonks/football/endpoint/CommentariesEndpoint.java \
        football/src/test/java/io/github/miro93/sportmonks/football/endpoint/CommentariesEndpointTest.java
git commit -m "feat(football): add Commentaries endpoint and model"
```

---

## Task 6: Decoding tests (nullable coverage + convention deviations)

Pure-decode tests pinning field mappings — all-scalars-present + all-optional-absent — and the flagged deviations (`Commentary.id` String, `Venue.cityId`/`Referee.cityId` String, booleans `is_goal`/`is_important`). **If a field assertion fails, fix the model + the matching endpoint test, then update the spec's "confirm in TDD" note.**

**Files:**
- Test: `football/src/test/java/io/github/miro93/sportmonks/football/model/VenueRefereeDecodingTest.java`
- Test: `football/src/test/java/io/github/miro93/sportmonks/football/model/StateTvCommentaryDecodingTest.java`

- [ ] **Step 1: Write `VenueRefereeDecodingTest`**

```java
package io.github.miro93.sportmonks.football.model;

import io.github.miro93.sportmonks.core.json.JacksonCodec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VenueRefereeDecodingTest {

    private final JacksonCodec codec = new JacksonCodec();

    @Test
    void decodesVenueWithAllScalars() {
        String json = """
                {
                  "data": {
                    "id": 8909,
                    "country_id": 1161,
                    "city_id": "51663",
                    "name": "Celtic Park",
                    "address": "Kerrydale Street",
                    "zipcode": "G40 3RE",
                    "latitude": "55.849689",
                    "longitude": "-4.205518",
                    "capacity": 60411,
                    "image_path": "https://cdn.sportmonks.com/images/soccer/venues/8909.png",
                    "city_name": "Glasgow",
                    "surface": "grass",
                    "national_team": false
                  }
                }
                """;

        Venue venue = codec.decode(json, codec.type(Venue.class)).data();

        assertThat(venue.id()).isEqualTo(8909L);
        assertThat(venue.countryId()).isEqualTo(1161L);
        assertThat(venue.cityId()).isEqualTo("51663");
        assertThat(venue.name()).isEqualTo("Celtic Park");
        assertThat(venue.address()).isEqualTo("Kerrydale Street");
        assertThat(venue.zipcode()).isEqualTo("G40 3RE");
        assertThat(venue.latitude()).isEqualTo("55.849689");
        assertThat(venue.longitude()).isEqualTo("-4.205518");
        assertThat(venue.capacity()).isEqualTo(60411);
        assertThat(venue.imagePath()).contains("8909.png");
        assertThat(venue.cityName()).isEqualTo("Glasgow");
        assertThat(venue.surface()).isEqualTo("grass");
        assertThat(venue.nationalTeam()).isFalse();
    }

    @Test
    void decodesVenueWithOptionalFieldsAbsent() {
        String json = """
                { "data": { "id": 8909, "name": "Celtic Park" } }
                """;

        Venue venue = codec.decode(json, codec.type(Venue.class)).data();

        assertThat(venue.id()).isEqualTo(8909L);
        assertThat(venue.name()).isEqualTo("Celtic Park");
        assertThat(venue.countryId()).isNull();
        assertThat(venue.cityId()).isNull();
        assertThat(venue.address()).isNull();
        assertThat(venue.zipcode()).isNull();
        assertThat(venue.latitude()).isNull();
        assertThat(venue.longitude()).isNull();
        assertThat(venue.capacity()).isNull();
        assertThat(venue.imagePath()).isNull();
        assertThat(venue.cityName()).isNull();
        assertThat(venue.surface()).isNull();
        assertThat(venue.nationalTeam()).isNull();
    }

    @Test
    void decodesRefereeWithAllScalars() {
        String json = """
                {
                  "data": {
                    "id": 14,
                    "sport_id": 1,
                    "country_id": 1161,
                    "city_id": "51663",
                    "common_name": "J. Beaton",
                    "firstname": "John",
                    "lastname": "Beaton",
                    "name": "John Beaton",
                    "display_name": "John Beaton",
                    "image_path": "https://cdn.sportmonks.com/images/soccer/referees/14.png",
                    "height": 180,
                    "weight": 75,
                    "date_of_birth": "1982-09-22",
                    "gender": "male"
                  }
                }
                """;

        Referee referee = codec.decode(json, codec.type(Referee.class)).data();

        assertThat(referee.id()).isEqualTo(14L);
        assertThat(referee.sportId()).isEqualTo(1L);
        assertThat(referee.countryId()).isEqualTo(1161L);
        assertThat(referee.cityId()).isEqualTo("51663");
        assertThat(referee.commonName()).isEqualTo("J. Beaton");
        assertThat(referee.firstname()).isEqualTo("John");
        assertThat(referee.lastname()).isEqualTo("Beaton");
        assertThat(referee.name()).isEqualTo("John Beaton");
        assertThat(referee.displayName()).isEqualTo("John Beaton");
        assertThat(referee.imagePath()).contains("14.png");
        assertThat(referee.height()).isEqualTo(180);
        assertThat(referee.weight()).isEqualTo(75);
        assertThat(referee.dateOfBirth()).isEqualTo("1982-09-22");
        assertThat(referee.gender()).isEqualTo("male");
    }

    @Test
    void decodesRefereeWithOptionalFieldsAbsent() {
        String json = """
                { "data": { "id": 14, "name": "John Beaton" } }
                """;

        Referee referee = codec.decode(json, codec.type(Referee.class)).data();

        assertThat(referee.id()).isEqualTo(14L);
        assertThat(referee.name()).isEqualTo("John Beaton");
        assertThat(referee.sportId()).isNull();
        assertThat(referee.countryId()).isNull();
        assertThat(referee.cityId()).isNull();
        assertThat(referee.commonName()).isNull();
        assertThat(referee.firstname()).isNull();
        assertThat(referee.lastname()).isNull();
        assertThat(referee.displayName()).isNull();
        assertThat(referee.imagePath()).isNull();
        assertThat(referee.height()).isNull();
        assertThat(referee.weight()).isNull();
        assertThat(referee.dateOfBirth()).isNull();
        assertThat(referee.gender()).isNull();
    }
}
```

- [ ] **Step 2: Write `StateTvCommentaryDecodingTest`**

```java
package io.github.miro93.sportmonks.football.model;

import io.github.miro93.sportmonks.core.json.JacksonCodec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StateTvCommentaryDecodingTest {

    private final JacksonCodec codec = new JacksonCodec();

    @Test
    void decodesState() {
        String json = """
                { "data": { "id": 5, "state": "FT", "name": "Full Time", "short_name": "FT", "developer_name": "FT" } }
                """;

        State state = codec.decode(json, codec.type(State.class)).data();

        assertThat(state.id()).isEqualTo(5L);
        assertThat(state.state()).isEqualTo("FT");
        assertThat(state.name()).isEqualTo("Full Time");
        assertThat(state.shortName()).isEqualTo("FT");
        assertThat(state.developerName()).isEqualTo("FT");
    }

    @Test
    void decodesTvStationWithNullableUrlAndImage() {
        String json = """
                { "data": { "id": 5, "name": "Sky Sports", "url": null, "image_path": null } }
                """;

        TvStation station = codec.decode(json, codec.type(TvStation.class)).data();

        assertThat(station.id()).isEqualTo(5L);
        assertThat(station.name()).isEqualTo("Sky Sports");
        assertThat(station.url()).isNull();
        assertThat(station.imagePath()).isNull();
    }

    @Test
    void decodesCommentaryWithStringIdAndBooleans() {
        String json = """
                {
                  "data": {
                    "id": "c-9981",
                    "fixture_id": 18535517,
                    "comment": "GOAL! What a strike.",
                    "minute": 23,
                    "extra_minute": 2,
                    "is_goal": true,
                    "is_important": true,
                    "order": 45
                  }
                }
                """;

        Commentary commentary = codec.decode(json, codec.type(Commentary.class)).data();

        assertThat(commentary.id()).isEqualTo("c-9981");
        assertThat(commentary.fixtureId()).isEqualTo(18535517L);
        assertThat(commentary.comment()).isEqualTo("GOAL! What a strike.");
        assertThat(commentary.minute()).isEqualTo(23);
        assertThat(commentary.extraMinute()).isEqualTo(2);
        assertThat(commentary.isGoal()).isTrue();
        assertThat(commentary.isImportant()).isTrue();
        assertThat(commentary.order()).isEqualTo(45);
    }

    @Test
    void decodesCommentaryWithOptionalFieldsAbsent() {
        String json = """
                { "data": { "id": "c-1" } }
                """;

        Commentary commentary = codec.decode(json, codec.type(Commentary.class)).data();

        assertThat(commentary.id()).isEqualTo("c-1");
        assertThat(commentary.fixtureId()).isNull();
        assertThat(commentary.comment()).isNull();
        assertThat(commentary.minute()).isNull();
        assertThat(commentary.extraMinute()).isNull();
        assertThat(commentary.isGoal()).isNull();
        assertThat(commentary.isImportant()).isNull();
        assertThat(commentary.order()).isNull();
    }
}
```

- [ ] **Step 3: Run both tests**

Run: `./gradlew :football:test --tests "io.github.miro93.sportmonks.football.model.VenueRefereeDecodingTest" --tests "io.github.miro93.sportmonks.football.model.StateTvCommentaryDecodingTest"`
Expected: PASS (8 tests). If `is_goal`/`is_important` don't decode into `isGoal()`/`isImportant()`, or `cityId`/`Commentary.id` don't decode, fix the model and re-run.

- [ ] **Step 4: Commit**

```bash
git add football/src/test/java/io/github/miro93/sportmonks/football/model/VenueRefereeDecodingTest.java \
        football/src/test/java/io/github/miro93/sportmonks/football/model/StateTvCommentaryDecodingTest.java
git commit -m "test(football): add referential decoding tests (venue/referee/state/tv/commentary)"
```

---

## Task 7: Wire 5 endpoints into FootballClient

Add the 5 new endpoints to `FootballClient`, keeping the `core` field/accessor (M7) LAST.

**Files:**
- Modify: `football/src/main/java/io/github/miro93/sportmonks/football/FootballClient.java`
- Test: `football/src/test/java/io/github/miro93/sportmonks/football/FootballClientM8Test.java`

- [ ] **Step 1: Write the failing `FootballClientM8Test`**

```java
package io.github.miro93.sportmonks.football;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class FootballClientM8Test {

    private FootballClient client(String baseUrl) {
        return FootballClient.builder()
                .apiToken(ApiToken.of("tok"))
                .baseUrl(baseUrl)
                .build();
    }

    @Test
    void exposesM8Endpoints(WireMockRuntimeInfo wm) {
        var client = client(wm.getHttpBaseUrl());
        assertThat(client.states()).isNotNull();
        assertThat(client.venues()).isNotNull();
        assertThat(client.referees()).isNotNull();
        assertThat(client.tvStations()).isNotNull();
        assertThat(client.commentaries()).isNotNull();
    }

    @Test
    void venuesBySeasonDecodesAndSendsAuth(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/venues/seasons/19735")).willReturn(okJson("""
                { "data": [{ "id": 8909, "name": "Celtic Park" }] }
                """)));

        var venues = client(wm.getHttpBaseUrl()).venues().bySeason(19735L).get().data();

        assertThat(venues).hasSize(1);
        assertThat(venues.getFirst().id()).isEqualTo(8909L);
        verify(getRequestedFor(urlPathEqualTo("/venues/seasons/19735"))
                .withHeader("Authorization", equalTo("tok")));
    }

    @Test
    void commentariesByFixtureDecodesAndSendsAuth(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/commentaries/fixtures/18535517")).willReturn(okJson("""
                { "data": [{ "id": "c-1", "fixture_id": 18535517, "comment": "Kick-off" }] }
                """)));

        var comments = client(wm.getHttpBaseUrl()).commentaries().byFixture(18535517L).get().data();

        assertThat(comments).hasSize(1);
        assertThat(comments.getFirst().id()).isEqualTo("c-1");
        verify(getRequestedFor(urlPathEqualTo("/commentaries/fixtures/18535517"))
                .withHeader("Authorization", equalTo("tok")));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :football:test --tests "io.github.miro93.sportmonks.football.FootballClientM8Test"`
Expected: COMPILE FAILURE — `states()`/`venues()`/`referees()`/`tvStations()`/`commentaries()` do not exist.

- [ ] **Step 3: Add the 5 endpoint fields**

In `FootballClient.java`, after the `topscorers` field and BEFORE `private final CoreClient core;`, add:

```java
    private final StatesEndpoint states;
    private final VenuesEndpoint venues;
    private final RefereesEndpoint referees;
    private final TvStationsEndpoint tvStations;
    private final CommentariesEndpoint commentaries;
```

- [ ] **Step 4: Add the 5 constructor parameters + assignments**

Change the private constructor so the 5 new params come after `TopscorersEndpoint topscorers` and BEFORE `CoreClient core`:

```java
            TopscorersEndpoint topscorers,
            StatesEndpoint states,
            VenuesEndpoint venues,
            RefereesEndpoint referees,
            TvStationsEndpoint tvStations,
            CommentariesEndpoint commentaries,
            CoreClient core) {
```

And add the assignments after `this.topscorers = topscorers;` and before `this.core = core;`:

```java
        this.states = states;
        this.venues = venues;
        this.referees = referees;
        this.tvStations = tvStations;
        this.commentaries = commentaries;
```

- [ ] **Step 5: Add the 5 accessors**

After the `topscorers()` accessor and before the `core()` accessor, add:

```java
    /// Returns the states endpoint.
    ///
    /// @return the {@code /states} endpoint accessor
    public StatesEndpoint states() {
        return states;
    }

    /// Returns the venues endpoint.
    ///
    /// @return the {@code /venues} endpoint accessor
    public VenuesEndpoint venues() {
        return venues;
    }

    /// Returns the referees endpoint.
    ///
    /// @return the {@code /referees} endpoint accessor
    public RefereesEndpoint referees() {
        return referees;
    }

    /// Returns the TV stations endpoint.
    ///
    /// @return the {@code /tv-stations} endpoint accessor
    public TvStationsEndpoint tvStations() {
        return tvStations;
    }

    /// Returns the commentaries endpoint.
    ///
    /// @return the {@code /commentaries} endpoint accessor
    public CommentariesEndpoint commentaries() {
        return commentaries;
    }
```

- [ ] **Step 6: Add the imports**

Add alongside the other `football.endpoint.*` imports:

```java
import io.github.miro93.sportmonks.football.endpoint.CommentariesEndpoint;
import io.github.miro93.sportmonks.football.endpoint.RefereesEndpoint;
import io.github.miro93.sportmonks.football.endpoint.StatesEndpoint;
import io.github.miro93.sportmonks.football.endpoint.TvStationsEndpoint;
import io.github.miro93.sportmonks.football.endpoint.VenuesEndpoint;
```

- [ ] **Step 7: Wire the endpoints in `build()`**

In the `return new FootballClient(...)` call, after `new TopscorersEndpoint(executor, codec),` and BEFORE `core)`, add:

```java
                    new TopscorersEndpoint(executor, codec),
                    new StatesEndpoint(executor, codec),
                    new VenuesEndpoint(executor, codec),
                    new RefereesEndpoint(executor, codec),
                    new TvStationsEndpoint(executor, codec),
                    new CommentariesEndpoint(executor, codec),
                    core);
```

- [ ] **Step 8: Run test to verify it passes**

Run: `./gradlew :football:test --tests "io.github.miro93.sportmonks.football.FootballClientM8Test"`
Expected: PASS (3 tests). Then run the full football suite to confirm nothing broke:
Run: `./gradlew :football:test`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 9: Commit**

```bash
git add football/src/main/java/io/github/miro93/sportmonks/football/FootballClient.java \
        football/src/test/java/io/github/miro93/sportmonks/football/FootballClientM8Test.java
git commit -m "feat(football): wire States/Venues/Referees/TvStations/Commentaries into FootballClient"
```

---

## Task 8: README + full suite verification

**Files:**
- Modify: `README.md`

- [ ] **Step 1: Add the 5 resources to the football endpoints table in README**

Locate the football "Available Endpoints" table (the one with "All endpoints are accessed via `FootballClient`"). Add 5 rows matching the existing table's columns (accessor, endpoint class, example methods):

```
| `states()`       | `StatesEndpoint`       | `all()`, `byId(id)` |
| `venues()`       | `VenuesEndpoint`       | `all()`, `byId(id)`, `bySeason(id)`, `search(name)` |
| `referees()`     | `RefereesEndpoint`     | `all()`, `byId(id)`, `byCountry(id)`, `bySeason(id)`, `search(name)` |
| `tvStations()`   | `TvStationsEndpoint`   | `all()`, `byId(id)`, `byFixture(id)` |
| `commentaries()` | `CommentariesEndpoint` | `all()`, `byFixture(id)` |
```

(Match the exact column layout/separators of the existing table — inspect it first and adapt the row format to fit.)

- [ ] **Step 2: Run the full test suite**

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL. New M8 tests: States 2, Venues 5, Referees 6, TvStations 3, Commentaries 2 (18 endpoint), 8 decoding, 3 `FootballClientM8Test` — all in `:football`. No failures.

- [ ] **Step 3: Commit**

```bash
git add README.md
git commit -m "docs: document football referential endpoints (M8)"
```

---

## Definition of Done

- [ ] 5 models + 5 endpoints created under `football.{model,endpoint}`, each with `///` JavaDoc.
- [ ] `FootballClient` exposes `states()`/`venues()`/`referees()`/`tvStations()`/`commentaries()`; `core()` + 14 prior endpoints unchanged; `core` stays LAST in the constructor.
- [ ] All endpoint paths verified by WireMock tests; nullable decoding verified for every model.
- [ ] Convention deviations pinned in Task 6: `Commentary.id` String, `Venue.cityId`/`Referee.cityId` String, booleans `is_goal`/`is_important`.
- [ ] `./gradlew test` green; README documents the 5 football referential endpoints.
- [ ] Conventional `feat:`/`test:`/`docs:` commits so release-please updates the CHANGELOG.
- [ ] Out of scope (deferred): `tv-stations/fixtures` join fields, typed includes.
```
