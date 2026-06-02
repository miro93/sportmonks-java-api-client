# M2 — Football: Fixtures & Livescores Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Deliver the first football endpoints — Fixtures and Livescores — with a `FootballClient`, strongly-typed model records, and reusable fluent request plumbing added to `core`.

**Architecture:** Add two generic request builders to `core` (`SingleResourceRequest<T>`, `CollectionRequest<T>`) that wrap a `RequestSpec.Builder` + `ApiExecutor` + Jackson `JavaType` and expose fluent `include/filter/select/sort/page` plus terminal `get()`/`getAsync()` (and `stream()` for collections, auto-paginating via `Pages`). In `football`, model `Fixture` and its includes (`Participant`/`ParticipantMeta`, `Score`/`ScoreDetail`, `State`, `Event`) as immutable records; `FixturesEndpoint` and `LivescoresEndpoint` turn calls into those request builders; `FootballClient` composes `JdkHttpTransport → RetryingTransport → ApiExecutor` and exposes the endpoints. Livescores return `Fixture` objects, so no separate model.

**Tech Stack:** Java 25, Gradle (existing multi-module), Jackson (Blackbird, snake_case), JUnit 5 + WireMock + AssertJ. Builds on the merged M1 `core`.

---

## File Structure

```
core/                                    # M1 module — ADD request plumbing only
└── src/main/java/io/github/miro93/sportmonks/core/request/
    ├── SingleResourceRequest.java       # NEW — fluent single-resource request
    └── CollectionRequest.java           # NEW — fluent collection request + stream()
core/src/test/java/io/github/miro93/sportmonks/core/request/
    ├── SingleResourceRequestTest.java   # NEW (WireMock)
    └── CollectionRequestTest.java        # NEW (WireMock)

football/                                # was empty — now the football API
└── src/main/java/io/github/miro93/sportmonks/football/
    ├── FootballClient.java              # entry point + builder
    ├── endpoint/
    │   ├── FixturesEndpoint.java
    │   └── LivescoresEndpoint.java
    └── model/
        ├── Fixture.java
        ├── Participant.java
        ├── ParticipantMeta.java
        ├── Score.java
        ├── ScoreDetail.java
        ├── State.java
        └── Event.java
football/src/test/java/io/github/miro93/sportmonks/football/
    ├── model/FixtureDecodingTest.java
    ├── endpoint/FixturesEndpointTest.java
    ├── endpoint/LivescoresEndpointTest.java
    └── FootballClientTest.java
```

Maps to M2 issues: core plumbing → Task 1; models (#13) → Task 2; FixturesEndpoint (#11) → Task 3; LivescoresEndpoint (#12) → Task 4; FootballClient (#10) → Task 5. WireMock endpoint tests (#14) are produced across Tasks 1, 3, 4, 5.

**Implementation order:** 1 (core plumbing) → 2 (models) → 3 (fixtures) → 4 (livescores) → 5 (client). Endpoint tests construct endpoints directly (`new FixturesEndpoint(executor, codec)`) so they don't depend on `FootballClient`; the client is wired and tested last.

---

## Task 1: Core request plumbing (SingleResourceRequest + CollectionRequest)

**Files:**
- Create: `core/src/main/java/io/github/miro93/sportmonks/core/request/SingleResourceRequest.java`
- Create: `core/src/main/java/io/github/miro93/sportmonks/core/request/CollectionRequest.java`
- Test: `core/src/test/java/io/github/miro93/sportmonks/core/request/SingleResourceRequestTest.java`
- Test: `core/src/test/java/io/github/miro93/sportmonks/core/request/CollectionRequestTest.java`

These are generic, sport-agnostic builders. `SingleResourceRequest<T>` resolves to `ApiResponse<T>`; `CollectionRequest<T>` resolves to `ApiResponse<List<T>>` and adds a lazy `stream()`.

- [ ] **Step 1: Write the failing tests**

`SingleResourceRequestTest.java`:
```java
package io.github.miro93.sportmonks.core.request;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.response.ApiResponse;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class SingleResourceRequestTest {

    record Team(long id, String name) {
    }

    private final JacksonCodec codec = new JacksonCodec();

    private ApiExecutor executor(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        return new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
    }

    @Test
    void fluentIncludesReachTheUrlAndResultDecodes(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/teams/1"))
                .withQueryParam("include", equalTo("country"))
                .willReturn(okJson("""
                        { "data": { "id": 1, "name": "Ajax" } }
                        """)));

        ApiResponse<Team> response = new SingleResourceRequest<Team>(
                executor(wm.getHttpBaseUrl()), RequestSpec.builder("teams/1"), codec.type(Team.class))
                .include("country")
                .get();

        assertThat(response.data().name()).isEqualTo("Ajax");
    }

    @Test
    void getAsyncReturnsDecodedResource(WireMockRuntimeInfo wm) throws Exception {
        stubFor(get(urlPathEqualTo("/teams/2")).willReturn(okJson("""
                { "data": { "id": 2, "name": "PSV" } }
                """)));

        var future = new SingleResourceRequest<Team>(
                executor(wm.getHttpBaseUrl()), RequestSpec.builder("teams/2"), codec.type(Team.class))
                .getAsync();

        assertThat(future.get().data().name()).isEqualTo("PSV");
    }
}
```

`CollectionRequestTest.java`:
```java
package io.github.miro93.sportmonks.core.request;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.response.ApiResponse;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class CollectionRequestTest {

    record Team(long id, String name) {
    }

    private final JacksonCodec codec = new JacksonCodec();

    private ApiExecutor executor(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        return new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
    }

    private CollectionRequest<Team> teams(String baseUrl) {
        return new CollectionRequest<>(
                executor(baseUrl), RequestSpec.builder("teams"), codec.listType(Team.class));
    }

    @Test
    void getReturnsDecodedList(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/teams")).willReturn(okJson("""
                { "data": [ { "id": 1, "name": "Ajax" }, { "id": 2, "name": "PSV" } ] }
                """)));

        ApiResponse<List<Team>> response = teams(wm.getHttpBaseUrl()).get();

        assertThat(response.data()).extracting(Team::name).containsExactly("Ajax", "PSV");
    }

    @Test
    void streamFollowsPaginationAcrossPages(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/teams")).withQueryParam("page", equalTo("1")).willReturn(okJson("""
                { "data": [ { "id": 1, "name": "Ajax" } ],
                  "pagination": { "count": 1, "per_page": 1, "current_page": 1, "next_page": "n", "has_more": true } }
                """)));
        stubFor(get(urlPathEqualTo("/teams")).withQueryParam("page", equalTo("2")).willReturn(okJson("""
                { "data": [ { "id": 2, "name": "PSV" } ],
                  "pagination": { "count": 1, "per_page": 1, "current_page": 2, "next_page": null, "has_more": false } }
                """)));

        List<String> names = teams(wm.getHttpBaseUrl()).stream().map(Team::name).toList();

        assertThat(names).containsExactly("Ajax", "PSV");
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :core:test --tests '*SingleResourceRequestTest' --tests '*CollectionRequestTest'`
Expected: FAIL — `SingleResourceRequest`, `CollectionRequest` do not exist.

- [ ] **Step 3: Create `SingleResourceRequest`**

```java
package io.github.miro93.sportmonks.core.request;

import com.fasterxml.jackson.databind.JavaType;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.response.ApiResponse;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/// Fluent builder for a single-resource request. Accumulates query options, then
/// resolves to an {@code ApiResponse<T>} via {@link #get()} / {@link #getAsync()}.
public final class SingleResourceRequest<T> {

    private final ApiExecutor executor;
    private final RequestSpec.Builder spec;
    private final JavaType dataType;

    public SingleResourceRequest(ApiExecutor executor, RequestSpec.Builder spec, JavaType dataType) {
        this.executor = Objects.requireNonNull(executor, "executor");
        this.spec = Objects.requireNonNull(spec, "spec");
        this.dataType = Objects.requireNonNull(dataType, "dataType");
    }

    public SingleResourceRequest<T> include(String... values) {
        spec.include(values);
        return this;
    }

    public SingleResourceRequest<T> filter(String name, String... values) {
        spec.filter(name, values);
        return this;
    }

    public SingleResourceRequest<T> select(String... fields) {
        spec.select(fields);
        return this;
    }

    public SingleResourceRequest<T> sort(String... fields) {
        spec.sort(fields);
        return this;
    }

    public ApiResponse<T> get() {
        return executor.execute(spec.build(), dataType);
    }

    public CompletableFuture<ApiResponse<T>> getAsync() {
        return executor.executeAsync(spec.build(), dataType);
    }
}
```

- [ ] **Step 4: Create `CollectionRequest`**

```java
package io.github.miro93.sportmonks.core.request;

import com.fasterxml.jackson.databind.JavaType;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.paging.Pages;
import io.github.miro93.sportmonks.core.response.ApiResponse;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/// Fluent builder for a collection request. Resolves to an {@code ApiResponse<List<T>>}
/// via {@link #get()} / {@link #getAsync()}, or a lazily-paginated {@link #stream()}.
///
/// @implNote {@link #stream()} is sequential; calling {@code parallel()} on it is
/// unsupported because the underlying page iterator is stateful.
public final class CollectionRequest<T> {

    private final ApiExecutor executor;
    private final RequestSpec.Builder spec;
    private final JavaType listType;

    public CollectionRequest(ApiExecutor executor, RequestSpec.Builder spec, JavaType listType) {
        this.executor = Objects.requireNonNull(executor, "executor");
        this.spec = Objects.requireNonNull(spec, "spec");
        this.listType = Objects.requireNonNull(listType, "listType");
    }

    public CollectionRequest<T> include(String... values) {
        spec.include(values);
        return this;
    }

    public CollectionRequest<T> filter(String name, String... values) {
        spec.filter(name, values);
        return this;
    }

    public CollectionRequest<T> select(String... fields) {
        spec.select(fields);
        return this;
    }

    public CollectionRequest<T> sort(String... fields) {
        spec.sort(fields);
        return this;
    }

    public CollectionRequest<T> page(int page) {
        spec.page(page);
        return this;
    }

    public ApiResponse<List<T>> get() {
        return executor.execute(spec.build(), listType);
    }

    public CompletableFuture<ApiResponse<List<T>>> getAsync() {
        return executor.executeAsync(spec.build(), listType);
    }

    /// Lazily walks every page, following {@code pagination.has_more}.
    public Stream<T> stream() {
        return Pages.stream(page -> {
            ApiResponse<List<T>> response = executor.execute(spec.page(page).build(), listType);
            return response;
        });
    }
}
```

- [ ] **Step 5: Run tests to verify they pass**

Run: `./gradlew :core:test --tests '*SingleResourceRequestTest' --tests '*CollectionRequestTest'`
Expected: PASS (4 tests).

- [ ] **Step 6: Commit**

```bash
git add core/src/main/java/io/github/miro93/sportmonks/core/request/SingleResourceRequest.java core/src/main/java/io/github/miro93/sportmonks/core/request/CollectionRequest.java core/src/test/java/io/github/miro93/sportmonks/core/request/SingleResourceRequestTest.java core/src/test/java/io/github/miro93/sportmonks/core/request/CollectionRequestTest.java
git commit -m "feat(core): add fluent SingleResourceRequest and CollectionRequest builders"
```
End the commit message with:
`Co-Authored-By: Claude Opus 4.8 (1M context) <noreply@anthropic.com>`

---

## Task 2: Football model records (Fixture + includes)

**Files:**
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/model/Fixture.java`
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/model/Participant.java`
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/model/ParticipantMeta.java`
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/model/Score.java`
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/model/ScoreDetail.java`
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/model/State.java`
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/model/Event.java`
- Test: `football/src/test/java/io/github/miro93/sportmonks/football/model/FixtureDecodingTest.java`

Strongly-typed records. Nullable scalar ids use boxed types; always-present fields use primitives. Include relations (`participants`, `scores`, `state`, `events`) are `null` when not requested. The codec already ignores unknown fields, so extra API fields won't break decoding.

- [ ] **Step 1: Write the failing test**

`FixtureDecodingTest.java`:
```java
package io.github.miro93.sportmonks.football.model;

import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.response.ApiResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FixtureDecodingTest {

    private final JacksonCodec codec = new JacksonCodec();

    @Test
    void decodesFixtureWithNestedIncludes() {
        String json = """
                {
                  "data": {
                    "id": 18535517,
                    "sport_id": 1,
                    "league_id": 501,
                    "season_id": 19735,
                    "stage_id": 77457866,
                    "round_id": 274719,
                    "state_id": 5,
                    "venue_id": 8909,
                    "name": "Celtic vs Rangers",
                    "starting_at": "2024-09-01 11:00:00",
                    "result_info": "Celtic won after full-time.",
                    "leg": "1/1",
                    "length": 90,
                    "placeholder": false,
                    "has_odds": true,
                    "starting_at_timestamp": 1725188400,
                    "participants": [
                      { "id": 53, "name": "Celtic", "short_code": "CEL", "meta": { "location": "home", "winner": true, "position": 1 } },
                      { "id": 62, "name": "Rangers", "short_code": "RAN", "meta": { "location": "away", "winner": false, "position": 2 } }
                    ],
                    "scores": [
                      { "id": 1, "fixture_id": 18535517, "type_id": 1525, "participant_id": 53,
                        "score": { "goals": 3, "participant": "home" }, "description": "CURRENT" }
                    ],
                    "state": { "id": 5, "state": "FT", "name": "Full-Time", "short_name": "FT", "developer_name": "FT" },
                    "events": [
                      { "id": 99, "fixture_id": 18535517, "type_id": 14, "participant_id": 53,
                        "player_id": 1001, "player_name": "Kyogo", "minute": 23, "result": "1-0" }
                    ]
                  }
                }
                """;

        ApiResponse<Fixture> response = codec.decode(json, codec.type(Fixture.class));
        Fixture fixture = response.data();

        assertThat(fixture.id()).isEqualTo(18535517L);
        assertThat(fixture.name()).isEqualTo("Celtic vs Rangers");
        assertThat(fixture.leagueId()).isEqualTo(501L);
        assertThat(fixture.startingAtTimestamp()).isEqualTo(1725188400L);
        assertThat(fixture.placeholder()).isFalse();
        assertThat(fixture.hasOdds()).isTrue();

        assertThat(fixture.participants()).hasSize(2);
        Participant home = fixture.participants().getFirst();
        assertThat(home.name()).isEqualTo("Celtic");
        assertThat(home.meta().location()).isEqualTo("home");
        assertThat(home.meta().winner()).isTrue();

        assertThat(fixture.scores()).hasSize(1);
        assertThat(fixture.scores().getFirst().score().goals()).isEqualTo(3);
        assertThat(fixture.scores().getFirst().score().participant()).isEqualTo("home");

        assertThat(fixture.state().developerName()).isEqualTo("FT");

        assertThat(fixture.events()).hasSize(1);
        assertThat(fixture.events().getFirst().playerName()).isEqualTo("Kyogo");
        assertThat(fixture.events().getFirst().minute()).isEqualTo(23);
    }

    @Test
    void decodesFixtureWithoutIncludes() {
        String json = """
                { "data": { "id": 1, "name": "A vs B", "placeholder": false, "has_odds": false } }
                """;

        Fixture fixture = codec.decode(json, codec.type(Fixture.class)).data();

        assertThat(fixture.id()).isEqualTo(1L);
        assertThat(fixture.participants()).isNull();
        assertThat(fixture.scores()).isNull();
        assertThat(fixture.state()).isNull();
        assertThat(fixture.events()).isNull();
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :football:test --tests '*FixtureDecodingTest'`
Expected: FAIL — model records do not exist.

- [ ] **Step 3: Create the model records**

`ParticipantMeta.java`:
```java
package io.github.miro93.sportmonks.football.model;

/// Per-fixture metadata attached to a participant when included via a fixture.
public record ParticipantMeta(String location, Boolean winner, Integer position) {
}
```

`Participant.java`:
```java
package io.github.miro93.sportmonks.football.model;

/// A team taking part in a fixture. {@code meta} is present when the participant
/// is loaded through a fixture include.
public record Participant(
        long id,
        Long sportId,
        Long countryId,
        Long venueId,
        String gender,
        String name,
        String shortCode,
        String imagePath,
        Integer founded,
        String type,
        boolean placeholder,
        String lastPlayedAt,
        ParticipantMeta meta) {
}
```

`ScoreDetail.java`:
```java
package io.github.miro93.sportmonks.football.model;

/// The inner score payload: goals and which side ("home"/"away") they belong to.
public record ScoreDetail(Integer goals, String participant) {
}
```

`Score.java`:
```java
package io.github.miro93.sportmonks.football.model;

/// A score line for a fixture (e.g. CURRENT, HT, FT) for one participant.
public record Score(
        long id,
        Long fixtureId,
        Long typeId,
        Long participantId,
        ScoreDetail score,
        String description) {
}
```

`State.java`:
```java
package io.github.miro93.sportmonks.football.model;

/// The lifecycle state of a fixture (NS, INPLAY, FT, ...).
public record State(
        long id,
        String state,
        String name,
        String shortName,
        String developerName) {
}
```

`Event.java`:
```java
package io.github.miro93.sportmonks.football.model;

/// A single in-match event (goal, card, substitution, ...).
public record Event(
        long id,
        Long fixtureId,
        Long periodId,
        Long participantId,
        Long typeId,
        Long subTypeId,
        Long playerId,
        Long relatedPlayerId,
        String playerName,
        String relatedPlayerName,
        String result,
        String info,
        String addition,
        Integer minute,
        Integer extraMinute,
        Boolean injured,
        Boolean onBench,
        Long coachId,
        Integer sortOrder) {
}
```

`Fixture.java`:
```java
package io.github.miro93.sportmonks.football.model;

import java.util.List;

/// A football fixture. Scalar fields are always present; the relation fields
/// ({@code participants}, {@code scores}, {@code state}, {@code events}) are
/// {@code null} unless requested via includes.
public record Fixture(
        long id,
        Long sportId,
        Long leagueId,
        Long seasonId,
        Long stageId,
        Long groupId,
        Long aggregateId,
        Long roundId,
        Integer stateId,
        Long venueId,
        String name,
        String startingAt,
        String resultInfo,
        String leg,
        String details,
        Integer length,
        boolean placeholder,
        boolean hasOdds,
        Long startingAtTimestamp,
        List<Participant> participants,
        List<Score> scores,
        State state,
        List<Event> events) {
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :football:test --tests '*FixtureDecodingTest'`
Expected: PASS (2 tests). Snake_case fields (`league_id`, `starting_at_timestamp`, `short_code`, `developer_name`, `player_name`) map to the camelCase record components via the codec's naming strategy; the codec ignores unknown fields.

- [ ] **Step 5: Commit**

```bash
git add football/src/main/java/io/github/miro93/sportmonks/football/model/ football/src/test/java/io/github/miro93/sportmonks/football/model/
git commit -m "feat(football): add Fixture and nested model records"
```
End the commit message with the Co-Authored-By trailer.

---

## Task 3: FixturesEndpoint

**Files:**
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/endpoint/FixturesEndpoint.java`
- Test: `football/src/test/java/io/github/miro93/sportmonks/football/endpoint/FixturesEndpointTest.java`

`FixturesEndpoint` builds request objects from the M1 `ApiExecutor` + `JacksonCodec` (used to build `JavaType`s). Single-resource methods return `SingleResourceRequest<Fixture>`; multi/list methods return `CollectionRequest<Fixture>`. Dates use `LocalDate` (its `toString()` is ISO `yyyy-MM-dd`, which SportMonks expects).

- [ ] **Step 1: Write the failing test**

`FixturesEndpointTest.java`:
```java
package io.github.miro93.sportmonks.football.endpoint;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.football.model.Fixture;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.LocalDate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class FixturesEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private FixturesEndpoint fixtures(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new FixturesEndpoint(executor, codec);
    }

    @Test
    void byIdHitsTheCorrectPathWithIncludes(WireMockRuntimeInfo wm) {
        stubFor(get(urlEqualTo("/fixtures/18535517?include=participants;scores"))
                .willReturn(okJson("""
                        { "data": { "id": 18535517, "name": "Celtic vs Rangers" } }
                        """)));

        Fixture fixture = fixtures(wm.getHttpBaseUrl())
                .byId(18535517L)
                .include("participants", "scores")
                .get()
                .data();

        assertThat(fixture.id()).isEqualTo(18535517L);
    }

    @Test
    void byDateHitsDatePath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/fixtures/date/2024-09-01")).willReturn(okJson("""
                { "data": [ { "id": 1, "name": "A vs B" } ] }
                """)));

        var response = fixtures(wm.getHttpBaseUrl())
                .byDate(LocalDate.of(2024, 9, 1))
                .get();

        assertThat(response.data()).hasSize(1);
    }

    @Test
    void byDateRangeHitsBetweenPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/fixtures/between/2024-09-01/2024-09-07")).willReturn(okJson("""
                { "data": [ { "id": 1, "name": "A vs B" }, { "id": 2, "name": "C vs D" } ] }
                """)));

        var response = fixtures(wm.getHttpBaseUrl())
                .byDateRange(LocalDate.of(2024, 9, 1), LocalDate.of(2024, 9, 7))
                .get();

        assertThat(response.data()).hasSize(2);
    }

    @Test
    void byDateRangeForTeamHitsBetweenTeamPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/fixtures/between/2024-09-01/2024-09-07/53")).willReturn(okJson("""
                { "data": [ { "id": 1, "name": "A vs B" } ] }
                """)));

        var response = fixtures(wm.getHttpBaseUrl())
                .byDateRangeForTeam(LocalDate.of(2024, 9, 1), LocalDate.of(2024, 9, 7), 53L)
                .get();

        assertThat(response.data()).hasSize(1);
    }

    @Test
    void byMultipleIdsHitsMultiPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/fixtures/multi/1,2,3")).willReturn(okJson("""
                { "data": [ { "id": 1, "name": "A" }, { "id": 2, "name": "B" }, { "id": 3, "name": "C" } ] }
                """)));

        var response = fixtures(wm.getHttpBaseUrl())
                .byMultipleIds(1L, 2L, 3L)
                .get();

        assertThat(response.data()).hasSize(3);
    }

    @Test
    void headToHeadHitsHeadToHeadPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/fixtures/head-to-head/53/62")).willReturn(okJson("""
                { "data": [ { "id": 1, "name": "Celtic vs Rangers" } ] }
                """)));

        var response = fixtures(wm.getHttpBaseUrl())
                .headToHead(53L, 62L)
                .get();

        assertThat(response.data()).hasSize(1);
    }

    @Test
    void searchHitsSearchPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/fixtures/search/Celtic")).willReturn(okJson("""
                { "data": [ { "id": 1, "name": "Celtic vs Rangers" } ] }
                """)));

        var response = fixtures(wm.getHttpBaseUrl())
                .search("Celtic")
                .get();

        assertThat(response.data()).hasSize(1);
    }

    @Test
    void allHitsFixturesRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/fixtures")).willReturn(okJson("""
                { "data": [ { "id": 1, "name": "A vs B" } ] }
                """)));

        var response = fixtures(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(1);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :football:test --tests '*FixturesEndpointTest'`
Expected: FAIL — `FixturesEndpoint` does not exist.

- [ ] **Step 3: Create `FixturesEndpoint`**

```java
package io.github.miro93.sportmonks.football.endpoint;

import com.fasterxml.jackson.databind.JavaType;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.core.request.SingleResourceRequest;
import io.github.miro93.sportmonks.football.model.Fixture;

import java.time.LocalDate;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/// Access to the SportMonks {@code /fixtures} endpoints.
public final class FixturesEndpoint {

    private final ApiExecutor executor;
    private final JavaType single;
    private final JavaType list;

    public FixturesEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.single = codec.type(Fixture.class);
        this.list = codec.listType(Fixture.class);
    }

    public CollectionRequest<Fixture> all() {
        return collection("fixtures");
    }

    public SingleResourceRequest<Fixture> byId(long id) {
        return new SingleResourceRequest<>(executor, RequestSpec.builder("fixtures/" + id), single);
    }

    public CollectionRequest<Fixture> byMultipleIds(long... ids) {
        String csv = LongStream.of(ids).mapToObj(Long::toString).collect(Collectors.joining(","));
        return collection("fixtures/multi/" + csv);
    }

    public CollectionRequest<Fixture> byDate(LocalDate date) {
        return collection("fixtures/date/" + date);
    }

    public CollectionRequest<Fixture> byDateRange(LocalDate start, LocalDate end) {
        return collection("fixtures/between/" + start + "/" + end);
    }

    public CollectionRequest<Fixture> byDateRangeForTeam(LocalDate start, LocalDate end, long teamId) {
        return collection("fixtures/between/" + start + "/" + end + "/" + teamId);
    }

    public CollectionRequest<Fixture> headToHead(long teamId1, long teamId2) {
        return collection("fixtures/head-to-head/" + teamId1 + "/" + teamId2);
    }

    public CollectionRequest<Fixture> search(String name) {
        return collection("fixtures/search/" + name);
    }

    private CollectionRequest<Fixture> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :football:test --tests '*FixturesEndpointTest'`
Expected: PASS (8 tests).

- [ ] **Step 5: Commit**

```bash
git add football/src/main/java/io/github/miro93/sportmonks/football/endpoint/FixturesEndpoint.java football/src/test/java/io/github/miro93/sportmonks/football/endpoint/FixturesEndpointTest.java
git commit -m "feat(football): add FixturesEndpoint"
```
End the commit message with the Co-Authored-By trailer.

---

## Task 4: LivescoresEndpoint

**Files:**
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/endpoint/LivescoresEndpoint.java`
- Test: `football/src/test/java/io/github/miro93/sportmonks/football/endpoint/LivescoresEndpointTest.java`

Livescores return `Fixture` collections. Three endpoints: in-play, all (15-min window), latest (updated in last 10s).

- [ ] **Step 1: Write the failing test**

`LivescoresEndpointTest.java`:
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
class LivescoresEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private LivescoresEndpoint livescores(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new LivescoresEndpoint(executor, codec);
    }

    @Test
    void inplayHitsInplayPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlEqualTo("/livescores/inplay?include=scores;state"))
                .willReturn(okJson("""
                        { "data": [ { "id": 1, "name": "A vs B" } ] }
                        """)));

        var response = livescores(wm.getHttpBaseUrl())
                .inplay()
                .include("scores", "state")
                .get();

        assertThat(response.data()).hasSize(1);
    }

    @Test
    void allHitsLivescoresRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/livescores")).willReturn(okJson("""
                { "data": [ { "id": 1, "name": "A vs B" } ] }
                """)));

        assertThat(livescores(wm.getHttpBaseUrl()).all().get().data()).hasSize(1);
    }

    @Test
    void latestHitsLatestPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/livescores/latest")).willReturn(okJson("""
                { "data": [ { "id": 1, "name": "A vs B" } ] }
                """)));

        assertThat(livescores(wm.getHttpBaseUrl()).latest().get().data()).hasSize(1);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :football:test --tests '*LivescoresEndpointTest'`
Expected: FAIL — `LivescoresEndpoint` does not exist.

- [ ] **Step 3: Create `LivescoresEndpoint`**

```java
package io.github.miro93.sportmonks.football.endpoint;

import com.fasterxml.jackson.databind.JavaType;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.football.model.Fixture;

import java.util.Objects;

/// Access to the SportMonks {@code /livescores} endpoints. All return fixtures.
public final class LivescoresEndpoint {

    private final ApiExecutor executor;
    private final JavaType list;

    public LivescoresEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.list = codec.listType(Fixture.class);
    }

    /// Fixtures currently in play.
    public CollectionRequest<Fixture> inplay() {
        return collection("livescores/inplay");
    }

    /// Fixtures within the 15-minute window around kickoff.
    public CollectionRequest<Fixture> all() {
        return collection("livescores");
    }

    /// Fixtures updated within the last 10 seconds.
    public CollectionRequest<Fixture> latest() {
        return collection("livescores/latest");
    }

    private CollectionRequest<Fixture> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :football:test --tests '*LivescoresEndpointTest'`
Expected: PASS (3 tests).

- [ ] **Step 5: Commit**

```bash
git add football/src/main/java/io/github/miro93/sportmonks/football/endpoint/LivescoresEndpoint.java football/src/test/java/io/github/miro93/sportmonks/football/endpoint/LivescoresEndpointTest.java
git commit -m "feat(football): add LivescoresEndpoint"
```
End the commit message with the Co-Authored-By trailer.

---

## Task 5: FootballClient + builder

**Files:**
- Create: `football/src/main/java/io/github/miro93/sportmonks/football/FootballClient.java`
- Test: `football/src/test/java/io/github/miro93/sportmonks/football/FootballClientTest.java`

The public entry point. Its builder composes `JdkHttpTransport → RetryingTransport → ApiExecutor`, creates one `JacksonCodec`, and constructs the endpoints. Default base URL is `https://api.sportmonks.com/v3/football`.

- [ ] **Step 1: Write the failing test**

`FootballClientTest.java`:
```java
package io.github.miro93.sportmonks.football;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest
class FootballClientTest {

    private FootballClient client(String baseUrl) {
        return FootballClient.builder()
                .apiToken(ApiToken.of("tok-77"))
                .baseUrl(baseUrl)
                .build();
    }

    @Test
    void exposesFixturesAndLivescoresEndpoints(WireMockRuntimeInfo wm) {
        var client = client(wm.getHttpBaseUrl());
        assertThat(client.fixtures()).isNotNull();
        assertThat(client.livescores()).isNotNull();
    }

    @Test
    void endToEndFixtureCallSendsAuthAndDecodes(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/fixtures/1")).willReturn(okJson("""
                { "data": { "id": 1, "name": "A vs B" } }
                """)));

        var fixture = client(wm.getHttpBaseUrl()).fixtures().byId(1L).get().data();

        assertThat(fixture.name()).isEqualTo("A vs B");
        verify(getRequestedFor(urlPathEqualTo("/fixtures/1"))
                .withHeader("Authorization", equalTo("tok-77")));
    }

    @Test
    void builderRequiresApiToken() {
        assertThatThrownBy(() -> FootballClient.builder().build())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void defaultBaseUrlIsSportmonksFootballV3() {
        assertThat(FootballClient.DEFAULT_BASE_URL)
                .isEqualTo("https://api.sportmonks.com/v3/football");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :football:test --tests '*FootballClientTest'`
Expected: FAIL — `FootballClient` does not exist.

- [ ] **Step 3: Create `FootballClient`**

```java
package io.github.miro93.sportmonks.football;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.http.HttpTransport;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.retry.RetryPolicy;
import io.github.miro93.sportmonks.core.retry.RetryingTransport;
import io.github.miro93.sportmonks.core.retry.Sleeper;
import io.github.miro93.sportmonks.football.endpoint.FixturesEndpoint;
import io.github.miro93.sportmonks.football.endpoint.LivescoresEndpoint;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Objects;

/// Entry point for the SportMonks football API. Build via {@link #builder()}.
public final class FootballClient {

    public static final String DEFAULT_BASE_URL = "https://api.sportmonks.com/v3/football";

    private final FixturesEndpoint fixtures;
    private final LivescoresEndpoint livescores;

    private FootballClient(FixturesEndpoint fixtures, LivescoresEndpoint livescores) {
        this.fixtures = fixtures;
        this.livescores = livescores;
    }

    public static Builder builder() {
        return new Builder();
    }

    public FixturesEndpoint fixtures() {
        return fixtures;
    }

    public LivescoresEndpoint livescores() {
        return livescores;
    }

    public static final class Builder {
        private ApiToken apiToken;
        private RetryPolicy retryPolicy = RetryPolicy.defaults();
        private String baseUrl = DEFAULT_BASE_URL;
        private Duration requestTimeout = Duration.ofSeconds(30);

        private Builder() {
        }

        public Builder apiToken(ApiToken apiToken) {
            this.apiToken = apiToken;
            return this;
        }

        public Builder retryPolicy(RetryPolicy retryPolicy) {
            this.retryPolicy = Objects.requireNonNull(retryPolicy, "retryPolicy");
            return this;
        }

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl");
            return this;
        }

        public Builder requestTimeout(Duration requestTimeout) {
            this.requestTimeout = Objects.requireNonNull(requestTimeout, "requestTimeout");
            return this;
        }

        public FootballClient build() {
            Objects.requireNonNull(apiToken, "apiToken is required");
            HttpTransport base = new JdkHttpTransport(HttpClient.newHttpClient(), requestTimeout);
            HttpTransport transport = new RetryingTransport(base, retryPolicy, Sleeper.REAL);
            JacksonCodec codec = new JacksonCodec();
            ApiExecutor executor = new ApiExecutor(transport, codec, apiToken, baseUrl);
            return new FootballClient(
                    new FixturesEndpoint(executor, codec),
                    new LivescoresEndpoint(executor, codec));
        }
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :football:test --tests '*FootballClientTest'`
Expected: PASS (4 tests).

- [ ] **Step 5: Run the full suite**

Run: `./gradlew build`
Expected: `BUILD SUCCESSFUL` — all `core` and `football` tests pass.

- [ ] **Step 6: Commit**

```bash
git add football/src/main/java/io/github/miro93/sportmonks/football/FootballClient.java football/src/test/java/io/github/miro93/sportmonks/football/FootballClientTest.java
git commit -m "feat(football): add FootballClient composing transport, retry and endpoints"
```
End the commit message with the Co-Authored-By trailer.

---

## Final verification

- [ ] **Step 1: Full build**

Run: `./gradlew build`
Expected: `BUILD SUCCESSFUL`; both modules compile and every test passes.

- [ ] **Step 2: Confirm no skips/failures**

Run: `./gradlew test --rerun`
Expected: all test classes pass; zero failures, zero skips.

---

## Notes for the implementer

- **`stream()` rebuilds the page each pull:** `CollectionRequest.stream()` calls `spec.page(n).build()` for each page. `RequestSpec.Builder.page(int)` overwrites the page number, and `build()` snapshots, so sequential page fetches reuse the same includes/filters with an advancing page number. The stream is sequential (documented `@implNote`); do not parallelize it.
- **Dates:** `LocalDate.toString()` is ISO `yyyy-MM-dd`, exactly what SportMonks path params expect — string concatenation is intentional, no formatter needed.
- **Search term encoding:** `search(String)` concatenates the term into the path. Tests use a single word ("Celtic"). Multi-word search terms with spaces are out of scope for M2; if needed later, percent-encode the path segment in `search(...)`.
- **Strongly-typed models with forward tolerance:** records model the documented fields; the codec's `FAIL_ON_UNKNOWN_PROPERTIES=false` means new/extra API fields are ignored rather than breaking decoding. Nullable scalars use boxed types; relation includes are `null` when not requested.
- **No new dependencies:** everything uses the JDK + Jackson + the M1 `core` module already on the classpath.
```
