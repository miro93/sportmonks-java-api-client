# M12 — Predictions (Probabilities / Value Bets / Predictability) — Design

**Date:** 2026-06-04
**Repo:** `miro93/sportmonks-java-api-client`
**Status:** Approved

## Objectif

Ajouter les **Predictions** de l'API SportMonks football (`/v3/football/predictions`) : les
probabilités prédites par fixture, les value bets, et la predictability par ligue. Tout est
servi sous `/v3/football` — **aucun nouvel executor**.

Architecture = continuation de M4/M5/M8/M9/M10/M11 : modèles dans `football.model`, un
endpoint dans `football.endpoint`, un accesseur sur `FootballClient`, pattern d'endpoint
identique (clone de `PreMatchOddsEndpoint`, collection-only). Réutilise
`CollectionRequest`/`RequestSpec`/`ApiExecutor`/`JacksonCodec`.

## Deux enveloppes distinctes (vérité API)

La nomenclature SportMonks est contre-intuitive ; les faits :

- **probabilities** et **value-bets** : fixture-scoped, enveloppe
  `{ id, fixture_id, type_id, predictions: object }`. L'objet `predictions` a une forme
  **variable selon `type_id`** (probabilities : `{yes,no}` / `{home,draw,away}` / scores… ;
  value bets : `{bet, bookmaker, odd, is_value, stake, fair_odd}`, forme **stable**).
- **predictability** (endpoint `predictions/predictability/leagues/{id}`) : league-scoped,
  enveloppe **différente** `{ id, league_id, type_id, data: object }` — clé `data` (pas
  `predictions`), `league_id` (pas `fixture_id`). L'objet `data` mappe marché → métrique de
  fiabilité (float). → justifie un record dédié.

## Modèles (records `football.model`)

Conventions du projet : `id` en `long` primitif ; tout autre scalaire boxé nullable ;
`///` JavaDoc (≥80 %, gate CodeRabbit). Champs **à confirmer contre les payloads réels en
TDD** (tests de décodage).

| Modèle | Champs (type) | Pour |
|---|---|---|
| `Prediction` | `id`(long), `fixtureId`(Long), `typeId`(Long), `predictions`(`Map<String,Object>`) | probabilities (payload free-form selon le type) |
| `ValueBet` | `id`(long), `fixtureId`(Long), `typeId`(Long), `predictions`(`ValueBetPrediction`) | value-bets (sous-objet stable, typé) |
| `ValueBetPrediction` (nested) | `bet`(String), `bookmaker`(String), `odd`(String), `isValue`(Boolean), `stake`(String), `fairOdd`(String) | sous-objet `predictions` des value bets |
| `Predictability` | `id`(long), `leagueId`(Long), `typeId`(Long), `data`(`Map<String,Object>`) | predictability par ligue (clé `data`, payload marché→fiabilité) |

### Notes de modélisation

- **`Prediction.predictions` et `Predictability.data` sont des `Map<String,Object>`** : la
  forme dépend du `type_id` (impossible/contre-productif à typer en un record fixe), cohérent
  avec le choix du projet de ne pas sur-typer les structures variables (cf. includes non
  typés). Jackson décode les valeurs numériques de la map en `Double`/`Integer` ; l'appelant
  lit les clés selon le `type_id` / l'endpoint appelé.
- **`ValueBetPrediction`** est la seule forme stable → typée. `odd`, `stake`, `fairOdd` en
  `String` (convention numbers-as-strings, cf. `Odd.value`) ; `isValue` → `Boolean` ;
  `bet`/`bookmaker` → `String`. **À confirmer en TDD** : si les payloads réels renvoient
  `odd`/`stake`/`fairOdd` en nombres purs et stables, on pourra les repasser en `Double`.
- Les `*_id` (`fixture_id`, `league_id`, `type_id`) → `Long`, cohérent avec les autres
  modèles.

## Endpoint (`football.endpoint`, base `https://api.sportmonks.com/v3/football`)

Un seul `PredictionsEndpoint`, calque de `PreMatchOddsEndpoint` : champ `executor` + trois
`DataType<List<…>>` (un par modèle de retour), helper privé `collection(path, type)`.
Toutes les méthodes renvoient `CollectionRequest<…>` (aucune ressource single).

| Méthode → chemin | Retour |
|---|---|
| `probabilities()` → `predictions/probabilities` | `CollectionRequest<Prediction>` |
| `probabilitiesByFixture(long fixtureId)` → `predictions/probabilities/fixtures/{fixtureId}` | `CollectionRequest<Prediction>` |
| `valueBets()` → `predictions/value-bets` | `CollectionRequest<ValueBet>` |
| `valueBetsByFixture(long fixtureId)` → `predictions/value-bets/fixtures/{fixtureId}` | `CollectionRequest<ValueBet>` |
| `predictabilityByLeague(long leagueId)` → `predictions/predictability/leagues/{leagueId}` | `CollectionRequest<Predictability>` |

## FootballClient

- 1 nouveau champ + accesseur `predictions()`, câblé sur l'`executor` football existant.
- Ajouté comme champ / paramètre de constructeur / argument de `build()` **après**
  `expectedLineups` et **avant** `core` (qui reste LAST). Aucun nouvel executor.

## Tests (TDD, WireMock + AssertJ + JUnit 5)

- `PredictionsEndpointTest` — les 5 méthodes (chemins, dont composés `.../fixtures/{id}` et
  `.../leagues/{id}`).
- Tests de décodage :
  - `PredictionDecodingTest` — `predictions` en Map (ex. `{ "yes": 0.67, "no": 0.33 }` et un
    cas `{ "home": ..., "draw": ..., "away": ... }`) ; vérifie l'accès aux clés et le
    `type_id`.
  - `ValueBetDecodingTest` — sous-objet `ValueBetPrediction` typé (tous champs + cas absents).
  - `PredictabilityDecodingTest` — clé `data` en Map + `leagueId`.
- `FootballClientM12Test` — accesseur `predictions()` non nul + un appel routé
  (`predictions/value-bets/fixtures/{id}`) vérifiant chemin + header d'authentification.

## Docs & release

- README : ajout d'une ligne `predictions()` (5 méthodes) à la table des endpoints football.
- Commits `feat:`/`test:`/`docs:` conventionnels → release-please met à jour sa PR de release.

## Hors périmètre (explicite)

- Le mapping interprétatif des `type_id` → clés (quel type produit `{yes,no}` vs
  `{home,draw,away}`) : reste documentaire, accessible via l'endpoint `types` du Core API
  (M7) si besoin.
- L'include `predictions` sur fixtures (utilisable via `.include("...")` générique, non typé).
