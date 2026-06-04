# M11 — Premium Expected Lineups (by team / by player) — Design

**Date:** 2026-06-04
**Repo:** `miro93/sportmonks-java-api-client`
**Status:** Approved

## Objectif

Ajouter le **Premium Expected Lineups** de l'API SportMonks football : les compositions
probables (starting eleven + remplaçants) prédites avant le coup d'envoi, accessibles par
équipe et par joueur. Ressource premium (nécessite un abonnement adéquat pour des données
réelles), mais servie sous `/v3/football` — **aucun nouvel executor**, contrairement à M10.

Architecture = continuation de M4/M5/M8/M9/M10 : un modèle dans `football.model`, un endpoint
dans `football.endpoint`, un accesseur sur `FootballClient`, pattern d'endpoint identique
(clone de `PreMatchOddsEndpoint`, collection-only). Réutilise
`CollectionRequest`/`RequestSpec`/`ApiExecutor`/`JacksonCodec`.

## Modèle (record `football.model`)

Conventions du projet : `id` en `long` primitif ; tout autre scalaire boxé nullable ;
`///` JavaDoc (≥80 %, gate CodeRabbit). Champs **à confirmer contre les payloads réels en
TDD** (tests de décodage).

| Modèle | Champs (type) |
|---|---|
| `ExpectedLineup` | `id`(long), `sportId`(Long), `fixtureId`(Long), `playerId`(Long), `teamId`(Long), `typeId`(Long), `playerName`(String), `jerseyNumber`(Integer), `positionId`(Long), `detailedPositionId`(Long), `formationField`(String), `formationPosition`(String) |

### Notes de modélisation

- Les champs `*_id` (`sport_id`, `fixture_id`, `player_id`, `team_id`, `type_id`,
  `position_id`, `detailed_position_id`) → `Long`, cohérent avec les autres modèles
  (`Odd.fixtureId`, etc.).
- `jersey_number` → `Integer`.
- **`formationField` et `formationPosition` → `String`** : la doc les annonce « integer »,
  mais SportMonks renvoie fréquemment ces marqueurs de placement en chaînes. On les type en
  `String` par prudence (convention numbers-as-strings du projet, cf. `Coach.cityId` et les
  cotes `Odd.value`). **À vérifier en TDD** : si les payloads réels les renvoient en entiers
  purs et stables, on pourra les repasser en `Integer` — le test de décodage tranchera.
- Pas de `participants`/includes typés : les includes (`type`, `fixture`, `participant`,
  `player`, `team`) restent accessibles via le mécanisme générique `.include("...")`.

## Endpoint (`football.endpoint`, base `https://api.sportmonks.com/v3/football`)

Calque de `PreMatchOddsEndpoint` : champs `executor`/`list`, helper privé `collection(path)`,
toutes les méthodes renvoient `CollectionRequest<ExpectedLineup>` (aucune ressource single).

| Endpoint | Méthodes → chemin |
|---|---|
| `ExpectedLineupsEndpoint` | `byTeam(long teamId)` → `expected-lineups/teams/{teamId}` · `byPlayer(long playerId)` → `expected-lineups/players/{playerId}` |

- Pas de `all()` ni de `byId(...)` : l'API n'expose que les deux variantes by-team / by-player.

## FootballClient

- 1 nouveau champ + accesseur `expectedLineups()`, câblé sur l'`executor` football existant.
- Ajouté comme champ / paramètre de constructeur / argument de `build()` **avant** `core`
  (qui reste LAST). Aucun nouvel executor.

## Tests (TDD, WireMock + AssertJ + JUnit 5)

- `ExpectedLineupsEndpointTest` — `byTeam` (chemin `expected-lineups/teams/{id}`),
  `byPlayer` (chemin `expected-lineups/players/{id}`).
- `ExpectedLineupDecodingTest` — tous champs présents (confirme le type retenu pour
  `formationField`/`formationPosition`) + cas optionnels absents (`{ "id": 1 }`).
- `FootballClientM11Test` — accesseur `expectedLineups()` non nul + un appel routé
  (`expected-lineups/teams/{id}`) vérifiant chemin + header d'authentification.

## Docs & release

- README : ajout d'une ligne `expectedLineups()` à la table des endpoints football.
- Commits `feat:`/`test:`/`docs:` conventionnels → release-please met à jour sa PR de release
  (mergée par l'utilisateur quand il le souhaite).

## Hors périmètre (explicite)

- L'include `expectedLineups` sur les endpoints fixtures / livescores (utilisable via le
  mécanisme générique `.include("...")`, non typé — aucun include n'est typé dans le projet).
- Les autres ressources premium (déjà couvertes en M10, ou produits distincts).
