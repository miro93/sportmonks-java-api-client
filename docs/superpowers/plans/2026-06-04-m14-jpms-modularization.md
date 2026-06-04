# M14 — JPMS Modularization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add `module-info.java` to both modules with maximum encapsulation — internal plumbing hidden from end users via qualified exports and relocation of internal classes — shipping as 2.0.0.

**Architecture:** A refactor under the existing test suite (no new behavior). First relocate internal classes out of mixed packages into `.internal` subpackages (tests stay green throughout), then add the two module descriptors (qualified exports + `opens` to Jackson), then docs + release. The spike already proved tests pass on the module-path with zero manual Gradle config.

**Tech Stack:** Java 25, Gradle, JPMS, Jackson 3 (modular: `tools.jackson.*`), JUnit 5 + WireMock + AssertJ.

**Refactor discipline:** This plan moves classes between packages. The reliable loop for each move is: `git mv` the file(s), fix the `package` declaration, then **compile and let the compiler list every broken reference**, add the exact import each one needs, repeat until green, then run the full test suite. The plan lists the known special cases explicitly so none are missed.

---

### Task 1: Relocate `ErrorMapper` to `core.error.internal`

**Files:**
- Move: `core/.../core/error/ErrorMapper.java` → `core/.../core/error/internal/ErrorMapper.java`
- Move (test): `core/src/test/.../core/error/ErrorMapperTest.java` → `core/src/test/.../core/error/internal/ErrorMapperTest.java`
- Modify: `core/.../core/ApiExecutor.java` (import)

- [ ] **Step 1: Move the class and its test, fix package declarations**

```bash
cd /home/mturk/project/sportmonks
mkdir -p core/src/main/java/io/github/miro93/sportmonks/core/error/internal
git mv core/src/main/java/io/github/miro93/sportmonks/core/error/ErrorMapper.java \
       core/src/main/java/io/github/miro93/sportmonks/core/error/internal/ErrorMapper.java
mkdir -p core/src/test/java/io/github/miro93/sportmonks/core/error/internal
git mv core/src/test/java/io/github/miro93/sportmonks/core/error/ErrorMapperTest.java \
       core/src/test/java/io/github/miro93/sportmonks/core/error/internal/ErrorMapperTest.java
```

In `internal/ErrorMapper.java`: change the package line to
`package io.github.miro93.sportmonks.core.error.internal;` and add imports for every
`core.error` exception type it references (it maps HTTP failures to exceptions —
`AuthenticationException`, `NotFoundException`, `RateLimitException`, `ServerException`,
`TransportException`, `ValidationException`, `SportmonksException`). Concretely add the
needed subset, e.g.:

```java
package io.github.miro93.sportmonks.core.error.internal;

import io.github.miro93.sportmonks.core.error.AuthenticationException;
import io.github.miro93.sportmonks.core.error.NotFoundException;
import io.github.miro93.sportmonks.core.error.RateLimitException;
import io.github.miro93.sportmonks.core.error.ServerException;
import io.github.miro93.sportmonks.core.error.SportmonksException;
import io.github.miro93.sportmonks.core.error.TransportException;
import io.github.miro93.sportmonks.core.error.ValidationException;
```

(Keep only the imports the file actually uses — the compiler will flag unused/missing.)

In `internal/ErrorMapperTest.java`: change package to
`...core.error.internal;` and add the same exception imports it asserts on.

- [ ] **Step 2: Update the `ApiExecutor` import**

In `core/.../core/ApiExecutor.java`, replace
`import io.github.miro93.sportmonks.core.error.ErrorMapper;`
with
`import io.github.miro93.sportmonks.core.error.internal.ErrorMapper;`

- [ ] **Step 3: Compile and fix any remaining references**

Run: `./gradlew :core:compileJava :core:compileTestJava`
Expected: BUILD SUCCESSFUL. If any "cannot find symbol: ErrorMapper" or
"package ... does not exist" appears, add `import io.github.miro93.sportmonks.core.error.internal.ErrorMapper;`
to that file. Repeat until green.

- [ ] **Step 4: Run the core test suite**

Run: `./gradlew :core:test`
Expected: PASS (all existing core tests green; nothing else changed behaviorally).

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "refactor: relocate ErrorMapper to core.error.internal"
```

---

### Task 2: Relocate `Sleeper` and `RetryingTransport` to `core.retry.internal`

**Files:**
- Move: `core/.../core/retry/Sleeper.java` → `.../core/retry/internal/Sleeper.java`
- Move: `core/.../core/retry/RetryingTransport.java` → `.../core/retry/internal/RetryingTransport.java`
- Move (tests): `SleeperTest.java`, `RetryingTransportTest.java` → mirrored `.../retry/internal/`
- Modify imports: `core/.../core/coreapi/CoreClient.java`, `football/.../football/FootballClient.java`

- [ ] **Step 1: Move classes + tests, fix package declarations**

```bash
cd /home/mturk/project/sportmonks
mkdir -p core/src/main/java/io/github/miro93/sportmonks/core/retry/internal
git mv core/src/main/java/io/github/miro93/sportmonks/core/retry/Sleeper.java \
       core/src/main/java/io/github/miro93/sportmonks/core/retry/internal/Sleeper.java
git mv core/src/main/java/io/github/miro93/sportmonks/core/retry/RetryingTransport.java \
       core/src/main/java/io/github/miro93/sportmonks/core/retry/internal/RetryingTransport.java
mkdir -p core/src/test/java/io/github/miro93/sportmonks/core/retry/internal
git mv core/src/test/java/io/github/miro93/sportmonks/core/retry/SleeperTest.java \
       core/src/test/java/io/github/miro93/sportmonks/core/retry/internal/SleeperTest.java
git mv core/src/test/java/io/github/miro93/sportmonks/core/retry/RetryingTransportTest.java \
       core/src/test/java/io/github/miro93/sportmonks/core/retry/internal/RetryingTransportTest.java
```

Fix `package` lines to `...core.retry.internal;` in all four moved files.

- `internal/RetryingTransport.java` now needs imports for the types it uses that stayed in
  `core.retry` (`RetryPolicy`, and `Backoff` if referenced) plus `core.http` types it wraps
  (`HttpTransport`, `RawResponse`). `Sleeper` is now in the same package (no import). Add:

```java
import io.github.miro93.sportmonks.core.retry.RetryPolicy;
import io.github.miro93.sportmonks.core.http.HttpTransport;
```

(Add `Backoff`/`RawResponse` imports too if the compiler flags them.)

- `internal/RetryingTransportTest.java` and `internal/SleeperTest.java`: fix package; add
  imports for `RetryPolicy`/`Backoff`/`HttpTransport`/`RawResponse` as the compiler flags.

- [ ] **Step 2: Update importers (`CoreClient`, `FootballClient`)**

In both `core/.../coreapi/CoreClient.java` and `football/.../football/FootballClient.java`,
replace:

```java
import io.github.miro93.sportmonks.core.retry.RetryingTransport;
import io.github.miro93.sportmonks.core.retry.Sleeper;
```

with:

```java
import io.github.miro93.sportmonks.core.retry.internal.RetryingTransport;
import io.github.miro93.sportmonks.core.retry.internal.Sleeper;
```

(Leave the existing `import ...core.retry.RetryPolicy;` untouched — `RetryPolicy` stays public.)

- [ ] **Step 3: Compile both modules and fix remaining references**

Run: `./gradlew :core:compileJava :core:compileTestJava :football:compileJava`
Expected: BUILD SUCCESSFUL. Add the `...retry.internal.` import to any file the compiler
flags. Repeat until green.

- [ ] **Step 4: Run both test suites**

Run: `./gradlew :core:test :football:test`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "refactor: relocate Sleeper and RetryingTransport to core.retry.internal"
```

---

### Task 3: Relocate `RequestSpec` and `UrlBuilder` to `core.request.internal`

This is the large one (~40 import sites across all endpoints in both modules). `RequestSpec`
uses `UrlBuilder`, so they move together into the same `core.request.internal` package.

**Files:**
- Move: `core/.../core/request/RequestSpec.java` → `.../request/internal/RequestSpec.java`
- Move: `core/.../core/request/UrlBuilder.java` → `.../request/internal/UrlBuilder.java`
- Move (tests): `RequestSpecTest.java`, `UrlBuilderTest.java` → mirrored `.../request/internal/`
- Modify: every endpoint (core `coreapi.endpoint.*` + football `endpoint.*`), `CollectionRequest`, `SingleResourceRequest`, `ApiExecutor`, and the moved/affected tests.

- [ ] **Step 1: Move classes + their tests, fix package declarations**

```bash
cd /home/mturk/project/sportmonks
mkdir -p core/src/main/java/io/github/miro93/sportmonks/core/request/internal
git mv core/src/main/java/io/github/miro93/sportmonks/core/request/RequestSpec.java \
       core/src/main/java/io/github/miro93/sportmonks/core/request/internal/RequestSpec.java
git mv core/src/main/java/io/github/miro93/sportmonks/core/request/UrlBuilder.java \
       core/src/main/java/io/github/miro93/sportmonks/core/request/internal/UrlBuilder.java
mkdir -p core/src/test/java/io/github/miro93/sportmonks/core/request/internal
git mv core/src/test/java/io/github/miro93/sportmonks/core/request/RequestSpecTest.java \
       core/src/test/java/io/github/miro93/sportmonks/core/request/internal/RequestSpecTest.java
git mv core/src/test/java/io/github/miro93/sportmonks/core/request/UrlBuilderTest.java \
       core/src/test/java/io/github/miro93/sportmonks/core/request/internal/UrlBuilderTest.java
```

Fix `package` lines to `...core.request.internal;` in all four moved files. `RequestSpec` and
`UrlBuilder` are now in the same package (RequestSpec's import of UrlBuilder, if any, can be
removed). The two test files: fix package; they reference only the moved classes (same
package now) — remove any now-redundant import of `RequestSpec`/`UrlBuilder`.

- [ ] **Step 2: Rewrite the `RequestSpec` import across all endpoint importers (bulk)**

Many files currently `import io.github.miro93.sportmonks.core.request.RequestSpec;`. Rewrite
them all in one pass:

```bash
cd /home/mturk/project/sportmonks
grep -rl "import io.github.miro93.sportmonks.core.request.RequestSpec;" core/src football/src \
  | xargs sed -i 's#import io.github.miro93.sportmonks.core.request.RequestSpec;#import io.github.miro93.sportmonks.core.request.internal.RequestSpec;#'
# UrlBuilder importers (ApiExecutor; RequestSpec already moved):
grep -rl "import io.github.miro93.sportmonks.core.request.UrlBuilder;" core/src football/src \
  | xargs sed -i 's#import io.github.miro93.sportmonks.core.request.UrlBuilder;#import io.github.miro93.sportmonks.core.request.internal.UrlBuilder;#'
```

- [ ] **Step 3: Add NEW imports to the same-package siblings that had none**

`CollectionRequest` and `SingleResourceRequest` live in `core.request` and used `RequestSpec`
**without an import** (same package). Now that `RequestSpec` moved, they need an explicit
import. Add to both `core/.../core/request/CollectionRequest.java` and
`core/.../core/request/SingleResourceRequest.java`:

```java
import io.github.miro93.sportmonks.core.request.internal.RequestSpec;
```

(`ApiExecutor` already imported `RequestSpec` and `UrlBuilder` explicitly, so it was handled
by the sed in Step 2 — verify.)

- [ ] **Step 4: Compile both modules and fix any stragglers**

Run: `./gradlew :core:compileJava :core:compileTestJava :football:compileJava :football:compileTestJava`
Expected: BUILD SUCCESSFUL. For any remaining "cannot find symbol: RequestSpec" /
"package ... does not exist", add
`import io.github.miro93.sportmonks.core.request.internal.RequestSpec;` (or the `UrlBuilder`
equivalent) to that file. Repeat until green.

- [ ] **Step 5: Run the full test suite**

Run: `./gradlew test`
Expected: PASS (all core + football tests green).

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "refactor: relocate RequestSpec and UrlBuilder to core.request.internal"
```

---

### Task 4: Add the two `module-info.java` descriptors

**Files:**
- Create: `core/src/main/java/module-info.java`
- Create: `football/src/main/java/module-info.java`
- Create (test): `football/src/test/java/io/github/miro93/sportmonks/football/ModuleDescriptorTest.java`

- [ ] **Step 1: Write the module descriptor test (football)**

Create `ModuleDescriptorTest.java`:

```java
package io.github.miro93.sportmonks.football;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ModuleDescriptorTest {

    @Test
    void footballRunsAsNamedModule() {
        Module module = FootballClient.class.getModule();
        assertThat(module.isNamed()).isTrue();
        assertThat(module.getName()).isEqualTo("io.github.miro93.sportmonks.football");
    }
}
```

- [ ] **Step 2: Run it to verify it fails**

Run: `./gradlew :football:test --tests "*ModuleDescriptorTest"`
Expected: FAIL — the module is unnamed (`isNamed()` false) until descriptors exist.

- [ ] **Step 3: Create `core/src/main/java/module-info.java`**

```java
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

- [ ] **Step 4: Create `football/src/main/java/module-info.java`**

```java
module io.github.miro93.sportmonks.football {
    requires transitive io.github.miro93.sportmonks.core;
    requires java.net.http;

    exports io.github.miro93.sportmonks.football;
    exports io.github.miro93.sportmonks.football.endpoint;
    exports io.github.miro93.sportmonks.football.model;

    opens io.github.miro93.sportmonks.football.model to tools.jackson.databind, tools.jackson.module.blackbird;
}
```

- [ ] **Step 5: Run the module descriptor test, then the full suite on the module-path**

Run: `./gradlew :football:test --tests "*ModuleDescriptorTest"`
Expected: PASS.

Run: `./gradlew test`
Expected: PASS (all core + football tests green on the module-path). If a runtime
`InaccessibleObjectException` from Jackson appears for a decoded type, add an `opens <pkg> to
tools.jackson.databind, tools.jackson.module.blackbird;` for that package (the model/response
packages are already opened; this would only arise for an overlooked decoded package).

- [ ] **Step 6: Commit (this is the breaking change → triggers 2.0.0)**

```bash
git add -A
git commit -m "feat!: add JPMS module descriptors with max encapsulation

Adds module-info.java for both modules. Internal plumbing (ApiExecutor,
JacksonCodec/DataType, HTTP transport, RequestSpec/UrlBuilder, retry internals,
ErrorMapper) is no longer part of the public API surface.

BREAKING CHANGE: previously-public internal types are now encapsulated and
unavailable to consumers; the artifacts are now named JPMS modules
(io.github.miro93.sportmonks.core / .football)."
```

---

### Task 5: README documentation

**Files:**
- Modify: `README.md`

- [ ] **Step 1: Add a JPMS section**

Run `grep -n "## " README.md` to find a sensible spot (e.g. after the install/usage section).
Add a short section verbatim:

```markdown
## Java Module System (JPMS)

The library ships as named modules:

- `io.github.miro93.sportmonks.core`
- `io.github.miro93.sportmonks.football`

Add them to your `module-info.java`:

```java
requires io.github.miro93.sportmonks.football; // transitively requires ...core
```

Internal plumbing (HTTP transport, JSON codec, request building, retry internals) is
encapsulated and not part of the public API. The library also works unchanged on the classpath.
```

- [ ] **Step 2: Verify and commit**

Run: `grep -n "Java Module System" README.md`
Expected: shows the new section.

```bash
git add README.md
git commit -m "docs: document JPMS modules"
```

---

### Task 6: Final verification

- [ ] **Step 1: Full clean build and test**

Run: `./gradlew clean build`
Expected: BUILD SUCCESSFUL — both modules compile as JPMS modules, all tests pass on the
module-path, jars assemble. A single cosmetic warning ("module name component miro93 should
avoid terminal digits") is expected and accepted.

- [ ] **Step 2: Confirm the public surface no longer leaks internals (sanity)**

Run:
```bash
jar --describe-module --file core/build/libs/core-*.jar | grep -E "exports|opens" | grep -v " to " | sed 's/^/PUBLIC: /'
```
Expected: the PUBLIC (unqualified) exports list contains only the public API packages
(`auth`, `error`, `paging`, `request`, `response`, `retry`, `coreapi`, `coreapi.endpoint`,
`coreapi.model`) — NOT `core` (root), `http`, `json`, or any `.internal` package.

- [ ] **Step 3: Release note (no action)**

This ships as **2.0.0**; the `feat!`/`BREAKING CHANGE` commit in Task 4 makes release-please
open a 2.0.0 release PR. Do NOT cut or merge the release as part of this plan.

---

## Self-Review Notes

- **Spec coverage:** internal relocations — ErrorMapper (Task 1), Sleeper/RetryingTransport
  (Task 2), RequestSpec/UrlBuilder (Task 3); module descriptors with qualified exports + opens
  (Task 4); README (Task 5); clean build + surface check + 2.0.0 note (Task 6). Wholly-internal
  packages (`core` root, `http`, `json`) are handled by qualified exports in Task 4 with no
  class move, matching the spec. All spec sections map to a task.
- **Type/name consistency:** new packages `core.error.internal`, `core.retry.internal`,
  `core.request.internal`; module names `io.github.miro93.sportmonks.core` /
  `io.github.miro93.sportmonks.football`; Jackson module names `tools.jackson.databind`,
  `tools.jackson.core`, `tools.jackson.module.blackbird` (verified against the resolved jars
  during the spike). These are used consistently across tasks.
- **Placeholders:** none — moves use exact `git mv`/`sed`; module descriptors are complete.
- **Refactor safety:** every move task ends by compiling and running the existing suite green
  before commit; the module-path behavior was validated by the spike.
- **Special cases called out:** `CollectionRequest`/`SingleResourceRequest` need a NEW
  `RequestSpec` import (were same-package); `RetryingTransport` needs new imports for
  `RetryPolicy`/`http` types after moving; `ErrorMapper` needs exception imports after moving.
