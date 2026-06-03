# M7 — Core API Foundation (CoreClient + géo + Types) — Design

**Date:** 2026-06-03
**Repo:** `miro93/sportmonks-java-api-client`
**Status:** Approved

## Objectif

Introduire la **SportMonks Core API** (`/v3/core`) dans le client : les ressources
**cross-sport** Continents, Countries, Regions, Cities et Types. C'est la première
brique hors football, et elle établit le pattern « ressources partagées multi-sports ».

Premier de deux milestones sur les référentiels :
- **M7 (ce spec)** — ressources Core API (`/v3/core`).
- **M8 (plus tard)** — référentiels football (`/v3/football`) : States, Venues, Referees,
  TV Stations, Commentaries. Spec/plan séparés.

## Décisions structurantes

| Décision | Choix | Raison |
|---|---|---|
| Module | Dans le module `core` existant | Pas de nouvel artefact Maven à publier ; « core = module cross-sport » (infra + ressources cross-sport) reste cohérent avec l'API SportMonks. `football` en dépend déjà. |
| Packages | `core.coreapi` (CoreClient), `core.coreapi.model`, `core.coreapi.endpoint` | Sépare nettement les ressources Core API de l'infra (`core.http`, `core.json`, `core.request`…), qui reste inchangée. |
| Client | `CoreClient` autonome **+** `FootballClient.core()` | Point d'entrée unique côté foot, tout en gardant `CoreClient` utilisable seul. Token/retry/timeout partagés. |
| Réutilisation infra | `CollectionRequest`/`SingleResourceRequest`/`RequestSpec`/`ApiExecutor`/`JacksonCodec` tels quels | Aucun changement d'infra ; calque exact des endpoints football (M4/M5). |

## Base URL

`CoreClient.DEFAULT_BASE_URL = https://api.sportmonks.com/v3/core`

(à comparer à `FootballClient.DEFAULT_BASE_URL = https://api.sportmonks.com/v3/football`)

## Modèles (records `core.coreapi.model`)

Conventions identiques à M4/M5 : seul `id` est `long` primitif ; tout autre scalaire est
un type **boxé nullable**. JavaDoc `///` (JEP 467) sur chaque type, ≥80 % de couverture
(gate CodeRabbit). Champs ci-dessous d'après la doc « entities » Core API ; **à confirmer
contre les payloads d'exemple réels en TDD** (cf. `Coach.cityId` typé `String` car l'API
renvoie une string — même vigilance ici).

| Modèle | Champs (type doc) |
|---|---|
| `Continent` | `id`(long), `name`(String), `code`(String) |
| `Country` | `id`(long), `continentId`(Long), `name`(String), `officialName`(String), `fifaName`(String), `iso2`(String), `iso3`(String), `latitude`(String), `longitude`(String), `geonameid`(Long), `borders`(String), `imagePath`(String) |
| `Region` | `id`(long), `countryId`(Long), `name`(String) |
| `City` | `id`(long), `countryId`(Long), `region`(Long), `name`(String), `latitude`(String), `longitude`(String), `geonameid`(Long) |
| `Type` | `id`(long), `parentId`(Long), `name`(String), `code`(String), `developerName`(String), `group`(String), `description`(String) |

> Notes à vérifier en TDD : `Country.borders` (la doc entité dit `border` au singulier,
> l'overview dit `borders` — confirmer le nom JSON réel) ; `City.region` (la doc nomme le
> champ `region`, **pas** `region_id` — confirmer) ; nullabilité exacte de `latitude`/
> `longitude`/`geonameid`. Aucune relation typée dans ce milestone (les `include` restent
> utilisables en strings via `.include()`).

## Endpoints (`core.coreapi.endpoint`)

Calque exact de `CoachesEndpoint` : champs `ApiExecutor executor`, `DataType<T> single`,
`DataType<List<T>> list` ; helper privé `collection(path)` ; chaque méthode renvoie
`CollectionRequest<T>` ou `SingleResourceRequest<T>`. `search(name)` fait
`Objects.requireNonNull(name, "name")`.

Chemins relatifs à la base `…/v3/core` :

| Endpoint | Méthodes → chemin |
|---|---|
| `ContinentsEndpoint` | `all()` → `continents` · `byId(long)` → `continents/{id}` |
| `CountriesEndpoint` | `all()` → `countries` · `byId(long)` → `countries/{id}` · `search(String)` → `countries/search/{name}` |
| `RegionsEndpoint` | `all()` → `regions` · `byId(long)` → `regions/{id}` · `search(String)` → `regions/search/{name}` |
| `CitiesEndpoint` | `all()` → `cities` · `byId(long)` → `cities/{id}` · `search(String)` → `cities/search/{name}` |
| `TypesEndpoint` | `all()` → `types` · `byId(long)` → `types/{id}` |

## CoreClient

Miroir de `FootballClient` :

- `public static final String DEFAULT_BASE_URL = "https://api.sportmonks.com/v3/core"`.
- 5 champs endpoint + accesseurs `continents()`, `countries()`, `regions()`, `cities()`, `types()`.
- `Builder` fluide : `apiToken(ApiToken)` (requis), `retryPolicy(RetryPolicy)` (défaut
  `RetryPolicy.defaults()`), `baseUrl(String)` (défaut `DEFAULT_BASE_URL`),
  `requestTimeout(Duration)` (défaut 30 s). `build()` assemble transport
  (`JdkHttpTransport` + `RetryingTransport`), `JacksonCodec`, `ApiExecutor`, puis les 5 endpoints.
- **Constructeur public `CoreClient(ApiExecutor executor, JacksonCodec codec)`** qui câble
  les 5 endpoints — réutilisé par `Builder.build()` **et** par `FootballClient`.

## FootballClient.core()

- Nouveau champ `private final CoreClient core;` + accesseur `public CoreClient core()`.
- `Builder` : nouveau `coreBaseUrl(String)` (défaut `CoreClient.DEFAULT_BASE_URL`).
- `Builder.build()` : après avoir créé `transport` + `codec`, crée **deux** `ApiExecutor`
  partageant le même `transport`/`codec`/`apiToken` :
  - un avec `baseUrl` (football) → endpoints football existants,
  - un avec `coreBaseUrl` → `new CoreClient(coreExecutor, codec)` stocké dans `core`.
- Aucun changement de comportement des endpoints football existants.

## Tests (TDD, style M4/M5, WireMock + AssertJ + JUnit 5)

Module `core` (nouveau répertoire de test `core/src/test/.../coreapi/`) :

- `endpoint/ContinentsEndpointTest`, `CountriesEndpointTest`, `RegionsEndpointTest`,
  `CitiesEndpointTest`, `TypesEndpointTest` — WireMock : vérifient le **chemin construit**
  (y compris encodage de `search/{name}`) et le décodage du payload.
- `model/GeographyDecodingTest` (Continent/Country/Region/City) et `model/TypeDecodingTest`
  — décodage pur à partir de JSON d'exemple réel.
- `CoreClientTest` — builder (token requis → `NullPointerException`), base URL par défaut,
  accesseurs non nuls.
- `FootballClientCoreTest` — `core()` non nul, base URL core dérivée correcte (WireMock sur
  un `countries` via `footballClient.core().countries().all()`), override `coreBaseUrl`.

> `core/build.gradle.kts` a déjà `wiremock`/`assertj`/`junit` en `testImplementation` — rien
> à ajouter côté dépendances.

## Docs & release

- README : nouvelle section « Core API » (exemple `CoreClient` + `footballClient.core()`).
- Commits `feat:` conventionnels → release-please met à jour `CHANGELOG.md` automatiquement.

## Hors périmètre (explicite)

- `types/entities` (`/v3/core/types/entities`) : réponse **groupée par entité** (objet
  clé→liste), forme différente d'une collection plate — non modélisé ce milestone.
- Modélisation typée des `include` Core (`continent`, `leagues`, `regions`, `cities`) —
  utilisables en strings via `.include()`, mais pas de relations typées.
- M8 (référentiels football : States, Venues, Referees, TV Stations, Commentaries).
