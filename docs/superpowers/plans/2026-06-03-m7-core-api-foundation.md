# M7 — Core API Foundation (CoreClient + géo + Types) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add the SportMonks **Core API** (`/v3/core`) cross-sport resources — Continents, Countries, Regions, Cities, Types — as model records + endpoint classes + a new `CoreClient`, and expose them from `FootballClient.core()`.

**Architecture:** New packages `io.github.miro93.sportmonks.core.coreapi` (`CoreClient`), `.coreapi.model`, `.coreapi.endpoint` **inside the existing `core` module**. Endpoints are exact clones of `football`'s `CoachesEndpoint` pattern, reusing the unchanged infra (`CollectionRequest`/`SingleResourceRequest`/`RequestSpec`/`ApiExecutor`/`JacksonCodec`). `CoreClient` mirrors `FootballClient`; `FootballClient.build()` creates a second `ApiExecutor` (Core base URL, same transport/codec/token) and embeds a `CoreClient`.

**Tech Stack:** Java 25, Gradle multi-module, Jackson Blackbird (snake_case), JUnit 5 + WireMock + AssertJ. No new dependencies (`core/build.gradle.kts` already has wiremock/assertj/junit in test scope).

**Source of truth:** `docs/superpowers/specs/2026-06-03-m7-core-api-foundation-design.md`. Field names from the Core API `entities` doc; **a few are flagged "confirm in TDD"** (`Country.borders` vs `border`, `City.region` vs `region_id`). The decoding-test task (Task 6) is where those get pinned to the real payload shape.

**Conventions (from M4/M5):** only `id` is primitive `long`; every other scalar is a BOXED nullable type. Each record/endpoint/client type carries `///` JavaDoc (JEP 467) — keep ≥80% coverage (CodeRabbit gate). Type-level docs must state `id` always-present, everything else nullable.

---

## File Structure

```
core/src/main/java/io/github/miro93/sportmonks/core/coreapi/
├── CoreClient.java                          # NEW (Task 7)
├── model/
│   ├── Continent.java                       # NEW (Task 1)
│   ├── Country.java                         # NEW (Task 2)
│   ├── Region.java                          # NEW (Task 3)
│   ├── City.java                            # NEW (Task 4)
│   └── Type.java                            # NEW (Task 5)
└── endpoint/
    ├── ContinentsEndpoint.java              # NEW (Task 1)
    ├── CountriesEndpoint.java               # NEW (Task 2)
    ├── RegionsEndpoint.java                 # NEW (Task 3)
    ├── CitiesEndpoint.java                  # NEW (Task 4)
    └── TypesEndpoint.java                   # NEW (Task 5)
core/src/test/java/io/github/miro93/sportmonks/core/coreapi/
├── CoreClientTest.java                      # NEW (Task 7)
├── model/
│   ├── GeographyDecodingTest.java           # NEW (Task 6)
│   └── TypeDecodingTest.java                # NEW (Task 6)
└── endpoint/
    ├── ContinentsEndpointTest.java          # NEW (Task 1)
    ├── CountriesEndpointTest.java           # NEW (Task 2)
    ├── RegionsEndpointTest.java             # NEW (Task 3)
    ├── CitiesEndpointTest.java              # NEW (Task 4)
    └── TypesEndpointTest.java               # NEW (Task 5)
football/src/main/java/io/github/miro93/sportmonks/football/
└── FootballClient.java                      # MODIFY (Task 8)
football/src/test/java/io/github/miro93/sportmonks/football/
└── FootballClientCoreTest.java              # NEW (Task 8)
README.md                                    # MODIFY (Task 9)
```

**Build/test commands** (run from repo root):
- One class: `./gradlew :core:test --tests "io.github.miro93.sportmonks.core.coreapi.endpoint.ContinentsEndpointTest"`
- Whole suite: `./gradlew test`

---

## Task 1: Continents (model + endpoint)

**Files:**
- Create: `core/src/main/java/io/github/miro93/sportmonks/core/coreapi/model/Continent.java`
- Create: `core/src/main/java/io/github/miro93/sportmonks/core/coreapi/endpoint/ContinentsEndpoint.java`
- Test: `core/src/test/java/io/github/miro93/sportmonks/core/coreapi/endpoint/ContinentsEndpointTest.java`

- [ ] **Step 1: Write the failing endpoint test**

```java
package io.github.miro93.sportmonks.core.coreapi.endpoint;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.coreapi.model.Continent;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class ContinentsEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private ContinentsEndpoint continents(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new ContinentsEndpoint(executor, codec);
    }

    @Test
    void allHitsContinentsRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/continents")).willReturn(okJson("""
                { "data": [ { "id": 1, "name": "Europe", "code": "EU" } ] }
                """)));

        var response = continents(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().name()).isEqualTo("Europe");
        assertThat(response.data().getFirst().code()).isEqualTo("EU");
    }

    @Test
    void byIdHitsTheCorrectPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlEqualTo("/continents/1")).willReturn(okJson("""
                { "data": { "id": 1, "name": "Europe", "code": "EU" } }
                """)));

        Continent continent = continents(wm.getHttpBaseUrl()).byId(1L).get().data();

        assertThat(continent.id()).isEqualTo(1L);
        assertThat(continent.name()).isEqualTo("Europe");
    }

    @Test
    void byIdWithIncludesHitsCorrectPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlEqualTo("/continents/1?include=countries")).willReturn(okJson("""
                { "data": { "id": 1, "name": "Europe", "code": "EU" } }
                """)));

        Continent continent = continents(wm.getHttpBaseUrl()).byId(1L).include("countries").get().data();

        assertThat(continent.id()).isEqualTo(1L);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :core:test --tests "io.github.miro93.sportmonks.core.coreapi.endpoint.ContinentsEndpointTest"`
Expected: COMPILE FAILURE — `Continent` and `ContinentsEndpoint` do not exist.

- [ ] **Step 3: Create the `Continent` model**

```java
package io.github.miro93.sportmonks.core.coreapi.model;

/// A geographic continent from the SportMonks Core API. {@code id} is always
/// present; {@code name} and {@code code} may be {@code null}.
public record Continent(long id, String name, String code) {
}
```

- [ ] **Step 4: Create the `ContinentsEndpoint`**

```java
package io.github.miro93.sportmonks.core.coreapi.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.coreapi.model.Continent;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.core.request.SingleResourceRequest;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks Core API {@code /continents} endpoints.
public final class ContinentsEndpoint {

    private final ApiExecutor executor;
    private final DataType<Continent> single;
    private final DataType<List<Continent>> list;

    /// Creates the endpoint, building the {@link Continent} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the single/list response types
    public ContinentsEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.single = codec.type(Continent.class);
        this.list = codec.listType(Continent.class);
    }

    /// Requests every continent, paginated.
    ///
    /// @return a collection request for all continents
    public CollectionRequest<Continent> all() {
        return new CollectionRequest<>(executor, RequestSpec.builder("continents"), list);
    }

    /// Requests a single continent by its id.
    ///
    /// @param id the continent id
    /// @return a single-resource request for that continent
    public SingleResourceRequest<Continent> byId(long id) {
        return new SingleResourceRequest<>(executor, RequestSpec.builder("continents/" + id), single);
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `./gradlew :core:test --tests "io.github.miro93.sportmonks.core.coreapi.endpoint.ContinentsEndpointTest"`
Expected: PASS (3 tests).

- [ ] **Step 6: Commit**

```bash
git add core/src/main/java/io/github/miro93/sportmonks/core/coreapi/model/Continent.java \
        core/src/main/java/io/github/miro93/sportmonks/core/coreapi/endpoint/ContinentsEndpoint.java \
        core/src/test/java/io/github/miro93/sportmonks/core/coreapi/endpoint/ContinentsEndpointTest.java
git commit -m "feat(core): add Continents Core API endpoint and model"
```

---

## Task 2: Countries (model + endpoint, with search)

**Files:**
- Create: `core/src/main/java/io/github/miro93/sportmonks/core/coreapi/model/Country.java`
- Create: `core/src/main/java/io/github/miro93/sportmonks/core/coreapi/endpoint/CountriesEndpoint.java`
- Test: `core/src/test/java/io/github/miro93/sportmonks/core/coreapi/endpoint/CountriesEndpointTest.java`

- [ ] **Step 1: Write the failing endpoint test**

```java
package io.github.miro93.sportmonks.core.coreapi.endpoint;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.coreapi.model.Country;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest
class CountriesEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private CountriesEndpoint countries(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new CountriesEndpoint(executor, codec);
    }

    @Test
    void allHitsCountriesRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/countries")).willReturn(okJson("""
                { "data": [ { "id": 320, "name": "Scotland" } ] }
                """)));

        var response = countries(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().name()).isEqualTo("Scotland");
    }

    @Test
    void byIdHitsTheCorrectPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlEqualTo("/countries/320")).willReturn(okJson("""
                { "data": { "id": 320, "continent_id": 1, "name": "Scotland", "iso2": "GB", "iso3": "GBR" } }
                """)));

        Country country = countries(wm.getHttpBaseUrl()).byId(320L).get().data();

        assertThat(country.id()).isEqualTo(320L);
        assertThat(country.continentId()).isEqualTo(1L);
        assertThat(country.iso2()).isEqualTo("GB");
    }

    @Test
    void searchHitsSearchPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/countries/search/Scot")).willReturn(okJson("""
                { "data": [ { "id": 320, "name": "Scotland" } ] }
                """)));

        var response = countries(wm.getHttpBaseUrl()).search("Scot").get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().id()).isEqualTo(320L);
    }

    @Test
    void searchRejectsNull(WireMockRuntimeInfo wm) {
        assertThatThrownBy(() -> countries(wm.getHttpBaseUrl()).search(null))
                .isInstanceOf(NullPointerException.class);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :core:test --tests "io.github.miro93.sportmonks.core.coreapi.endpoint.CountriesEndpointTest"`
Expected: COMPILE FAILURE — `Country` and `CountriesEndpoint` do not exist.

- [ ] **Step 3: Create the `Country` model**

```java
package io.github.miro93.sportmonks.core.coreapi.model;

/// A country from the SportMonks Core API. {@code id} is always present; every
/// other field may be {@code null}: the {@code continentId} foreign key, the
/// names ({@code name}, {@code officialName}, {@code fifaName}), the ISO codes
/// ({@code iso2}, {@code iso3}), the geo coordinates {@code latitude}/
/// {@code longitude} (returned as strings by the API), {@code geonameid},
/// {@code borders} and {@code imagePath}.
public record Country(
        long id,
        Long continentId,
        String name,
        String officialName,
        String fifaName,
        String iso2,
        String iso3,
        String latitude,
        String longitude,
        Long geonameid,
        String borders,
        String imagePath) {
}
```

- [ ] **Step 4: Create the `CountriesEndpoint`**

```java
package io.github.miro93.sportmonks.core.coreapi.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.coreapi.model.Country;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.core.request.SingleResourceRequest;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks Core API {@code /countries} endpoints.
public final class CountriesEndpoint {

    private final ApiExecutor executor;
    private final DataType<Country> single;
    private final DataType<List<Country>> list;

    /// Creates the endpoint, building the {@link Country} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the single/list response types
    public CountriesEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.single = codec.type(Country.class);
        this.list = codec.listType(Country.class);
    }

    /// Requests every country, paginated.
    ///
    /// @return a collection request for all countries
    public CollectionRequest<Country> all() {
        return collection("countries");
    }

    /// Requests a single country by its id.
    ///
    /// @param id the country id
    /// @return a single-resource request for that country
    public SingleResourceRequest<Country> byId(long id) {
        return new SingleResourceRequest<>(executor, RequestSpec.builder("countries/" + id), single);
    }

    /// Searches countries by name.
    ///
    /// @param name the search term (must not be {@code null})
    /// @return a collection request for the matching countries
    /// @throws NullPointerException if {@code name} is {@code null}
    public CollectionRequest<Country> search(String name) {
        Objects.requireNonNull(name, "name");
        return collection("countries/search/" + name);
    }

    private CollectionRequest<Country> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `./gradlew :core:test --tests "io.github.miro93.sportmonks.core.coreapi.endpoint.CountriesEndpointTest"`
Expected: PASS (4 tests).

- [ ] **Step 6: Commit**

```bash
git add core/src/main/java/io/github/miro93/sportmonks/core/coreapi/model/Country.java \
        core/src/main/java/io/github/miro93/sportmonks/core/coreapi/endpoint/CountriesEndpoint.java \
        core/src/test/java/io/github/miro93/sportmonks/core/coreapi/endpoint/CountriesEndpointTest.java
git commit -m "feat(core): add Countries Core API endpoint and model"
```

---

## Task 3: Regions (model + endpoint, with search)

**Files:**
- Create: `core/src/main/java/io/github/miro93/sportmonks/core/coreapi/model/Region.java`
- Create: `core/src/main/java/io/github/miro93/sportmonks/core/coreapi/endpoint/RegionsEndpoint.java`
- Test: `core/src/test/java/io/github/miro93/sportmonks/core/coreapi/endpoint/RegionsEndpointTest.java`

- [ ] **Step 1: Write the failing endpoint test**

```java
package io.github.miro93.sportmonks.core.coreapi.endpoint;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.coreapi.model.Region;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest
class RegionsEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private RegionsEndpoint regions(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new RegionsEndpoint(executor, codec);
    }

    @Test
    void allHitsRegionsRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/regions")).willReturn(okJson("""
                { "data": [ { "id": 10, "country_id": 320, "name": "Glasgow" } ] }
                """)));

        var response = regions(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().countryId()).isEqualTo(320L);
    }

    @Test
    void byIdHitsTheCorrectPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlEqualTo("/regions/10")).willReturn(okJson("""
                { "data": { "id": 10, "country_id": 320, "name": "Glasgow" } }
                """)));

        Region region = regions(wm.getHttpBaseUrl()).byId(10L).get().data();

        assertThat(region.id()).isEqualTo(10L);
        assertThat(region.name()).isEqualTo("Glasgow");
    }

    @Test
    void searchHitsSearchPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/regions/search/Glas")).willReturn(okJson("""
                { "data": [ { "id": 10, "name": "Glasgow" } ] }
                """)));

        var response = regions(wm.getHttpBaseUrl()).search("Glas").get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().id()).isEqualTo(10L);
    }

    @Test
    void searchRejectsNull(WireMockRuntimeInfo wm) {
        assertThatThrownBy(() -> regions(wm.getHttpBaseUrl()).search(null))
                .isInstanceOf(NullPointerException.class);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :core:test --tests "io.github.miro93.sportmonks.core.coreapi.endpoint.RegionsEndpointTest"`
Expected: COMPILE FAILURE — `Region` and `RegionsEndpoint` do not exist.

- [ ] **Step 3: Create the `Region` model**

```java
package io.github.miro93.sportmonks.core.coreapi.model;

/// A region (sub-national area) from the SportMonks Core API. {@code id} is
/// always present; {@code countryId} and {@code name} may be {@code null}.
public record Region(long id, Long countryId, String name) {
}
```

- [ ] **Step 4: Create the `RegionsEndpoint`**

```java
package io.github.miro93.sportmonks.core.coreapi.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.coreapi.model.Region;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.core.request.SingleResourceRequest;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks Core API {@code /regions} endpoints.
public final class RegionsEndpoint {

    private final ApiExecutor executor;
    private final DataType<Region> single;
    private final DataType<List<Region>> list;

    /// Creates the endpoint, building the {@link Region} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the single/list response types
    public RegionsEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.single = codec.type(Region.class);
        this.list = codec.listType(Region.class);
    }

    /// Requests every region, paginated.
    ///
    /// @return a collection request for all regions
    public CollectionRequest<Region> all() {
        return collection("regions");
    }

    /// Requests a single region by its id.
    ///
    /// @param id the region id
    /// @return a single-resource request for that region
    public SingleResourceRequest<Region> byId(long id) {
        return new SingleResourceRequest<>(executor, RequestSpec.builder("regions/" + id), single);
    }

    /// Searches regions by name.
    ///
    /// @param name the search term (must not be {@code null})
    /// @return a collection request for the matching regions
    /// @throws NullPointerException if {@code name} is {@code null}
    public CollectionRequest<Region> search(String name) {
        Objects.requireNonNull(name, "name");
        return collection("regions/search/" + name);
    }

    private CollectionRequest<Region> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `./gradlew :core:test --tests "io.github.miro93.sportmonks.core.coreapi.endpoint.RegionsEndpointTest"`
Expected: PASS (4 tests).

- [ ] **Step 6: Commit**

```bash
git add core/src/main/java/io/github/miro93/sportmonks/core/coreapi/model/Region.java \
        core/src/main/java/io/github/miro93/sportmonks/core/coreapi/endpoint/RegionsEndpoint.java \
        core/src/test/java/io/github/miro93/sportmonks/core/coreapi/endpoint/RegionsEndpointTest.java
git commit -m "feat(core): add Regions Core API endpoint and model"
```

---

## Task 4: Cities (model + endpoint, with search)

**Files:**
- Create: `core/src/main/java/io/github/miro93/sportmonks/core/coreapi/model/City.java`
- Create: `core/src/main/java/io/github/miro93/sportmonks/core/coreapi/endpoint/CitiesEndpoint.java`
- Test: `core/src/test/java/io/github/miro93/sportmonks/core/coreapi/endpoint/CitiesEndpointTest.java`

- [ ] **Step 1: Write the failing endpoint test**

```java
package io.github.miro93.sportmonks.core.coreapi.endpoint;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.coreapi.model.City;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest
class CitiesEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private CitiesEndpoint cities(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new CitiesEndpoint(executor, codec);
    }

    @Test
    void allHitsCitiesRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/cities")).willReturn(okJson("""
                { "data": [ { "id": 100, "country_id": 320, "region": 10, "name": "Glasgow" } ] }
                """)));

        var response = cities(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().countryId()).isEqualTo(320L);
        assertThat(response.data().getFirst().region()).isEqualTo(10L);
    }

    @Test
    void byIdHitsTheCorrectPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlEqualTo("/cities/100")).willReturn(okJson("""
                { "data": { "id": 100, "country_id": 320, "name": "Glasgow" } }
                """)));

        City city = cities(wm.getHttpBaseUrl()).byId(100L).get().data();

        assertThat(city.id()).isEqualTo(100L);
        assertThat(city.name()).isEqualTo("Glasgow");
    }

    @Test
    void searchHitsSearchPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/cities/search/Glas")).willReturn(okJson("""
                { "data": [ { "id": 100, "name": "Glasgow" } ] }
                """)));

        var response = cities(wm.getHttpBaseUrl()).search("Glas").get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().id()).isEqualTo(100L);
    }

    @Test
    void searchRejectsNull(WireMockRuntimeInfo wm) {
        assertThatThrownBy(() -> cities(wm.getHttpBaseUrl()).search(null))
                .isInstanceOf(NullPointerException.class);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :core:test --tests "io.github.miro93.sportmonks.core.coreapi.endpoint.CitiesEndpointTest"`
Expected: COMPILE FAILURE — `City` and `CitiesEndpoint` do not exist.

- [ ] **Step 3: Create the `City` model**

> Note: the API field for the region id is `region` (not `region_id`); Jackson's
> snake_case strategy leaves a single-word property unchanged, so the Java field
> is named `region`. Confirm against the live payload here — if the API actually
> sends `region_id`, rename the field to `regionId`.

```java
package io.github.miro93.sportmonks.core.coreapi.model;

/// A city from the SportMonks Core API. {@code id} is always present; every
/// other field may be {@code null}: {@code countryId}, {@code region} (the
/// region id — the API names this field {@code region}, not {@code region_id}),
/// {@code name}, the geo coordinates {@code latitude}/{@code longitude}
/// (returned as strings) and {@code geonameid}.
public record City(
        long id,
        Long countryId,
        Long region,
        String name,
        String latitude,
        String longitude,
        Long geonameid) {
}
```

- [ ] **Step 4: Create the `CitiesEndpoint`**

```java
package io.github.miro93.sportmonks.core.coreapi.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.coreapi.model.City;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.core.request.SingleResourceRequest;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks Core API {@code /cities} endpoints.
public final class CitiesEndpoint {

    private final ApiExecutor executor;
    private final DataType<City> single;
    private final DataType<List<City>> list;

    /// Creates the endpoint, building the {@link City} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the single/list response types
    public CitiesEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.single = codec.type(City.class);
        this.list = codec.listType(City.class);
    }

    /// Requests every city, paginated.
    ///
    /// @return a collection request for all cities
    public CollectionRequest<City> all() {
        return collection("cities");
    }

    /// Requests a single city by its id.
    ///
    /// @param id the city id
    /// @return a single-resource request for that city
    public SingleResourceRequest<City> byId(long id) {
        return new SingleResourceRequest<>(executor, RequestSpec.builder("cities/" + id), single);
    }

    /// Searches cities by name.
    ///
    /// @param name the search term (must not be {@code null})
    /// @return a collection request for the matching cities
    /// @throws NullPointerException if {@code name} is {@code null}
    public CollectionRequest<City> search(String name) {
        Objects.requireNonNull(name, "name");
        return collection("cities/search/" + name);
    }

    private CollectionRequest<City> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `./gradlew :core:test --tests "io.github.miro93.sportmonks.core.coreapi.endpoint.CitiesEndpointTest"`
Expected: PASS (4 tests).

- [ ] **Step 6: Commit**

```bash
git add core/src/main/java/io/github/miro93/sportmonks/core/coreapi/model/City.java \
        core/src/main/java/io/github/miro93/sportmonks/core/coreapi/endpoint/CitiesEndpoint.java \
        core/src/test/java/io/github/miro93/sportmonks/core/coreapi/endpoint/CitiesEndpointTest.java
git commit -m "feat(core): add Cities Core API endpoint and model"
```

---

## Task 5: Types (model + endpoint)

**Files:**
- Create: `core/src/main/java/io/github/miro93/sportmonks/core/coreapi/model/Type.java`
- Create: `core/src/main/java/io/github/miro93/sportmonks/core/coreapi/endpoint/TypesEndpoint.java`
- Test: `core/src/test/java/io/github/miro93/sportmonks/core/coreapi/endpoint/TypesEndpointTest.java`

- [ ] **Step 1: Write the failing endpoint test**

```java
package io.github.miro93.sportmonks.core.coreapi.endpoint;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.coreapi.model.Type;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class TypesEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private TypesEndpoint types(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new TypesEndpoint(executor, codec);
    }

    @Test
    void allHitsTypesRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/types")).willReturn(okJson("""
                { "data": [ { "id": 208, "name": "Goals", "code": "goals", "developer_name": "GOALS", "group": "events" } ] }
                """)));

        var response = types(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().name()).isEqualTo("Goals");
        assertThat(response.data().getFirst().developerName()).isEqualTo("GOALS");
    }

    @Test
    void byIdHitsTheCorrectPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlEqualTo("/types/208")).willReturn(okJson("""
                { "data": { "id": 208, "name": "Goals", "code": "goals", "group": "events" } }
                """)));

        Type type = types(wm.getHttpBaseUrl()).byId(208L).get().data();

        assertThat(type.id()).isEqualTo(208L);
        assertThat(type.code()).isEqualTo("goals");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :core:test --tests "io.github.miro93.sportmonks.core.coreapi.endpoint.TypesEndpointTest"`
Expected: COMPILE FAILURE — `Type` and `TypesEndpoint` do not exist.

- [ ] **Step 3: Create the `Type` model**

```java
package io.github.miro93.sportmonks.core.coreapi.model;

/// A type definition from the SportMonks Core API — the lookup behind the
/// {@code type_id} foreign keys used across the platform (events, statistics,
/// standings details, …). {@code id} is always present; {@code parentId},
/// {@code name}, {@code code}, {@code developerName}, {@code group} and
/// {@code description} may be {@code null}.
public record Type(
        long id,
        Long parentId,
        String name,
        String code,
        String developerName,
        String group,
        String description) {
}
```

- [ ] **Step 4: Create the `TypesEndpoint`**

```java
package io.github.miro93.sportmonks.core.coreapi.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.coreapi.model.Type;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.core.request.SingleResourceRequest;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks Core API {@code /types} endpoints.
public final class TypesEndpoint {

    private final ApiExecutor executor;
    private final DataType<Type> single;
    private final DataType<List<Type>> list;

    /// Creates the endpoint, building the {@link Type} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the single/list response types
    public TypesEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.single = codec.type(Type.class);
        this.list = codec.listType(Type.class);
    }

    /// Requests every type, paginated.
    ///
    /// @return a collection request for all types
    public CollectionRequest<Type> all() {
        return new CollectionRequest<>(executor, RequestSpec.builder("types"), list);
    }

    /// Requests a single type by its id.
    ///
    /// @param id the type id
    /// @return a single-resource request for that type
    public SingleResourceRequest<Type> byId(long id) {
        return new SingleResourceRequest<>(executor, RequestSpec.builder("types/" + id), single);
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `./gradlew :core:test --tests "io.github.miro93.sportmonks.core.coreapi.endpoint.TypesEndpointTest"`
Expected: PASS (2 tests).

- [ ] **Step 6: Commit**

```bash
git add core/src/main/java/io/github/miro93/sportmonks/core/coreapi/model/Type.java \
        core/src/main/java/io/github/miro93/sportmonks/core/coreapi/endpoint/TypesEndpoint.java \
        core/src/test/java/io/github/miro93/sportmonks/core/coreapi/endpoint/TypesEndpointTest.java
git commit -m "feat(core): add Types Core API endpoint and model"
```

---

## Task 6: Decoding tests (nullable coverage + field-name confirmation)

These pure-decode tests pin the model field mappings against representative payloads — including the "all optional fields absent" case (mirrors M5's nullable-fields test) and the flagged ambiguities (`Country.borders`, `City.region`). **If a real example payload from the docs disagrees with a field name, fix the model and its endpoint test now, then update the spec's "confirm in TDD" note.**

**Files:**
- Test: `core/src/test/java/io/github/miro93/sportmonks/core/coreapi/model/GeographyDecodingTest.java`
- Test: `core/src/test/java/io/github/miro93/sportmonks/core/coreapi/model/TypeDecodingTest.java`

- [ ] **Step 1: Write `GeographyDecodingTest`**

```java
package io.github.miro93.sportmonks.core.coreapi.model;

import io.github.miro93.sportmonks.core.json.JacksonCodec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GeographyDecodingTest {

    private final JacksonCodec codec = new JacksonCodec();

    @Test
    void decodesCountryWithAllScalars() {
        String json = """
                {
                  "data": {
                    "id": 320,
                    "continent_id": 1,
                    "name": "Scotland",
                    "official_name": "Scotland",
                    "fifa_name": "SCO",
                    "iso2": "GB",
                    "iso3": "GBR",
                    "latitude": "56.49067",
                    "longitude": "-4.20264",
                    "geonameid": 2638360,
                    "borders": "ENG",
                    "image_path": "https://cdn.sportmonks.com/images/countries/320.png"
                  }
                }
                """;

        Country country = codec.decode(json, codec.type(Country.class)).data();

        assertThat(country.id()).isEqualTo(320L);
        assertThat(country.continentId()).isEqualTo(1L);
        assertThat(country.name()).isEqualTo("Scotland");
        assertThat(country.officialName()).isEqualTo("Scotland");
        assertThat(country.fifaName()).isEqualTo("SCO");
        assertThat(country.iso2()).isEqualTo("GB");
        assertThat(country.iso3()).isEqualTo("GBR");
        assertThat(country.latitude()).isEqualTo("56.49067");
        assertThat(country.longitude()).isEqualTo("-4.20264");
        assertThat(country.geonameid()).isEqualTo(2638360L);
        assertThat(country.borders()).isEqualTo("ENG");
        assertThat(country.imagePath()).contains("320.png");
    }

    @Test
    void decodesCountryWithOptionalFieldsAbsent() {
        String json = """
                { "data": { "id": 320, "name": "Scotland" } }
                """;

        Country country = codec.decode(json, codec.type(Country.class)).data();

        assertThat(country.id()).isEqualTo(320L);
        assertThat(country.name()).isEqualTo("Scotland");
        assertThat(country.continentId()).isNull();
        assertThat(country.officialName()).isNull();
        assertThat(country.fifaName()).isNull();
        assertThat(country.iso2()).isNull();
        assertThat(country.iso3()).isNull();
        assertThat(country.latitude()).isNull();
        assertThat(country.longitude()).isNull();
        assertThat(country.geonameid()).isNull();
        assertThat(country.borders()).isNull();
        assertThat(country.imagePath()).isNull();
    }

    @Test
    void decodesContinent() {
        String json = """
                { "data": { "id": 1, "name": "Europe", "code": "EU" } }
                """;

        Continent continent = codec.decode(json, codec.type(Continent.class)).data();

        assertThat(continent.id()).isEqualTo(1L);
        assertThat(continent.name()).isEqualTo("Europe");
        assertThat(continent.code()).isEqualTo("EU");
    }

    @Test
    void decodesRegion() {
        String json = """
                { "data": { "id": 10, "country_id": 320, "name": "Glasgow" } }
                """;

        Region region = codec.decode(json, codec.type(Region.class)).data();

        assertThat(region.id()).isEqualTo(10L);
        assertThat(region.countryId()).isEqualTo(320L);
        assertThat(region.name()).isEqualTo("Glasgow");
    }

    @Test
    void decodesCityWithRegionAndCoordinates() {
        String json = """
                {
                  "data": {
                    "id": 100,
                    "country_id": 320,
                    "region": 10,
                    "name": "Glasgow",
                    "latitude": "55.86515",
                    "longitude": "-4.25763",
                    "geonameid": 2648579
                  }
                }
                """;

        City city = codec.decode(json, codec.type(City.class)).data();

        assertThat(city.id()).isEqualTo(100L);
        assertThat(city.countryId()).isEqualTo(320L);
        assertThat(city.region()).isEqualTo(10L);
        assertThat(city.name()).isEqualTo("Glasgow");
        assertThat(city.latitude()).isEqualTo("55.86515");
        assertThat(city.longitude()).isEqualTo("-4.25763");
        assertThat(city.geonameid()).isEqualTo(2648579L);
    }

    @Test
    void decodesCityWithOptionalFieldsAbsent() {
        String json = """
                { "data": { "id": 100, "name": "Glasgow" } }
                """;

        City city = codec.decode(json, codec.type(City.class)).data();

        assertThat(city.id()).isEqualTo(100L);
        assertThat(city.name()).isEqualTo("Glasgow");
        assertThat(city.countryId()).isNull();
        assertThat(city.region()).isNull();
        assertThat(city.latitude()).isNull();
        assertThat(city.longitude()).isNull();
        assertThat(city.geonameid()).isNull();
    }
}
```

- [ ] **Step 2: Write `TypeDecodingTest`**

```java
package io.github.miro93.sportmonks.core.coreapi.model;

import io.github.miro93.sportmonks.core.json.JacksonCodec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TypeDecodingTest {

    private final JacksonCodec codec = new JacksonCodec();

    @Test
    void decodesTypeWithAllScalars() {
        String json = """
                {
                  "data": {
                    "id": 208,
                    "parent_id": 1,
                    "name": "Goals",
                    "code": "goals",
                    "developer_name": "GOALS",
                    "group": "events",
                    "description": "Number of goals"
                  }
                }
                """;

        Type type = codec.decode(json, codec.type(Type.class)).data();

        assertThat(type.id()).isEqualTo(208L);
        assertThat(type.parentId()).isEqualTo(1L);
        assertThat(type.name()).isEqualTo("Goals");
        assertThat(type.code()).isEqualTo("goals");
        assertThat(type.developerName()).isEqualTo("GOALS");
        assertThat(type.group()).isEqualTo("events");
        assertThat(type.description()).isEqualTo("Number of goals");
    }

    @Test
    void decodesTypeWithOptionalFieldsAbsent() {
        String json = """
                { "data": { "id": 208, "name": "Goals" } }
                """;

        Type type = codec.decode(json, codec.type(Type.class)).data();

        assertThat(type.id()).isEqualTo(208L);
        assertThat(type.name()).isEqualTo("Goals");
        assertThat(type.parentId()).isNull();
        assertThat(type.code()).isNull();
        assertThat(type.developerName()).isNull();
        assertThat(type.group()).isNull();
        assertThat(type.description()).isNull();
    }
}
```

- [ ] **Step 3: Run both tests to verify they pass**

Run: `./gradlew :core:test --tests "io.github.miro93.sportmonks.core.coreapi.model.*"`
Expected: PASS (8 tests). If a field assertion fails, the model field name/type is wrong — fix the model + the matching endpoint test, then re-run.

- [ ] **Step 4: Commit**

```bash
git add core/src/test/java/io/github/miro93/sportmonks/core/coreapi/model/GeographyDecodingTest.java \
        core/src/test/java/io/github/miro93/sportmonks/core/coreapi/model/TypeDecodingTest.java
git commit -m "test(core): add Core API geography and type decoding tests"
```

---

## Task 7: CoreClient (builder + accessors)

**Files:**
- Create: `core/src/main/java/io/github/miro93/sportmonks/core/coreapi/CoreClient.java`
- Test: `core/src/test/java/io/github/miro93/sportmonks/core/coreapi/CoreClientTest.java`

- [ ] **Step 1: Write the failing `CoreClientTest`**

```java
package io.github.miro93.sportmonks.core.coreapi;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest
class CoreClientTest {

    @Test
    void defaultBaseUrlIsCoreApi() {
        assertThat(CoreClient.DEFAULT_BASE_URL).isEqualTo("https://api.sportmonks.com/v3/core");
    }

    @Test
    void builderRequiresApiToken() {
        assertThatThrownBy(() -> CoreClient.builder().build())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void exposesAllEndpoints() {
        var client = CoreClient.builder().apiToken(ApiToken.of("tok")).build();
        assertThat(client.continents()).isNotNull();
        assertThat(client.countries()).isNotNull();
        assertThat(client.regions()).isNotNull();
        assertThat(client.cities()).isNotNull();
        assertThat(client.types()).isNotNull();
    }

    @Test
    void honoursConfiguredBaseUrlAndSendsAuth(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/continents")).willReturn(okJson("""
                { "data": [ { "id": 1, "name": "Europe", "code": "EU" } ] }
                """)));

        var client = CoreClient.builder()
                .apiToken(ApiToken.of("tok"))
                .baseUrl(wm.getHttpBaseUrl())
                .build();

        var response = client.continents().all().get();

        assertThat(response.data()).hasSize(1);
        verify(getRequestedFor(urlPathEqualTo("/continents"))
                .withHeader("Authorization", equalTo("tok")));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :core:test --tests "io.github.miro93.sportmonks.core.coreapi.CoreClientTest"`
Expected: COMPILE FAILURE — `CoreClient` does not exist.

- [ ] **Step 3: Create `CoreClient`**

```java
package io.github.miro93.sportmonks.core.coreapi;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.coreapi.endpoint.CitiesEndpoint;
import io.github.miro93.sportmonks.core.coreapi.endpoint.ContinentsEndpoint;
import io.github.miro93.sportmonks.core.coreapi.endpoint.CountriesEndpoint;
import io.github.miro93.sportmonks.core.coreapi.endpoint.RegionsEndpoint;
import io.github.miro93.sportmonks.core.coreapi.endpoint.TypesEndpoint;
import io.github.miro93.sportmonks.core.http.HttpTransport;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.retry.RetryPolicy;
import io.github.miro93.sportmonks.core.retry.RetryingTransport;
import io.github.miro93.sportmonks.core.retry.Sleeper;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Objects;

/// Entry point for the SportMonks Core API (cross-sport resources: continents,
/// countries, regions, cities, types). Build via {@link #builder()}, or obtain
/// one from a football client via {@code FootballClient.core()}.
public final class CoreClient {

    public static final String DEFAULT_BASE_URL = "https://api.sportmonks.com/v3/core";

    private final ContinentsEndpoint continents;
    private final CountriesEndpoint countries;
    private final RegionsEndpoint regions;
    private final CitiesEndpoint cities;
    private final TypesEndpoint types;

    /// Creates a client wiring the Core API endpoints onto the given executor.
    /// Shared by {@link Builder#build()} and by {@code FootballClient} so a
    /// football client can expose a Core client over the same transport/token.
    ///
    /// @param executor the executor (already configured with the Core base URL)
    /// @param codec    the codec used to derive response types
    public CoreClient(ApiExecutor executor, JacksonCodec codec) {
        Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.continents = new ContinentsEndpoint(executor, codec);
        this.countries = new CountriesEndpoint(executor, codec);
        this.regions = new RegionsEndpoint(executor, codec);
        this.cities = new CitiesEndpoint(executor, codec);
        this.types = new TypesEndpoint(executor, codec);
    }

    /// Creates a new builder for a {@link CoreClient}.
    ///
    /// @return a fresh builder
    public static Builder builder() {
        return new Builder();
    }

    /// Returns the continents endpoint.
    ///
    /// @return the {@code /continents} endpoint accessor
    public ContinentsEndpoint continents() {
        return continents;
    }

    /// Returns the countries endpoint.
    ///
    /// @return the {@code /countries} endpoint accessor
    public CountriesEndpoint countries() {
        return countries;
    }

    /// Returns the regions endpoint.
    ///
    /// @return the {@code /regions} endpoint accessor
    public RegionsEndpoint regions() {
        return regions;
    }

    /// Returns the cities endpoint.
    ///
    /// @return the {@code /cities} endpoint accessor
    public CitiesEndpoint cities() {
        return cities;
    }

    /// Returns the types endpoint.
    ///
    /// @return the {@code /types} endpoint accessor
    public TypesEndpoint types() {
        return types;
    }

    /// Fluent builder for {@link CoreClient}. The API token is required; the
    /// retry policy, base URL and request timeout default to sensible values.
    public static final class Builder {
        private ApiToken apiToken;
        private RetryPolicy retryPolicy = RetryPolicy.defaults();
        private String baseUrl = DEFAULT_BASE_URL;
        private Duration requestTimeout = Duration.ofSeconds(30);

        private Builder() {
        }

        /// Sets the SportMonks API token used to authenticate requests (required).
        ///
        /// @param apiToken the API token
        /// @return this builder
        public Builder apiToken(ApiToken apiToken) {
            this.apiToken = apiToken;
            return this;
        }

        /// Overrides the retry policy applied to transient failures.
        ///
        /// @param retryPolicy the retry policy to use
        /// @return this builder
        public Builder retryPolicy(RetryPolicy retryPolicy) {
            this.retryPolicy = Objects.requireNonNull(retryPolicy, "retryPolicy");
            return this;
        }

        /// Overrides the API base URL (defaults to {@link #DEFAULT_BASE_URL}).
        ///
        /// @param baseUrl the base URL
        /// @return this builder
        public Builder baseUrl(String baseUrl) {
            this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl");
            return this;
        }

        /// Overrides the per-request timeout (defaults to 30 seconds).
        ///
        /// @param requestTimeout the request timeout
        /// @return this builder
        public Builder requestTimeout(Duration requestTimeout) {
            this.requestTimeout = Objects.requireNonNull(requestTimeout, "requestTimeout");
            return this;
        }

        /// Builds the configured {@link CoreClient}.
        ///
        /// @return a ready-to-use client
        /// @throws NullPointerException if no API token was set
        public CoreClient build() {
            Objects.requireNonNull(apiToken, "apiToken is required");
            HttpTransport base = new JdkHttpTransport(HttpClient.newHttpClient(), requestTimeout);
            HttpTransport transport = new RetryingTransport(base, retryPolicy, Sleeper.REAL);
            JacksonCodec codec = new JacksonCodec();
            ApiExecutor executor = new ApiExecutor(transport, codec, apiToken, baseUrl);
            return new CoreClient(executor, codec);
        }
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :core:test --tests "io.github.miro93.sportmonks.core.coreapi.CoreClientTest"`
Expected: PASS (4 tests).

- [ ] **Step 5: Commit**

```bash
git add core/src/main/java/io/github/miro93/sportmonks/core/coreapi/CoreClient.java \
        core/src/test/java/io/github/miro93/sportmonks/core/coreapi/CoreClientTest.java
git commit -m "feat(core): add CoreClient entry point for the Core API"
```

---

## Task 8: FootballClient.core() wiring

Expose an embedded `CoreClient` from `FootballClient`, built over the same transport/codec/token but with a configurable Core base URL.

**Files:**
- Modify: `football/src/main/java/io/github/miro93/sportmonks/football/FootballClient.java`
- Test: `football/src/test/java/io/github/miro93/sportmonks/football/FootballClientCoreTest.java`

- [ ] **Step 1: Write the failing `FootballClientCoreTest`**

```java
package io.github.miro93.sportmonks.football;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class FootballClientCoreTest {

    @Test
    void exposesCoreClient(WireMockRuntimeInfo wm) {
        var client = FootballClient.builder()
                .apiToken(ApiToken.of("tok"))
                .baseUrl(wm.getHttpBaseUrl())
                .build();

        assertThat(client.core()).isNotNull();
        assertThat(client.core().countries()).isNotNull();
    }

    @Test
    void coreUsesConfiguredCoreBaseUrlAndSendsAuth(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/countries")).willReturn(okJson("""
                { "data": [ { "id": 320, "name": "Scotland" } ] }
                """)));

        var client = FootballClient.builder()
                .apiToken(ApiToken.of("tok"))
                .baseUrl(wm.getHttpBaseUrl())
                .coreBaseUrl(wm.getHttpBaseUrl())
                .build();

        var response = client.core().countries().all().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().name()).isEqualTo("Scotland");
        verify(getRequestedFor(urlPathEqualTo("/countries"))
                .withHeader("Authorization", equalTo("tok")));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :football:test --tests "io.github.miro93.sportmonks.football.FootballClientCoreTest"`
Expected: COMPILE FAILURE — `FootballClient.core()` and `Builder.coreBaseUrl(..)` do not exist.

- [ ] **Step 3: Add the import and `core` field to `FootballClient`**

In `FootballClient.java`, add the import (with the other `core.*` imports, after line 3 `import io.github.miro93.sportmonks.core.ApiExecutor;`):

```java
import io.github.miro93.sportmonks.core.coreapi.CoreClient;
```

Add the field after the `topscorers` field (currently `FootballClient.java:48`):

```java
    private final TopscorersEndpoint topscorers;
    private final CoreClient core;
```

- [ ] **Step 4: Add `core` to the constructor**

Change the constructor signature to accept `CoreClient core` as the final parameter and assign it. The constructor's last parameter/assignment becomes:

```java
            TopscorersEndpoint topscorers,
            CoreClient core) {
        this.fixtures = fixtures;
        this.livescores = livescores;
        this.leagues = leagues;
        this.seasons = seasons;
        this.stages = stages;
        this.rounds = rounds;
        this.schedules = schedules;
        this.teams = teams;
        this.players = players;
        this.coaches = coaches;
        this.squads = squads;
        this.transfers = transfers;
        this.standings = standings;
        this.topscorers = topscorers;
        this.core = core;
    }
```

- [ ] **Step 5: Add the `core()` accessor**

Add after the `topscorers()` accessor (currently ends at `FootballClient.java:184`):

```java
    /// Returns the SportMonks Core API client (continents, countries, regions,
    /// cities, types) backed by the same credentials and transport.
    ///
    /// @return the embedded {@link CoreClient}
    public CoreClient core() {
        return core;
    }
```

- [ ] **Step 6: Add the `coreBaseUrl` builder field + setter**

In `Builder`, add the field after `private String baseUrl = DEFAULT_BASE_URL;`:

```java
        private String baseUrl = DEFAULT_BASE_URL;
        private String coreBaseUrl = CoreClient.DEFAULT_BASE_URL;
```

Add the setter after the `baseUrl(..)` method:

```java
        /// Overrides the SportMonks Core API base URL used by {@link FootballClient#core()}
        /// (defaults to {@link CoreClient#DEFAULT_BASE_URL}).
        ///
        /// @param coreBaseUrl the Core API base URL
        /// @return this builder
        public Builder coreBaseUrl(String coreBaseUrl) {
            this.coreBaseUrl = Objects.requireNonNull(coreBaseUrl, "coreBaseUrl");
            return this;
        }
```

- [ ] **Step 7: Wire the `CoreClient` in `build()`**

Replace the `build()` body's executor/return section (currently `FootballClient.java:242-257`) with:

```java
            ApiExecutor executor = new ApiExecutor(transport, codec, apiToken, baseUrl);
            ApiExecutor coreExecutor = new ApiExecutor(transport, codec, apiToken, coreBaseUrl);
            CoreClient core = new CoreClient(coreExecutor, codec);
            return new FootballClient(
                    new FixturesEndpoint(executor, codec),
                    new LivescoresEndpoint(executor, codec),
                    new LeaguesEndpoint(executor, codec),
                    new SeasonsEndpoint(executor, codec),
                    new StagesEndpoint(executor, codec),
                    new RoundsEndpoint(executor, codec),
                    new SchedulesEndpoint(executor, codec),
                    new TeamsEndpoint(executor, codec),
                    new PlayersEndpoint(executor, codec),
                    new CoachesEndpoint(executor, codec),
                    new SquadsEndpoint(executor, codec),
                    new TransfersEndpoint(executor, codec),
                    new StandingsEndpoint(executor, codec),
                    new TopscorersEndpoint(executor, codec),
                    core);
```

- [ ] **Step 8: Run test to verify it passes**

Run: `./gradlew :football:test --tests "io.github.miro93.sportmonks.football.FootballClientCoreTest"`
Expected: PASS (2 tests).

- [ ] **Step 9: Commit**

```bash
git add football/src/main/java/io/github/miro93/sportmonks/football/FootballClient.java \
        football/src/test/java/io/github/miro93/sportmonks/football/FootballClientCoreTest.java
git commit -m "feat(football): expose embedded CoreClient via FootballClient.core()"
```

---

## Task 9: README + full suite verification

**Files:**
- Modify: `README.md`

- [ ] **Step 1: Add a "Core API" usage section to README**

Find the section that documents `FootballClient` usage and add the following subsection after it (adjust the surrounding heading level to match the file — use `##`/`###` consistent with neighbours):

````markdown
### Core API (cross-sport reference data)

The Core API (`/v3/core`) exposes cross-sport reference resources: continents,
countries, regions, cities and types. Use it standalone:

```java
CoreClient core = CoreClient.builder()
        .apiToken(ApiToken.of(System.getenv("SPORTMONKS_TOKEN")))
        .build();

List<Country> countries = core.countries().search("Scot").get().data();
List<Type> types = core.types().all().get().data();
```

…or reach it from a football client, which shares the same credentials:

```java
FootballClient football = FootballClient.builder()
        .apiToken(ApiToken.of(System.getenv("SPORTMONKS_TOKEN")))
        .build();

Country country = football.core().countries().byId(320L).get().data();
```
````

- [ ] **Step 2: Run the full test suite**

Run: `./gradlew test`
Expected: PASS — all pre-existing tests plus the new tests. New count: 17 endpoint tests (Continents 3 + Countries 4 + Regions 4 + Cities 4 + Types 2), 8 decoding tests, 4 `CoreClientTest` (all in `:core`), and 2 `FootballClientCoreTest` (in `:football`). No failures.

- [ ] **Step 3: Commit**

```bash
git add README.md
git commit -m "docs: document Core API (CoreClient + FootballClient.core)"
```

---

## Definition of Done

- [ ] 5 Core API models + 5 endpoints created under `core.coreapi.*`, each with `///` JavaDoc.
- [ ] `CoreClient` (builder + 5 accessors) and `FootballClient.core()` exposed; `coreBaseUrl` configurable.
- [ ] All endpoint paths verified by WireMock tests; nullable decoding verified for every model.
- [ ] Flagged field ambiguities (`Country.borders`, `City.region`) confirmed against real payloads in Task 6 (model/spec corrected if needed).
- [ ] `./gradlew test` green; README documents Core API usage.
- [ ] Conventional `feat:`/`test:`/`docs:` commits so release-please updates the CHANGELOG.
- [ ] Out of scope (deferred): `types/entities` grouped response, typed Core includes, M8 football referentials.
```
