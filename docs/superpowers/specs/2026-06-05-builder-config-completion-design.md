# Builder Configuration Completion — Design

**Date:** 2026-06-05
**Repo:** `miro93/sportmonks-java-api-client`
**Status:** Approved
**Version cible:** 2.2.0 (additif, non-breaking)

## Objectif

Rendre **toute** la configuration du client tunable **explicitement via le builder** (et donc
câblable depuis n'importe quel framework par quelques lignes de glue), sans introduire de système
de properties/fichier/environnement implicite. On comble les **deux seuls trous** restants :

1. Le **connect timeout** n'a pas de setter (seulement la constante `DEFAULT_CONNECT_TIMEOUT`).
2. Les **statuts HTTP retryables** sont hardcodés (`429 || ≥500`) dans `RetryPolicy.isRetryableStatus`.

Le reste est déjà couvert : `apiToken`, `httpClient`, `requestTimeout`, `baseUrl`
(football/core/odds), `retryPolicy` (maxAttempts + backoff).

## Décision de cadrage (vérifiée)

Un système de **properties** a été **écarté** après analyse. Les fichiers de config des frameworks
(`application.properties` Spring/Quarkus, `application.yaml` Helidon, `microprofile-config.properties`)
**ne sont PAS** exposés via `System.getProperty()` : chaque framework charge sa config dans sa propre
abstraction (`Environment`, MicroProfile/SmallRye Config, Helidon Config). Un `System.getProperty()`
dans la lib ne verrait que les `-D` JVM, pas les fichiers. La voie idiomatique pour une lib est donc
**setters explicites + glue côté application** : l'utilisateur mappe sa config framework vers les
setters du builder en quelques lignes. Ce design rend cette voie **complète à 100 %** et la documente.

MicroProfile Config intégré (auto pour Quarkus/Helidon) a aussi été écarté : ne couvre pas Spring,
ajoute une dépendance + un `requires` JPMS, pour un gain marginal vs la glue.

## Surface API (additive)

### 1. `connectTimeout(Duration)` sur `CoreClient.Builder` et `FootballClient.Builder`

```java
/// Sets the connection-establishment timeout for the built-in default {@link HttpClient}.
/// Only meaningful when no custom {@link #httpClient(HttpClient)} is supplied — a user-provided
/// client already carries its own (immutable) connect timeout. Setting BOTH is rejected at
/// {@code build()} time. When unset, defaults to {@link JdkHttpTransport#DEFAULT_CONNECT_TIMEOUT}.
///
/// @param connectTimeout the connect timeout (non-null)
/// @return this builder
public Builder connectTimeout(Duration connectTimeout) {
    this.connectTimeout = Objects.requireNonNull(connectTimeout, "connectTimeout");
    return this;
}
```

- Nouveau champ `private Duration connectTimeout;` (nullable ; défaut appliqué dans `build()`).
- `.connectTimeout(null)` → `NullPointerException` (cohérent avec les autres setters).

### 2. Factory surchargée dans `JdkHttpTransport`

```java
/// Builds the default {@link HttpClient} with the given connect timeout (NORMAL redirects,
/// JDK-default HTTP version). Automatic connection pooling, not builder-tunable.
public static HttpClient newDefaultClient(Duration connectTimeout) {
    return HttpClient.newBuilder()
            .connectTimeout(connectTimeout)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
}

/// Builds the default {@link HttpClient} using {@link #DEFAULT_CONNECT_TIMEOUT}.
public static HttpClient newDefaultClient() {
    return newDefaultClient(DEFAULT_CONNECT_TIMEOUT);
}
```

Visibilité : **`public static`** (obligatoire — `core.http` est exporté de façon *qualifiée* à
football, qui ne voit que les membres `public` ; package-private casserait `:football:compileJava`).
Reste invisible aux utilisateurs finaux (export non non-qualifié).

### 3. `RetryPolicy.Builder` + prédicat de statuts retryables

`RetryPolicy` gagne un champ `private final IntPredicate retryableStatus`, **dont le défaut est
exactement le comportement actuel** :

```java
private static final IntPredicate DEFAULT_RETRYABLE = status -> status == 429 || status >= 500;
```

- `isRetryableStatus(int)` délègue désormais à ce prédicat → **`RetryingTransport` inchangé**.
- Nouveau ctor `RetryPolicy(int maxAttempts, Backoff backoff, IntPredicate retryableStatus)`.
- Le ctor existant `RetryPolicy(int, Backoff)` délègue au nouveau avec `DEFAULT_RETRYABLE` →
  rétrocompatible. `defaults()` et `none()` **inchangés** (mêmes valeurs, défaut préservé).
- Nouveau `RetryPolicy.Builder` :

```java
RetryPolicy rp = RetryPolicy.builder()      // maxAttempts défaut 3, backoff défaut, statuts défaut
        .maxAttempts(5)
        .backoff(new Backoff(Duration.ofMillis(500), Duration.ofSeconds(30), 2.0))
        .retryableStatuses(429, 502, 503, 504)   // → Set.of(codes)::contains
        .build();
```

  - `retryableStatuses(int... codes)` : remplace le prédicat par une appartenance ensembliste
    (`Set.of(codes)::contains`). Non appelé → prédicat par défaut conservé.
  - Validation `maxAttempts >= 1` (réutilise la garde existante du ctor).

> **Note :** la config retry vit **uniquement** dans `RetryPolicy` (un seul foyer : maxAttempts +
> backoff + statuts). Le builder client reste inchangé côté retry — `retryPolicy(RetryPolicy)`
> existe déjà.

## Câblage dans `build()` (les deux builders)

```java
if (httpClient != null && connectTimeout != null) {
    throw new IllegalStateException(
        "connectTimeout() and httpClient() are mutually exclusive: a supplied HttpClient "
        + "carries its own connect timeout");
}
HttpClient client = (httpClient != null)
        ? httpClient
        : JdkHttpTransport.newDefaultClient(
              connectTimeout != null ? connectTimeout : JdkHttpTransport.DEFAULT_CONNECT_TIMEOUT);
HttpTransport base = new JdkHttpTransport(client, requestTimeout);
// suite inchangée : RetryingTransport(base, retryPolicy, Sleeper.REAL), etc.
```

`FootballClient.build()` partage déjà un unique `transport` entre ses trois `ApiExecutor` → le client
(et donc le connect timeout) s'applique aux trois automatiquement.

## Tests (TDD)

1. `JdkHttpTransport.newDefaultClient(Duration.ofSeconds(5)).connectTimeout()` == `Optional.of(5s)` ;
   `newDefaultClient()` (no-arg) reste à 10 s + `Redirect.NORMAL`.
2. `Builder.connectTimeout(null)` → `NullPointerException` (Core **et** Football).
3. **Factory honore la durée (déterministe)** : `JdkHttpTransport.newDefaultClient(Duration.ofSeconds(5))
   .connectTimeout()` == `Optional.of(5s)` (déjà couvert au point 1 ; c'est la preuve directe que la
   valeur est appliquée, sans timing réseau).
4. **Chemin `connectTimeout` câblé (déterministe, WireMock)** : builder avec `connectTimeout(...)` et
   **sans** `httpClient`, pointé sur un stub WireMock → la requête **réussit** (200). Prouve que le
   chemin client-par-défaut-avec-connect-timeout-custom est branché et fonctionnel (une mauvaise
   liaison — NPE, échec de `build()` — apparaîtrait). Core **et** Football.
5. **Conflit** : `connectTimeout(...) + httpClient(...)` ensemble → `IllegalStateException` au
   `build()` (Core **et** Football), message mentionnant la mutuelle exclusion.
6. `RetryPolicy.builder().retryableStatuses(429, 503).build().isRetryableStatus(...)` : `true` pour
   429/503, `false` pour 500/502/504 (prouve l'override exact).
7. **Défaut préservé** : `RetryPolicy.defaults().isRetryableStatus(...)` → `true` pour 429 et tout
   5xx (500, 502, 503, 504) ; `false` pour 200/404 ; `none()` inchangé.
8. `RetryPolicy.builder().maxAttempts(0)` → `IllegalArgumentException` (garde réutilisée).

> Précision testabilité : le `HttpClient` par défaut est interne au transport et n'est pas relisible
> depuis le client construit. La couverture du wiring connect-timeout repose donc sur la **paire**
> test 3 (la factory applique bien la `Duration`) + test 4 (le `build()` emprunte le chemin
> client-par-défaut sans erreur). On **n'asserte aucun timing réseau** (anti-flaky).

## Documentation

### JavaDoc
- `connectTimeout(...)` (les deux builders) : distinction vs `httpClient()` + mutuelle exclusion.
- `RetryPolicy.builder()` / `retryableStatuses(...)` : défaut = 429 + tout 5xx.

### README — nouvelle section « Configuring from Spring / Quarkus / Helidon »
Montre la **voie A** (config framework → setters builder) pour les trois frameworks, p.ex. :

- **Spring Boot** : `@ConfigurationProperties("sportmonks")` → un `@Bean FootballClient` qui appelle
  `.requestTimeout(...).connectTimeout(...).retryPolicy(...)`.
- **Quarkus** : `@ConfigProperty(name = "sportmonks.request-timeout") Duration rt;` → builder dans un
  `@Produces` CDI.
- **Helidon** : `Config.get("sportmonks.request-timeout").as(Duration.class)` → builder.

Plus la doc des deux nouveaux leviers (`connectTimeout`, `retryableStatuses`). On précise
explicitement que la lib **ne lit aucune source de config implicite** — tout passe par le builder.

## Hors périmètre (YAGNI)

- Tout système de properties / fichier `sportmonks.properties` / variables d'environnement implicites.
- Intégration MicroProfile Config / starters/extensions par framework.
- Setters base-URL (déjà présents) ; `followRedirects` configurable ; tuning de pool (props système
  JDK `jdk.httpclient.*` seulement).
- Token via fichier (repo public : secrets jamais sur disque ; rester sur `fromEnv()`/setter).
