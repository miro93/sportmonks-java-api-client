# M15 — GraalVM Native-Image Readiness Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the library native-image ready — ship Jackson reachability metadata in the jars, drop the native-hostile Blackbird module, and prove it with a native smoke test (locally and in CI).

**Architecture:** Remove `BlackbirdModule` so Jackson uses standard reflection (native-friendly). Apply the GraalVM `native-build-tools` plugin to `core`/`football` to generate reachability metadata via the tracing agent over the existing test suite, committed under `META-INF/native-image/`. Add a self-contained `native-smoke` subproject (in-process `jdk.httpserver` + `FootballClient` decode) that compiles to a native image and asserts it works. A `native.yml` CI workflow rebuilds the smoke image and checks metadata drift.

**Tech Stack:** Java 25, Gradle, GraalVM 25 (`native-image`), `org.graalvm.buildtools.native` 1.1.1, Jackson 3, JDK `HttpClient` + `com.sun.net.httpserver`.

**GraalVM note:** Tasks 3 and 4 require Gradle to run on a GraalVM 25 JDK. Select it before those tasks, e.g. `sdk use java 25-graal` (SDKMAN) or `export JAVA_HOME="$HOME/.sdkman/candidates/java/25-graal"`. Tasks 1, 2, 5, 6 work on the normal Temurin JDK. Verify which JDK is active with `java -version` (should print "GraalVM" for tasks 3–4).

---

### Task 1: Drop the Blackbird module

**Files:**
- Modify: `core/src/main/java/io/github/miro93/sportmonks/core/json/JacksonCodec.java`
- Modify: `core/src/main/java/module-info.java`
- Modify: `football/src/main/java/module-info.java`
- Modify: `gradle/libs.versions.toml`, `core/build.gradle.kts`

- [ ] **Step 1: Remove Blackbird from `JacksonCodec`**

In `JacksonCodec.java`: delete the line `import tools.jackson.module.blackbird.BlackbirdModule;` and the builder line `.addModule(new BlackbirdModule())`. Update the two `///` doc lines that mention Blackbird to drop the mention, e.g.:

```java
/// Wraps a snake_case-aware Jackson {@link JsonMapper} and decodes the SportMonks
/// envelope into a typed {@link ApiResponse}.
```
and
```java
    /// Creates a codec with a snake_case naming strategy and lenient handling of
    /// unknown properties and of `null`/absent primitives.
```

The builder becomes:
```java
        this.mapper = JsonMapper.builder()
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
                .build();
```

- [ ] **Step 2: Remove Blackbird from the module descriptors**

In `core/src/main/java/module-info.java`:
- delete `requires tools.jackson.module.blackbird;`
- change both `opens` lines to target only databind:
```java
    opens io.github.miro93.sportmonks.core.response to tools.jackson.databind;
    opens io.github.miro93.sportmonks.core.coreapi.model to tools.jackson.databind;
```

In `football/src/main/java/module-info.java`, change the opens to:
```java
    opens io.github.miro93.sportmonks.football.model to tools.jackson.databind;
```

- [ ] **Step 3: Remove the dependency**

In `gradle/libs.versions.toml`, delete the `jackson-blackbird` library line under `[libraries]`.
In `core/build.gradle.kts`, delete the line `implementation(libs.jackson.blackbird)`.

- [ ] **Step 4: Build and test (Temurin is fine)**

Run: `./gradlew clean build`
Expected: BUILD SUCCESSFUL, all tests pass, and the previous `module not found: tools.jackson.module.blackbird` warning is gone.

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "refactor: drop Jackson Blackbird module for native-image friendliness"
```

---

### Task 2: Apply the GraalVM native-build-tools plugin

**Files:**
- Modify: `gradle/libs.versions.toml`, `core/build.gradle.kts`, `football/build.gradle.kts`

- [ ] **Step 1: Add the plugin to the version catalog**

In `gradle/libs.versions.toml`, under `[plugins]`, add:
```toml
graalvm-native = { id = "org.graalvm.buildtools.native", version = "1.1.1" }
```

- [ ] **Step 2: Apply it to both modules**

In BOTH `core/build.gradle.kts` and `football/build.gradle.kts`, add to the `plugins { }` block (after the existing entries):
```kotlin
    alias(libs.plugins.graalvm.native)
```

- [ ] **Step 3: Verify the normal build still works on Temurin**

Run: `./gradlew clean build`
Expected: BUILD SUCCESSFUL. The plugin only registers native/agent tasks; it must not change the normal JVM build or add runtime dependencies. Confirm with:
`./gradlew :core:dependencies --configuration runtimeClasspath | grep -i graal` → expect NO output (no runtime GraalVM dep leaked into the published artifact).

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "build: apply GraalVM native-build-tools plugin to core and football"
```

---

### Task 3: Generate and commit reachability metadata (requires GraalVM)

**Files:**
- Create: `core/src/main/resources/META-INF/native-image/io.github.miro93.sportmonks/core/` (agent output)
- Create: `football/src/main/resources/META-INF/native-image/io.github.miro93.sportmonks/football/` (agent output)

- [ ] **Step 1: Select the GraalVM JDK**

Run: `sdk use java 25-graal` (or `export JAVA_HOME="$HOME/.sdkman/candidates/java/25-graal"`).
Verify: `java -version` prints `GraalVM`.

- [ ] **Step 2: Run the tracing agent over the test suite**

Run: `./gradlew -Pagent test`
Expected: BUILD SUCCESSFUL. The agent records reflection used while decoding every model
(the WireMock test suites cover all DTOs). Output lands under each module's
`build/native/agent-output/test/`.

- [ ] **Step 3: Copy the merged metadata into packaged resources**

```bash
./gradlew :core:metadataCopy --task test \
    --dir core/src/main/resources/META-INF/native-image/io.github.miro93.sportmonks/core
./gradlew :football:metadataCopy --task test \
    --dir football/src/main/resources/META-INF/native-image/io.github.miro93.sportmonks/football
```

- [ ] **Step 4: Sanity-check the metadata (public repo — no local paths)**

Run: `grep -rl "$HOME\|/home/" core/src/main/resources/META-INF football/src/main/resources/META-INF || echo "clean (no local paths)"`
Expected: `clean (no local paths)`. The files should contain only class/member names. Also confirm files exist:
`find core/src/main/resources/META-INF football/src/main/resources/META-INF -type f`
Expected: at least a `reachability-metadata.json` (or `reflect-config.json`) per module.

- [ ] **Step 5: Rebuild on the GraalVM JDK to confirm nothing breaks**

Run: `./gradlew clean build`
Expected: BUILD SUCCESSFUL (the committed resources are just packaged; tests still pass).

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "feat: ship GraalVM reachability metadata for core and football models"
```

---

### Task 4: Add the `native-smoke` subproject and verify the native image (requires GraalVM)

**Files:**
- Modify: `settings.gradle.kts`
- Create: `native-smoke/build.gradle.kts`
- Create: `native-smoke/src/main/java/io/github/miro93/sportmonks/smoke/NativeSmoke.java`

- [ ] **Step 1: Register the subproject**

In `settings.gradle.kts`, change the include line to:
```kotlin
include("core", "football", "native-smoke")
```

- [ ] **Step 2: Create `native-smoke/build.gradle.kts`**

```kotlin
plugins {
    application
    alias(libs.plugins.graalvm.native)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":football"))
}

application {
    mainClass = "io.github.miro93.sportmonks.smoke.NativeSmoke"
}

graalvmNative {
    binaries.named("main") {
        imageName = "native-smoke"
        // Fail fast and keep the smoke build lean.
        buildArgs.add("--no-fallback")
    }
}
```

- [ ] **Step 3: Create the smoke `main`**

`native-smoke/src/main/java/io/github/miro93/sportmonks/smoke/NativeSmoke.java`:

```java
package io.github.miro93.sportmonks.smoke;

import com.sun.net.httpserver.HttpServer;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.football.FootballClient;
import io.github.miro93.sportmonks.football.model.Fixture;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/// Self-contained native-image smoke test: serves a canned SportMonks envelope
/// from an in-process JDK HTTP server, fetches and decodes it through
/// {@link FootballClient}, and asserts the result. Exits 0 on success, 1 on failure.
public final class NativeSmoke {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        byte[] body = """
                { "data": { "id": 18533878, "name": "Celtic vs Rangers" } }
                """.getBytes(StandardCharsets.UTF_8);
        server.createContext("/fixtures/18533878", exchange -> {
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        server.start();
        try {
            int port = server.getAddress().getPort();
            FootballClient client = FootballClient.builder()
                    .apiToken(ApiToken.of("smoke"))
                    .baseUrl("http://127.0.0.1:" + port)
                    .build();

            Fixture fixture = client.fixtures().byId(18533878L).get().data();

            if (fixture == null || fixture.id() != 18533878L
                    || !"Celtic vs Rangers".equals(fixture.name())) {
                System.err.println("NATIVE SMOKE FAILED: unexpected decode result: " + fixture);
                System.exit(1);
            }
            System.out.println("NATIVE SMOKE OK");
        } finally {
            server.stop(0);
        }
    }
}
```

- [ ] **Step 4: Confirm it runs on the JVM first**

Run: `./gradlew :native-smoke:run`
Expected: prints `NATIVE SMOKE OK`, build successful.

- [ ] **Step 5: Compile and run the NATIVE image (the real proof)**

Run: `./gradlew :native-smoke:nativeCompile`
Expected: BUILD SUCCESSFUL (a native binary is produced; may take a few minutes).

Run: `./native-smoke/build/native/nativeCompile/native-smoke`
Expected: prints `NATIVE SMOKE OK` and exits 0 (`echo $?` → 0). If it fails with a Jackson
`InaccessibleObjectException`/reflection error, the metadata from Task 3 is incomplete — re-run
Task 3's agent over the smoke run too (`./gradlew :native-smoke:run` under `-Pagent`) and merge,
then recompile.

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "test: add native-image smoke test (native-smoke subproject)"
```

---

### Task 5: CI workflow `native.yml`

**Files:**
- Create: `.github/workflows/native.yml`

- [ ] **Step 1: Resolve a pinned SHA for setup-graalvm (repo convention is SHA-pinning)**

Run: `gh api repos/graalvm/setup-graalvm/commits/main --jq '.sha'` (and note the latest release tag via `gh release list --repo graalvm/setup-graalvm --limit 1`). Use the resolved 40-char SHA in the `uses:` line below, with the tag in a trailing comment. Reuse the already-pinned SHAs for `actions/checkout` and `gradle/actions/setup-gradle` from `.github/workflows/ci.yml`.

- [ ] **Step 2: Create the workflow**

`.github/workflows/native.yml` (replace `<SETUP_GRAALVM_SHA>`/`<TAG>` and the checkout/gradle SHAs with the resolved values from `ci.yml` and Step 1):

```yaml
name: Native

on:
  push:
    branches: [ main ]
  pull_request:

permissions:
  contents: read

jobs:
  native-smoke:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@df4cb1c069e1874edd31b4311f1884172cec0e10 # v6.0.3

      - name: Set up GraalVM 25
        uses: graalvm/setup-graalvm@<SETUP_GRAALVM_SHA> # <TAG>
        with:
          java-version: '25'
          distribution: graalvm

      - name: Set up Gradle
        uses: gradle/actions/setup-gradle@50e97c2cd7a37755bbfafc9c5b7cafaece252f6e # v6.1.0

      - name: Build native smoke image
        run: ./gradlew :native-smoke:nativeCompile --no-daemon

      - name: Run native smoke binary
        run: ./native-smoke/build/native/nativeCompile/native-smoke

      - name: Check reachability metadata is up to date
        run: |
          ./gradlew -Pagent test --no-daemon
          ./gradlew :core:metadataCopy --task test \
              --dir core/src/main/resources/META-INF/native-image/io.github.miro93.sportmonks/core --no-daemon
          ./gradlew :football:metadataCopy --task test \
              --dir football/src/main/resources/META-INF/native-image/io.github.miro93.sportmonks/football --no-daemon
          git diff --exit-code -- core/src/main/resources/META-INF football/src/main/resources/META-INF \
            || (echo "Reachability metadata is stale — regenerate with the agent and commit." && exit 1)
```

- [ ] **Step 3: Validate the YAML**

Run: `python3 -c "import yaml,sys; yaml.safe_load(open('.github/workflows/native.yml')); print('yaml ok')"`
Expected: `yaml ok`. (Full CI execution happens once pushed; cannot run Actions locally.)

- [ ] **Step 4: Commit**

```bash
git add .github/workflows/native.yml
git commit -m "ci: add GraalVM native smoke + metadata drift workflow"
```

---

### Task 6: README documentation

**Files:**
- Modify: `README.md`

- [ ] **Step 1: Add a GraalVM section**

Run `grep -n "Java Module System" README.md` to find the JPMS section (added in M14); add the new section right after it. Insert verbatim:

```markdown
## GraalVM native-image

The library is native-image ready: it ships Jackson reachability metadata under
`META-INF/native-image`, so an application that depends on it compiles to a native image
without extra reflection configuration. JSON decoding uses standard Jackson reflection (the
Blackbird module is intentionally not used, as its runtime code generation is unfriendly to
GraalVM's closed-world analysis).
```

- [ ] **Step 2: Verify and commit**

Run: `grep -n "GraalVM native-image" README.md`
Expected: shows the new section.

```bash
git add README.md
git commit -m "docs: document GraalVM native-image readiness"
```

---

### Task 7: Final verification

- [ ] **Step 1: Full JVM build (Temurin or GraalVM)**

Run: `./gradlew clean build`
Expected: BUILD SUCCESSFUL — all modules + `native-smoke` compile, all tests pass, no Blackbird warning.

- [ ] **Step 2: Native proof (GraalVM)**

Run (on the GraalVM JDK): `./gradlew :native-smoke:nativeCompile && ./native-smoke/build/native/nativeCompile/native-smoke; echo "exit=$?"`
Expected: `NATIVE SMOKE OK` and `exit=0`.

- [ ] **Step 3: Release note (no action)**

Ships as a minor release (additive: no public functional API change). Do NOT cut or merge a
release as part of this plan.

---

## Self-Review Notes

- **Spec coverage:** drop Blackbird incl. module-info + dependency (Task 1); apply native plugin
  (Task 2); generate + commit reachability metadata via agent (Task 3); self-contained
  `native-smoke` with `jdk.httpserver` + native compile/run (Task 4); `native.yml` with smoke +
  drift check (Task 5); README (Task 6); final JVM + native verification (Task 7). All spec
  sections map to a task.
- **Placeholders:** none, except the CI workflow's `setup-graalvm` SHA, which Task 5 Step 1
  explicitly resolves via `gh` before writing — not a left-in placeholder.
- **Consistency:** metadata resource dirs (`META-INF/native-image/io.github.miro93.sportmonks/core`
  and `.../football`) are identical across Task 3, Task 5 (drift check), and the build; the smoke
  endpoint (`fixtures().byId(18533878L)`, asserting `name == "Celtic vs Rangers"`) matches the
  canned JSON served at `/fixtures/18533878`.
- **GraalVM gating:** Tasks 3 and 4 are clearly marked as requiring the GraalVM JDK; Tasks 1, 2,
  5, 6 run on Temurin. No local personal paths are committed (`$HOME`/`sdk use` only).
