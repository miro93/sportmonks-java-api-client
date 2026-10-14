# sportmonks-java-api-client

A type-safe Java 25 client for the [SportMonks v3 football API](https://docs.sportmonks.com/football).

![CI](https://github.com/miro93/sportmonks-java-api-client/actions/workflows/ci.yml/badge.svg)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![OpenSSF Scorecard](https://api.scorecard.dev/projects/github.com/miro93/sportmonks-java-api-client/badge)](https://scorecard.dev/viewer/?uri=github.com/miro93/sportmonks-java-api-client)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.miro93.sportmonks/sportmonks-football)](https://central.sonatype.com/artifact/io.github.miro93.sportmonks/sportmonks-football)

> [!IMPORTANT]
> **Unofficial library.** This is an independent, community-maintained client. It is **not**
> affiliated with, endorsed by, or supported by SportMonks, and the maintainers are not part of
> the SportMonks team. "SportMonks" and related names are trademarks of their respective owners.
> You need your own SportMonks subscription and API token to use it.
>
> - Official website: <https://www.sportmonks.com>
> - Official API documentation: <https://docs.sportmonks.com>

---

## Requirements

- Java 25 or later

---

## Installation

Available on Maven Central. Add the `sportmonks-football` artifact to your build — it
pulls `sportmonks-core` transitively, so you only need one dependency. For the Motorsport
API (Formula 1), add `sportmonks-motorsport` instead (or as well); see
[Motorsport API](#motorsport-api). The snippets below
pin `3.0.0`; check the Maven Central badge at the top of this file for the latest version.

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("io.github.miro93.sportmonks:sportmonks-football:3.0.0")
}
```

### Gradle (Groovy DSL)

```groovy
dependencies {
    implementation 'io.github.miro93.sportmonks:sportmonks-football:3.0.0'
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.miro93.sportmonks</groupId>
    <artifactId>sportmonks-football</artifactId>
    <version>3.0.0</version>
</dependency>
```

---

## Migrating from 2.x

`3.0.0` replaces Jackson with Helidon's compile-time JSON binding. Breaking changes:

- Jackson is gone from both the public API surface and the module graph — `sportmonks-football`
  and `sportmonks-core` no longer depend on `tools.jackson.core`/`tools.jackson.databind`, and
  neither `module-info.java` opens packages to them anymore.
- `JacksonCodec` is renamed to `HelidonJsonCodec` (same package, same public methods).
- The free-form-payload records — `StatisticDetail`, `Prediction`, `Expected`, and
  `Predictability` — now take an `io.helidon.json.JsonValue` (an `@Api.Preview` Helidon type) in
  their canonical constructor instead of `java.util.Map`. Their public accessors
  (`StatisticDetail.value()`, `Prediction.predictions()`, `Expected.data()`,
  `Predictability.data()`) are unchanged and still return `Map<String, Object>` — only code that
  constructed these records directly (rather than decoding them) is affected.

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
import io.github.miro93.sportmonks.core.request.Order;
import io.github.miro93.sportmonks.core.response.ApiResponse;
import io.github.miro93.sportmonks.football.endpoint.Includes;
import io.github.miro93.sportmonks.football.model.Fixture;
import java.util.List;

ApiResponse<List<Fixture>> response = client.fixtures()
        .all()
        .include(Includes.Fixtures.PARTICIPANTS, Includes.Fixtures.SCORES)
        .filter("league_id", "271")
        .select("id", "name", "starting_at")
        .sortBy("starting_at")
        .order(Order.DESC)
        .page(1)
        .get();

List<Fixture> fixtures = response.data();
```

### Typed dates

Date fields stay `String` on the records (the API format is stable), with null-safe
typed accessors next to them: `Fixture.startingAtDateTime()` / `startingAtInstant()`,
`Player.dateOfBirthDate()`, `Team.lastPlayedAtDateTime()`, `Season.startingAtDate()`,
`Transfer.localDate()`, ... Date-times are in the requested `timezone` (UTC by default).

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
tracking needed. It follows the server's `next_cursor` (cursor-based pagination), so it
is not subject to the 20 000-row offset cap that applies to `page(int)`, and it requests
the next page while you consume the current one (one-page lookahead), so a consumer that
stops early has triggered at most one extra request.

```java
import java.util.stream.Stream;

Stream<Fixture> allFixtures = client.fixtures().all().stream();
allFixtures.forEach(f -> System.out.println(f.id()));
```

### Core API (cross-sport reference data)

The Core API (`/v3/core`) exposes cross-sport reference resources: continents,
countries, regions, cities and types. Use it standalone:

```java
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.coreapi.CoreClient;
import io.github.miro93.sportmonks.core.coreapi.model.Country;
import io.github.miro93.sportmonks.core.coreapi.model.Type;
import java.util.List;

CoreClient core = CoreClient.builder()
        .apiToken(ApiToken.of(System.getenv("SPORTMONKS_API_TOKEN")))
        .build();

List<Country> countries = core.countries().search("Scot").get().data();
List<Type> types = core.types().all().get().data();
Map<String, EntityTypes> perEntity = core.types().byEntity().get().data(); // keyed by entity name
```

…or reach it from a football client, which shares the same credentials:

```java
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.coreapi.model.Country;
import io.github.miro93.sportmonks.football.FootballClient;

FootballClient football = FootballClient.builder()
        .apiToken(ApiToken.of(System.getenv("SPORTMONKS_API_TOKEN")))
        .build();

Country country = football.core().countries().byId(320L).get().data();
```

### Configuring the HTTP transport

Both `CoreClient.Builder` and `FootballClient.Builder` accept a custom
`java.net.http.HttpClient`, letting you set a proxy, SSL context, authenticator,
HTTP version preference, or executor:

```java
HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .proxy(ProxySelector.getDefault())
        .build();

FootballClient client = FootballClient.builder()
        .apiToken(ApiToken.of(System.getenv("SPORTMONKS_API_TOKEN")))
        .httpClient(httpClient)
        .build();
```

When no client is supplied, a default one is used: a 10-second connect timeout, **no**
redirect following (the API never redirects, and following one to another host could
replay the `Authorization` header there), with the JDK's default HTTP/2 (falling back to
HTTP/1.1).

Two timeout dimensions apply, mirroring the JDK client:

- **connect timeout** — on the `HttpClient`, bounds connection establishment.
- **request timeout** — `requestTimeout(Duration)` on the builder (default 30s), the
  request→response deadline (`HttpTimeoutException` if exceeded).

Every request asks for `gzip` and the transport decompresses gzipped responses
transparently (the cap below applies to the decompressed size).

Response bodies are capped at 32 MiB by default (`maxResponseSize(long)` on the builders);
a larger body fails with `TransportException` instead of being buffered in memory.

Base URLs must be `https`; plain `http` is only accepted on `localhost` / `127.0.0.1` /
`::1` (stubs, local proxies), so a token is never sent in clear text by accident.

Connection pooling is automatic (keep-alive plus HTTP/2 multiplexing). The JDK exposes
no builder-level pool sizing; tune it via the `jdk.httpclient.*` system properties if
needed.

If you only need to change the connection-establishment timeout (and not the whole client),
use `connectTimeout(...)` instead — it tunes the built-in default client:

```java
FootballClient client = FootballClient.builder()
        .apiToken(ApiToken.fromEnv())
        .connectTimeout(Duration.ofSeconds(5))   // default: 10s
        .build();
```

`connectTimeout(...)` and `httpClient(...)` are **mutually exclusive**: a client you supply
already carries its own connect timeout, so setting both throws `IllegalStateException` at
`build()`. `connectTimeout` bounds connection establishment; `requestTimeout` bounds the
request→response deadline.

### Observing requests (logging, metrics)

`listener(RequestListener)` on either builder receives every HTTP attempt, retries
included, with the URI, status and elapsed time. Hook it up to SLF4J or Micrometer:

```java
import io.github.miro93.sportmonks.core.request.RequestListener;

FootballClient client = FootballClient.builder()
        .apiToken(ApiToken.fromEnv())
        .listener(new RequestListener() {
            @Override
            public void onResponse(URI uri, int status, Duration elapsed) {
                Timer.builder("sportmonks.request").tag("status", String.valueOf(status))
                        .register(registry).record(elapsed);
            }
            @Override
            public void onError(URI uri, Throwable error, Duration elapsed) {
                log.warn("SportMonks call {} failed after {}", uri.getPath(), elapsed, error);
            }
        })
        .build();
```

---

### Configuring from Spring, Quarkus or Helidon

This library reads **no implicit configuration source** — every option is set explicitly on the
builder. Framework config files (`application.properties` / `application.yaml`) are not exposed as
JVM system properties, so map your framework's config to the builder setters with a few lines.
(JVM `-D` system properties, e.g. `-Dhttp.proxyHost`, still apply at the JDK level.)

**Spring Boot** — bind properties and expose the client as a bean:

```java
@ConfigurationProperties("sportmonks")
record SportmonksProps(Duration connectTimeout, Duration requestTimeout, int maxAttempts) {}

@Bean
FootballClient footballClient(SportmonksProps props) {
    return FootballClient.builder()
            .apiToken(ApiToken.fromEnv())
            .connectTimeout(props.connectTimeout())
            .requestTimeout(props.requestTimeout())
            // maxAttempts is total tries (e.g. 3 = 1 initial + 2 retries)
            .retryPolicy(RetryPolicy.builder().maxAttempts(props.maxAttempts()).build())
            .build();
}
```

**Quarkus** — inject MicroProfile config and produce a CDI bean:

```java
@Produces
@ApplicationScoped
FootballClient footballClient(
        @ConfigProperty(name = "sportmonks.connect-timeout") Duration connectTimeout,
        @ConfigProperty(name = "sportmonks.request-timeout") Duration requestTimeout) {
    return FootballClient.builder()
            .apiToken(ApiToken.fromEnv())
            .connectTimeout(connectTimeout)
            .requestTimeout(requestTimeout)
            .build();
}
```

**Helidon** — read from `Config` and build:

```java
Config cfg = Config.global().get("sportmonks");
FootballClient client = FootballClient.builder()
        .apiToken(ApiToken.fromEnv())
        .connectTimeout(cfg.get("connect-timeout").as(Duration.class).orElse(Duration.ofSeconds(10)))
        .requestTimeout(cfg.get("request-timeout").as(Duration.class).orElse(Duration.ofSeconds(30)))
        .build();
```

---

## Java Module System (JPMS)

The library ships as named modules:

- `io.github.miro93.sportmonks.core`
- `io.github.miro93.sportmonks.football`

If your application is modular, require the football module (it transitively requires core):

```java
module your.app {
    requires io.github.miro93.sportmonks.football;
}
```

Internal plumbing (HTTP transport, JSON codec, request building, retry internals) is
encapsulated and not part of the public API. The library also works unchanged on the classpath.

---

## GraalVM native-image

The library is native-image ready: it ships reachability metadata under
`META-INF/native-image`, so an application that depends on it compiles to a native image
without adding any reflection configuration of its own. JSON decoding goes through Helidon
JSON's compile-time-generated binding — no runtime class scanning, dynamic proxies, or
reflective field access — and the shipped metadata only covers the record-constructor
reflection Helidon's runtime performs during decode. That metadata is generated at build
time from the compiled classes (one entry per `@Json.Entity` converter descriptor, plus the
fixed set of Helidon runtime types), so it can never drift from the code; the `native-smoke`
CI job compiles a native image against it and decodes every entity.

---

## Includes / Filters / Select / Sort / Pagination

Both request types (`CollectionRequest` and `SingleResourceRequest`) share the
fluent builder methods `include`, `filter`, `select`, `sortBy`, `order`, `locale`,
`timezone`, `get` and `getAsync`. Paging (`page`, `perPage`, `cursor`, `stream`) is
specific to `CollectionRequest`:

| Method | Description |
|--------|-------------|
| `include(String... values)` | Load related resources. `Includes.<Group>.<NAME>` holds the documented names per endpoint group (e.g. `Includes.Fixtures.PARTICIPANTS`); `Includes.nested("events", "player")` builds `events.player` |
| `filter(String name, String... values)` | Filter by an attribute |
| `select(String... fields)` | Restrict the returned fields |
| `sortBy(String field)` | Field to sort by (a single field; the API rejects more) |
| `order(Order order)` | `Order.ASC` / `Order.DESC`; without `sortBy` it orders by id |
| `locale(String locale)` | Translate name fields (e.g. `"de"`) |
| `timezone(ZoneId zone)` | Timezone for returned date-times |
| `perPage(int perPage)` | Page size, 1 to 50 (API default 25). `stream()` sends it on the first request only (the API rejects it with `cursor`) |
| `page(int page)` | 1-based page number (`CollectionRequest` only; ignored by `stream()`). Offset pagination is rejected by SportMonks beyond 20 000 rows (`(page - 1) × per_page > 20000`, i.e. page 801 at the default `per_page=25`) |
| `cursor(String cursor)` | Pass back a previous response's `pagination.nextCursor()` to fetch the next page (`CollectionRequest` only; ignored by `stream()`) |
| `get()` | Execute synchronously, returns `ApiResponse<T>` |
| `getAsync()` | Execute asynchronously, returns `CompletableFuture<ApiResponse<T>>` |
| `stream()` | Lazily walk all pages, returns `Stream<T>` (`CollectionRequest` only). Follows `next_cursor` when the server returns one, otherwise increments `page` |

`ApiResponse<T>` is a record with:

```java
T                    data()          // decoded payload
Pagination           pagination()    // null on single-resource endpoints
RateLimit            rateLimit()     // null if unavailable
String               timezone()
Optional<Pagination> paginationOpt()
Optional<RateLimit>  rateLimitOpt()
```

`Pagination` fields: `count`, `perPage`, `currentPage`, `nextPage`, `nextCursor`, `hasMore`.
`nextCursor` is `null` on the last page; `nextPage` is the legacy offset-based link.

---

## Rate limits

SportMonks allows 3000 requests per hour **per entity**. Every response carries the
remaining quota (`ApiResponse.rateLimit()`); the client also keeps the last block seen per
entity, keyed by the first path segment, until its window resets:

```java
Map<String, RateLimit> quota = client.rateLimits();   // e.g. {"fixtures": RateLimit[remaining=2874, ...]}
```

An optional guard stops you from burning the quota blindly (off by default):

```java
import io.github.miro93.sportmonks.core.ratelimit.RateLimitPolicy;

FootballClient client = FootballClient.builder()
        .apiToken(ApiToken.fromEnv())
        .rateLimitPolicy(RateLimitPolicy.waitBelow(50))   // sleep until the window resets
        // .rateLimitPolicy(RateLimitPolicy.failBelow(50)) // or throw RateLimitException (retryAfter set)
        .build();
```

The guard runs before a request is sent, using the last block seen for that entity. It
is shared by the football, core and odds executors of a client.

---

## Retry policy

By default the client retries `429` and all `5xx` responses, up to 3 attempts, with jittered
exponential backoff. Override any of these via `RetryPolicy.builder()`:

```java
import io.github.miro93.sportmonks.core.retry.RetryPolicy;

RetryPolicy retry = RetryPolicy.builder()
        .maxAttempts(5)
        .retryableStatuses(429, 502, 503, 504)   // replaces the default 429-and-all-5xx rule
        .build();

FootballClient client = FootballClient.builder()
        .apiToken(ApiToken.fromEnv())
        .retryPolicy(retry)
        .build();
```

`retryableStatuses(...)` replaces the default rule entirely with exact membership of the codes
you list.

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
| `fixtures()` | `FixturesEndpoint` | `all()`, `byId(id)`, `byMultipleIds(ids...)`, `byDate(date)`, `byDateRange(start, end)`, `byDateRangeForTeam(start, end, teamId)`, `headToHead(t1, t2)`, `search(name)`, `latest()`, `upcomingByMarket(marketId)`, `upcomingByTvStation(tvStationId)`, `pastByTvStation(tvStationId)` |
| `livescores()` | `LivescoresEndpoint` | `all()`, `inplay()`, `latest()` (since 2026-03-26 the `odds` and `premiumOdds` includes are no longer accepted here) |
| `leagues()` | `LeaguesEndpoint` | `all()`, `byId(id)`, `byCountry(countryId)`, `live()`, `byDate(date)`, `search(name)` |
| `seasons()` | `SeasonsEndpoint` | `all()`, `byId(id)`, `byTeam(teamId)`, `brackets(seasonId)`, `search(name)` |
| `stages()` | `StagesEndpoint` | `all()`, `byId(id)`, `bySeason(seasonId)`, `search(name)` |
| `rounds()` | `RoundsEndpoint` | `all()`, `byId(id)`, `bySeason(seasonId)`, `search(name)` |
| `schedules()` | `SchedulesEndpoint` | `bySeason(seasonId)`, `byTeam(teamId)`, `bySeasonAndTeam(seasonId, teamId)` |
| `teams()` | `TeamsEndpoint` | `all()`, `byId(id)`, `byMultipleIds(ids...)`, `bySeason(seasonId)`, `byCountry(countryId)`, `search(name)` |
| `players()` | `PlayersEndpoint` | `all()`, `byId(id)`, `byCountry(countryId)`, `latest()`, `search(name)` |
| `coaches()` | `CoachesEndpoint` | `all()`, `byId(id)`, `byCountry(countryId)`, `latest()`, `search(name)` |
| `squads()` | `SquadsEndpoint` | `byTeam(teamId)`, `bySeasonAndTeam(seasonId, teamId)`, `extendedByTeam(teamId)` |
| `transfers()` | `TransfersEndpoint` | `all()`, `latest()`, `byTeam(teamId)`, `byPlayer(playerId)`, `byDateRange(start, end)` |
| `standings()` | `StandingsEndpoint` | `all()`, `bySeason(seasonId)`, `byRound(roundId)`, `correctionsBySeason(seasonId)`, `liveByLeague(leagueId)` |
| `topscorers()` | `TopscorersEndpoint` | `bySeason(seasonId)`, `byStage(stageId)` |
| `states()` | `StatesEndpoint` | `all()`, `byId(id)` |
| `venues()` | `VenuesEndpoint` | `all()`, `byId(id)`, `bySeason(seasonId)`, `search(name)` |
| `referees()` | `RefereesEndpoint` | `all()`, `byId(id)`, `byCountry(countryId)`, `bySeason(seasonId)`, `search(name)` |
| `tvStations()` | `TvStationsEndpoint` | `all()`, `byId(id)`, `byFixture(fixtureId)` |
| `commentaries()` | `CommentariesEndpoint` | `all()`, `byFixture(fixtureId)` |
| `bookmakers()` | `BookmakersEndpoint` | `all()`, `byId(id)`, `search(name)`, `byFixture(fixtureId)` |
| `markets()` | `MarketsEndpoint` | `all()`, `byId(id)`, `search(name)` |
| `preMatchOdds()` | `PreMatchOddsEndpoint` | `all()`, `byFixture(fixtureId)`, `byFixtureAndBookmaker(fixtureId, bookmakerId)`, `byFixtureAndMarket(fixtureId, marketId)`, `latest()` |
| `inplayOdds()` | `InplayOddsEndpoint` | `all()`, `byFixture(fixtureId)`, `byFixtureAndBookmaker(fixtureId, bookmakerId)`, `byFixtureAndMarket(fixtureId, marketId)`, `latest()` |
| `premiumOdds()` | `PremiumOddsEndpoint` | `all()`, `byFixture(fixtureId)`, `byFixtureAndBookmaker(fixtureId, bookmakerId)`, `byFixtureAndMarket(fixtureId, marketId)`, `updatedBetween(fromEpochSeconds, toEpochSeconds)` |
| `premiumOddsHistory()` | `PremiumOddsHistoryEndpoint` | `all()`, `updatedBetween(fromEpochSeconds, toEpochSeconds)` |
| `premiumMarkets()` | `PremiumMarketsEndpoint` | `all()` (served from the `/v3/odds` base URL) |
| `premiumBookmakers()` | `PremiumBookmakersEndpoint` | `all()` (served from the `/v3/odds` base URL) |
| `expectedLineups()` | `ExpectedLineupsEndpoint` | `byTeam(teamId)`, `byPlayer(playerId)` |
| `predictions()` | `PredictionsEndpoint` | `probabilities()`, `probabilitiesByFixture(fixtureId)`, `liveProbabilities()`, `liveProbabilitiesByFixture(fixtureId)`, `valueBets()`, `valueBetsByFixture(fixtureId)`, `predictabilityByLeague(leagueId)` |
| `statistics()` | `StatisticsEndpoint` | `seasonByTeam(teamId)`, `seasonByPlayer(playerId)`, `seasonByCoach(coachId)`, `seasonByReferee(refereeId)`, `byStage(stageId)`, `byRound(roundId)` |
| `expected()` | `ExpectedEndpoint` | `fixtures()`, `lineups()` |
| `matchFacts()` | `MatchFactsEndpoint` | `all()`, `byFixture(fixtureId)`, `byDateRange(start, end)`, `byLeague(leagueId)` |
| `transferRumours()` | `TransferRumoursEndpoint` | `all()`, `byId(id)`, `byDateRange(start, end)`, `byTeam(teamId)`, `byPlayer(playerId)`, `config()` |
| `rivals()` | `RivalsEndpoint` | `all()`, `byTeam(teamId)` |
| `news()` | `NewsEndpoint` | `preMatch()`, `preMatchBySeason(seasonId)`, `preMatchUpcoming()`, `postMatch()`, `postMatchBySeason(seasonId)` |
| `teamRankings()` | `TeamRankingsEndpoint` | `all()`, `byTeam(teamId)`, `byDate(date)` (beta) |
| `teamOfTheWeek()` | `TeamOfTheWeekEndpoint` | `all()`, `byRound(roundId)`, `latestByLeague(leagueId)` (beta) |
| `bookmakerMappings()` | `BookmakerMappingsEndpoint` | `byFixture(fixtureId)` (served from the `/v3/odds` base URL) |

Premium markets and bookmakers (`premiumMarkets()`, `premiumBookmakers()`) and the bookmaker
match-id mappings (`bookmakerMappings()`) are served from a
separate odds base URL (`/v3/odds`), configurable via `oddsBaseUrl(...)` on the builder; all
other football endpoints — including the premium odds feed itself — use `/v3/football`.

The Core API reference resources are reachable via `CoreClient` (standalone) or
`FootballClient.core()`:

| Accessor | Endpoint class | Example methods |
|----------|---------------|-----------------|
| `continents()` | `ContinentsEndpoint` | `all()`, `byId(id)` |
| `countries()` | `CountriesEndpoint` | `all()`, `byId(id)`, `search(name)` |
| `regions()` | `RegionsEndpoint` | `all()`, `byId(id)`, `search(name)` |
| `cities()` | `CitiesEndpoint` | `all()`, `byId(id)`, `search(name)` |
| `types()` | `TypesEndpoint` | `all()`, `byId(id)` |

---

## Motorsport API

`sportmonks-motorsport` covers the SportMonks Motorsport API v3 (`/v3/motorsport`, Formula 1)
with the same builder, request options, pagination, retry, rate-limit and listener features
as the football client:

```java
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.motorsport.MotorsportClient;
import io.github.miro93.sportmonks.motorsport.model.Stage;

MotorsportClient f1 = MotorsportClient.builder()
        .apiToken(ApiToken.fromEnv())
        .build();

List<Stage> weekends = f1.schedules().bySeason(26733L).get().data();   // race weekends + sessions
var standings = f1.standings().driversBySeason(26733L).get().data();
var results = f1.raceResults().bySeasonAndDriver(26733L, 37920800L).get().data(); // lineups + details per race
```

| Accessor | Example methods |
|----------|-----------------|
| `drivers()` | `all()`, `byId(id)`, `byCountry(countryId)`, `bySeason(seasonId)`, `search(name)`, `latest()` |
| `fixtures()` | `all()`, `byId(id)`, `byMultipleIds(ids...)`, `byDate(date)`, `byDateRange(start, end)`, `latest()` |
| `laps()`, `pitstops()`, `stints()` | `byFixture(fixtureId)`, `byFixtureAndDriver(fixtureId, driverId)`, `byFixtureAndNumber(fixtureId, n)`, `latestByFixture(fixtureId)` |
| `livescores()` | `all()` |
| `leagues()` | `all()`, `byId(id)`, `byCountry(countryId)`, `byDate(date)`, `live()`, `search(name)`, `byTeam(teamId)`, `currentByTeam(teamId)` |
| `raceResults()` | `bySeasonAndDriver(seasonId, driverId)`, `bySeasonAndTeam(seasonId, teamId)` |
| `schedules()` | `bySeason(seasonId)` |
| `seasons()` | `all()`, `byId(id)` |
| `stages()` | `all()`, `byId(id)`, `bySeason(seasonId)`, `search(name)` |
| `standings()` | `drivers()`, `teams()`, `driversBySeason(seasonId)`, `teamsBySeason(seasonId)` |
| `states()` | `all()`, `byId(id)` |
| `teams()` | `all()`, `byId(id)`, `byCountry(countryId)`, `bySeason(seasonId)`, `search(name)` |
| `venues()` | `all()`, `byId(id)`, `bySeason(seasonId)`, `search(name)` |

`core()` gives the shared Core API client, as on the football client.

---

## Modules

| Module | Artifact | Description |
|--------|----------|-------------|
| `core` | `sportmonks-core` | Sport-agnostic HTTP transport, authentication, codec, request builders, response types, and error hierarchy |
| `football` | `sportmonks-football` | Football/soccer endpoints and model classes; depends on `core` |
| `motorsport` | `sportmonks-motorsport` | Motorsport API v3 (Formula 1) endpoints and model classes; depends on `core` |

SportMonks' cricket and legacy Formula One APIs are v2 APIs with a different envelope and
base URL, so they cannot be built on `core`. Any further v3 sport is a new Gradle module
that depends on `sportmonks-core` and follows the `Endpoint` + model pattern of `football`.

---

## Javadoc

[https://miro93.github.io/sportmonks-java-api-client/](https://miro93.github.io/sportmonks-java-api-client/)

---

## License

This project is licensed under the [Apache License, Version 2.0](LICENSE).
