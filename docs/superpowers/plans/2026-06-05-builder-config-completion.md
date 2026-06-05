# Builder Configuration Completion Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Close the two remaining configuration gaps by adding an explicit `connectTimeout(Duration)` setter (mutually exclusive with `httpClient()`) on both client builders and configurable retryable HTTP statuses via a `RetryPolicy.Builder` — no implicit properties/file/env config.

**Architecture:** `JdkHttpTransport` gains a `newDefaultClient(Duration)` overload. `RetryPolicy` gains an `IntPredicate` field (default preserves `429 || ≥500`) and a fluent `Builder`. Both client builders get a nullable `connectTimeout` field whose value feeds the default-client factory; supplying both `connectTimeout()` and `httpClient()` throws `IllegalStateException` at `build()`. README documents wiring from Spring/Quarkus/Helidon config to these setters.

**Tech Stack:** Java 25, JPMS, JUnit 5, WireMock, AssertJ. Spec: `docs/superpowers/specs/2026-06-05-builder-config-completion-design.md`.

---

## File Structure

- **Modify** `core/src/main/java/io/github/miro93/sportmonks/core/http/JdkHttpTransport.java` — add `newDefaultClient(Duration)` overload; existing no-arg delegates to it.
- **Modify** `core/src/main/java/io/github/miro93/sportmonks/core/retry/RetryPolicy.java` — add `IntPredicate retryableStatus` field (default-preserving), 3-arg ctor, `builder()`, nested `Builder`.
- **Modify** `core/src/main/java/io/github/miro93/sportmonks/core/coreapi/CoreClient.java` — `connectTimeout` field + setter; `build()` wiring + mutual-exclusion guard.
- **Modify** `football/src/main/java/io/github/miro93/sportmonks/football/FootballClient.java` — same builder changes.
- **Modify** `core/src/test/java/io/github/miro93/sportmonks/core/http/JdkHttpTransportTest.java` — overload test.
- **Create** `core/src/test/java/io/github/miro93/sportmonks/core/retry/RetryPolicyTest.java` — predicate default + builder override + validation tests.
- **Modify** `core/src/test/java/io/github/miro93/sportmonks/core/coreapi/CoreClientTest.java` — connectTimeout null/conflict/wired tests.
- **Modify** `football/src/test/java/io/github/miro93/sportmonks/football/FootballClientTest.java` — same.
- **Modify** `README.md` — connectTimeout + retryableStatuses docs and "Configuring from Spring / Quarkus / Helidon" section.

The factory overload is `public static` (mandatory: `core.http` is exported **only** to football via a qualified export, which grants football access to `public` members only; package-private would not compile against `:football`). It stays invisible to end users (the export is not unqualified).

---

## Task 1: `newDefaultClient(Duration)` overload on JdkHttpTransport

**Files:**
- Modify: `core/src/main/java/io/github/miro93/sportmonks/core/http/JdkHttpTransport.java`
- Test: `core/src/test/java/io/github/miro93/sportmonks/core/http/JdkHttpTransportTest.java`

- [ ] **Step 1: Write the failing test**

Add to `JdkHttpTransportTest` (imports `java.time.Duration`, `java.net.http.HttpClient`, and the static `assertThat` are already present):

```java
    @Test
    void newDefaultClientWithCustomConnectTimeoutHonoursIt() {
        HttpClient client = JdkHttpTransport.newDefaultClient(Duration.ofSeconds(5));

        assertThat(client.connectTimeout()).contains(Duration.ofSeconds(5));
        assertThat(client.followRedirects()).isEqualTo(HttpClient.Redirect.NORMAL);
    }

    @Test
    void noArgNewDefaultClientStillUses10sConnectTimeout() {
        HttpClient client = JdkHttpTransport.newDefaultClient();

        assertThat(client.connectTimeout()).contains(Duration.ofSeconds(10));
    }
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :core:test --tests "io.github.miro93.sportmonks.core.http.JdkHttpTransportTest"`
Expected: FAIL — `newDefaultClient(Duration)` does not exist (compile error).

- [ ] **Step 3: Add the overload**

In `JdkHttpTransport.java`, replace the existing `newDefaultClient()` method with these two methods (the no-arg one now delegates):

```java
    /// Builds the default {@link HttpClient} using {@link #DEFAULT_CONNECT_TIMEOUT}.
    /// See {@link #newDefaultClient(Duration)} for the configuration details.
    public static HttpClient newDefaultClient() {
        return newDefaultClient(DEFAULT_CONNECT_TIMEOUT);
    }

    /// Builds the default {@link HttpClient} with the given connect timeout: NORMAL redirect
    /// following and the JDK-default HTTP version (HTTP/2 with HTTP/1.1 fallback). Connection
    /// pooling is automatic (keep-alive + HTTP/2 multiplexing) and not builder-tunable.
    ///
    /// @param connectTimeout the connection-establishment timeout
    /// @return a new default HTTP client
    public static HttpClient newDefaultClient(Duration connectTimeout) {
        return HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew :core:test --tests "io.github.miro93.sportmonks.core.http.JdkHttpTransportTest"`
Expected: PASS (new tests + all pre-existing ones).

- [ ] **Step 5: Commit**

```bash
git add core/src/main/java/io/github/miro93/sportmonks/core/http/JdkHttpTransport.java \
        core/src/test/java/io/github/miro93/sportmonks/core/http/JdkHttpTransportTest.java
git commit -m "feat: add newDefaultClient(Duration) connect-timeout overload

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

## Task 2: Configurable retryable statuses via `RetryPolicy.Builder`

**Files:**
- Modify: `core/src/main/java/io/github/miro93/sportmonks/core/retry/RetryPolicy.java`
- Test: `core/src/test/java/io/github/miro93/sportmonks/core/retry/RetryPolicyTest.java` (new)

- [ ] **Step 1: Write the failing tests**

Create `core/src/test/java/io/github/miro93/sportmonks/core/retry/RetryPolicyTest.java`:

```java
package io.github.miro93.sportmonks.core.retry;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RetryPolicyTest {

    @Test
    void defaultsRetry429AndAll5xx() {
        RetryPolicy policy = RetryPolicy.defaults();

        assertThat(policy.isRetryableStatus(429)).isTrue();
        assertThat(policy.isRetryableStatus(500)).isTrue();
        assertThat(policy.isRetryableStatus(502)).isTrue();
        assertThat(policy.isRetryableStatus(503)).isTrue();
        assertThat(policy.isRetryableStatus(504)).isTrue();
        assertThat(policy.isRetryableStatus(200)).isFalse();
        assertThat(policy.isRetryableStatus(404)).isFalse();
    }

    @Test
    void noneKeepsDefaultRetryablePredicate() {
        // none() only changes attempts to 1; the predicate is unchanged.
        assertThat(RetryPolicy.none().maxAttempts()).isEqualTo(1);
        assertThat(RetryPolicy.none().isRetryableStatus(503)).isTrue();
    }

    @Test
    void builderDefaultsMatchDefaultsFactory() {
        RetryPolicy policy = RetryPolicy.builder().build();

        assertThat(policy.maxAttempts()).isEqualTo(3);
        assertThat(policy.isRetryableStatus(429)).isTrue();
        assertThat(policy.isRetryableStatus(500)).isTrue();
    }

    @Test
    void retryableStatusesOverridesPredicateExactly() {
        RetryPolicy policy = RetryPolicy.builder()
                .retryableStatuses(429, 503)
                .build();

        assertThat(policy.isRetryableStatus(429)).isTrue();
        assertThat(policy.isRetryableStatus(503)).isTrue();
        assertThat(policy.isRetryableStatus(500)).isFalse();
        assertThat(policy.isRetryableStatus(502)).isFalse();
        assertThat(policy.isRetryableStatus(504)).isFalse();
    }

    @Test
    void builderSetsMaxAttemptsAndBackoff() {
        Backoff backoff = new Backoff(java.time.Duration.ofMillis(100),
                java.time.Duration.ofSeconds(5), 3.0);
        RetryPolicy policy = RetryPolicy.builder()
                .maxAttempts(5)
                .backoff(backoff)
                .build();

        assertThat(policy.maxAttempts()).isEqualTo(5);
        assertThat(policy.backoff()).isSameAs(backoff);
    }

    @Test
    void builderRejectsMaxAttemptsBelowOne() {
        assertThatThrownBy(() -> RetryPolicy.builder().maxAttempts(0).build())
                .isInstanceOf(IllegalArgumentException.class);
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :core:test --tests "io.github.miro93.sportmonks.core.retry.RetryPolicyTest"`
Expected: FAIL — `RetryPolicy.builder()` does not exist (compile error).

- [ ] **Step 3: Implement the predicate field + builder**

Replace the entire contents of `RetryPolicy.java` with:

```java
package io.github.miro93.sportmonks.core.retry;

import java.util.Objects;
import java.util.Set;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/// How many attempts to make and which statuses are retryable. Any 5xx and 429
/// are retried by default. Build a custom policy via {@link #builder()}.
public final class RetryPolicy {

    private static final IntPredicate DEFAULT_RETRYABLE = status -> status == 429 || status >= 500;

    private final int maxAttempts;
    private final Backoff backoff;
    private final IntPredicate retryableStatus;

    public RetryPolicy(int maxAttempts, Backoff backoff) {
        this(maxAttempts, backoff, DEFAULT_RETRYABLE);
    }

    public RetryPolicy(int maxAttempts, Backoff backoff, IntPredicate retryableStatus) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }
        this.maxAttempts = maxAttempts;
        this.backoff = backoff;
        this.retryableStatus = Objects.requireNonNull(retryableStatus, "retryableStatus");
    }

    public static RetryPolicy defaults() {
        return new RetryPolicy(3, Backoff.defaults());
    }

    public static RetryPolicy none() {
        return new RetryPolicy(1, Backoff.defaults());
    }

    /// @return a new builder (defaults: 3 attempts, default backoff, retry 429 + all 5xx)
    public static Builder builder() {
        return new Builder();
    }

    public boolean isRetryableStatus(int status) {
        return retryableStatus.test(status);
    }

    public int maxAttempts() {
        return maxAttempts;
    }

    public Backoff backoff() {
        return backoff;
    }

    /// Fluent builder for {@link RetryPolicy}.
    public static final class Builder {
        private int maxAttempts = 3;
        private Backoff backoff = Backoff.defaults();
        private IntPredicate retryableStatus = DEFAULT_RETRYABLE;

        private Builder() {
        }

        /// @param maxAttempts total attempts (must be >= 1, validated at {@link #build()})
        public Builder maxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
            return this;
        }

        /// @param backoff the backoff strategy (non-null)
        public Builder backoff(Backoff backoff) {
            this.backoff = Objects.requireNonNull(backoff, "backoff");
            return this;
        }

        /// Replaces the retryable-status rule with exact membership of the given codes.
        /// Overrides the default (429 + all 5xx) entirely.
        ///
        /// @param statuses the HTTP status codes to treat as retryable
        public Builder retryableStatuses(int... statuses) {
            Set<Integer> set = IntStream.of(statuses).boxed().collect(Collectors.toUnmodifiableSet());
            this.retryableStatus = set::contains;
            return this;
        }

        public RetryPolicy build() {
            return new RetryPolicy(maxAttempts, backoff, retryableStatus);
        }
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew :core:test --tests "io.github.miro93.sportmonks.core.retry.RetryPolicyTest"`
Expected: PASS.

- [ ] **Step 5: Run the full core retry suite (no regressions)**

Run: `./gradlew :core:test --tests "io.github.miro93.sportmonks.core.retry.*"`
Expected: PASS — `BackoffTest` and any `RetryingTransport` tests still green (`isRetryableStatus` signature unchanged).

- [ ] **Step 6: Commit**

```bash
git add core/src/main/java/io/github/miro93/sportmonks/core/retry/RetryPolicy.java \
        core/src/test/java/io/github/miro93/sportmonks/core/retry/RetryPolicyTest.java
git commit -m "feat: configurable retryable statuses via RetryPolicy.builder()

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

## Task 3: `connectTimeout()` on CoreClient.Builder

**Files:**
- Modify: `core/src/main/java/io/github/miro93/sportmonks/core/coreapi/CoreClient.java`
- Test: `core/src/test/java/io/github/miro93/sportmonks/core/coreapi/CoreClientTest.java`

- [ ] **Step 1: Write the failing tests**

Add to `CoreClientTest` (imports `java.time.Duration` — add it; `Duration` is not yet imported in this file). All other needed imports (`HttpClient`, `WireMockRuntimeInfo`, static WireMock + AssertJ) are already present:

```java
    @Test
    void connectTimeoutRejectsNull() {
        assertThatThrownBy(() -> CoreClient.builder().connectTimeout(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void connectTimeoutAndHttpClientAreMutuallyExclusive() {
        HttpClient anyClient = HttpClient.newHttpClient();
        assertThatThrownBy(() -> CoreClient.builder()
                .apiToken(ApiToken.of("tok"))
                .connectTimeout(Duration.ofSeconds(5))
                .httpClient(anyClient)
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mutually exclusive");
    }

    @Test
    void connectTimeoutOnDefaultClientStillReachesServer(WireMockRuntimeInfo wm) {
        // Proves the connectTimeout path builds a working default client (no httpClient supplied).
        stubFor(get(urlPathEqualTo("/continents")).willReturn(okJson("""
                { "data": [ { "id": 1, "name": "Europe", "code": "EU" } ] }
                """)));

        var client = CoreClient.builder()
                .apiToken(ApiToken.of("tok"))
                .baseUrl(wm.getHttpBaseUrl())
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        assertThat(client.continents().all().get().data()).hasSize(1);
    }
```

Add this import near the other `java.*` imports at the top of the file:

```java
import java.time.Duration;
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :core:test --tests "io.github.miro93.sportmonks.core.coreapi.CoreClientTest"`
Expected: FAIL — `connectTimeout` method does not exist (compile error).

- [ ] **Step 3: Add the field, setter, and build() wiring**

In `CoreClient.java`, add the field to `Builder` next to the existing `httpClient` field (after line `private HttpClient httpClient;`):

```java
        private Duration connectTimeout;
```

Add the setter after the existing `httpClient(...)` setter:

```java
        /// Sets the connection-establishment timeout for the built-in default {@link HttpClient}.
        /// Only meaningful when no custom {@link #httpClient(HttpClient)} is supplied — a
        /// user-provided client carries its own (immutable) connect timeout. Supplying BOTH is
        /// rejected at {@link #build()}. When unset, defaults to
        /// {@link JdkHttpTransport#DEFAULT_CONNECT_TIMEOUT}.
        ///
        /// @param connectTimeout the connect timeout (non-null)
        /// @return this builder
        public Builder connectTimeout(Duration connectTimeout) {
            this.connectTimeout = Objects.requireNonNull(connectTimeout, "connectTimeout");
            return this;
        }
```

Replace the first two lines of the `build()` body (the `Objects.requireNonNull(apiToken...)` and the `HttpClient client = ...` line) with:

```java
            Objects.requireNonNull(apiToken, "apiToken is required");
            if (httpClient != null && connectTimeout != null) {
                throw new IllegalStateException(
                        "connectTimeout() and httpClient() are mutually exclusive: a supplied "
                        + "HttpClient carries its own connect timeout");
            }
            HttpClient client = (httpClient != null)
                    ? httpClient
                    : JdkHttpTransport.newDefaultClient(
                          connectTimeout != null ? connectTimeout : JdkHttpTransport.DEFAULT_CONNECT_TIMEOUT);
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew :core:test --tests "io.github.miro93.sportmonks.core.coreapi.CoreClientTest"`
Expected: PASS (new tests + all pre-existing ones).

- [ ] **Step 5: Commit**

```bash
git add core/src/main/java/io/github/miro93/sportmonks/core/coreapi/CoreClient.java \
        core/src/test/java/io/github/miro93/sportmonks/core/coreapi/CoreClientTest.java
git commit -m "feat: expose connectTimeout() on CoreClient.Builder

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

## Task 4: `connectTimeout()` on FootballClient.Builder

**Files:**
- Modify: `football/src/main/java/io/github/miro93/sportmonks/football/FootballClient.java`
- Test: `football/src/test/java/io/github/miro93/sportmonks/football/FootballClientTest.java`

- [ ] **Step 1: Write the failing tests**

Add to `FootballClientTest` (add `import java.time.Duration;` to the top imports; `HttpClient`, `WireMockRuntimeInfo`, static WireMock + AssertJ are already present). Note: this test class uses token `"tok-77"` and a `client(String baseUrl)` helper — mirror the existing style:

```java
    @Test
    void connectTimeoutRejectsNull() {
        assertThatThrownBy(() -> FootballClient.builder().connectTimeout(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void connectTimeoutAndHttpClientAreMutuallyExclusive() {
        HttpClient anyClient = HttpClient.newHttpClient();
        assertThatThrownBy(() -> FootballClient.builder()
                .apiToken(ApiToken.of("tok-77"))
                .connectTimeout(Duration.ofSeconds(5))
                .httpClient(anyClient)
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mutually exclusive");
    }

    @Test
    void connectTimeoutOnDefaultClientStillReachesServer(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/fixtures/1")).willReturn(okJson("""
                { "data": { "id": 1, "name": "A vs B" } }
                """)));

        var client = FootballClient.builder()
                .apiToken(ApiToken.of("tok-77"))
                .baseUrl(wm.getHttpBaseUrl())
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        assertThat(client.fixtures().byId(1L).get().data().name()).isEqualTo("A vs B");
    }
```

Add this import near the other `java.*` imports at the top of the file:

```java
import java.time.Duration;
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :football:test --tests "io.github.miro93.sportmonks.football.FootballClientTest"`
Expected: FAIL — `connectTimeout` method does not exist (compile error).

- [ ] **Step 3: Add the field, setter, and build() wiring**

In `FootballClient.java`, add the field to `Builder` next to the existing `httpClient` field:

```java
        private Duration connectTimeout;
```

Add the setter after the existing `httpClient(...)` setter (identical JavaDoc to CoreClient):

```java
        /// Sets the connection-establishment timeout for the built-in default {@link HttpClient}.
        /// Only meaningful when no custom {@link #httpClient(HttpClient)} is supplied — a
        /// user-provided client carries its own (immutable) connect timeout. Supplying BOTH is
        /// rejected at {@link #build()}. When unset, defaults to
        /// {@link JdkHttpTransport#DEFAULT_CONNECT_TIMEOUT}.
        ///
        /// @param connectTimeout the connect timeout (non-null)
        /// @return this builder
        public Builder connectTimeout(Duration connectTimeout) {
            this.connectTimeout = Objects.requireNonNull(connectTimeout, "connectTimeout");
            return this;
        }
```

Replace the first two lines of the `build()` body (the `Objects.requireNonNull(apiToken...)` and the `HttpClient client = ...` line) with:

```java
            Objects.requireNonNull(apiToken, "apiToken is required");
            if (httpClient != null && connectTimeout != null) {
                throw new IllegalStateException(
                        "connectTimeout() and httpClient() are mutually exclusive: a supplied "
                        + "HttpClient carries its own connect timeout");
            }
            HttpClient client = (httpClient != null)
                    ? httpClient
                    : JdkHttpTransport.newDefaultClient(
                          connectTimeout != null ? connectTimeout : JdkHttpTransport.DEFAULT_CONNECT_TIMEOUT);
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew :football:test --tests "io.github.miro93.sportmonks.football.FootballClientTest"`
Expected: PASS (new tests + all pre-existing ones).

- [ ] **Step 5: Commit**

```bash
git add football/src/main/java/io/github/miro93/sportmonks/football/FootballClient.java \
        football/src/test/java/io/github/miro93/sportmonks/football/FootballClientTest.java
git commit -m "feat: expose connectTimeout() on FootballClient.Builder

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

## Task 5: Documentation — new setters + framework wiring

**Files:**
- Modify: `README.md`

- [ ] **Step 1: Document `connectTimeout` in the existing transport section**

In `README.md`, find the "### Configuring the HTTP transport" section. After the existing code block that shows `httpClient(httpClient)`, add this paragraph + snippet:

````markdown
If you only need to change the connection-establishment timeout (and not the whole client),
use `connectTimeout(...)` instead — it tunes the built-in default client:

```java
FootballClient client = FootballClient.builder()
        .apiToken(ApiToken.fromEnv())
        .connectTimeout(Duration.ofSeconds(5))   // default: 10s
        .build();
```

`connectTimeout(...)` and `httpClient(...)` are **mutually exclusive**: a client you supply
already carries its own connect timeout, so setting both throws `IllegalStateException` at
`build()`. `connectTimeout` bounds connection establishment; `requestTimeout` bounds the
request→response deadline.
````

- [ ] **Step 2: Document configurable retryable statuses**

Still in `README.md`, locate the "## Error Handling" section (or the nearest retry/`RetryPolicy` mention). If there is no retry section, add a new "### Retry policy" subsection immediately before "## Error Handling" with this content; if a retry section already exists, append the snippet to it:

````markdown
### Retry policy

By default the client retries `429` and all `5xx` responses, up to 3 attempts, with jittered
exponential backoff. Override any of these via `RetryPolicy.builder()`:

```java
import io.github.miro93.sportmonks.core.retry.RetryPolicy;

RetryPolicy retry = RetryPolicy.builder()
        .maxAttempts(5)
        .retryableStatuses(429, 502, 503, 504)   // replaces the default 429-and-all-5xx rule
        .build();

FootballClient client = FootballClient.builder()
        .apiToken(ApiToken.fromEnv())
        .retryPolicy(retry)
        .build();
```

`retryableStatuses(...)` replaces the default rule entirely with exact membership of the codes
you list.
````

- [ ] **Step 3: Add the "Configuring from Spring / Quarkus / Helidon" section**

Still in `README.md`, immediately after the "### Configuring the HTTP transport" section (and its new `connectTimeout` paragraph from Step 1), add:

````markdown
### Configuring from Spring, Quarkus or Helidon

This library reads **no implicit configuration source** — every option is set explicitly on the
builder. Framework config files (`application.properties` / `application.yaml`) are not exposed as
JVM system properties, so map your framework's config to the builder setters with a few lines.
(JVM `-D` system properties, e.g. `-Dhttp.proxyHost`, still apply at the JDK level.)

**Spring Boot** — bind properties and expose the client as a bean:

```java
@ConfigurationProperties("sportmonks")
record SportmonksProps(Duration connectTimeout, Duration requestTimeout, int maxRetries) {}

@Bean
FootballClient footballClient(SportmonksProps props) {
    return FootballClient.builder()
            .apiToken(ApiToken.fromEnv())
            .connectTimeout(props.connectTimeout())
            .requestTimeout(props.requestTimeout())
            .retryPolicy(RetryPolicy.builder().maxAttempts(props.maxRetries()).build())
            .build();
}
```

**Quarkus** — inject MicroProfile config and produce a CDI bean:

```java
@Produces
@ApplicationScoped
FootballClient footballClient(
        @ConfigProperty(name = "sportmonks.connect-timeout") Duration connectTimeout,
        @ConfigProperty(name = "sportmonks.request-timeout") Duration requestTimeout) {
    return FootballClient.builder()
            .apiToken(ApiToken.fromEnv())
            .connectTimeout(connectTimeout)
            .requestTimeout(requestTimeout)
            .build();
}
```

**Helidon** — read from `Config` and build:

```java
Config cfg = Config.global().get("sportmonks");
FootballClient client = FootballClient.builder()
        .apiToken(ApiToken.fromEnv())
        .connectTimeout(cfg.get("connect-timeout").as(Duration.class).orElse(Duration.ofSeconds(10)))
        .requestTimeout(cfg.get("request-timeout").as(Duration.class).orElse(Duration.ofSeconds(30)))
        .build();
```
````

- [ ] **Step 4: Verify the README renders and examples are consistent**

Run: `grep -n "connectTimeout\|retryableStatuses\|Configuring from Spring" README.md`
Expected: matches in the transport section, the retry section, and the framework section. Confirm `SPORTMONKS_API_TOKEN` (not `SPORTMONKS_TOKEN`) is still used anywhere a raw env var is shown, and that `Duration` usages are obvious from context (the snippets assume `import java.time.Duration;`).

- [ ] **Step 5: Commit**

```bash
git add README.md
git commit -m "docs: document connectTimeout, retryableStatuses and framework wiring

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>"
```

---

## Final verification

- [ ] **Run the full build**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL, all modules compile, all tests pass (the existing 351 + the new connectTimeout/RetryPolicy tests).

- [ ] **Confirm additive-only public API**

Manually confirm no existing public signature changed: `RetryPolicy(int, Backoff)`, `defaults()`, `none()`, `isRetryableStatus(int)`, `JdkHttpTransport.newDefaultClient()` all still exist with the same signatures. Only additions: `newDefaultClient(Duration)`, the 3-arg `RetryPolicy` ctor, `RetryPolicy.builder()`/`Builder`, and `connectTimeout(Duration)` on both builders. → release-please will pick a **minor** bump (2.2.0).
