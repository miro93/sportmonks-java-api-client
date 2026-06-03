# M9 — Standard Odds Feed (Bookmakers / Markets / Pre-match / Inplay) — Design

**Date:** 2026-06-03
**Repo:** `miro93/sportmonks-java-api-client`
**Status:** Approved

## Objectif

Ajouter le **Standard Odds Feed** de l'API SportMonks football (`/v3/football`) : les cotes
pre-match et in-play, plus les ressources de référence Bookmakers et Markets nécessaires
pour les interpréter. Premier de deux milestones « odds » ; le **Premium Feed** (base URL
différente `/v3/odds`, params time-range/historiques) fera l'objet de **M10**.

Architecture = continuation de M4/M5/M8 : modèles dans `football.model`, endpoints dans
`football.endpoint`, accesseurs sur `FootballClient`, pattern d'endpoint identique
(clone de `CoachesEndpoint`). Aucun changement d'infra : réutilise
`CollectionRequest`/`SingleResourceRequest`/`RequestSpec`/`ApiExecutor`/`JacksonCodec`.

## Modèles (records `football.model`)

Conventions M4/M5/M8 : `id` en `long` primitif ; tout autre scalaire boxé nullable ;
`///` JavaDoc (≥80 %, gate CodeRabbit). Champs d'après la doc + exemple JSON,
**à confirmer contre les payloads réels en TDD** (tests de décodage).

| Modèle | Champs (type) |
|---|---|
| `Bookmaker` | `id`(long), `legacyId`(Long), `name`(String) |
| `Market` | `id`(long), `legacyId`(Long), `name`(String), `developerName`(String), `hasWinningCalculations`(Boolean) |
| `Odd` | `id`(long), `fixtureId`(Long), `marketId`(Long), `bookmakerId`(Long), `label`(String), `value`(String), `name`(String), `sortOrder`(Integer), `marketDescription`(String), `probability`(String), `dp3`(String), `fractional`(String), `american`(String), `winning`(Boolean), `stopped`(Boolean), `total`(String), `handicap`(String), `participants`(String), `createdAt`(String), `updatedAt`(String), `originalLabel`(String), `latestBookmakerUpdate`(String) |

### Notes de modélisation

- **`Odd.value`, `Odd.probability`, `Odd.dp3`, `Odd.fractional`, `Odd.american`, `Odd.total`,
  `Odd.handicap` sont des `String`** : l'API renvoie les cotes/valeurs numériques en chaînes
  (ex. `"value": "1.48"`, `"probability": "67.57%"`) — cohérent avec la convention
  numbers-as-strings du projet (cf. `Coach.cityId`). Ne pas les typer en nombres.
- `Odd` est **partagé** par les endpoints pre-match et inplay (forme JSON identique).
- `Odd` n'a **pas** de `byId` (toujours renvoyé en collection : all / byFixture / latest).
  `Bookmaker` et `Market` ont `byId` (single).
- `winning`/`stopped` → `Boolean` ; `sortOrder` → `Integer` (souvent `null`).

## Endpoints (`football.endpoint`, base `https://api.sportmonks.com/v3/football`)

Calque de `CoachesEndpoint` : champs `executor`/`single`/`list`, helper privé
`collection(path)` pour les ressources à plusieurs collections, `search(name)` avec
`Objects.requireNonNull(name,"name")`.

| Endpoint | Méthodes → chemin |
|---|---|
| `BookmakersEndpoint` | `all()` → `bookmakers` · `byId(long)` → `bookmakers/{id}` · `search(String)` → `bookmakers/search/{name}` · `byFixture(long)` → `bookmakers/fixtures/{id}` |
| `MarketsEndpoint` | `all()` → `markets` · `byId(long)` → `markets/{id}` · `search(String)` → `markets/search/{name}` |
| `PreMatchOddsEndpoint` | `all()` → `odds/pre-match` · `byFixture(long)` → `odds/pre-match/fixtures/{id}` · `byFixtureAndBookmaker(long,long)` → `odds/pre-match/fixtures/{fixtureId}/bookmakers/{bookmakerId}` · `byFixtureAndMarket(long,long)` → `odds/pre-match/fixtures/{fixtureId}/markets/{marketId}` · `latest()` → `odds/pre-match/latest` |
| `InplayOddsEndpoint` | `all()` → `odds/inplay` · `byFixture(long)` → `odds/inplay/fixtures/{id}` · `byFixtureAndBookmaker(long,long)` → `odds/inplay/fixtures/{fixtureId}/bookmakers/{bookmakerId}` · `byFixtureAndMarket(long,long)` → `odds/inplay/fixtures/{fixtureId}/markets/{marketId}` · `latest()` → `odds/inplay/latest` |

- Toutes les méthodes d'odds renvoient `CollectionRequest<Odd>` (pas de single).
  `BookmakersEndpoint.byId`/`MarketsEndpoint.byId` renvoient `SingleResourceRequest<…>`.
- Deux classes distinctes pour pre-match et inplay (reflètent les deux feeds de l'API) ;
  `PreMatchOddsEndpoint` et `InplayOddsEndpoint` partagent le décodeur `Odd`.

## FootballClient

- 4 nouveaux champs + accesseurs : `bookmakers()`, `markets()`, `preMatchOdds()`,
  `inplayOdds()`.
- Câblés dans `Builder.build()` sur l'`executor` football existant, ajoutés comme arguments
  du constructeur privé **avant** `core` (qui reste LAST). 14 endpoints existants + les 5
  référentiels M8 + `core()` inchangés.

## Tests (TDD, WireMock + AssertJ + JUnit 5)

- 4 tests d'endpoint :
  - `BookmakersEndpointTest` — all/byId/search/byFixture + `search(null)`.
  - `MarketsEndpointTest` — all/byId/search + `search(null)`.
  - `PreMatchOddsEndpointTest` — all, byFixture, byFixtureAndBookmaker (chemin composé),
    byFixtureAndMarket (chemin composé), latest.
  - `InplayOddsEndpointTest` — mêmes 5 (chemins `odds/inplay/…`).
- Tests de décodage : `OddDecodingTest` (tous champs présents avec `value`/`probability`
  String + cas optionnels-absents) et `BookmakerMarketDecodingTest`
  (Bookmaker/Market, nullable inclus).
- `FootballClientM9Test` — accesseurs non nuls + 2-3 appels routés (auth header + chemin,
  ex. `odds/pre-match/fixtures/{id}` et `bookmakers/fixtures/{id}`).

## Docs & release

- README : ajout des 4 accesseurs à la table des endpoints football.
- Commits `feat:`/`test:`/`docs:` conventionnels → release-please met à jour le CHANGELOG.

## Hors périmètre (explicite — M10 ou jamais)

- **Premium Odds Feed** (base URL `/v3/odds` : premium pre-match, premium markets/bookmakers,
  historiques, time-range) → **M10**, design séparé (franchit une base URL, comme le Core API).
- Produit « Odds API 3.0 » distinct.
- Bookmaker mappings / event-ids par fixture (niche).
- Filtre `havingOdds` / propriété `has_odds` (filtre transversal, utilisable en string via
  `.filter()` si besoin, non typé).
