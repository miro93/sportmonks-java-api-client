# M8 — Football Referentials (States / Venues / Referees / TV Stations / Commentaries) — Design

**Date:** 2026-06-03
**Repo:** `miro93/sportmonks-java-api-client`
**Status:** Approved

## Objectif

Second et dernier milestone « référentiels ». Ajoute les ressources **football-specific**
de l'API SportMonks (`/v3/football`) : States, Venues, Referees, TV Stations, Commentaries.
Complète M7 (Core API, `/v3/core`, déjà mergé).

Architecture = continuation directe du design M7 approuvé : modèles dans `football.model`,
endpoints dans `football.endpoint`, accesseurs sur `FootballClient`, pattern d'endpoint
identique à M4/M5 (clone de `CoachesEndpoint`). Aucun changement d'infra : réutilise
`CollectionRequest`/`SingleResourceRequest`/`RequestSpec`/`ApiExecutor`/`JacksonCodec`.

## Modèles (records `football.model`)

Conventions M4/M5 : `id` en `long` primitif ; tout autre scalaire boxé nullable ;
`///` JavaDoc (≥80 % de couverture, gate CodeRabbit). Champs d'après la doc « entities »,
**à confirmer contre les payloads réels en TDD** (les tests de décodage pinnent les écarts).

| Modèle | Champs (type) |
|---|---|
| `State` | `id`(long), `state`(String), `name`(String), `shortName`(String), `developerName`(String) |
| `Venue` | `id`(long), `countryId`(Long), `cityId`(String⚠️), `name`(String), `address`(String), `zipcode`(String), `latitude`(String), `longitude`(String), `capacity`(Integer), `imagePath`(String), `cityName`(String), `surface`(String), `nationalTeam`(Boolean) |
| `Referee` | calque exact de `Coach` : `id`(long), `sportId`(Long), `countryId`(Long), `cityId`(String), `commonName`(String), `firstname`(String), `lastname`(String), `name`(String), `displayName`(String), `imagePath`(String), `height`(Integer), `weight`(Integer), `dateOfBirth`(String), `gender`(String) |
| `TvStation` | `id`(long), `name`(String), `url`(String), `imagePath`(String) |
| `Commentary` | **`id`(String⚠️)**, `fixtureId`(Long), `comment`(String), `minute`(Integer), `extraMinute`(Integer), `isGoal`(Boolean), `isImportant`(Boolean), `order`(Integer) |

### Écarts de convention (notés et délibérés)

- **`Commentary.id` est une `String`**, pas un `long` — la doc type l'id de Commentary en
  string. C'est le seul modèle du projet dont l'id n'est pas `long`. La JavaDoc du record
  doit l'expliciter. Pas de méthode `byId` pour Commentaries (l'API n'en expose pas).
- **`cityId` typé `String`** sur `Venue` et `Referee` : le précédent `Coach.cityId` montre
  que l'API renvoie `city_id` en string. À confirmer en TDD pour `Venue` (pour `Referee`,
  calque assumé de `Coach`).
- Champs booléens `isGoal`/`isImportant` : composants de record nommés `isGoal`/`isImportant`
  → la stratégie SNAKE_CASE de Jackson les mappe sur `is_goal`/`is_important`.

## Endpoints (`football.endpoint`, base `https://api.sportmonks.com/v3/football`)

Calque de `CoachesEndpoint` : champs `executor`/`single`/`list`, helper privé `collection(path)`
pour les ressources à `all()`+ autres collections, `search(name)` avec
`Objects.requireNonNull(name,"name")`.

| Endpoint | Méthodes → chemin |
|---|---|
| `StatesEndpoint` | `all()` → `states` · `byId(long)` → `states/{id}` |
| `VenuesEndpoint` | `all()` → `venues` · `byId(long)` → `venues/{id}` · `bySeason(long)` → `venues/seasons/{id}` · `search(String)` → `venues/search/{name}` |
| `RefereesEndpoint` | `all()` → `referees` · `byId(long)` → `referees/{id}` · `byCountry(long)` → `referees/countries/{id}` · `bySeason(long)` → `referees/seasons/{id}` · `search(String)` → `referees/search/{name}` |
| `TvStationsEndpoint` | `all()` → `tv-stations` · `byId(long)` → `tv-stations/{id}` · `byFixture(long)` → `tv-stations/fixtures/{id}` |
| `CommentariesEndpoint` | `all()` → `commentaries` · `byFixture(long)` → `commentaries/fixtures/{id}` |

(Chemins `bySeason`/`byCountry`/`byFixture` confirmés depuis la doc.)

## FootballClient

- 5 nouveaux champs + accesseurs : `states()`, `venues()`, `referees()`, `tvStations()`,
  `commentaries()`.
- Câblés dans `Builder.build()` sur l'`executor` football **existant** (base URL football) —
  ajoutés comme arguments du constructeur privé. `core()` (M7) et les 14 endpoints existants
  restent inchangés.

## Tests (TDD, WireMock + AssertJ + JUnit 5)

- 5 tests d'endpoint (`StatesEndpointTest`, `VenuesEndpointTest`, `RefereesEndpointTest`,
  `TvStationsEndpointTest`, `CommentariesEndpointTest`) — chemin (dont encodage `search/{name}`,
  `seasons/{id}`, `countries/{id}`, `fixtures/{id}`) + décodage + rejet `search(null)`.
- Tests de décodage : `VenueRefereeDecodingTest` et `StateTvCommentaryDecodingTest` —
  cas tous-champs-présents + tous-optionnels-absents (nullable), et pinning des écarts
  (`Commentary.id` String, `Venue.cityId`/`Referee.cityId` String, booléens `is_goal`/`is_important`).
- `FootballClientM8Test` — accesseurs non nuls + 2-3 appels routés (auth header + chemin).

## Docs & release

- README : ajout des 5 ressources à la table des endpoints football.
- Commits `feat:`/`test:`/`docs:` conventionnels → release-please met à jour le CHANGELOG.

## Hors périmètre (explicite)

- Champs de jointure éventuels de `tv-stations/fixtures/{id}` (`type_id`, `related_id`) — non
  modélisés (on s'en tient aux 4 champs documentés de `TvStation`).
- Includes typés (`fixture`, `player`, `relatedPlayer`, `country`…) — utilisables en strings
  via `.include()`, mais pas de relations typées.
- Le client est désormais complet sur les référentiels (Core M7 + football M8).
