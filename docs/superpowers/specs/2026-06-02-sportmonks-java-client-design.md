# SportMonks Java Client — Design

**Date:** 2026-06-02
**Repo:** `miro93/sportmonks-java-api-client`
**Status:** Approved

## Objectif

Client Java moderne pour l'API SportMonks v3. Architecture générique multi-sports,
mais la première version cible le **football**. Bibliothèque publiable (Maven Central),
extensible : ajouter un sport = nouveau module dépendant de `core`.

## Décisions techniques

| Aspect | Choix |
|---|---|
| Langage / JDK | Java 25 (toolchain Gradle) |
| Build | Gradle Kotlin DSL, multi-module |
| HTTP | `java.net.http.HttpClient` (JDK, zéro dépendance) |
| JSON | Jackson + module Blackbird (records, sealed, streaming) |
| Concurrence | Virtual threads ; API sync + async (`CompletableFuture`) |
| Résilience | Retry maison (backoff exponentiel + jitter sur 429/5xx), zéro dépendance |
| Tests | JUnit 5 + WireMock (stub HTTP, test-scope) + AssertJ |
| Group Maven | `io.github.miro93.sportmonks` |

### Pourquoi Jackson plutôt qu'une lib « plus rapide »

Pour un client HTTP, la latence réseau domine ; le parsing JSON n'est pas le goulot.
Jackson offre support natif `record`/`sealed`, API streaming pour les gros payloads
livescores, écosystème mature. Le module Blackbird (successeur d'Afterburner) génère
les accesseurs à la volée pour des perfs élevées sans friction. Le gain marginal de
DSL-JSON serait invisible derrière la latence de l'API.

## API SportMonks v3 — rappel structurel

- Base URL : `https://api.sportmonks.com/v3` (football sous `/v3/football`)
- Auth : `api_token` (header `Authorization` ou query param `?api_token=`)
- Includes : séparés par `;`, nesting via `.` (ex: `include=participants;events.player`)
- Filtres, sélection de champs (`select`), tri (`sort`)
- Pagination : objet `pagination` dans la réponse (`has_more`, `next_page`)
- Rate limit : ~3000 req/h par entité, HTTP 429, objet `rate_limit` dans chaque réponse
- Réponse JSON : `{ data, pagination, subscription, rate_limit, timezone }`

## Structure des modules

```
sportmonks-java-api-client/
├── settings.gradle.kts            # inclut core + football
├── build.gradle.kts               # config commune (toolchain 25, repos, publish)
├── core/                          # générique, agnostique du sport
│   └── io.github.miro93.sportmonks.core
│       ├── http/      → HttpTransport, JdkHttpTransport (wrap HttpClient)
│       ├── auth/      → ApiToken (builder + env SPORTMONKS_API_TOKEN)
│       ├── request/   → RequestSpec, Include, Filter, Select, Sort (builder fluide)
│       ├── response/  → ApiResponse<T>, Pagination, RateLimit, Meta
│       ├── paging/    → Page<T> + Iterator/Stream auto-paginé
│       ├── retry/     → RetryPolicy, Backoff (expo + jitter)
│       ├── json/      → JacksonCodec (Blackbird, records)
│       └── error/     → SportmonksException + sous-types
└── football/                      # dépend de core
    └── io.github.miro93.sportmonks.football
        ├── FootballClient         # point d'entrée + builder
        ├── endpoint/  → Fixtures, Livescores, Leagues, Seasons, Stages,
        │                Teams, Players, Squads, Standings, Topscorers
        └── model/     → records: Fixture, Livescore, League, Season, Stage,
                         Team, Player, Standing, Topscorer…
```

Le module `core` ne connaît rien au football. Ajouter un sport = nouveau module
(`cricket/`, etc.) dépendant de `core`.

## Modèle d'usage cible

```java
var client = FootballClient.builder()
        .apiToken(ApiToken.fromEnv())          // ou .apiToken("xxx")
        .retryPolicy(RetryPolicy.defaults())
        .build();

// Sync
ApiResponse<Fixture> r = client.fixtures()
        .byId(18535517)
        .include("participants", "events.player", "scores")
        .filter("eventTypes", 15, 16)
        .select("name", "starting_at")
        .get();

// Async
CompletableFuture<ApiResponse<List<Fixture>>> f = client.fixtures()
        .byDateRange(LocalDate.now().minusDays(7), LocalDate.now())
        .getAsync();

// Pagination auto (Stream paresseux, suit pagination.has_more)
client.leagues().all().stream().forEach(System.out::println);
```

## Flux d'une requête

`Endpoint.byXxx()` → `RequestSpec` (path + includes/filters/select/sort)
→ `HttpTransport` construit l'URL (`include=a;b.c`, filtres, header `Authorization`)
→ exécute via `HttpClient` → `RetryPolicy` réessaie sur 429/5xx en lisant `rate_limit`
→ `JacksonCodec` mappe le JSON en `ApiResponse<T>` typé.

## Hiérarchie d'erreurs (sealed)

`SportmonksException` (scellée) :
- `AuthenticationException` (401)
- `NotFoundException` (404)
- `RateLimitException` (429, expose `retryAfter`)
- `ValidationException` (4xx)
- `ServerException` (5xx)
- `TransportException` (I/O)

En async, le `CompletableFuture` est complété exceptionnellement.

## Tests

JUnit 5 + WireMock (test-scope) + AssertJ. Fixtures JSON réalistes →
tests parsing, includes/filtres dans l'URL, pagination, retry/backoff, mapping erreurs.
Aucun appel réseau réel en CI.

## Plan de livraison (milestones)

1. **Core foundation** — transport, auth, request builder, JSON codec, erreurs, retry, pagination
2. **Football: Fixtures & Livescores**
3. **Football: Leagues/Seasons/Stages**
4. **Football: Teams/Players/Squads**
5. **Football: Standings/Topscorers**
6. **Packaging & publication** — CI GitHub Actions, doc, publish Maven Central

Une issue par composant/endpoint, rattachée à son milestone.
