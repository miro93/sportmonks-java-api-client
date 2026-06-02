# M1 — Core Foundation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the sport-agnostic `core` module of the SportMonks Java client: HTTP transport, auth, request building, JSON codec, typed errors, retry/backoff, pagination, and an executor that ties them together — all under TDD.

**Architecture:** A small layered core. `JdkHttpTransport` (wraps `java.net.http.HttpClient`) performs raw GETs; `RetryingTransport` decorates it with exponential-backoff retry on 429/5xx; `ApiExecutor` builds the URL from a `RequestSpec`, injects the `ApiToken`, runs the transport, maps non-2xx to a sealed `SportmonksException` hierarchy, and decodes the JSON envelope into a typed `ApiResponse<T>` via `JacksonCodec`. Async is provided at the executor level on a virtual-thread executor (Java 25). `Pages.stream` lazily walks paginated collections.

**Tech Stack:** Java 25, Gradle 9.5 (Kotlin DSL, multi-module), `java.net.http.HttpClient`, Jackson 2.18 + Blackbird module, JUnit 5 + WireMock 3 + AssertJ.

---

## File Structure

```
sportmonks-java-api-client/
├── settings.gradle.kts                     # rootProject name + include core, football
├── gradle/libs.versions.toml               # version catalog
├── gradlew / gradlew.bat / gradle/wrapper/ # Gradle wrapper 9.5.0
├── core/
│   ├── build.gradle.kts
│   └── src/main/java/io/github/miro93/sportmonks/core/
│   │   ├── auth/ApiToken.java
│   │   ├── http/HttpTransport.java
│   │   ├── http/RawResponse.java
│   │   ├── http/JdkHttpTransport.java
│   │   ├── error/SportmonksException.java          (+ 6 final subclasses)
│   │   ├── error/ErrorMapper.java
│   │   ├── response/Pagination.java
│   │   ├── response/RateLimit.java
│   │   ├── response/ApiResponse.java
│   │   ├── json/JacksonCodec.java
│   │   ├── json/CodecException.java
│   │   ├── request/RequestSpec.java
│   │   ├── request/UrlBuilder.java
│   │   ├── retry/Backoff.java
│   │   ├── retry/Sleeper.java
│   │   ├── retry/RetryPolicy.java
│   │   ├── retry/RetryingTransport.java
│   │   ├── paging/Pages.java
│   │   └── ApiExecutor.java
│   └── src/test/java/io/github/miro93/sportmonks/core/...   (mirrors main)
└── football/
    └── build.gradle.kts                    # depends on :core (empty module for now, fleshed in M2)
```

Maps to M1 issues: scaffold → Task 1; ApiToken → Task 2; transport → Task 3; errors → Task 4; codec+response → Task 5; request builder → Task 6; retry/backoff → Task 7; pagination → Task 8; executor (integration seam for football) → Task 9. WireMock test infra is established in Task 1 and first exercised in Task 3.

---

## Task 1: Project scaffold (Gradle multi-module + version catalog + wrapper)

**Files:**
- Create: `settings.gradle.kts`
- Create: `gradle/libs.versions.toml`
- Create: `core/build.gradle.kts`
- Create: `football/build.gradle.kts`
- Create: `.gitignore`

- [ ] **Step 1: Create `settings.gradle.kts`**

```kotlin
rootProject.name = "sportmonks-java-api-client"

include("core", "football")
```

- [ ] **Step 2: Create `gradle/libs.versions.toml`**

```toml
[versions]
jackson = "2.18.2"
junit = "5.11.4"
assertj = "3.27.3"
wiremock = "3.10.0"

[libraries]
jackson-databind = { module = "com.fasterxml.jackson.core:jackson-databind", version.ref = "jackson" }
jackson-blackbird = { module = "com.fasterxml.jackson.module:jackson-module-blackbird", version.ref = "jackson" }
junit-jupiter = { module = "org.junit.jupiter:junit-jupiter", version.ref = "junit" }
junit-launcher = { module = "org.junit.platform:junit-platform-launcher", version = "1.11.4" }
assertj = { module = "org.assertj:assertj-core", version.ref = "assertj" }
wiremock = { module = "org.wiremock:wiremock", version.ref = "wiremock" }
```

- [ ] **Step 3: Create `core/build.gradle.kts`**

```kotlin
plugins {
    `java-library`
}

group = "io.github.miro93.sportmonks"
version = "0.1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    api(libs.jackson.databind)
    implementation(libs.jackson.blackbird)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj)
    testImplementation(libs.wiremock)
    testRuntimeOnly(libs.junit.launcher)
}

tasks.test {
    useJUnitPlatform()
}
```

- [ ] **Step 4: Create `football/build.gradle.kts`**

```kotlin
plugins {
    `java-library`
}

group = "io.github.miro93.sportmonks"
version = "0.1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":core"))

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj)
    testImplementation(libs.wiremock)
    testRuntimeOnly(libs.junit.launcher)
}

tasks.test {
    useJUnitPlatform()
}
```

- [ ] **Step 5: Create `.gitignore`**

```gitignore
.gradle/
build/
*.class
.idea/
*.iml
.DS_Store
```

- [ ] **Step 6: Generate the Gradle wrapper**

Run: `gradle wrapper --gradle-version 9.5.0`
Expected: creates `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`, `gradle/wrapper/gradle-wrapper.properties`. Output ends with `BUILD SUCCESSFUL`.

- [ ] **Step 7: Verify the build wires up (no sources yet)**

Run: `./gradlew build`
Expected: `BUILD SUCCESSFUL`. Both `:core` and `:football` configure with the Java 25 toolchain and no compilation errors (no sources to compile yet).

- [ ] **Step 8: Commit**

```bash
git add settings.gradle.kts gradle/ core/build.gradle.kts football/build.gradle.kts .gitignore gradlew gradlew.bat
git commit -m "build: scaffold Gradle multi-module (core + football, JDK 25)"
```

---

## Task 2: ApiToken (auth)

**Files:**
- Create: `core/src/main/java/io/github/miro93/sportmonks/core/auth/ApiToken.java`
- Test: `core/src/test/java/io/github/miro93/sportmonks/core/auth/ApiTokenTest.java`

- [ ] **Step 1: Write the failing test**

```java
package io.github.miro93.sportmonks.core.auth;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApiTokenTest {

    @Test
    void ofExposesTheValue() {
        ApiToken token = ApiToken.of("secret-123");
        assertThat(token.value()).isEqualTo("secret-123");
    }

    @Test
    void ofRejectsBlankToken() {
        assertThatThrownBy(() -> ApiToken.of("  "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void toStringDoesNotLeakTheValue() {
        ApiToken token = ApiToken.of("super-secret");
        assertThat(token.toString()).doesNotContain("super-secret");
    }

    @Test
    void fromLookupReadsNamedVariable() {
        ApiToken token = ApiToken.from("SPORTMONKS_API_TOKEN", name -> "env-token");
        assertThat(token.value()).isEqualTo("env-token");
    }

    @Test
    void fromLookupFailsWhenMissing() {
        assertThatThrownBy(() -> ApiToken.from("SPORTMONKS_API_TOKEN", name -> null))
                .isInstanceOf(IllegalStateException.class);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :core:test --tests '*ApiTokenTest'`
Expected: FAIL — `ApiToken` does not exist (compilation error).

- [ ] **Step 3: Write minimal implementation**

```java
package io.github.miro93.sportmonks.core.auth;

import java.util.function.UnaryOperator;

/// Holds a SportMonks API token. Sent on requests as the {@code Authorization} header value.
public final class ApiToken {

    private final String value;

    private ApiToken(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("API token must not be blank");
        }
        this.value = value;
    }

    public static ApiToken of(String value) {
        return new ApiToken(value);
    }

    /// Reads {@code SPORTMONKS_API_TOKEN} from the process environment.
    public static ApiToken fromEnv() {
        return from("SPORTMONKS_API_TOKEN", System::getenv);
    }

    /// Package-visible seam for testing without touching the real environment.
    static ApiToken from(String variableName, UnaryOperator<String> lookup) {
        String resolved = lookup.apply(variableName);
        if (resolved == null || resolved.isBlank()) {
            throw new IllegalStateException("Environment variable " + variableName + " is not set");
        }
        return of(resolved);
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return "ApiToken{****}";
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :core:test --tests '*ApiTokenTest'`
Expected: PASS (5 tests).

- [ ] **Step 5: Commit**

```bash
git add core/src/main/java/io/github/miro93/sportmonks/core/auth/ApiToken.java core/src/test/java/io/github/miro93/sportmonks/core/auth/ApiTokenTest.java
git commit -m "feat(core): add ApiToken with env lookup and redacted toString"
```

---

## Task 3: HTTP transport (RawResponse + HttpTransport + JdkHttpTransport)

**Files:**
- Create: `core/src/main/java/io/github/miro93/sportmonks/core/http/RawResponse.java`
- Create: `core/src/main/java/io/github/miro93/sportmonks/core/http/HttpTransport.java`
- Create: `core/src/main/java/io/github/miro93/sportmonks/core/http/JdkHttpTransport.java`
- Depends on: `TransportException` (created here as a minimal standalone first, then folded into the sealed hierarchy in Task 4)
- Test: `core/src/test/java/io/github/miro93/sportmonks/core/http/JdkHttpTransportTest.java`

> **Execution-order dependency:** this task's code references `io.github.miro93.sportmonks.core.error.TransportException`, which is created in **Task 4**. The sealed `SportmonksException` base lists all six subclasses in its `permits` clause, so the error hierarchy cannot compile in pieces — it must be created as one atomic task. **Therefore implement Task 4 before Task 3.** (Subagent-driven execution: dispatch Task 4, then Task 3. Inline execution: do Task 4's steps first, then return here.)

- [ ] **Step 1: Write the failing test**

```java
package io.github.miro93.sportmonks.core.http;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.error.TransportException;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest
class JdkHttpTransportTest {

    private JdkHttpTransport transport() {
        return new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
    }

    @Test
    void getReturnsStatusBodyAndHeaders(WireMockRuntimeInfo wm) {
        stubFor(get("/ping").willReturn(aResponse()
                .withStatus(200)
                .withHeader("X-Test", "yes")
                .withBody("pong")));

        RawResponse response = transport().get(
                URI.create(wm.getHttpBaseUrl() + "/ping"), Map.of());

        assertThat(response.status()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("pong");
        assertThat(response.header("x-test")).contains("yes");
    }

    @Test
    void getSendsRequestHeaders(WireMockRuntimeInfo wm) {
        stubFor(get("/secured").willReturn(ok()));

        transport().get(URI.create(wm.getHttpBaseUrl() + "/secured"),
                Map.of("Authorization", "tok-42"));

        verify(getRequestedFor(urlEqualTo("/secured"))
                .withHeader("Authorization", equalTo("tok-42")));
    }

    @Test
    void getWrapsConnectionFailureAsTransportException() {
        // Nothing listening on this port.
        assertThatThrownBy(() -> transport().get(
                URI.create("http://localhost:1/nope"), Map.of()))
                .isInstanceOf(TransportException.class);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :core:test --tests '*JdkHttpTransportTest'`
Expected: FAIL — `RawResponse`, `HttpTransport`, `JdkHttpTransport`, `TransportException` do not exist.

- [ ] **Step 3: Create `RawResponse`**

```java
package io.github.miro93.sportmonks.core.http;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/// Raw, undecoded HTTP response. Header lookup is case-insensitive.
public record RawResponse(int status, String body, Map<String, List<String>> headers) {

    public Optional<String> header(String name) {
        return headers.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(name))
                .flatMap(entry -> entry.getValue().stream())
                .findFirst();
    }

    public boolean isSuccessful() {
        return status >= 200 && status < 300;
    }
}
```

- [ ] **Step 4: Create `HttpTransport`**

```java
package io.github.miro93.sportmonks.core.http;

import java.net.URI;
import java.util.Map;

/// Performs raw HTTP GETs. Implementations throw
/// {@link io.github.miro93.sportmonks.core.error.TransportException} on I/O failure.
public interface HttpTransport {

    RawResponse get(URI uri, Map<String, String> headers);
}
```

- [ ] **Step 5: Create `JdkHttpTransport`** (also requires `TransportException` from Task 4 Step 3)

```java
package io.github.miro93.sportmonks.core.http;

import io.github.miro93.sportmonks.core.error.TransportException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/// {@link HttpTransport} backed by the JDK {@link HttpClient}.
public final class JdkHttpTransport implements HttpTransport {

    private final HttpClient client;
    private final Duration requestTimeout;

    public JdkHttpTransport(HttpClient client, Duration requestTimeout) {
        this.client = Objects.requireNonNull(client, "client");
        this.requestTimeout = Objects.requireNonNull(requestTimeout, "requestTimeout");
    }

    @Override
    public RawResponse get(URI uri, Map<String, String> headers) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri).GET().timeout(requestTimeout);
        headers.forEach(builder::header);
        HttpRequest request = builder.build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return new RawResponse(response.statusCode(), response.body(), response.headers().map());
        } catch (IOException e) {
            throw new TransportException("HTTP request failed: " + uri, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TransportException("HTTP request interrupted: " + uri, e);
        }
    }
}
```

- [ ] **Step 6: Run test to verify it passes**

Run: `./gradlew :core:test --tests '*JdkHttpTransportTest'`
Expected: PASS (3 tests). (Requires Task 4's `TransportException` to exist.)

- [ ] **Step 7: Commit**

```bash
git add core/src/main/java/io/github/miro93/sportmonks/core/http/ core/src/test/java/io/github/miro93/sportmonks/core/http/
git commit -m "feat(core): add HTTP transport over java.net.http.HttpClient"
```

---

## Task 4: Sealed error hierarchy + ErrorMapper

**Files:**
- Create: `core/src/main/java/io/github/miro93/sportmonks/core/error/SportmonksException.java`
- Create: `core/src/main/java/io/github/miro93/sportmonks/core/error/AuthenticationException.java`
- Create: `core/src/main/java/io/github/miro93/sportmonks/core/error/NotFoundException.java`
- Create: `core/src/main/java/io/github/miro93/sportmonks/core/error/RateLimitException.java`
- Create: `core/src/main/java/io/github/miro93/sportmonks/core/error/ValidationException.java`
- Create: `core/src/main/java/io/github/miro93/sportmonks/core/error/ServerException.java`
- Create: `core/src/main/java/io/github/miro93/sportmonks/core/error/TransportException.java`
- Create: `core/src/main/java/io/github/miro93/sportmonks/core/error/ErrorMapper.java`
- Test: `core/src/test/java/io/github/miro93/sportmonks/core/error/ErrorMapperTest.java`

- [ ] **Step 1: Write the failing test**

```java
package io.github.miro93.sportmonks.core.error;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorMapperTest {

    @Test
    void maps401ToAuthentication() {
        SportmonksException ex = ErrorMapper.fromResponse(401, "unauthorized", Optional.empty());
        assertThat(ex).isInstanceOf(AuthenticationException.class);
        assertThat(ex.statusCode()).isEqualTo(401);
    }

    @Test
    void maps404ToNotFound() {
        assertThat(ErrorMapper.fromResponse(404, "missing", Optional.empty()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void maps429ToRateLimitWithRetryAfter() {
        SportmonksException ex = ErrorMapper.fromResponse(429, "slow down",
                Optional.of(Duration.ofSeconds(30)));
        assertThat(ex).isInstanceOf(RateLimitException.class);
        assertThat(((RateLimitException) ex).retryAfter()).contains(Duration.ofSeconds(30));
    }

    @Test
    void maps422ToValidation() {
        assertThat(ErrorMapper.fromResponse(422, "bad", Optional.empty()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void maps503ToServer() {
        assertThat(ErrorMapper.fromResponse(503, "down", Optional.empty()))
                .isInstanceOf(ServerException.class);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :core:test --tests '*ErrorMapperTest'`
Expected: FAIL — error types do not exist.

- [ ] **Step 3: Create the sealed base and six subclasses**

`SportmonksException.java`:
```java
package io.github.miro93.sportmonks.core.error;

/// Base type for every SportMonks client error. {@code statusCode} is the HTTP
/// status that produced the error, or {@code -1} for transport/I-O failures.
public sealed abstract class SportmonksException extends RuntimeException
        permits AuthenticationException, NotFoundException, RateLimitException,
        ValidationException, ServerException, TransportException {

    private final int statusCode;

    protected SportmonksException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    protected SportmonksException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int statusCode() {
        return statusCode;
    }
}
```

`AuthenticationException.java`:
```java
package io.github.miro93.sportmonks.core.error;

public final class AuthenticationException extends SportmonksException {
    public AuthenticationException(String message, int statusCode) {
        super(message, statusCode);
    }
}
```

`NotFoundException.java`:
```java
package io.github.miro93.sportmonks.core.error;

public final class NotFoundException extends SportmonksException {
    public NotFoundException(String message, int statusCode) {
        super(message, statusCode);
    }
}
```

`RateLimitException.java`:
```java
package io.github.miro93.sportmonks.core.error;

import java.time.Duration;
import java.util.Optional;

public final class RateLimitException extends SportmonksException {

    private final Duration retryAfter;

    public RateLimitException(String message, int statusCode, Duration retryAfter) {
        super(message, statusCode);
        this.retryAfter = retryAfter;
    }

    public Optional<Duration> retryAfter() {
        return Optional.ofNullable(retryAfter);
    }
}
```

`ValidationException.java`:
```java
package io.github.miro93.sportmonks.core.error;

public final class ValidationException extends SportmonksException {
    public ValidationException(String message, int statusCode) {
        super(message, statusCode);
    }
}
```

`ServerException.java`:
```java
package io.github.miro93.sportmonks.core.error;

public final class ServerException extends SportmonksException {
    public ServerException(String message, int statusCode) {
        super(message, statusCode);
    }
}
```

`TransportException.java`:
```java
package io.github.miro93.sportmonks.core.error;

public final class TransportException extends SportmonksException {
    public TransportException(String message, Throwable cause) {
        super(message, -1, cause);
    }
}
```

- [ ] **Step 4: Create `ErrorMapper`**

```java
package io.github.miro93.sportmonks.core.error;

import java.time.Duration;
import java.util.Optional;

/// Maps a non-2xx HTTP status + body into the matching {@link SportmonksException}.
public final class ErrorMapper {

    private ErrorMapper() {
    }

    public static SportmonksException fromResponse(int status, String body, Optional<Duration> retryAfter) {
        String message = "SportMonks API error " + status + ": " + snippet(body);
        return switch (status) {
            case 401, 403 -> new AuthenticationException(message, status);
            case 404 -> new NotFoundException(message, status);
            case 429 -> new RateLimitException(message, status, retryAfter.orElse(null));
            default -> {
                if (status >= 500) {
                    yield new ServerException(message, status);
                }
                yield new ValidationException(message, status);
            }
        };
    }

    private static String snippet(String body) {
        if (body == null) {
            return "";
        }
        return body.length() <= 500 ? body : body.substring(0, 500) + "…";
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `./gradlew :core:test --tests '*ErrorMapperTest'`
Expected: PASS (5 tests).

- [ ] **Step 6: Commit**

```bash
git add core/src/main/java/io/github/miro93/sportmonks/core/error/ core/src/test/java/io/github/miro93/sportmonks/core/error/
git commit -m "feat(core): add sealed SportmonksException hierarchy and ErrorMapper"
```

---

## Task 5: Response records + JacksonCodec

**Files:**
- Create: `core/src/main/java/io/github/miro93/sportmonks/core/response/Pagination.java`
- Create: `core/src/main/java/io/github/miro93/sportmonks/core/response/RateLimit.java`
- Create: `core/src/main/java/io/github/miro93/sportmonks/core/response/ApiResponse.java`
- Create: `core/src/main/java/io/github/miro93/sportmonks/core/json/CodecException.java`
- Create: `core/src/main/java/io/github/miro93/sportmonks/core/json/JacksonCodec.java`
- Test: `core/src/test/java/io/github/miro93/sportmonks/core/json/JacksonCodecTest.java`

- [ ] **Step 1: Write the failing test**

```java
package io.github.miro93.sportmonks.core.json;

import com.fasterxml.jackson.databind.JavaType;
import io.github.miro93.sportmonks.core.response.ApiResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JacksonCodecTest {

    record Team(long id, String name) {
    }

    private final JacksonCodec codec = new JacksonCodec();

    @Test
    void decodesSingleResourceWithEnvelope() {
        String json = """
                {
                  "data": { "id": 1, "name": "Ajax" },
                  "rate_limit": { "resets_in_seconds": 3600, "remaining": 2999, "requested_entity": "Team" },
                  "timezone": "UTC"
                }
                """;

        ApiResponse<Team> response = codec.decode(json, codec.type(Team.class));

        assertThat(response.data().name()).isEqualTo("Ajax");
        assertThat(response.rateLimit().remaining()).isEqualTo(2999);
        assertThat(response.rateLimit().resetsInSeconds()).isEqualTo(3600);
        assertThat(response.timezone()).isEqualTo("UTC");
    }

    @Test
    void decodesCollectionWithPagination() {
        String json = """
                {
                  "data": [ { "id": 1, "name": "Ajax" }, { "id": 2, "name": "PSV" } ],
                  "pagination": { "count": 2, "per_page": 25, "current_page": 1,
                                  "next_page": "https://api/next", "has_more": true }
                }
                """;

        ApiResponse<List<Team>> response = codec.decode(json, codec.listType(Team.class));

        assertThat(response.data()).hasSize(2);
        assertThat(response.pagination().hasMore()).isTrue();
        assertThat(response.pagination().currentPage()).isEqualTo(1);
    }

    @Test
    void ignoresUnknownFields() {
        String json = """
                { "data": { "id": 1, "name": "Ajax", "founded": 1900 }, "extra": "ignored" }
                """;

        ApiResponse<Team> response = codec.decode(json, codec.type(Team.class));

        assertThat(response.data().id()).isEqualTo(1);
    }

    @Test
    void throwsCodecExceptionOnMalformedJson() {
        assertThatThrownBy(() -> codec.decode("{ not json", codec.type(Team.class)))
                .isInstanceOf(CodecException.class);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :core:test --tests '*JacksonCodecTest'`
Expected: FAIL — response records and codec do not exist.

- [ ] **Step 3: Create the response records**

`Pagination.java`:
```java
package io.github.miro93.sportmonks.core.response;

/// SportMonks pagination block. Field names map to snake_case JSON via the codec.
public record Pagination(int count, int perPage, int currentPage, String nextPage, boolean hasMore) {
}
```

`RateLimit.java`:
```java
package io.github.miro93.sportmonks.core.response;

/// SportMonks {@code rate_limit} block returned on every successful response.
public record RateLimit(int remaining, int resetsInSeconds, String requestedEntity) {
}
```

`ApiResponse.java`:
```java
package io.github.miro93.sportmonks.core.response;

import java.util.Optional;

/// Typed wrapper around the SportMonks response envelope.
/// {@code pagination} is present only for collection endpoints; {@code rateLimit}
/// is present on successful calls.
public record ApiResponse<T>(T data, Pagination pagination, RateLimit rateLimit, String timezone) {

    public Optional<Pagination> paginationOpt() {
        return Optional.ofNullable(pagination);
    }

    public Optional<RateLimit> rateLimitOpt() {
        return Optional.ofNullable(rateLimit);
    }
}
```

- [ ] **Step 4: Create `CodecException`**

```java
package io.github.miro93.sportmonks.core.json;

/// Thrown when a response body cannot be parsed into the expected shape.
/// Internal to the codec layer; {@code ApiExecutor} converts it into a
/// {@code ServerException} carrying the real HTTP status.
public final class CodecException extends RuntimeException {
    public CodecException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

- [ ] **Step 5: Create `JacksonCodec`**

```java
package io.github.miro93.sportmonks.core.json;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.module.blackbird.BlackbirdModule;
import io.github.miro93.sportmonks.core.response.ApiResponse;

import java.util.List;

/// Wraps a snake_case-aware Jackson {@link JsonMapper} (with the Blackbird module)
/// and decodes the SportMonks envelope into a typed {@link ApiResponse}.
public final class JacksonCodec {

    private final JsonMapper mapper;

    public JacksonCodec() {
        this.mapper = JsonMapper.builder()
                .addModule(new BlackbirdModule())
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .build();
    }

    /// Build the {@link JavaType} for a single-resource {@code data} payload.
    public JavaType type(Class<?> dataClass) {
        return mapper.getTypeFactory().constructType(dataClass);
    }

    /// Build the {@link JavaType} for a {@code List<dataClass>} {@code data} payload.
    public JavaType listType(Class<?> dataClass) {
        return mapper.getTypeFactory().constructCollectionType(List.class, dataClass);
    }

    public <T> ApiResponse<T> decode(String json, JavaType dataType) {
        JavaType responseType = mapper.getTypeFactory()
                .constructParametricType(ApiResponse.class, dataType);
        try {
            return mapper.readValue(json, responseType);
        } catch (JacksonException e) {
            throw new CodecException("Failed to decode SportMonks response", e);
        }
    }
}
```

- [ ] **Step 6: Run test to verify it passes**

Run: `./gradlew :core:test --tests '*JacksonCodecTest'`
Expected: PASS (4 tests).

- [ ] **Step 7: Commit**

```bash
git add core/src/main/java/io/github/miro93/sportmonks/core/response/ core/src/main/java/io/github/miro93/sportmonks/core/json/ core/src/test/java/io/github/miro93/sportmonks/core/json/
git commit -m "feat(core): add response records and Jackson codec"
```

---

## Task 6: RequestSpec + builder + UrlBuilder

**Files:**
- Create: `core/src/main/java/io/github/miro93/sportmonks/core/request/RequestSpec.java`
- Create: `core/src/main/java/io/github/miro93/sportmonks/core/request/UrlBuilder.java`
- Test: `core/src/test/java/io/github/miro93/sportmonks/core/request/RequestSpecTest.java`
- Test: `core/src/test/java/io/github/miro93/sportmonks/core/request/UrlBuilderTest.java`

- [ ] **Step 1: Write the failing tests**

`RequestSpecTest.java`:
```java
package io.github.miro93.sportmonks.core.request;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RequestSpecTest {

    @Test
    void builderAccumulatesIncludesAndFilters() {
        RequestSpec spec = RequestSpec.builder("fixtures/18535517")
                .include("participants", "events.player")
                .include("scores")
                .filter("eventTypes", "15", "16")
                .select("name", "starting_at")
                .sort("starting_at")
                .page(2)
                .build();

        assertThat(spec.path()).isEqualTo("fixtures/18535517");
        assertThat(spec.includes()).containsExactly("participants", "events.player", "scores");
        assertThat(spec.filters()).containsEntry("eventTypes", java.util.List.of("15", "16"));
        assertThat(spec.select()).containsExactly("name", "starting_at");
        assertThat(spec.sort()).containsExactly("starting_at");
        assertThat(spec.page()).isEqualTo(2);
    }

    @Test
    void builderDefaultsAreEmpty() {
        RequestSpec spec = RequestSpec.builder("leagues").build();

        assertThat(spec.includes()).isEmpty();
        assertThat(spec.filters()).isEmpty();
        assertThat(spec.select()).isEmpty();
        assertThat(spec.sort()).isEmpty();
        assertThat(spec.page()).isNull();
    }
}
```

`UrlBuilderTest.java`:
```java
package io.github.miro93.sportmonks.core.request;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class UrlBuilderTest {

    private static final String BASE = "https://api.sportmonks.com/v3/football";

    @Test
    void buildsPathWithNoParams() {
        URI uri = UrlBuilder.build(BASE, RequestSpec.builder("leagues").build());
        assertThat(uri.toString()).isEqualTo(BASE + "/leagues");
    }

    @Test
    void joinsIncludesWithSemicolonAndKeepsNestingDot() {
        URI uri = UrlBuilder.build(BASE, RequestSpec.builder("fixtures/1")
                .include("participants", "events.player")
                .build());
        assertThat(uri.toString()).isEqualTo(BASE + "/fixtures/1?include=participants;events.player");
    }

    @Test
    void encodesFiltersSelectSortAndPage() {
        URI uri = UrlBuilder.build(BASE, RequestSpec.builder("fixtures/1")
                .filter("eventTypes", "15", "16")
                .select("name", "starting_at")
                .sort("starting_at")
                .page(3)
                .build());

        String s = uri.toString();
        assertThat(s).contains("filters=eventTypes:15,16");
        assertThat(s).contains("select=name,starting_at");
        assertThat(s).contains("sort=starting_at");
        assertThat(s).contains("page=3");
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :core:test --tests '*RequestSpecTest' --tests '*UrlBuilderTest'`
Expected: FAIL — `RequestSpec`, `UrlBuilder` do not exist.

- [ ] **Step 3: Create `RequestSpec`**

```java
package io.github.miro93.sportmonks.core.request;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/// Immutable description of a SportMonks request: a relative path plus the
/// optional include/filter/select/sort/page query options. Built via {@link #builder}.
public record RequestSpec(
        String path,
        List<String> includes,
        Map<String, List<String>> filters,
        List<String> select,
        List<String> sort,
        Integer page) {

    public RequestSpec {
        includes = List.copyOf(includes);
        filters = Map.copyOf(filters);
        select = List.copyOf(select);
        sort = List.copyOf(sort);
    }

    public static Builder builder(String path) {
        return new Builder(path);
    }

    public static final class Builder {
        private final String path;
        private final List<String> includes = new ArrayList<>();
        private final Map<String, List<String>> filters = new LinkedHashMap<>();
        private final List<String> select = new ArrayList<>();
        private final List<String> sort = new ArrayList<>();
        private Integer page;

        private Builder(String path) {
            this.path = path;
        }

        public Builder include(String... values) {
            includes.addAll(List.of(values));
            return this;
        }

        public Builder filter(String name, String... values) {
            filters.put(name, List.of(values));
            return this;
        }

        public Builder select(String... fields) {
            select.addAll(List.of(fields));
            return this;
        }

        public Builder sort(String... fields) {
            sort.addAll(List.of(fields));
            return this;
        }

        public Builder page(int page) {
            this.page = page;
            return this;
        }

        public RequestSpec build() {
            return new RequestSpec(path, includes, filters, select, sort, page);
        }
    }
}
```

- [ ] **Step 4: Create `UrlBuilder`**

```java
package io.github.miro93.sportmonks.core.request;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/// Turns a base URL + {@link RequestSpec} into a SportMonks-style {@link URI}.
/// Structural separators (`;` `,` `:`) are kept literal; only the atomic values
/// are percent-encoded.
public final class UrlBuilder {

    private UrlBuilder() {
    }

    public static URI build(String baseUrl, RequestSpec spec) {
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        StringBuilder url = new StringBuilder(base).append('/').append(spec.path());

        List<String> params = new ArrayList<>();

        if (!spec.includes().isEmpty()) {
            params.add("include=" + spec.includes().stream()
                    .map(UrlBuilder::encode)
                    .collect(Collectors.joining(";")));
        }
        if (!spec.filters().isEmpty()) {
            String filters = spec.filters().entrySet().stream()
                    .map(e -> encode(e.getKey()) + ":" + e.getValue().stream()
                            .map(UrlBuilder::encode)
                            .collect(Collectors.joining(",")))
                    .collect(Collectors.joining(";"));
            params.add("filters=" + filters);
        }
        if (!spec.select().isEmpty()) {
            params.add("select=" + spec.select().stream()
                    .map(UrlBuilder::encode)
                    .collect(Collectors.joining(",")));
        }
        if (!spec.sort().isEmpty()) {
            params.add("sort=" + spec.sort().stream()
                    .map(UrlBuilder::encode)
                    .collect(Collectors.joining(",")));
        }
        if (spec.page() != null) {
            params.add("page=" + spec.page());
        }

        if (!params.isEmpty()) {
            url.append('?').append(String.join("&", params));
        }
        return URI.create(url.toString());
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
```

- [ ] **Step 5: Run tests to verify they pass**

Run: `./gradlew :core:test --tests '*RequestSpecTest' --tests '*UrlBuilderTest'`
Expected: PASS (5 tests total).

- [ ] **Step 6: Commit**

```bash
git add core/src/main/java/io/github/miro93/sportmonks/core/request/ core/src/test/java/io/github/miro93/sportmonks/core/request/
git commit -m "feat(core): add RequestSpec builder and SportMonks URL builder"
```

---

## Task 7: Backoff + Sleeper + RetryPolicy + RetryingTransport

**Files:**
- Create: `core/src/main/java/io/github/miro93/sportmonks/core/retry/Backoff.java`
- Create: `core/src/main/java/io/github/miro93/sportmonks/core/retry/Sleeper.java`
- Create: `core/src/main/java/io/github/miro93/sportmonks/core/retry/RetryPolicy.java`
- Create: `core/src/main/java/io/github/miro93/sportmonks/core/retry/RetryingTransport.java`
- Test: `core/src/test/java/io/github/miro93/sportmonks/core/retry/BackoffTest.java`
- Test: `core/src/test/java/io/github/miro93/sportmonks/core/retry/RetryingTransportTest.java`

- [ ] **Step 1: Write the failing tests**

`BackoffTest.java`:
```java
package io.github.miro93.sportmonks.core.retry;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class BackoffTest {

    private final Backoff backoff = Backoff.defaults(); // base 500ms, x2, max 30s

    @RepeatedTest(20)
    void firstAttemptDelayIsBetweenHalfAndFullBase() {
        Duration delay = backoff.delayFor(1);
        assertThat(delay).isBetween(Duration.ofMillis(250), Duration.ofMillis(500));
    }

    @Test
    void delayIsCappedAtMax() {
        Duration delay = backoff.delayFor(100);
        assertThat(delay).isBetween(Duration.ofMillis(15_000), Duration.ofMillis(30_000));
    }
}
```

`RetryingTransportTest.java`:
```java
package io.github.miro93.sportmonks.core.retry;

import io.github.miro93.sportmonks.core.error.TransportException;
import io.github.miro93.sportmonks.core.http.HttpTransport;
import io.github.miro93.sportmonks.core.http.RawResponse;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RetryingTransportTest {

    private final List<Duration> slept = new java.util.ArrayList<>();
    private final Sleeper recordingSleeper = slept::add;
    private final URI uri = URI.create("https://api.test/x");

    @Test
    void retriesOn503ThenReturnsSuccess() {
        Queue<RawResponse> responses = new ArrayDeque<>(List.of(
                new RawResponse(503, "down", Map.of()),
                new RawResponse(200, "ok", Map.of())));
        AtomicInteger calls = new AtomicInteger();
        HttpTransport delegate = (u, h) -> {
            calls.incrementAndGet();
            return responses.poll();
        };

        RetryingTransport transport = new RetryingTransport(delegate, RetryPolicy.defaults(), recordingSleeper);
        RawResponse response = transport.get(uri, Map.of());

        assertThat(response.status()).isEqualTo(200);
        assertThat(calls).hasValue(2);
        assertThat(slept).hasSize(1);
    }

    @Test
    void doesNotRetryOn404() {
        AtomicInteger calls = new AtomicInteger();
        HttpTransport delegate = (u, h) -> {
            calls.incrementAndGet();
            return new RawResponse(404, "nope", Map.of());
        };

        RawResponse response = new RetryingTransport(delegate, RetryPolicy.defaults(), recordingSleeper)
                .get(uri, Map.of());

        assertThat(response.status()).isEqualTo(404);
        assertThat(calls).hasValue(1);
        assertThat(slept).isEmpty();
    }

    @Test
    void honorsRetryAfterHeaderOn429() {
        Queue<RawResponse> responses = new ArrayDeque<>(List.of(
                new RawResponse(429, "slow", Map.of("Retry-After", List.of("7"))),
                new RawResponse(200, "ok", Map.of())));
        HttpTransport delegate = (u, h) -> responses.poll();

        new RetryingTransport(delegate, RetryPolicy.defaults(), recordingSleeper).get(uri, Map.of());

        assertThat(slept).containsExactly(Duration.ofSeconds(7));
    }

    @Test
    void exhaustsAttemptsAndReturnsLastFailure() {
        HttpTransport delegate = (u, h) -> new RawResponse(500, "boom", Map.of());

        RawResponse response = new RetryingTransport(delegate, RetryPolicy.defaults(), recordingSleeper)
                .get(uri, Map.of());

        assertThat(response.status()).isEqualTo(500);
        assertThat(slept).hasSize(2); // 3 attempts -> 2 sleeps
    }

    @Test
    void retriesTransportExceptionThenRethrowsAfterMaxAttempts() {
        HttpTransport delegate = (u, h) -> {
            throw new TransportException("conn refused", new RuntimeException());
        };

        assertThatThrownBy(() -> new RetryingTransport(delegate, RetryPolicy.defaults(), recordingSleeper)
                .get(uri, Map.of()))
                .isInstanceOf(TransportException.class);
        assertThat(slept).hasSize(2);
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :core:test --tests '*BackoffTest' --tests '*RetryingTransportTest'`
Expected: FAIL — retry types do not exist.

- [ ] **Step 3: Create `Sleeper`**

```java
package io.github.miro93.sportmonks.core.retry;

import java.time.Duration;

/// Abstracts thread sleeping so retry timing is testable.
@FunctionalInterface
public interface Sleeper {

    void sleep(Duration duration);

    Sleeper REAL = duration -> {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during backoff", e);
        }
    };
}
```

- [ ] **Step 4: Create `Backoff`**

```java
package io.github.miro93.sportmonks.core.retry;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

/// Exponential backoff with "equal jitter": the delay for attempt N is a random
/// value in [capped/2, capped], where capped = min(base * multiplier^(N-1), max).
public final class Backoff {

    private final Duration base;
    private final Duration max;
    private final double multiplier;

    public Backoff(Duration base, Duration max, double multiplier) {
        this.base = base;
        this.max = max;
        this.multiplier = multiplier;
    }

    public static Backoff defaults() {
        return new Backoff(Duration.ofMillis(500), Duration.ofSeconds(30), 2.0);
    }

    /// @param attempt 1-based attempt number.
    public Duration delayFor(int attempt) {
        double raw = base.toMillis() * Math.pow(multiplier, attempt - 1);
        long capped = (long) Math.min(raw, max.toMillis());
        long half = Math.max(1, capped / 2);
        long jitter = ThreadLocalRandom.current().nextLong(half + 1);
        return Duration.ofMillis(half + jitter);
    }
}
```

- [ ] **Step 5: Create `RetryPolicy`**

```java
package io.github.miro93.sportmonks.core.retry;

/// How many attempts to make and which statuses are retryable. Any 5xx and 429
/// are retried by default.
public final class RetryPolicy {

    private final int maxAttempts;
    private final Backoff backoff;

    public RetryPolicy(int maxAttempts, Backoff backoff) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }
        this.maxAttempts = maxAttempts;
        this.backoff = backoff;
    }

    public static RetryPolicy defaults() {
        return new RetryPolicy(3, Backoff.defaults());
    }

    public static RetryPolicy none() {
        return new RetryPolicy(1, Backoff.defaults());
    }

    public boolean isRetryableStatus(int status) {
        return status == 429 || status >= 500;
    }

    public int maxAttempts() {
        return maxAttempts;
    }

    public Backoff backoff() {
        return backoff;
    }
}
```

- [ ] **Step 6: Create `RetryingTransport`**

```java
package io.github.miro93.sportmonks.core.retry;

import io.github.miro93.sportmonks.core.error.TransportException;
import io.github.miro93.sportmonks.core.http.HttpTransport;
import io.github.miro93.sportmonks.core.http.RawResponse;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/// Decorates an {@link HttpTransport} with retry on 429/5xx and on
/// {@link TransportException}, honoring a {@code Retry-After} header when present.
public final class RetryingTransport implements HttpTransport {

    private final HttpTransport delegate;
    private final RetryPolicy policy;
    private final Sleeper sleeper;

    public RetryingTransport(HttpTransport delegate, RetryPolicy policy, Sleeper sleeper) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.policy = Objects.requireNonNull(policy, "policy");
        this.sleeper = Objects.requireNonNull(sleeper, "sleeper");
    }

    @Override
    public RawResponse get(URI uri, Map<String, String> headers) {
        int attempt = 1;
        while (true) {
            RawResponse response;
            try {
                response = delegate.get(uri, headers);
            } catch (TransportException e) {
                if (attempt >= policy.maxAttempts()) {
                    throw e;
                }
                sleeper.sleep(policy.backoff().delayFor(attempt));
                attempt++;
                continue;
            }

            if (policy.isRetryableStatus(response.status()) && attempt < policy.maxAttempts()) {
                sleeper.sleep(delayFor(response, attempt));
                attempt++;
                continue;
            }
            return response;
        }
    }

    private Duration delayFor(RawResponse response, int attempt) {
        return response.header("Retry-After")
                .map(RetryingTransport::parseSeconds)
                .orElseGet(() -> policy.backoff().delayFor(attempt));
    }

    private static Duration parseSeconds(String value) {
        try {
            return Duration.ofSeconds(Long.parseLong(value.trim()));
        } catch (NumberFormatException e) {
            return Duration.ZERO;
        }
    }
}
```

- [ ] **Step 7: Run tests to verify they pass**

Run: `./gradlew :core:test --tests '*BackoffTest' --tests '*RetryingTransportTest'`
Expected: PASS (BackoffTest: 21 executions; RetryingTransportTest: 5 tests).

- [ ] **Step 8: Commit**

```bash
git add core/src/main/java/io/github/miro93/sportmonks/core/retry/ core/src/test/java/io/github/miro93/sportmonks/core/retry/
git commit -m "feat(core): add backoff + retrying transport decorator"
```

---

## Task 8: Pagination (Pages.stream)

**Files:**
- Create: `core/src/main/java/io/github/miro93/sportmonks/core/paging/Pages.java`
- Test: `core/src/test/java/io/github/miro93/sportmonks/core/paging/PagesTest.java`

- [ ] **Step 1: Write the failing test**

```java
package io.github.miro93.sportmonks.core.paging;

import io.github.miro93.sportmonks.core.response.ApiResponse;
import io.github.miro93.sportmonks.core.response.Pagination;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class PagesTest {

    private static ApiResponse<List<Integer>> page(List<Integer> data, int current, boolean hasMore) {
        return new ApiResponse<>(data,
                new Pagination(data.size(), 2, current, hasMore ? "next" : null, hasMore),
                null, null);
    }

    @Test
    void streamsAcrossAllPagesInOrder() {
        Stream<Integer> stream = Pages.stream(pageNumber -> switch (pageNumber) {
            case 1 -> page(List.of(1, 2), 1, true);
            case 2 -> page(List.of(3, 4), 2, true);
            case 3 -> page(List.of(5), 3, false);
            default -> throw new IllegalStateException("unexpected page " + pageNumber);
        });

        assertThat(stream).containsExactly(1, 2, 3, 4, 5);
    }

    @Test
    void fetchesLazilyOnlyAsConsumed() {
        AtomicInteger fetched = new AtomicInteger();
        Stream<Integer> stream = Pages.stream(pageNumber -> {
            fetched.incrementAndGet();
            return page(List.of(pageNumber * 10), pageNumber, true); // infinite source
        });

        List<Integer> firstThree = stream.limit(3).toList();

        assertThat(firstThree).containsExactly(10, 20, 30);
        assertThat(fetched.get()).isEqualTo(3); // only 3 pages pulled
    }

    @Test
    void handlesSinglePageWithNoMore() {
        Stream<Integer> stream = Pages.stream(pageNumber -> page(List.of(7, 8), 1, false));
        assertThat(stream).containsExactly(7, 8);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :core:test --tests '*PagesTest'`
Expected: FAIL — `Pages` does not exist.

- [ ] **Step 3: Create `Pages`**

```java
package io.github.miro93.sportmonks.core.paging;

import io.github.miro93.sportmonks.core.response.ApiResponse;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.IntFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/// Lazily walks a paginated collection endpoint, following
/// {@link io.github.miro93.sportmonks.core.response.Pagination#hasMore()}.
public final class Pages {

    private Pages() {
    }

    /// @param fetchPage 1-based page number -> the response for that page.
    public static <T> Stream<T> stream(IntFunction<ApiResponse<List<T>>> fetchPage) {
        Iterator<T> iterator = new PageIterator<>(fetchPage);
        Spliterator<T> spliterator = Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED);
        return StreamSupport.stream(spliterator, false);
    }

    private static final class PageIterator<T> implements Iterator<T> {
        private final IntFunction<ApiResponse<List<T>>> fetchPage;
        private int nextPage = 1;
        private boolean exhausted = false;
        private Iterator<T> current = Collections.emptyIterator();

        private PageIterator(IntFunction<ApiResponse<List<T>>> fetchPage) {
            this.fetchPage = fetchPage;
        }

        @Override
        public boolean hasNext() {
            while (!current.hasNext() && !exhausted) {
                ApiResponse<List<T>> response = fetchPage.apply(nextPage);
                List<T> data = response.data() == null ? List.of() : response.data();
                current = data.iterator();
                boolean hasMore = response.pagination() != null && response.pagination().hasMore();
                if (hasMore) {
                    nextPage++;
                } else {
                    exhausted = true;
                }
            }
            return current.hasNext();
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return current.next();
        }
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :core:test --tests '*PagesTest'`
Expected: PASS (3 tests).

- [ ] **Step 5: Commit**

```bash
git add core/src/main/java/io/github/miro93/sportmonks/core/paging/ core/src/test/java/io/github/miro93/sportmonks/core/paging/
git commit -m "feat(core): add lazy pagination stream"
```

---

## Task 9: ApiExecutor (integration seam for sport modules)

**Files:**
- Create: `core/src/main/java/io/github/miro93/sportmonks/core/ApiExecutor.java`
- Test: `core/src/test/java/io/github/miro93/sportmonks/core/ApiExecutorTest.java`

This is the single entry point football endpoints will call: build URL → inject auth header → run (retrying) transport → map non-2xx to a typed exception → decode into `ApiResponse<T>`. Provides both sync and async (virtual-thread) variants.

- [ ] **Step 1: Write the failing test**

```java
package io.github.miro93.sportmonks.core;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.error.NotFoundException;
import io.github.miro93.sportmonks.core.error.ServerException;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.core.response.ApiResponse;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest
class ApiExecutorTest {

    record Team(long id, String name) {
    }

    private final JacksonCodec codec = new JacksonCodec();

    private ApiExecutor executor(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        return new ApiExecutor(transport, codec, ApiToken.of("tok-99"), baseUrl);
    }

    @Test
    void executesAndDecodesSingleResource(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/teams/1")).willReturn(okJson("""
                { "data": { "id": 1, "name": "Ajax" } }
                """)));

        ApiResponse<Team> response = executor(wm.getHttpBaseUrl())
                .execute(RequestSpec.builder("teams/1").build(), codec.type(Team.class));

        assertThat(response.data().name()).isEqualTo("Ajax");
        verify(getRequestedFor(urlPathEqualTo("/teams/1"))
                .withHeader("Authorization", equalTo("tok-99")));
    }

    @Test
    void mapsNotFoundToTypedException(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/teams/999")).willReturn(aResponse().withStatus(404).withBody("missing")));

        assertThatThrownBy(() -> executor(wm.getHttpBaseUrl())
                .execute(RequestSpec.builder("teams/999").build(), codec.type(Team.class)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void wrapsUnparseableSuccessBodyAsServerException(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/teams/1")).willReturn(aResponse().withStatus(200).withBody("{ not json")));

        assertThatThrownBy(() -> executor(wm.getHttpBaseUrl())
                .execute(RequestSpec.builder("teams/1").build(), codec.type(Team.class)))
                .isInstanceOf(ServerException.class);
    }

    @Test
    void executeAsyncReturnsDecodedResponse(WireMockRuntimeInfo wm) throws Exception {
        stubFor(get(urlPathEqualTo("/teams")).willReturn(okJson("""
                { "data": [ { "id": 1, "name": "Ajax" } ] }
                """)));

        ApiResponse<List<Team>> response = executor(wm.getHttpBaseUrl())
                .executeAsync(RequestSpec.builder("teams").build(), codec.listType(Team.class))
                .get();

        assertThat(response.data()).hasSize(1);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :core:test --tests '*ApiExecutorTest'`
Expected: FAIL — `ApiExecutor` does not exist.

- [ ] **Step 3: Create `ApiExecutor`**

```java
package io.github.miro93.sportmonks.core;

import com.fasterxml.jackson.databind.JavaType;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.error.ErrorMapper;
import io.github.miro93.sportmonks.core.error.ServerException;
import io.github.miro93.sportmonks.core.http.HttpTransport;
import io.github.miro93.sportmonks.core.http.RawResponse;
import io.github.miro93.sportmonks.core.json.CodecException;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.core.request.UrlBuilder;
import io.github.miro93.sportmonks.core.response.ApiResponse;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/// Ties transport + auth + codec + error mapping together. Sport modules call this.
/// Async variants run the blocking pipeline on a virtual-thread executor (Java 25).
public final class ApiExecutor {

    private final HttpTransport transport;
    private final JacksonCodec codec;
    private final ApiToken token;
    private final String baseUrl;
    private final Executor asyncExecutor;

    public ApiExecutor(HttpTransport transport, JacksonCodec codec, ApiToken token, String baseUrl) {
        this(transport, codec, token, baseUrl,
                Executors.newVirtualThreadPerTaskExecutor());
    }

    public ApiExecutor(HttpTransport transport, JacksonCodec codec, ApiToken token,
                       String baseUrl, Executor asyncExecutor) {
        this.transport = Objects.requireNonNull(transport, "transport");
        this.codec = Objects.requireNonNull(codec, "codec");
        this.token = Objects.requireNonNull(token, "token");
        this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl");
        this.asyncExecutor = Objects.requireNonNull(asyncExecutor, "asyncExecutor");
    }

    public <T> ApiResponse<T> execute(RequestSpec spec, JavaType dataType) {
        RawResponse response = transport.get(
                UrlBuilder.build(baseUrl, spec),
                Map.of("Authorization", token.value(), "Accept", "application/json"));

        if (!response.isSuccessful()) {
            throw ErrorMapper.fromResponse(response.status(), response.body(), retryAfter(response));
        }
        try {
            return codec.decode(response.body(), dataType);
        } catch (CodecException e) {
            throw new ServerException(
                    "Failed to parse successful response (status " + response.status() + ")",
                    response.status());
        }
    }

    public <T> CompletableFuture<ApiResponse<T>> executeAsync(RequestSpec spec, JavaType dataType) {
        return CompletableFuture.supplyAsync(() -> execute(spec, dataType), asyncExecutor);
    }

    private static Optional<Duration> retryAfter(RawResponse response) {
        return response.header("Retry-After").map(value -> {
            try {
                return Duration.ofSeconds(Long.parseLong(value.trim()));
            } catch (NumberFormatException e) {
                return null;
            }
        });
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :core:test --tests '*ApiExecutorTest'`
Expected: PASS (4 tests).

- [ ] **Step 5: Run the full core test suite**

Run: `./gradlew :core:test`
Expected: `BUILD SUCCESSFUL`, all tests green across every package.

- [ ] **Step 6: Commit**

```bash
git add core/src/main/java/io/github/miro93/sportmonks/core/ApiExecutor.java core/src/test/java/io/github/miro93/sportmonks/core/ApiExecutorTest.java
git commit -m "feat(core): add ApiExecutor wiring transport, auth, codec and errors"
```

---

## Final verification

- [ ] **Step 1: Full build**

Run: `./gradlew build`
Expected: `BUILD SUCCESSFUL`; `:core` compiles and all tests pass; `:football` configures (no sources yet).

- [ ] **Step 2: Confirm no skipped/failing tests**

Run: `./gradlew :core:test --rerun`
Expected: every test class reported as passed; zero failures, zero skips.

---

## Notes for the implementer

- **Library versions** in `gradle/libs.versions.toml` are pinned to known-good releases. If Gradle reports a newer patch, bumping is safe; do not change major versions without checking the Blackbird module still matches the Jackson version.
- **Why async lives in `ApiExecutor`, not the transport:** virtual threads (Java 25) make the blocking `get` cheap to run on `supplyAsync`, so retry logic stays single-path and fully tested in `RetryingTransport`. The `FootballClient` (M2) will compose `JdkHttpTransport` → `RetryingTransport` → `ApiExecutor`.
- **`RetryingTransport` is not yet wired into `ApiExecutor` here** — `ApiExecutor` takes any `HttpTransport`. M2's client builder is responsible for wrapping the JDK transport with `RetryingTransport` before handing it to `ApiExecutor`. Both are tested independently; their composition is exercised end-to-end in M2.
- **Filter syntax** (`filters=name:v1,v2;name2:v3`) follows SportMonks v3 dynamic filters. Per-entity nested filtering can be added to `RequestSpec` later without breaking the URL builder.
- **Toolchain:** JDK 25 (Temurin) is installed under `/usr/lib/jvm/java-25`, so Gradle's auto-detection finds it without the foojay resolver. If `./gradlew build` ever fails with "No compatible toolchains found", add `plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0" }` to the top of `settings.gradle.kts` to enable auto-provisioning.
```
