# M10 — Premium Odds Feed (Premium Pre-match / History / Markets / Bookmakers) — Design

**Date:** 2026-06-04
**Repo:** `miro93/sportmonks-java-api-client`
**Status:** Approved

## Objectif

Ajouter le **Premium Odds Feed** de l'API SportMonks : les cotes premium pre-match, leur
historique, et les ressources premium markets / premium bookmakers. Second et dernier des
deux milestones « odds » (M9 = Standard). Une fois M10 mergé, M9 + M10 sortiront ensemble
dans une seule release (la PR release-please #70 reste OUVERTE jusque-là).

Particularité : le feed est **réparti sur deux base URLs**. Le cœur premium est sous
`/v3/football` (executor football existant) ; premium markets / premium bookmakers
franchissent vers `/v3/odds`, ce qui justifie un **nouvel `ApiExecutor`** sur le
`FootballClient`, exactement comme M7 a ajouté `coreExecutor` pour `/v3/core`.

Architecture = continuation de M4/M5/M8/M9 : modèles dans `football.model`, endpoints dans
`football.endpoint`, accesseurs sur `FootballClient`, pattern d'endpoint identique
(clone de `CoachesEndpoint` / `PreMatchOddsEndpoint`). Aucun changement d'infra hors le
second executor : réutilise `CollectionRequest`/`RequestSpec`/`ApiExecutor`/`JacksonCodec`.

## Modèles (records `football.model`)

Conventions M4/M5/M8/M9 : `id` en `long` primitif ; tout autre scalaire boxé nullable ;
`///` JavaDoc (≥80 %, gate CodeRabbit) ; valeurs numériques (cotes/probabilités) en `String`
(convention numbers-as-strings du projet). Champs **à confirmer contre les payloads réels en
TDD** (tests de décodage).

| Modèle | Champs (type) |
|---|---|
| `PremiumOdd` | `id`(long), `fixtureId`(Long), `marketId`(Long), `bookmakerId`(Long), `label`(String), `value`(String), `name`(String), `marketDescription`(String), `probability`(String), `dp3`(String), `fractional`(String), `american`(String), `stopped`(Boolean), `total`(String), `handicap`(String), `participants`(String), `latestBookmakerUpdate`(String) |
| `HistoricalOdd` | `id`(long), `oddId`(Long), `value`(String), `probability`(String), `dp3`(String), `fractional`(String), `american`(String), `bookmakerUpdate`(String) |

### Notes de modélisation

- **`PremiumOdd` = `Odd` (M9) sans `winning` ni `originalLabel`** : la doc indique que les
  premium odds n'ont pas encore la fonctionnalité « winning ». Record dédié (et non
  réutilisation de `Odd`) pour ne pas exposer deux champs toujours `null` sur du premium, et
  refléter fidèlement la séparation faite côté API (`Odd` vs `PremiumOdd`).
- **`HistoricalOdd`** (entité `PremiumOddHistory`) est distincte et plus petite : pas de
  `fixtureId`/`marketId`/`bookmakerId`/`label`/`name`, mais un `oddId` qui référence la
  premium odd parente, et `bookmakerUpdate` (timestamp).
- `Market` et `Bookmaker` (M9) sont **réutilisés** pour premium markets / premium bookmakers
  (mêmes champs : la doc premium markets renvoie `id`/`legacy_id`/`name`/`developer_name`).
- `stopped` → `Boolean`. Toutes les valeurs de cotes (`value`, `probability`, `dp3`,
  `fractional`, `american`, `total`, `handicap`) → `String`.

## Endpoints (`football.endpoint`)

Calque de `PreMatchOddsEndpoint` : champs `executor`/`list`, helper privé `collection(path)`,
toutes les méthodes renvoient `CollectionRequest<…>` (aucune ressource single).

### Sur l'executor football (base `https://api.sportmonks.com/v3/football`)

| Endpoint | Méthodes → chemin |
|---|---|
| `PremiumOddsEndpoint` (→ `PremiumOdd`) | `all()` → `odds/premium` · `byFixture(long)` → `odds/premium/fixtures/{id}` · `byFixtureAndBookmaker(long,long)` → `odds/premium/fixtures/{fixtureId}/bookmakers/{bookmakerId}` · `byFixtureAndMarket(long,long)` → `odds/premium/fixtures/{fixtureId}/markets/{marketId}` · `updatedBetween(long,long)` → `odds/premium/updated/between/{from}/{to}` |
| `PremiumOddsHistoryEndpoint` (→ `HistoricalOdd`) | `all()` → `odds/premium/history` · `updatedBetween(long,long)` → `odds/premium/history/updated/between/{from}/{to}` |

### Sur le nouvel executor odds (base `https://api.sportmonks.com/v3/odds`)

| Endpoint | Méthodes → chemin |
|---|---|
| `PremiumMarketsEndpoint` (→ `Market`) | `all()` → `markets/premium` |
| `PremiumBookmakersEndpoint` (→ `Bookmaker`) | `all()` → `bookmakers/premium` |

- `updatedBetween(long from, long to)` prend deux timestamps **UNIX en secondes** interpolés
  comme segments de chemin (ex. `odds/premium/updated/between/1767225600/1767225900`). Pas de
  conversion ; colle au brut de l'API. Un binding `Instant` pourra être ajouté plus tard si
  besoin.
- History séparé du feed premium en **deux classes** (entités `PremiumOdd` vs `HistoricalOdd`
  différentes), cohérent avec le split `PreMatchOddsEndpoint` / `InplayOddsEndpoint` de M9.

## FootballClient

- Nouvelle constante `ODDS_BASE_URL = "https://api.sportmonks.com/v3/odds"` et méthode builder
  `oddsBaseUrl(String)` (défaut = la constante), calquées sur `coreBaseUrl`/`CoreClient.DEFAULT_BASE_URL`.
- Dans `Builder.build()` : construire `ApiExecutor oddsExecutor = new ApiExecutor(transport,
  codec, apiToken, oddsBaseUrl)` (même transport/codec/token que l'executor football, comme
  `coreExecutor`).
- 4 nouveaux champs + accesseurs : `premiumOdds()`, `premiumOddsHistory()` (executor football),
  `premiumMarkets()`, `premiumBookmakers()` (executor odds).
- Ordre : les 4 nouveaux champs / params de constructeur / arguments de `build()` s'insèrent
  **après** `inplayOdds` et **avant** `core` (qui reste LAST).

## Tests (TDD, WireMock + AssertJ + JUnit 5)

- Tests d'endpoint :
  - `PremiumOddsEndpointTest` — all, byFixture, byFixtureAndBookmaker (chemin composé),
    byFixtureAndMarket (chemin composé), updatedBetween (deux segments timestamp).
  - `PremiumOddsHistoryEndpointTest` — all, updatedBetween.
  - `PremiumMarketsEndpointTest` — all.
  - `PremiumBookmakersEndpointTest` — all.
- Tests de décodage :
  - `PremiumOddDecodingTest` — tous champs présents (`value`/`probability` String) + cas où
    `winning`/`original_label` sont absents du payload (record n'a pas ces champs).
  - `HistoricalOddDecodingTest` — `oddId`/`bookmakerUpdate` inclus.
- `FootballClientM10Test` — les 4 accesseurs non nuls + routage : un appel sur
  `/v3/football/odds/premium/fixtures/{id}` (executor football) et un sur
  `/v3/odds/markets/premium` (executor odds) ; vérifie chemin + base URL + header
  d'authentification sur les deux executors.

## Docs & release

- README : ajout des 4 accesseurs à la table des endpoints football + note sur la seconde
  base URL `/v3/odds` pour premium markets/bookmakers.
- Commits `feat:`/`test:`/`docs:` conventionnels → release-please met à jour la PR #70.
- **Ne pas merger la PR #70** : elle regroupera M9 + M10 ; l'utilisateur la mergera après M10.

## Hors périmètre (explicite)

- **Premium Expected Lineups** (`/v3/football/.../premium-expected-lineups`, par équipe / par
  joueur) → milestone séparé (entité lineup différente, pas du tout des odds).
- Produit « Odds API 3.0 » distinct.
- Surcharges `Instant` des méthodes time-range (ajoutables plus tard sans rupture).
- Filtre `havingOdds` / propriété `has_odds` (filtre transversal, utilisable en string via
  `.filter()` si besoin, non typé).
