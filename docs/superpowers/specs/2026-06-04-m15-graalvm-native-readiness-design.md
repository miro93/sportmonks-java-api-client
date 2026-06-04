# M15 — GraalVM Native-Image Readiness — Design

**Date:** 2026-06-04
**Repo:** `miro93/sportmonks-java-api-client`
**Status:** Approved

## Objectif

Rendre la bibliothèque **native-image ready** : une application GraalVM qui dépend de la lib
doit pouvoir être compilée en image native sans configuration de réflexion supplémentaire. La
lib livre sa propre *reachability metadata* dans ses jars, et un smoke test natif prouve que
le décodage Jackson + l'appel HTTP fonctionnent en image native.

**Outillage disponible en local** : GraalVM 25 via SDKMAN
(`~/.sdkman/candidates/java/25-graal`, `native-image` fonctionnel, gcc + zlib présents). La
métadonnée est donc **générée et vérifiée localement** (agent + build natif), pas seulement en
CI.

**Version : release mineure** (additif : suppression d'une dépendance interne + ajout de
resources + nouveau sous-projet non publié + CI). Aucune API publique fonctionnelle ne change.

## 1. Retirer Blackbird (point hostile au natif)

`JacksonCodec` utilise `BlackbirdModule`, qui génère des accessors à l'exécution via
`LambdaMetafactory`/MethodHandles — mal géré par le monde fermé de native-image.

- `core/.../json/JacksonCodec.java` : supprimer `import ...blackbird.BlackbirdModule;` et
  l'appel `.addModule(new BlackbirdModule())`. Le décodage repose alors sur la réflexion
  Jackson standard (native-friendly avec métadonnée). Mettre à jour le JavaDoc qui mentionne
  Blackbird.
- `core/module-info.java` : retirer `requires tools.jackson.module.blackbird;` et retirer
  `tools.jackson.module.blackbird` des deux directives `opens` (ne garder que
  `tools.jackson.databind`). Effet de bord positif : **supprime le warning JPMS
  « module not found: tools.jackson.module.blackbird »** de M14.
- `football/module-info.java` : retirer `tools.jackson.module.blackbird` de l'`opens`
  `football.model` (ne garder que `tools.jackson.databind`).
- `gradle/libs.versions.toml` + `core/build.gradle.kts` : retirer la dépendance
  `jackson-blackbird`.

Justification : le gain de perf de Blackbird est négligeable pour un client réseau (le réseau
domine le parsing). Vérifiable en local : la suite de tests JVM reste verte sans Blackbird.

## 2. Reachability metadata livrée dans les jars

- Appliquer le plugin `org.graalvm.buildtools.native` à `core` et `football` (build-time
  seulement ; n'ajoute aucune dépendance runtime au jar publié). Version pinnée dans
  `libs.versions.toml`.
- Générer la métadonnée via l'**agent de tracing** en rejouant la suite de tests existante
  (elle décode tous les modèles), puis la copier dans les resources packagées :
  ```
  export JAVA_HOME=~/.sdkman/candidates/java/25-graal
  ./gradlew -Pagent test
  ./gradlew :core:metadataCopy --task test \
      --dir core/src/main/resources/META-INF/native-image/io.github.miro93.sportmonks/core
  ./gradlew :football:metadataCopy --task test \
      --dir football/src/main/resources/META-INF/native-image/io.github.miro93.sportmonks/football
  ```
- La métadonnée générée (`reachability-metadata.json` ou `reflect-config.json` + co.) est
  **commitée** sous `src/main/resources/META-INF/native-image/<group>/<artifact>/` de chaque
  module → packagée dans les jars publiés. Repo PUBLIC : vérifier qu'elle ne contient que des
  noms de classes (pas de chemins locaux).
- Réalisé **par moi en local** (GraalVM dispo). Re-générer à chaque ajout de modèle.

## 3. Smoke test natif auto-contenu (`native-smoke`, non publié)

Nouveau sous-projet Gradle `native-smoke` (dans `settings.gradle.kts`), plugins `application`
+ `org.graalvm.buildtools.native`, dépend de `:football`. **Pas** de plugin de publication.

Un `main()` qui :
1. démarre un `com.sun.net.httpserver.HttpServer` local (module JDK `jdk.httpserver`,
   native-friendly) servant une enveloppe SportMonks canned (ex. une fixture) ;
2. construit un `FootballClient` pointé sur `http://localhost:<port>` ;
3. appelle un endpoint (ex. `fixtures().byId(...)` ou `leagues().all()`), **décode** la réponse ;
4. vérifie un champ attendu ; affiche `NATIVE SMOKE OK` et exit 0, sinon exit ≠ 0.

Pas de WireMock dans l'image native (évite son arbre non-modulaire). Exercice réel de JDK
HttpClient + décodage Jackson sous native-image.

Vérification locale (moi) :
```
export JAVA_HOME=~/.sdkman/candidates/java/25-graal
./gradlew :native-smoke:nativeCompile
./native-smoke/build/native/nativeCompile/native-smoke   # doit afficher NATIVE SMOKE OK, exit 0
```

## 4. Workflow CI `native.yml`

- Déclencheurs : `push` sur `main` + `pull_request` (comme `ci.yml`).
- `permissions: contents: read`.
- `graalvm/setup-graalvm` (action **SHA-pinnée**, cohérent avec le pinning du repo),
  `java-version: '25'`, distribution `graalvm`.
- Étapes : checkout → setup-graalvm → setup-gradle → `./gradlew :native-smoke:nativeCompile`
  → exécuter le binaire (échec du job si exit ≠ 0).
- **Drift check** (recommandé, même job ou job séparé) : `./gradlew -Pagent test` +
  `metadataCopy` vers un dossier temporaire, puis `git diff --exit-code` contre la métadonnée
  commitée → échoue si la métadonnée est obsolète (nouveau modèle non régénéré).

## 5. README

Section « GraalVM native-image » : indiquer que la lib embarque sa reachability-metadata sous
`META-INF/native-image`, qu'une app GraalVM la dépendant compile en natif sans config
supplémentaire, et que le décodage repose sur la réflexion Jackson standard.

## Tests / vérification

- **Local (moi)** : (a) tests JVM verts après drop Blackbird ; (b) `./gradlew build` vert avec
  le plugin natif appliqué (sur Temurin, sans étape native) ; (c) métadonnée générée via
  l'agent (GraalVM) ; (d) `nativeCompile` + exécution du binaire smoke = `NATIVE SMOKE OK`.
- **CI** : `native.yml` reconstruit et réexécute le smoke natif + drift check à chaque PR/push.
- La preuve de native-readiness est donc obtenue **localement ET en CI**.

## Hors périmètre (explicite)

- Pas de compilation native de la lib elle-même (c'est une bibliothèque, pas une application).
- Pas de métadonnée resources/JNI/proxy/serialization (la lib n'en utilise pas).
- Pas de PGO, `-Os`, mostly-static/musl, ni d'autres optimisations d'image.
- Pas de support multi-version de GraalVM (cible : GraalVM 25, alignée sur le toolchain JDK 25).
- Blackbird n'est pas conservé en option (coupe nette ; simplicité + native-friendly).
