# Configurable HttpClient — Design

**Date:** 2026-06-04
**Repo:** `miro93/sportmonks-java-api-client`
**Status:** Approved
**Version cible:** 2.1.0 (additif, non-breaking)

## Objectif

Permettre à l'utilisateur d'**injecter son propre `java.net.http.HttpClient`** sur les deux
builders publics (`CoreClient.Builder`, `FootballClient.Builder`), tout en fournissant un
**default mieux configuré** que le `HttpClient.newHttpClient()` nu utilisé aujourd'hui.

Couvre le vrai besoin de tuning : proxy, `connectTimeout`, `SSLContext`, préférence de version
HTTP, `Executor`, `Authenticator`. Reste 100 % REST/HTTP — voir « Périmètre protocole » ci-dessous.

## Périmètre protocole (vérifié contre la doc SportMonks)

SportMonks API v3 = **REST/JSON sur HTTP uniquement**. Temps réel = **polling** (Livescores
toutes les ~10–15 s). **Pas de gRPC, pas de WebSocket/push natif, pas de HTTP/3 annoncé** (et le
`HttpClient` JDK ne fait pas HTTP/3 de toute façon). Il n'y a donc **aucun protocole alternatif à
brancher** : le seul levier utile est la configuration du `HttpClient`. C'est ce que couvre ce
design.

## Décision JPMS (encapsulation M14 préservée)

On **n'expose PAS** `HttpTransport`/`RawResponse` en public. Le package `core.http` reste un export
**qualifié vers football** (plomberie interne de M14/2.0.0). On expose seulement le type JDK
standard `HttpClient`. Un SPI transport pluggable (OkHttp/Apache) a été **écarté** : faible intérêt
réel (SportMonks est REST-only ; le vrai usage serait le mock en test, qui est interne) pour un coût
de surface API publique permanent.

## Surface API (2 ajouts, additifs)

Une méthode identique sur chaque builder :

```java
/// Overrides the underlying JDK {@link HttpClient} used for all requests.
/// When not set, a default client is used (see implementation note: connect timeout +
/// follow-redirects). Note: this is distinct from {@link #requestTimeout(Duration)} —
/// the client's connect timeout bounds connection establishment, while requestTimeout
/// bounds the request→response deadline.
///
/// @param httpClient the HTTP client to use
/// @return this builder
public Builder httpClient(HttpClient httpClient) {
    this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
    return this;
}
```

- Nouveau champ `private HttpClient httpClient;` (nullable ; default appliqué dans `build()`).
- `.httpClient(null)` → `NullPointerException` (cohérent avec les autres setters du builder).

## Default configuré (factory partagée)

Factory statique dans `core.http` (déjà accessible à football → **zéro duplication** entre les deux
builders), portant les valeurs **en dur sous forme de constantes nommées**. Les **deux** timeouts
(connect ET request) sont regroupés ici, prêts pour une externalisation commune en properties :

```java
// dans JdkHttpTransport (ou un petit helper du même package core.http)
static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(10);
static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(30); // extrait du magic value inline

static HttpClient newDefaultClient() {
    return HttpClient.newBuilder()
            .connectTimeout(DEFAULT_CONNECT_TIMEOUT)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    // version HTTP non forcée → défaut JDK = HTTP/2 avec fallback HTTP/1.1.
    // Pooling de connexions = automatique (keep-alive + multiplexing HTTP/2),
    // aucun réglage de pool au niveau builder dans le JDK (uniquement props système jdk.httpclient.*).
}
```

**Extraction du `requestTimeout` par défaut** : aujourd'hui chaque builder initialise
`private Duration requestTimeout = Duration.ofSeconds(30);` (valeur magique dupliquée dans les deux
modules). Les deux field initializers référenceront désormais
`JdkHttpTransport.DEFAULT_REQUEST_TIMEOUT` — même source unique que le connect timeout. Comportement
identique (30 s), juste centralisé.

> **Note d'évolution :** les deux constantes sont volontairement **en dur** ; un ticket ultérieur les
> externalisera ensemble vers des properties. Ce design ne crée **aucune** plomberie de configuration
> — il suffira de changer la source de ces deux constantes co-localisées.

## Câblage dans `build()`

Les deux `build()` remplacent `new JdkHttpTransport(HttpClient.newHttpClient(), requestTimeout)` par :

```java
HttpClient client = (httpClient != null) ? httpClient : JdkHttpTransport.newDefaultClient();
HttpTransport base = new JdkHttpTransport(client, requestTimeout);
// suite inchangée : RetryingTransport(base, retryPolicy, Sleeper.REAL), etc.
```

- `FootballClient.build()` partage déjà un unique `transport` entre ses trois `ApiExecutor`
  (football / core / odds) → le client injecté s'applique aux trois automatiquement.
- `JdkHttpTransport`, `RetryingTransport`, `requestTimeout`, `baseUrl`, `apiToken` : **inchangés**.

## Modèle de timeouts (documenté pour lever la confusion)

Le `HttpClient` JDK n'a que **deux** dimensions (≠ OkHttp connect/read/write/call) :

| Dimension | API JDK | Dans la lib | Défaut |
|---|---|---|---|
| Connexion TCP/TLS | `HttpClient.connectTimeout()` | **nouveau** (default) | 10 s |
| Requête → réception réponse | `HttpRequest.timeout()` | `requestTimeout` (existant) | 30 s |

`requestTimeout` **est** le « response timeout » : deadline unique depuis l'envoi jusqu'à réception
de la réponse (`HttpTimeoutException` sinon). Ce n'est **pas** un idle/read-timeout par paquet (le
JDK n'a pas d'équivalent) — sans conséquence pour un client REST/polling sur des payloads JSON. La
javadoc du setter `httpClient()` distingue explicitement les deux.

## Tests (TDD)

1. `JdkHttpTransport.newDefaultClient()` → `connectTimeout()` == `Optional.of(Duration.ofSeconds(10))`
   et `followRedirects()` == `HttpClient.Redirect.NORMAL`.
2. `CoreClient.Builder.httpClient(null)` et `FootballClient.Builder.httpClient(null)` → `NullPointerException`.
3. **Injection effective** : un `HttpClient` custom pointant sur un `com.sun.net.httpserver.HttpServer`
   local (réutilise le pattern de `JdkHttpTransportTest`) reçoit bien l'appel, prouvant que le client
   injecté est utilisé — vérifié sur `CoreClient` **et** `FootballClient`.

## Docs

- JavaDoc sur les deux nouvelles méthodes `httpClient(...)` (incluant la distinction
  connectTimeout / requestTimeout).
- Note README : tuning du transport via `httpClient(...)`, pooling automatique, et pointeur vers les
  propriétés système `jdk.httpclient.*` pour le tuning avancé du pool.

## Hors périmètre (YAGNI)

- SPI `HttpTransport` pluggable public (écarté, cf. décision JPMS).
- Setter `connectTimeout()` dédié sur le builder (le default en dur + l'override `httpClient()`
  complet suffisent ; externalisation = ticket properties à venir).
- Tout support gRPC / WebSocket / HTTP/3 (non supporté par SportMonks).
