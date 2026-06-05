# Configurable HttpClient Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let users inject their own `java.net.http.HttpClient` on both client builders, with a sensibly-configured default, while keeping `HttpTransport` internal (M14 encapsulation intact).

**Architecture:** Add a shared, `public static` factory + two timeout constants to `JdkHttpTransport` (the package `core.http` is already a qualified export to football, so both modules reach it with zero duplication — the members must be `public` because the qualified export only grants football access to `public` members of public types). Add a `httpClient(HttpClient)` setter to `CoreClient.Builder` and `FootballClient.Builder`; their `build()` methods use the injected client or fall back to the factory default, and source the `requestTimeout` default from the new constant.

**Tech Stack:** Java 25, JPMS, JUnit 5, WireMock, AssertJ. Spec: `docs/superpowers/specs/2026-06-04-configurable-http-client-design.md`.

---

## File Structure

- **Modify** `core/src/main/java/io/github/miro93/sportmonks/core/http/JdkHttpTransport.java` — add `DEFAULT_CONNECT_TIMEOUT`, `DEFAULT_REQUEST_TIMEOUT` constants and `newDefaultClient()` factory (all `public static`, visible only to core + football via the existing qualified export).
- **Modify** `core/src/main/java/io/github/miro93/sportmonks/core/coreapi/CoreClient.java` — `httpClient` field + setter; `build()` uses it; `requestTimeout` default sourced from constant.
- **Modify** `football/src/main/java/io/github/miro93/sportmonks/football/FootballClient.java` — same builder changes (single shared transport already feeds all three executors).
- **Modify** `core/src/test/java/io/github/miro93/sportmonks/core/http/JdkHttpTransportTest.java` — factory config + constant tests.
- **Modify** `core/src/test/java/io/github/miro93/sportmonks/core/coreapi/CoreClientTest.java` — null-check + injection-effective tests.
- **Modify** `football/src/test/java/io/github/miro93/sportmonks/football/FootballClientTest.java` — null-check + injection-effective tests.
- **Modify** `README.md` — transport tuning section.

Constants/factory are **`public static`** on `JdkHttpTransport` (already a `public final class`). They **must** be `public` so `FootballClient.Builder` — a different module/package — can reach them across the qualified export (which only exposes `public` members); package-private would not compile against `:football`. Because `core.http` is exported **only** to football (not unqualified), end users never see them — encapsulation preserved.

---

## Task 1: Default-client factory + timeout constants

**Files:**
- Modify: `core/src/main/java/io/github/miro93/sportmonks/core/http/JdkHttpTransport.java`
- Test: `core/src/test/java/io/github/miro93/sportmonks/core/http/JdkHttpTransportTest.java`

- [ ] **Step 1: Write the failing tests**

Add to `JdkHttpTransportTest` (same package `io.github.miro93.sportmonks.core.http`, so the `public static` members are visible). Add imports `java.net.http.HttpClient.Redirect` is not needed — use `HttpClient.Redirect.NORMAL` (class `HttpClient` already imported). Ensure `java.time.Duration` and `org.assertj.core.api.Assertions.assertThat` are imported (they already are).

```java
    @Test
    void defaultRequestTimeoutConstantIs30s() {
        assertThat(JdkHttpTransport.DEFAULT_REQUEST_TIMEOUT).isEqualTo(Duration.ofSeconds(30));
    }

    @Test
    void newDefaultClientHasConnectTimeoutAndFollowsRedirects() {
        HttpClient client = JdkHttpTransport.newDefaultClient();

        assertThat(client.connectTimeout()).contains(Duration.ofSeconds(10));
        assertThat(client.followRedirects()).isEqualTo(HttpClient.Redirect.NORMAL);
    }
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :core:test --tests "io.github.miro93.sportmonks.core.http.JdkHttpTransportTest"`
Expected: FAIL — compile error / cannot resolve `DEFAULT_REQUEST_TIMEOUT` and `newDefaultClient`.

- [ ] **Step 3: Add the constants and factory**

In `JdkHttpTransport.java`, add a `java.net.http.HttpClient.Redirect` is reachable via `HttpClient.Redirect`. Insert the members near the top of the class body (after the existing fields, before the constructor):

```java
    /// Default connection-establishment timeout for the built-in {@link HttpClient}.
    /// Hard-coded for now; a later ticket may externalise this to a property.
    public static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(10);

    /// Default request→response deadline applied per request (see {@link HttpRequest#timeout}).
    /// Hard-coded for now; a later ticket may externalise this to a property.
    public static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(30);

    /// Builds the default {@link HttpClient}: an explicit connect timeout and NORMAL redirect
    /// following. The HTTP version is left at the JDK default (HTTP/2 with HTTP/1.1 fallback);
    /// connection pooling is automatic (keep-alive + HTTP/2 multiplexing) and not builder-tunable.
    public static HttpClient newDefaultClient() {
        return HttpClient.newBuilder()
                .connectTimeout(DEFAULT_CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew :core:test --tests "io.github.miro93.sportmonks.core.http.JdkHttpTransportTest"`
Expected: PASS (all tests, including the 3 pre-existing ones).

- [ ] **Step 5: Commit**

```bash
git add core/src/main/java/io/github/miro93/sportmonks/core/http/JdkHttpTransport.java \
        core/src/test/java/io/github/miro93/sportmonks/core/http/JdkHttpTransportTest.java
git commit -m "feat: add default HttpClient factory and timeout constants

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

## Task 2: `httpClient()` on CoreClient.Builder

**Files:**
- Modify: `core/src/main/java/io/github/miro93/sportmonks/core/coreapi/CoreClient.java`
- Test: `core/src/test/java/io/github/miro93/sportmonks/core/coreapi/CoreClientTest.java`

- [ ] **Step 1: Write the failing tests**

Add to `CoreClientTest`. Add imports: `io.github.miro93.sportmonks.core.error.TransportException`, `io.github.miro93.sportmonks.core.retry.RetryPolicy`, `java.net.InetSocketAddress`, `java.net.ProxySelector`, `java.net.http.HttpClient`. The static AssertJ import `assertThatThrownBy` is already present.

```java
    @Test
    void httpClientRejectsNull() {
        assertThatThrownBy(() -> CoreClient.builder().httpClient(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void injectedHttpClientIsActuallyUsed(WireMockRuntimeInfo wm) {
        // Would succeed via the default client; a dead proxy proves the injected client is used.
        stubFor(get(urlPathEqualTo("/continents")).willReturn(okJson("""
                { "data": [ { "id": 1, "name": "Europe", "code": "EU" } ] }
                """)));
        HttpClient deadProxyClient = HttpClient.newBuilder()
                .proxy(ProxySelector.of(new InetSocketAddress("localhost", 1)))
                .build();

        var client = CoreClient.builder()
                .apiToken(ApiToken.of("tok"))
                .baseUrl(wm.getHttpBaseUrl())
                .httpClient(deadProxyClient)
                .retryPolicy(RetryPolicy.none())
                .build();

        assertThatThrownBy(() -> client.continents().all().get())
                .isInstanceOf(TransportException.class);
    }
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :core:test --tests "io.github.miro93.sportmonks.core.coreapi.CoreClientTest"`
Expected: FAIL — `httpClient` method does not exist (compile error).

- [ ] **Step 3: Implement the builder change**

In `CoreClient.java`, add the field alongside the other builder fields (the `requestTimeout` field currently reads `private Duration requestTimeout = Duration.ofSeconds(30);` near line 99):

```java
        private HttpClient httpClient;
        private Duration requestTimeout = JdkHttpTransport.DEFAULT_REQUEST_TIMEOUT;
```

(Replace the existing `requestTimeout` initializer with the constant; add the `httpClient` field directly above it.)

Add the setter next to the other setters (e.g. after `requestTimeout(...)`):

```java
        /// Overrides the underlying JDK {@link HttpClient} used for all requests.
        /// When not set, a default client is used (explicit 10s connect timeout, NORMAL redirects).
        /// This is distinct from {@link #requestTimeout(Duration)}: the client's connect timeout
        /// bounds connection establishment, while requestTimeout bounds the request→response deadline.
        ///
        /// @param httpClient the HTTP client to use
        /// @return this builder
        public Builder httpClient(HttpClient httpClient) {
            this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
            return this;
        }
```

In `build()`, replace:

```java
            HttpTransport base = new JdkHttpTransport(HttpClient.newHttpClient(), requestTimeout);
```

with:

```java
            HttpClient client = (httpClient != null) ? httpClient : JdkHttpTransport.newDefaultClient();
            HttpTransport base = new JdkHttpTransport(client, requestTimeout);
```

(`HttpClient`, `Duration`, `JdkHttpTransport`, `Objects` are already imported.)

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew :core:test --tests "io.github.miro93.sportmonks.core.coreapi.CoreClientTest"`
Expected: PASS (new tests + all pre-existing CoreClientTest tests).

- [ ] **Step 5: Commit**

```bash
git add core/src/main/java/io/github/miro93/sportmonks/core/coreapi/CoreClient.java \
        core/src/test/java/io/github/miro93/sportmonks/core/coreapi/CoreClientTest.java
git commit -m "feat: expose httpClient() on CoreClient.Builder

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

## Task 3: `httpClient()` on FootballClient.Builder

**Files:**
- Modify: `football/src/main/java/io/github/miro93/sportmonks/football/FootballClient.java`
- Test: `football/src/test/java/io/github/miro93/sportmonks/football/FootballClientTest.java`

- [ ] **Step 1: Write the failing tests**

Add to `FootballClientTest`. Add imports: `io.github.miro93.sportmonks.core.error.TransportException`, `io.github.miro93.sportmonks.core.retry.RetryPolicy`, `java.net.InetSocketAddress`, `java.net.ProxySelector`, `java.net.http.HttpClient`. `assertThatThrownBy` is already imported.

```java
    @Test
    void httpClientRejectsNull() {
        assertThatThrownBy(() -> FootballClient.builder().httpClient(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void injectedHttpClientIsActuallyUsed(WireMockRuntimeInfo wm) {
        // Would succeed via the default client; a dead proxy proves the injected client is used.
        stubFor(get(urlPathEqualTo("/fixtures/1")).willReturn(okJson("""
                { "data": { "id": 1, "name": "A vs B" } }
                """)));
        HttpClient deadProxyClient = HttpClient.newBuilder()
                .proxy(ProxySelector.of(new InetSocketAddress("localhost", 1)))
                .build();

        var client = FootballClient.builder()
                .apiToken(ApiToken.of("tok-77"))
                .baseUrl(wm.getHttpBaseUrl())
                .httpClient(deadProxyClient)
                .retryPolicy(RetryPolicy.none())
                .build();

        assertThatThrownBy(() -> client.fixtures().byId(1L).get())
                .isInstanceOf(TransportException.class);
    }
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :football:test --tests "io.github.miro93.sportmonks.football.FootballClientTest"`
Expected: FAIL — `httpClient` method does not exist (compile error).

- [ ] **Step 3: Implement the builder change**

In `FootballClient.java`, add the field alongside the other builder fields (the `requestTimeout` field currently reads `private Duration requestTimeout = Duration.ofSeconds(30);` near line 398). Reference the constant via the already-used `JdkHttpTransport` import:

```java
        private HttpClient httpClient;
        private Duration requestTimeout = JdkHttpTransport.DEFAULT_REQUEST_TIMEOUT;
```

(Replace the existing `requestTimeout` initializer with the constant; add the `httpClient` field directly above it.)

Add the setter next to the other setters (e.g. after `requestTimeout(...)`):

```java
        /// Overrides the underlying JDK {@link HttpClient} used for all requests (football, core,
        /// and odds executors share it). When not set, a default client is used (explicit 10s
        /// connect timeout, NORMAL redirects). This is distinct from {@link #requestTimeout(Duration)}:
        /// the client's connect timeout bounds connection establishment, while requestTimeout bounds
        /// the request→response deadline.
        ///
        /// @param httpClient the HTTP client to use
        /// @return this builder
        public Builder httpClient(HttpClient httpClient) {
            this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
            return this;
        }
```

In `build()`, replace:

```java
            HttpTransport base = new JdkHttpTransport(HttpClient.newHttpClient(), requestTimeout);
```

with:

```java
            HttpClient client = (httpClient != null) ? httpClient : JdkHttpTransport.newDefaultClient();
            HttpTransport base = new JdkHttpTransport(client, requestTimeout);
```

(`HttpClient`, `Duration`, `JdkHttpTransport`, `Objects` are already imported.)

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew :football:test --tests "io.github.miro93.sportmonks.football.FootballClientTest"`
Expected: PASS (new tests + all pre-existing FootballClientTest tests).

- [ ] **Step 5: Commit**

```bash
git add football/src/main/java/io/github/miro93/sportmonks/football/FootballClient.java \
        football/src/test/java/io/github/miro93/sportmonks/football/FootballClientTest.java
git commit -m "feat: expose httpClient() on FootballClient.Builder

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

## Task 4: README — transport tuning section

**Files:**
- Modify: `README.md`

- [ ] **Step 1: Add a documentation section**

Add a subsection under the existing configuration/usage docs (place it after the builder/configuration examples; match the surrounding heading depth and code-fence style). Use this content:

````markdown
### Configuring the HTTP transport

Both `CoreClient.Builder` and `FootballClient.Builder` accept a custom
`java.net.http.HttpClient`, letting you set a proxy, SSL context, authenticator,
HTTP version preference, or executor:

```java
HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .proxy(ProxySelector.getDefault())
        .build();

FootballClient client = FootballClient.builder()
        .apiToken(ApiToken.of(System.getenv("SPORTMONKS_TOKEN")))
        .httpClient(httpClient)
        .build();
```

When no client is supplied, a default one is used: a 10-second connect timeout and
`NORMAL` redirect following, with the JDK's default HTTP/2 (falling back to HTTP/1.1).

Two timeout dimensions apply, mirroring the JDK client:

- **connect timeout** — on the `HttpClient`, bounds connection establishment.
- **request timeout** — `requestTimeout(Duration)` on the builder (default 30s), the
  request→response deadline (`HttpTimeoutException` if exceeded).

Connection pooling is automatic (keep-alive plus HTTP/2 multiplexing). The JDK exposes
no builder-level pool sizing; tune it via the `jdk.httpclient.*` system properties if
needed.
````

- [ ] **Step 2: Verify it renders**

Run: `grep -n "Configuring the HTTP transport" README.md`
Expected: prints the new heading line.

- [ ] **Step 3: Commit**

```bash
git add README.md
git commit -m "docs: document configurable HttpClient and timeouts

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

## Task 5: Full build + verification

**Files:** none (verification only)

- [ ] **Step 1: Run the full build and test suite**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL; all modules compile (JPMS descriptors unchanged), all tests green (the 345 existing + the new ones).

- [ ] **Step 2: Confirm no new public API leaked**

Run: `grep -n "exports io.github.miro93.sportmonks.core.http" core/src/main/java/module-info.java`
Expected: still the single qualified line `exports io.github.miro93.sportmonks.core.http to io.github.miro93.sportmonks.football;` — i.e. `HttpTransport`/`RawResponse`/`newDefaultClient` remain invisible to end users.

- [ ] **Step 3: Final state**

No commit needed if Steps 1–2 pass and the tree is clean (all work committed in Tasks 1–4). The `feat:` commits will drive release-please to a 2.1.0 minor release.

---

## Self-Review Notes

- **Spec coverage:** factory + both constants (Task 1); `httpClient()` on both builders + default wiring + `requestTimeout` constant extraction (Tasks 2–3); all three spec tests — factory config, null-check, injection-effective (Tasks 1–3); docs (Task 4); JPMS encapsulation guard (Task 5 Step 2). Covered.
- **Type consistency:** `newDefaultClient()`, `DEFAULT_CONNECT_TIMEOUT`, `DEFAULT_REQUEST_TIMEOUT`, `httpClient(HttpClient)` named identically across all tasks. `RetryPolicy.none()` and `TransportException` verified to exist in the codebase.
- **No placeholders:** every code and command step is concrete.
