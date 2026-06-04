# M14 — JPMS Modularization (max encapsulation) — Design

**Date:** 2026-06-04
**Repo:** `miro93/sportmonks-java-api-client`
**Status:** Approved

## Objectif

Ajouter le **Java Platform Module System (JPMS)** à la bibliothèque : un `module-info.java`
par module Gradle (`core`, `football`), avec **encapsulation maximale** — la plomberie interne
n'est jamais exposée à l'utilisateur final, via des exports qualifiés et le déplacement des
classes internes hors des packages mixtes.

**Version : 2.0.0.** Des types aujourd'hui techniquement publics (`JacksonCodec`,
`ApiExecutor`, `RequestSpec`…) sortent de la surface publique → réduction d'API = breaking
change. La lib est jeune (1.0.0 fraîche) ; on coupe net, sans cycle de dépréciation.

Faisabilité validée par spike (créé puis révoqué) : les deux modules compilent et **tous les
tests passent sur le module-path sans aucune config Gradle manuelle** ; les dépendances sont
de vrais modules JPMS (`tools.jackson.databind`/`core`/`module.blackbird`, `java.net.http`) ;
le décodage Jackson cross-module fonctionne.

## Surface publique vs interne

**Public (exporté normalement) :**

| Module | Packages publics |
|---|---|
| core | `coreapi`, `coreapi.endpoint`, `coreapi.model`, `auth`, `error` (exceptions), `response`, `paging`, `retry` (`RetryPolicy`/`Backoff`), `request` (`CollectionRequest`/`SingleResourceRequest`) |
| football | `football` (`FootballClient`), `football.endpoint`, `football.model` |

**Interne (jamais exposé à l'utilisateur) :**

- Packages 100 % plomberie → **exports qualifiés vers `football`** uniquement (pas de
  déplacement de classe) : `core` (racine : `ApiExecutor`), `core.http`
  (`HttpTransport`/`JdkHttpTransport`/`RawResponse`), `core.json`
  (`JacksonCodec`/`DataType`/`CodecException`).
- Classes internes nichées dans des packages **mixtes** → **déplacées** vers un sous-package
  `.internal`, lui aussi qualifié-exporté vers `football` :
  - `core.request` → déplacer `RequestSpec`, `UrlBuilder` vers **`core.request.internal`**
    (laisser `CollectionRequest`/`SingleResourceRequest` publics dans `core.request`).
  - `core.retry` → déplacer `Sleeper`, `RetryingTransport` vers **`core.retry.internal`**
    (laisser `RetryPolicy`, `Backoff` publics).
  - `core.error` → déplacer `ErrorMapper` vers **`core.error.internal`** (laisser les
    exceptions publiques).

Note : des constructeurs publics de types publics (ex. `CollectionRequest(ApiExecutor,
RequestSpec.Builder, DataType)`, constructeurs d'endpoints `(ApiExecutor, JacksonCodec)`)
référencent des types internes. C'est légal et voulu : l'utilisateur voit le constructeur mais
**ne peut pas l'invoquer** (types des paramètres inaccessibles) — la construction reste
réservée à notre code (FootballClient/CoreClient et leurs builders).

## module-info

`core/src/main/java/module-info.java` :

```text
module io.github.miro93.sportmonks.core {
    requires transitive tools.jackson.databind;
    requires tools.jackson.core;
    requires tools.jackson.module.blackbird;
    requires java.net.http;

    // Public API
    exports io.github.miro93.sportmonks.core.auth;
    exports io.github.miro93.sportmonks.core.error;
    exports io.github.miro93.sportmonks.core.paging;
    exports io.github.miro93.sportmonks.core.request;
    exports io.github.miro93.sportmonks.core.response;
    exports io.github.miro93.sportmonks.core.retry;
    exports io.github.miro93.sportmonks.core.coreapi;
    exports io.github.miro93.sportmonks.core.coreapi.endpoint;
    exports io.github.miro93.sportmonks.core.coreapi.model;

    // Internal plumbing — visible only to the football module
    exports io.github.miro93.sportmonks.core to io.github.miro93.sportmonks.football;
    exports io.github.miro93.sportmonks.core.http to io.github.miro93.sportmonks.football;
    exports io.github.miro93.sportmonks.core.json to io.github.miro93.sportmonks.football;
    exports io.github.miro93.sportmonks.core.request.internal to io.github.miro93.sportmonks.football;
    exports io.github.miro93.sportmonks.core.retry.internal to io.github.miro93.sportmonks.football;
    exports io.github.miro93.sportmonks.core.error.internal to io.github.miro93.sportmonks.football;

    // Jackson reflects into decoded types
    opens io.github.miro93.sportmonks.core.response to tools.jackson.databind, tools.jackson.module.blackbird;
    opens io.github.miro93.sportmonks.core.coreapi.model to tools.jackson.databind, tools.jackson.module.blackbird;
}
```

`football/src/main/java/module-info.java` :

```text
module io.github.miro93.sportmonks.football {
    requires transitive io.github.miro93.sportmonks.core;
    requires java.net.http;

    exports io.github.miro93.sportmonks.football;
    exports io.github.miro93.sportmonks.football.endpoint;
    exports io.github.miro93.sportmonks.football.model;

    opens io.github.miro93.sportmonks.football.model to tools.jackson.databind, tools.jackson.module.blackbird;
}
```

- Le warning « composant de nom de module `miro93` finit par un chiffre » est cosmétique et
  assumé (les noms de modules suivent les packages — le bon choix). On peut le neutraliser via
  une option javac `-Xlint:-module` si on veut un build sans warning ; sinon on l'accepte.

## Déplacements & sites d'import (mécanique)

| Classe déplacée | Nouveau package | Sites d'import à mettre à jour |
|---|---|---|
| `RequestSpec` | `core.request.internal` | ~40 (tous les endpoints core+football, `CollectionRequest`, `SingleResourceRequest`, `ApiExecutor`, `UrlBuilder`, + tests `RequestSpecTest`/`CollectionRequestTest`/`SingleResourceRequestTest`/`ApiExecutorTest`) |
| `UrlBuilder` | `core.request.internal` | 3 (`RequestSpec`, `ApiExecutor`, `UrlBuilderTest`) |
| `Sleeper` | `core.retry.internal` | 5 (`RetryingTransport`, `CoreClient`, `FootballClient`, `SleeperTest`, `RetryingTransportTest`) |
| `RetryingTransport` | `core.retry.internal` | 3 (`CoreClient`, `FootballClient`, `RetryingTransportTest`) |
| `ErrorMapper` | `core.error.internal` | 2 (`ApiExecutor`, `ErrorMapperTest`) |

- Les classes **non déplacées mais rendues internes** (`ApiExecutor`, `JacksonCodec`,
  `DataType`, `http.*`) gardent leur package → **aucun changement d'import** ; seul leur statut
  d'export change dans `module-info`.
- Test packages : les tests des classes déplacées suivent le même renommage de package (ou
  importent le nouveau package). Ils restent dans le module patché → accès intra-module OK.

## Tests

- Aucune nouvelle logique : la suite existante est le filet de sécurité du refactor. Elle doit
  rester **verte après chaque déplacement** (compile + test), puis sur module-path après ajout
  des `module-info`.
- Optionnel : un test léger `ModuleDescriptorTest` (dans football) vérifiant que
  `FootballClient.class.getModule().isNamed()` et que le nom = `io.github.miro93.sportmonks.football`.

## Docs & release

- README : section « Java Module System (JPMS) » documentant les noms de modules
  (`io.github.miro93.sportmonks.core` / `.football`) et le fait que la plomberie est interne.
- **Release 2.0.0** : commits conventionnels avec un commit portant un footer breaking change
  (ex. `feat!:` ou `BREAKING CHANGE:`) pour que release-please bump en 2.0.0. PR de release
  mergée par l'utilisateur.

## Hors périmètre (explicite)

- Aucun changement de comportement runtime ni de signature **publique fonctionnelle** (les
  déplacements ne touchent que de l'interne ; `CollectionRequest`/`SingleResourceRequest`/
  `FootballClient`/modèles inchangés côté API publique).
- Pas de migration progressive / `@Deprecated` (coupe nette en 2.0.0).
- Pas de `provides`/`uses` (aucun ServiceLoader dans la lib).
- Pas de multi-release jar ni de rétro-compat Java < 25.
