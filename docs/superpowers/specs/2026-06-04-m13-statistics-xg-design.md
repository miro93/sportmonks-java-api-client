# M13 — Statistics + Expected (xG) — Design

**Date:** 2026-06-04
**Repo:** `miro93/sportmonks-java-api-client`
**Status:** Approved

## Objectif

Ajouter les **Statistics** (statistiques agrégées par saison/participant, par stage, par round)
et l'**Expected / xG** (expected goals par fixture et par lineup) de l'API SportMonks football.
Dernière grande feature ; sera taguée **1.0.0**. Tout est servi sous `/v3/football` — **aucun
nouvel executor**.

Architecture = continuation de M4–M12 : modèles dans `football.model`, deux endpoints dans
`football.endpoint`, deux accesseurs sur `FootballClient`, pattern d'endpoint identique
(clone de `PreMatchOddsEndpoint`, collection-only). Réutilise
`CollectionRequest`/`RequestSpec`/`ApiExecutor`/`JacksonCodec`.

Note de périmètre : la majorité des statistiques SportMonks sont livrées via des **includes**
(`statistics` sur teams/players/fixtures…), transversaux et non typés dans le projet
(utilisables via `.include("...")`). Ce milestone couvre uniquement les **endpoints dédiés**.

## Modèles (records `football.model`)

Conventions du projet : `id` en `long` primitif ; tout autre scalaire boxé nullable ;
`///` JavaDoc (≥80 %, gate CodeRabbit). Les payloads variables selon `type_id` sont des
`Map<String,Object>` (cohérent avec M12). Champs **à confirmer contre les payloads réels en
TDD**.

| Modèle | Champs (type) | Pour |
|---|---|---|
| `Statistic` | `id`(long), `playerId`(Long), `coachId`(Long), `teamId`(Long), `refereeId`(Long), `seasonId`(Long), `stageId`(Long), `roundId`(Long), `hasValues`(Boolean), `positionId`(Long), `jerseyNumber`(Integer), `details`(`List<StatisticDetail>`) | season-by-participant, stage, round |
| `StatisticDetail` (nested) | `id`(long), `typeId`(Long), `value`(`Map<String,Object>`) | un détail de stat ; `value` varie selon `type_id` |
| `Expected` | `id`(long), `fixtureId`(Long), `typeId`(Long), `participantId`(Long), `location`(String), `data`(`Map<String,Object>`) | xG par fixture (équipe) et par lineup (joueur) |

### Notes de modélisation

- **`Statistic` est une enveloppe unifiée** : un seul record couvre les quatre types de
  participant (player/coach/team/referee) plus les scopes stage/round ; les ids non
  pertinents au contexte sont `null`. Évite quatre records quasi identiques.
- **`StatisticDetail.value` et `Expected.data` sont des `Map<String,Object>`** : la forme
  dépend du `type_id` (`{total}`, `{home,away}`, `{goals,penalties}`, `{average,highest,lowest}`,
  `{value}`…). Jackson décode les nombres en `Double`/`Integer` ; l'appelant lit les clés
  selon le `type_id`. Cohérent avec le choix M12 de ne pas sur-typer les structures variables.
- `details` est une **liste** d'objets `StatisticDetail` ; le décodeur la mappe via le
  mécanisme records standard (pas d'include requis, `details` est dans la réponse de base).
- `StatisticDetail.id` en `long` (les objets détail SportMonks portent un id) — **à confirmer
  TDD** ; si un payload réel l'omet, repasser en envelope adéquate.
- `Expected.location` → `String` (`"home"`/`"away"`). Les `*_id` → `Long`.

## Endpoints (`football.endpoint`, base `https://api.sportmonks.com/v3/football`)

Calque de `PreMatchOddsEndpoint` : champ `executor` + `DataType<List<…>>`, helper privé
`collection(path)`. Toutes les méthodes renvoient `CollectionRequest<…>` (aucune single).

**`StatisticsEndpoint`** (→ `Statistic`) :

| Méthode → chemin |
|---|
| `seasonByTeam(long teamId)` → `statistics/seasons/teams/{teamId}` |
| `seasonByPlayer(long playerId)` → `statistics/seasons/players/{playerId}` |
| `seasonByCoach(long coachId)` → `statistics/seasons/coaches/{coachId}` |
| `seasonByReferee(long refereeId)` → `statistics/seasons/referees/{refereeId}` |
| `byStage(long stageId)` → `statistics/stages/{stageId}` |
| `byRound(long roundId)` → `statistics/rounds/{roundId}` |

**`ExpectedEndpoint`** (→ `Expected`) :

| Méthode → chemin | Note |
|---|---|
| `fixtures()` → `expected/fixtures` | xG niveau équipe (agrégé par fixture) |
| `lineups()` → `expected/lineups` | xG niveau joueur (par lineup) |

- `fixtures()`/`lineups()` n'ont pas d'id en chemin (collections filtrables via `.filter()`/
  `.include()` génériques).
- Les quatre méthodes `seasonBy…` figent le segment `{participant}` (teams/players/coaches/
  referees) → type-safe, pas de string libre.

## FootballClient

- 2 nouveaux champs + accesseurs : `statistics()`, `expected()`, câblés sur l'`executor`
  football existant.
- Ajoutés comme champs / paramètres de constructeur / arguments de `build()` **après**
  `predictions` (M12) et **avant** `core` (qui reste LAST). Aucun nouvel executor.

## Tests (TDD, WireMock + AssertJ + JUnit 5)

- `StatisticsEndpointTest` — les 6 méthodes (chemins, dont `seasons/{participant}/{id}`,
  `stages/{id}`, `rounds/{id}`).
- `ExpectedEndpointTest` — `fixtures`, `lineups`.
- Tests de décodage :
  - `StatisticDecodingTest` — enveloppe (ids participant/scope) + `details[]` avec plusieurs
    formes de `value` (ex. `{ "total": 12 }` et `{ "home": 5, "away": 7 }`) ; cas optionnels
    absents.
  - `ExpectedDecodingTest` — `data` en Map (`{ "value": 1.85 }`), `location`, `participantId`.
- `FootballClientM13Test` — accesseurs `statistics()`/`expected()` non nuls + deux appels
  routés (`statistics/seasons/teams/{id}` et `expected/fixtures`) vérifiant chemin + header
  d'authentification.

## Docs & release

- README : ajout des lignes `statistics()` et `expected()` à la table des endpoints football.
- Commits `feat:`/`test:`/`docs:` conventionnels.
- **Release 1.0.0** : à la merge de ce milestone, forcer release-please à sortir **1.0.0**
  (ajout d'un footer `Release-As: 1.0.0` dans un commit, ou édition de la PR release-please) ;
  la PR de release #75 (0.7.0) reste OUVERTE jusque-là et sera remplacée/mergée en 1.0.0 par
  l'utilisateur.

## Hors périmètre (explicite)

- Les statistiques en **includes** (team/player/fixture stats via `.include("statistics")`) :
  transversales, non typées.
- Le mapping interprétatif des `type_id` → clés de `value` : documentaire, accessible via
  l'endpoint `types` du Core API (M7) si besoin.
- Premium Expected Lineups : déjà couvert en M11 (endpoint distinct `expected-lineups`).
