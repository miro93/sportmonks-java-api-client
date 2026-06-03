# sportmonks-java-api-client

A type-safe Java 25 client for the [SportMonks v3 API](https://sportmonks.com/).

![CI](https://github.com/miro93/sportmonks-java-api-client/actions/workflows/ci.yml/badge.svg)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

---

## Requirements

- Java 25 or later

---

## Installation

> Once the first release is published to Maven Central, add the `sportmonks-football`
> artifact to your build. It pulls `sportmonks-core` transitively — you only need one
> dependency.

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("io.github.miro93.sportmonks:sportmonks-football:0.1.0")
}
```

### Gradle (Groovy DSL)

```groovy
dependencies {
    implementation 'io.github.miro93.sportmonks:sportmonks-football:0.1.0'
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.miro93.sportmonks</groupId>
    <artifactId>sportmonks-football</artifactId>
    <version>0.1.0</version>
</dependency>
```

---

## Quickstart

### Build a client

```java
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.football.FootballClient;

// Read the token from the SPORTMONKS_API_TOKEN environment variable
FootballClient client = FootballClient.builder()
        .apiToken(ApiToken.fromEnv())
        .build();

// Or supply a token directly (useful in tests with a placeholder value)
FootballClient client = FootballClient.builder()
        .apiToken(ApiToken.of("YOUR_TOKEN"))
        .build();
```

### Synchronous request with includes, filters and field selection

```java
import io.github.miro93.sportmonks.core.response.ApiResponse;
import io.github.miro93.sportmonks.football.model.Fixture;
import java.util.List;

ApiResponse<List<Fixture>> response = client.fixtures()
        .all()
        .include("participants", "scores")
        .filter("league_id", "271")
        .select("id", "name", "starting_at")
        .sort("-starting_at")
        .page(1)
        .get();

List<Fixture> fixtures = response.data();
```

### Asynchronous request

```java
import java.util.concurrent.CompletableFuture;

CompletableFuture<ApiResponse<List<Fixture>>> future = client.fixtures()
        .all()
        .include("participants")
        .getAsync();

future.thenAccept(r -> r.data().forEach(f -> System.out.println(f.name())));
```

### Lazy auto-paginated stream

`stream()` automatically fetches subsequent pages on demand — no manual page
tracking needed.

```java
import java.util.stream.Stream;

Stream<Fixture> allFixtures = client.fixtures().all().stream();
allFixtures.forEach(f -> System.out.println(f.id()));
```

---

## Includes / Filters / Select / Sort / Pagination

All request objects (`CollectionRequest` and `SingleResourceRequest`) expose the
same fluent builder methods:

| Method | Description |
|--------|-------------|
| `include(String... values)` | Load related resources (e.g. `"participants"`, `"scores"`) |
| `filter(String name, String... values)` | Filter by an attribute |
| `select(String... fields)` | Restrict the returned fields |
| `sort(String... fields)` | Sort order; prefix with `-` for descending |
| `page(int page)` | 1-based page number (collection requests only; ignored by `stream()`) |
| `get()` | Execute synchronously, returns `ApiResponse<T>` |
| `getAsync()` | Execute asynchronously, returns `CompletableFuture<ApiResponse<T>>` |
| `stream()` | Lazily walk all pages, returns `Stream<T>` |

`ApiResponse<T>` is a record with:

```java
T                    data()          // decoded payload
Pagination           pagination()    // null on single-resource endpoints
RateLimit            rateLimit()     // null if unavailable
String               timezone()
Optional<Pagination> paginationOpt()
Optional<RateLimit>  rateLimitOpt()
```

`Pagination` fields: `count`, `perPage`, `currentPage`, `nextPage`, `hasMore`.

---

## Error Handling

Every error thrown by the client extends the sealed base class
`io.github.miro93.sportmonks.core.error.SportmonksException` (a `RuntimeException`).

```java
import io.github.miro93.sportmonks.core.error.*;

try {
    ApiResponse<List<Fixture>> r = client.fixtures().all().get();
} catch (AuthenticationException e) {
    // HTTP 401 — invalid or missing API token
} catch (RateLimitException e) {
    // HTTP 429 — quota exceeded; e.retryAfter() may give a hint
} catch (NotFoundException e) {
    // HTTP 404 — resource not found
} catch (ValidationException e) {
    // HTTP 422 — request rejected by the API
} catch (ServerException e) {
    // HTTP 5xx — SportMonks server error
} catch (TransportException e) {
    // Network / I-O failure; statusCode() returns -1
} catch (SportmonksException e) {
    // Catch-all for any SportMonks client error
    System.err.println("status=" + e.statusCode() + " " + e.getMessage());
}
```

`RateLimitException.retryAfter()` returns `Optional<Duration>`.

---

## Available Endpoints

All endpoints are accessed via `FootballClient`:

| Accessor | Endpoint class | Example methods |
|----------|---------------|-----------------|
| `fixtures()` | `FixturesEndpoint` | `all()`, `byId(id)`, `byMultipleIds(ids...)`, `byDate(date)`, `byDateRange(start, end)`, `byDateRangeForTeam(start, end, teamId)`, `headToHead(t1, t2)`, `search(name)` |
| `livescores()` | `LivescoresEndpoint` | `all()`, `inplay()`, `latest()` |
| `leagues()` | `LeaguesEndpoint` | `all()`, `byId(id)`, `byCountry(countryId)`, `live()`, `byDate(date)`, `search(name)` |
| `seasons()` | `SeasonsEndpoint` | `all()`, `byId(id)`, `byTeam(teamId)`, `search(name)` |
| `stages()` | `StagesEndpoint` | `all()`, `byId(id)`, `bySeason(seasonId)`, `search(name)` |
| `rounds()` | `RoundsEndpoint` | `all()`, `byId(id)`, `bySeason(seasonId)`, `search(name)` |
| `schedules()` | `SchedulesEndpoint` | `bySeason(seasonId)`, `byTeam(teamId)`, `bySeasonAndTeam(seasonId, teamId)` |
| `teams()` | `TeamsEndpoint` | `all()`, `byId(id)`, `byMultipleIds(ids...)`, `bySeason(seasonId)`, `byCountry(countryId)`, `search(name)` |
| `players()` | `PlayersEndpoint` | `all()`, `byId(id)`, `byCountry(countryId)`, `latest()`, `search(name)` |
| `coaches()` | `CoachesEndpoint` | `all()`, `byId(id)`, `byCountry(countryId)`, `latest()`, `search(name)` |
| `squads()` | `SquadsEndpoint` | `byTeam(teamId)`, `bySeasonAndTeam(seasonId, teamId)` |
| `transfers()` | `TransfersEndpoint` | `all()`, `latest()`, `byTeam(teamId)`, `byPlayer(playerId)`, `byDateRange(start, end)` |
| `standings()` | `StandingsEndpoint` | `all()`, `bySeason(seasonId)`, `byRound(roundId)`, `correctionsBySeason(seasonId)`, `liveByLeague(leagueId)` |
| `topscorers()` | `TopscorersEndpoint` | `bySeason(seasonId)`, `byStage(stageId)` |

---

## Modules

| Module | Artifact | Description |
|--------|----------|-------------|
| `core` | `sportmonks-core` | Sport-agnostic HTTP transport, authentication, codec, request builders, response types, and error hierarchy |
| `football` | `sportmonks-football` | Football/soccer endpoints and model classes; depends on `core` |

To add support for another sport (e.g. cricket), implement a new Gradle module that depends on `sportmonks-core` and follows the same `Endpoint` + model pattern used by the `football` module.

---

## Javadoc

[https://miro93.github.io/sportmonks-java-api-client/](https://miro93.github.io/sportmonks-java-api-client/)

---

## License

This project is licensed under the [Apache License, Version 2.0](LICENSE).
