# Helidon JSON Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace Jackson with Helidon JSON (compile-time binding) as the only JSON codec, shipped as 3.0.0 under the unchanged artifactIds.

**Architecture:** Concrete class swap: `JacksonCodec` becomes `HelidonJsonCodec` with the identical public surface (`type`, `listType`, `decode`), backed by `io.helidon.json.binding.JsonBinding` and pre-resolved `GenericType<ApiResponse<T>>` tokens. Every decoded record gains `@Json.Entity` plus per-field `@Json.Property("snake_case")` (Helidon has no global naming strategy). Module migration order: core, then football, then native-smoke/docs.

**Tech Stack:** Java 25, Gradle (Kotlin DSL, version catalog), Helidon JSON 4.5.4 (`io.helidon.json:helidon-json-binding`, annotation processors `io.helidon.codegen:helidon-codegen-apt` + `io.helidon.json:helidon-json-codegen`), JUnit 6, AssertJ, WireMock.

**Spec:** `docs/superpowers/specs/2026-09-02-helidon-json-migration-design.md`

## Global Constraints

- Helidon version: exactly `4.5.4`. Group/artifact names of published modules unchanged (`io.github.miro93.sportmonks:sportmonks-core`, `:sportmonks-football`).
- All existing tests must pass unchanged except: codec-class renames (`JacksonCodec` → `HelidonJsonCodec`) and test-local records gaining `@Json.Entity`.
- Behavioural contract to preserve (encoded in existing tests): snake_case JSON → camelCase records; unknown JSON properties ignored; absent/null primitives decode to Java defaults (if Helidon throws instead, switch the affected record component to its wrapper type — that is the sanctioned fallback).
- Branch: `feat/helidon-json`. Final PR title must be `feat!: replace Jackson with Helidon JSON` (release-please cuts 3.0.0 from the squash commit).
- Work on this machine: run Gradle plainly (`./gradlew`); JDK 25 auto-provisions via foojay.

---

### Task 1: Migrate the `core` module (dependencies, annotations, codec, rename)

The core module cannot compile half-migrated (endpoints reference the codec class), so this task carries the whole core swap; it is still one reviewable unit with one test cycle.

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `core/build.gradle.kts`
- Modify: `core/src/main/java/module-info.java`
- Modify: `core/src/main/java/io/github/miro93/sportmonks/core/response/ApiResponse.java`, `Pagination.java`, `RateLimit.java`
- Modify: `core/src/main/java/io/github/miro93/sportmonks/core/coreapi/model/*.java` (5 records)
- Create: `core/src/main/java/io/github/miro93/sportmonks/core/json/HelidonJsonCodec.java` (delete `JacksonCodec.java`)
- Modify: `core/src/main/java/io/github/miro93/sportmonks/core/json/DataType.java`
- Modify (mechanical rename only): `ApiExecutor.java`, `coreapi/CoreClient.java`, `coreapi/endpoint/*.java`, and core test files referencing `JacksonCodec`
- Test: `core/src/test/java/io/github/miro93/sportmonks/core/json/HelidonJsonCodecTest.java` (renamed from `JacksonCodecTest.java`)

**Interfaces:**
- Consumes: nothing (first task).
- Produces: `public final class HelidonJsonCodec` with `HelidonJsonCodec()`, `<T> DataType<T> type(Class<T>)`, `<T> DataType<List<T>> listType(Class<T>)`, `<T> ApiResponse<T> decode(String, DataType<T>)`, `<T> ApiResponse<T> decode(byte[], DataType<T>)` throwing `CodecException` on bad input. `DataType<T>` now wraps `io.helidon.common.GenericType<ApiResponse<T>>` exposed package-private as `genericType()`. Task 2 relies on these exact names.

- [ ] **Step 1: Swap dependencies in the version catalog and core build**

`gradle/libs.versions.toml` — remove the `jackson` version and `jackson-databind` library; add:

```toml
[versions]
helidon = "4.5.4"
junit = "6.1.1"
junit-platform = "6.1.1"
assertj = "3.27.7"
wiremock = "3.13.2"

[libraries]
helidon-json-binding = { module = "io.helidon.json:helidon-json-binding", version.ref = "helidon" }
helidon-json-codegen = { module = "io.helidon.json:helidon-json-codegen", version.ref = "helidon" }
helidon-codegen-apt = { module = "io.helidon.codegen:helidon-codegen-apt", version.ref = "helidon" }
junit-jupiter = { module = "org.junit.jupiter:junit-jupiter", version.ref = "junit" }
junit-launcher = { module = "org.junit.platform:junit-platform-launcher", version.ref = "junit-platform" }
assertj = { module = "org.assertj:assertj-core", version.ref = "assertj" }
wiremock = { module = "org.wiremock:wiremock", version.ref = "wiremock" }
```

(keep the existing `[plugins]` block as is.)

`core/build.gradle.kts` dependencies block becomes:

```kotlin
dependencies {
    api(libs.helidon.json.binding)
    annotationProcessor(libs.helidon.codegen.apt)
    annotationProcessor(libs.helidon.json.codegen)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj)
    testImplementation(libs.wiremock)
    testAnnotationProcessor(libs.helidon.codegen.apt)
    testAnnotationProcessor(libs.helidon.json.codegen)
    testRuntimeOnly(libs.junit.launcher)
}
```

(`testAnnotationProcessor` matters: test sources define local `@Json.Entity` records.)

- [ ] **Step 2: Rename the codec test and make it the failing spec**

`git mv core/src/test/java/io/github/miro93/sportmonks/core/json/JacksonCodecTest.java core/src/test/java/io/github/miro93/sportmonks/core/json/HelidonJsonCodecTest.java`

In the file: rename class to `HelidonJsonCodecTest`, replace both `JacksonCodec` usages with `HelidonJsonCodec`, annotate the test-local record, and add the two behaviour-guard tests:

```java
import io.helidon.json.binding.Json;

@Json.Entity
record Team(long id, String name) {
}

private final HelidonJsonCodec codec = new HelidonJsonCodec();

@Test
void ignoresUnknownProperties() {
    String json = """
            { "data": { "id": 1, "name": "Ajax", "brand_new_field": 42 }, "timezone": "UTC" }
            """;
    ApiResponse<Team> response = codec.decode(json, codec.type(Team.class));
    assertThat(response.data().name()).isEqualTo("Ajax");
}

@Test
void absentPrimitiveDecodesToDefault() {
    // Pagination.hasMore (boolean) missing from JSON must decode to false,
    // matching Jackson's previous lenient FAIL_ON_NULL_FOR_PRIMITIVES=false.
    String json = """
            { "data": [], "pagination": { "count": 0, "per_page": 25, "current_page": 1 } }
            """;
    ApiResponse<List<Team>> response = codec.decode(json, codec.listType(Team.class));
    assertThat(response.pagination().hasMore()).isFalse();
}
```

Keep every existing test method (envelope, pagination, byte[], malformed-JSON → `CodecException`) — only the codec class name changes.

- [ ] **Step 3: Run the codec test to verify it fails**

Run: `./gradlew :core:test --tests 'io.github.miro93.sportmonks.core.json.HelidonJsonCodecTest'`
Expected: compilation FAILURE (`HelidonJsonCodec` does not exist). That is the red state.

- [ ] **Step 4: Annotate the core records**

Rule (applies verbatim here and in Task 2): every record decoded from JSON gets `@Json.Entity` (import `io.helidon.json.binding.Json`); every record component whose name contains an uppercase letter gets `@Json.Property("<snake_case>")` where the snake_case name is the camelCase name with each uppercase letter replaced by `_` + lowercase.

The three response records, exactly:

```java
@Json.Entity
public record ApiResponse<T>(T data,
                             Pagination pagination,
                             @Json.Property("rate_limit") RateLimit rateLimit,
                             String timezone) {
```

```java
@Json.Entity
public record Pagination(int count,
                         @Json.Property("per_page") int perPage,
                         @Json.Property("current_page") int currentPage,
                         @Json.Property("next_page") String nextPage,
                         @Json.Property("has_more") boolean hasMore) {
```

```java
@Json.Entity
public record RateLimit(int remaining,
                        @Json.Property("resets_in_seconds") int resetsInSeconds,
                        @Json.Property("requested_entity") String requestedEntity) {
```

Then the 5 records in `coreapi/model/` (`City`, `Country`, `Continent`, `Region`, `Type`): add `@Json.Entity` to each and apply the rule to their camelCase components (e.g. `City.countryId` → `@Json.Property("country_id")`). Verify nothing is missed with:

```bash
grep -rEn '^\s+[A-Za-z<>,\[\] .]+ [a-z]+[A-Z][a-zA-Z]*[,)]' core/src/main/java/io/github/miro93/sportmonks/core/coreapi/model core/src/main/java/io/github/miro93/sportmonks/core/response
```

every hit must sit next to a `@Json.Property`.

- [ ] **Step 5: Rewrite DataType and implement HelidonJsonCodec**

`DataType.java` (full content):

```java
package io.github.miro93.sportmonks.core.json;

import io.helidon.common.GenericType;
import io.github.miro93.sportmonks.core.response.ApiResponse;

/// An opaque, type-safe token describing the `data` payload type of a SportMonks
/// response. Obtain instances from {@link HelidonJsonCodec#type(Class)} or
/// {@link HelidonJsonCodec#listType(Class)} — the wrapped envelope
/// {@link GenericType} is an internal detail and not part of the public API.
///
/// The `ApiResponse<T>` envelope type is resolved once, when the token is created,
/// and reused for every decode. Instances are immutable and thread-safe; create
/// one per endpoint and reuse it across requests.
public final class DataType<T> {

    private final GenericType<ApiResponse<T>> envelopeType;

    DataType(GenericType<ApiResponse<T>> envelopeType) {
        this.envelopeType = envelopeType;
    }

    GenericType<ApiResponse<T>> genericType() {
        return envelopeType;
    }
}
```

`HelidonJsonCodec.java` (full content; `JacksonCodec.java` is deleted):

```java
package io.github.miro93.sportmonks.core.json;

import io.helidon.common.GenericType;
import io.helidon.json.JsonException;
import io.helidon.json.binding.JsonBinding;
import io.github.miro93.sportmonks.core.response.ApiResponse;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/// Wraps a Helidon JSON {@link JsonBinding} (compile-time binding, no reflection)
/// and decodes the SportMonks envelope into a typed {@link ApiResponse}.
/// snake_case mapping is declared per field via `@Json.Property` on the records.
public final class HelidonJsonCodec {

    private final JsonBinding binding;

    public HelidonJsonCodec() {
        this.binding = JsonBinding.create();
    }

    /// Build the {@link DataType} for a single-resource `data` payload.
    public <T> DataType<T> type(Class<T> dataClass) {
        return envelopeType(dataClass);
    }

    /// Build the {@link DataType} for a `List<T>` `data` payload.
    public <T> DataType<List<T>> listType(Class<T> dataClass) {
        return envelopeType(parameterized(List.class, dataClass));
    }

    /// Decode a SportMonks JSON envelope using a typed {@link DataType} token.
    public <T> ApiResponse<T> decode(String json, DataType<T> dataType) {
        try {
            return binding.deserialize(json, dataType.genericType());
        } catch (JsonException e) {
            throw new CodecException("Failed to decode SportMonks response", e);
        }
    }

    /// Decode directly from raw UTF-8 bytes (the transport's preferred path).
    public <T> ApiResponse<T> decode(byte[] json, DataType<T> dataType) {
        try {
            return binding.deserialize(json, dataType.genericType());
        } catch (JsonException e) {
            throw new CodecException("Failed to decode SportMonks response", e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> DataType<T> envelopeType(Type dataType) {
        GenericType<ApiResponse<T>> envelope =
                (GenericType<ApiResponse<T>>) GenericType.create(parameterized(ApiResponse.class, dataType));
        return new DataType<>(envelope);
    }

    private static ParameterizedType parameterized(Class<?> raw, Type argument) {
        return new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[] {argument};
            }

            @Override
            public Type getRawType() {
                return raw;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };
    }
}
```

(`JsonBindingException extends JsonException`, so one catch covers parser and binding failures. If `GenericType.create(Type)` has a different name in 4.5.4, check `io.helidon.common.GenericType` javadoc — the factory taking a `java.lang.reflect.Type` is the one to use.)

- [ ] **Step 6: Mechanical rename across the core module and module-info**

```bash
grep -rl 'JacksonCodec' core/src | xargs sed -i '' 's/JacksonCodec/HelidonJsonCodec/g'
rm core/src/main/java/io/github/miro93/sportmonks/core/json/JacksonCodec.java
```

(the sed also fixes imports since the package is unchanged; doc comments mentioning Jackson in `ApiExecutor`/`CoreClient` should be reworded to "the JSON codec" while there.)

`core/src/main/java/module-info.java`: replace the two Jackson `requires` with

```java
requires transitive io.helidon.json.binding;
requires io.helidon.common;
```

and DELETE both `opens ... to tools.jackson.databind;` lines (compile-time binding does no reflection).

- [ ] **Step 7: Run the core build**

Run: `./gradlew :core:build`
Expected: PASS, including `HelidonJsonCodecTest` (green from red in Step 3).

Contingencies, in order, if the codec tests fail with "no converter found" for an entity:
1. The annotation processor did not run — check `build/generated/sources/annotationProcessor/java/main` for generated `*__JsonConverter` classes; if absent, verify Step 1's `annotationProcessor`/`testAnnotationProcessor` lines.
2. Converters generate but are not discovered at runtime under JPMS — add `requires io.helidon.service.registry;` to `module-info.java`; if the error names a missing `provides` directive, add exactly what it names.
3. `absentPrimitiveDecodesToDefault` fails because Helidon rejects a missing/null primitive — apply the sanctioned fallback: change `Pagination.hasMore` to `Boolean` and add a `public boolean hasMore() { return hasMore != null && hasMore; }`-style accessor is NOT possible on a record component; instead keep the component `boolean` only if Helidon defaults it, otherwise make it `Boolean hasMore` and update `Pages.PageIterator.hasNext` to `Boolean.TRUE.equals(response.pagination().hasMore())`. Record the choice in the commit message.

- [ ] **Step 8: Commit**

```bash
git add -A
git commit -m "feat(core)!: replace Jackson with Helidon JSON compile-time binding

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

### Task 2: Migrate the `football` module

**Files:**
- Modify: `football/build.gradle.kts`
- Modify: `football/src/main/java/module-info.java`
- Modify: `football/src/main/java/io/github/miro93/sportmonks/football/model/*.java` (36 records)
- Modify (mechanical rename only): `football/src/main/java/io/github/miro93/sportmonks/football/FootballClient.java`, `endpoint/*.java`, and football test files referencing `JacksonCodec`
- Test: existing `football/src/test/...` suites (no new tests; they are the spec)

**Interfaces:**
- Consumes: `HelidonJsonCodec` (constructor `new HelidonJsonCodec()`, methods `type(Class<T>)`, `listType(Class<T>)`) and `DataType<T>` from Task 1.
- Produces: nothing new — same public football API, now Jackson-free.

- [ ] **Step 1: Add the annotation processor to the football build**

In `football/build.gradle.kts` dependencies block, add after `api(project(":core"))`:

```kotlin
    annotationProcessor(libs.helidon.codegen.apt)
    annotationProcessor(libs.helidon.json.codegen)
```

and next to the other test dependencies:

```kotlin
    testAnnotationProcessor(libs.helidon.codegen.apt)
    testAnnotationProcessor(libs.helidon.json.codegen)
```

- [ ] **Step 2: Run the football tests to see the red state**

Run: `./gradlew :football:test`
Expected: compilation FAILURE — `JacksonCodec` no longer exists in core. (If it somehow compiles, Task 1 was incomplete; stop and fix Task 1.)

- [ ] **Step 3: Annotate the 36 football model records**

Apply exactly the Task 1 Step 4 rule to every record in `football/src/main/java/io/github/miro93/sportmonks/football/model/`: `@Json.Entity` on the record (import `io.helidon.json.binding.Json`), `@Json.Property("<snake_case>")` on every camelCase component (`imagePath` → `"image_path"`, `hasOdds` → `"has_odds"`, `latestBookmakerUpdate` → `"latest_bookmaker_update"`, ...). Completeness check — every hit of

```bash
grep -rEn '^\s+[A-Za-z<>,\[\] .]+ [a-z]+[A-Z][a-zA-Z]*[,)]' football/src/main/java/io/github/miro93/sportmonks/football/model
```

must sit next to a `@Json.Property`. Records nested as components of other records are also in this directory and also need `@Json.Entity`.

- [ ] **Step 4: Mechanical rename and module-info**

```bash
grep -rl 'JacksonCodec' football/src | xargs sed -i '' 's/JacksonCodec/HelidonJsonCodec/g'
```

`football/src/main/java/module-info.java`: delete the line `opens io.github.miro93.sportmonks.football.model to tools.jackson.databind;`. No new `requires` needed (`io.helidon.json.binding` arrives transitively from core).

Test sources that declare their own decoded records (grep `record ` under `football/src/test`) get `@Json.Entity` the same way.

- [ ] **Step 5: Run the full build**

Run: `./gradlew build`
Expected: PASS — all core + football suites green. Any `DecodingTest` failure means a missed `@Json.Property` (the assertion message names the field).

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "feat(football)!: decode models with Helidon JSON

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
```

---

### Task 3: native-smoke, docs, and the release-ready PR

**Files:**
- Modify: `native-smoke/src/main/java/**` (only if it references Jackson — check first)
- Delete if present: any `reflect-config.json`/reachability metadata for Jackson under `core/src/main/resources/META-INF/native-image` or `football/.../META-INF/native-image`
- Modify: `README.md`, `docs/**` — every mention of Jackson
- Test: `./gradlew build` + CI's `native-smoke` job on the PR

**Interfaces:**
- Consumes: the migrated modules from Tasks 1-2.
- Produces: the final PR.

- [ ] **Step 1: Sweep the remaining Jackson references**

```bash
grep -rni "jackson" --include='*.java' --include='*.kts' --include='*.md' --include='*.json' --include='*.properties' . | grep -v build/ | grep -v docs/superpowers
```

For each hit: native-smoke code → rename like Tasks 1-2; native-image metadata for Jackson types → delete the file; README/docs prose → reword to Helidon JSON (mention compile-time binding, no reflection config needed). The spec and plan under `docs/superpowers/` keep their historical mentions.

- [ ] **Step 2: Full verification**

Run: `./gradlew clean build`
Expected: PASS with zero remaining source references to Jackson (re-run the Step 1 grep — only `docs/superpowers/` hits remain).

- [ ] **Step 3: Commit and open the PR**

```bash
git add -A
git commit -m "feat!: finish Helidon JSON migration (native-smoke, docs)

Co-Authored-By: Claude Fable 5 <noreply@anthropic.com>"
git -c credential.helper= -c credential.helper='!gh auth git-credential' push -u https://github.com/miro93/sportmonks-java-api-client.git feat/helidon-json
```

Open the PR titled exactly `feat!: replace Jackson with Helidon JSON` with a body summarising: why (compile-time binding, zero reflection, native-image friendly), the breaking changes (`JacksonCodec` → `HelidonJsonCodec`, Jackson gone from the API surface, 3.0.0), and the snake_case annotation approach. End the body with the standard generation footer. CI must be green including the `native-smoke` job before merge.
