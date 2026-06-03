# M6 — Packaging & Publication Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax.

**Goal:** Make the library publishable to Maven Central and ship the supporting infra/docs — without performing a live publish. Closes #29 (CI), #30 (README + Javadoc), #31 (publishing config). This is the final milestone; it produces no new endpoint code.

**Decisions (confirmed with the maintainer):**
- **Publishing tooling:** `com.vanniktech.maven.publish` plugin, version **0.36.0** (handles the Central Publisher Portal + GPG signing + sources/javadoc jars + POM from a concise config).
- **Scope:** Infra **ready to publish**, but **no live publish** in this milestone. The release workflow is tag-driven and reads all credentials from **GitHub Secrets** (never committed). The maintainer triggers the first real release later.
- **Javadoc:** produce the javadoc jar (required by Maven Central) **and** publish per-module Javadoc to **GitHub Pages** on release.
- **License:** **Apache-2.0** (add `LICENSE` file + POM license metadata). _Easily changed if the maintainer prefers otherwise._
- **Coordinates:** group `io.github.miro93.sportmonks`; artifactIds `sportmonks-core` and `sportmonks-football`. Version stays `0.1.0-SNAPSHOT` in `gradle.properties`; the release workflow overrides `VERSION_NAME` from the git tag.
- **No personal data** anywhere (public repo): POM `developer` uses the GitHub handle `miro93` only — **no email, no real name, no local paths**. Credentials live only in GitHub Secrets.

**Tech Stack:** Gradle 9.5 Kotlin DSL multi-module (`core` + `football`), JDK 25 toolchain, version catalog (`gradle/libs.versions.toml`). GitHub Actions for CI/release/pages.

**Current state (verified):** No root `build.gradle.kts`. `settings.gradle.kts` includes `core`, `football`. Each module's `build.gradle.kts` sets `group`/`version` literally and applies `java-library`. No `gradle.properties`, no `.github/`, no `README`, no `LICENSE`.

---

## Task 1: Publishing configuration (vanniktech) + POM metadata + LICENSE (#31)

**Files:**
- MODIFY `gradle/libs.versions.toml` — add a `[plugins]` table with `vanniktech-mavenPublish = { id = "com.vanniktech.maven.publish", version = "0.36.0" }`.
- CREATE root `gradle.properties` — shared config + POM metadata (vanniktech reads these automatically):
  ```properties
  GROUP=io.github.miro93.sportmonks
  VERSION_NAME=0.1.0-SNAPSHOT

  SONATYPE_HOST=CENTRAL_PORTAL
  SONATYPE_AUTOMATIC_RELEASE=false
  RELEASE_SIGNING_ENABLED=true

  POM_URL=https://github.com/miro93/sportmonks-java-api-client
  POM_INCEPTION_YEAR=2026

  POM_LICENSE_NAME=The Apache License, Version 2.0
  POM_LICENSE_URL=https://www.apache.org/licenses/LICENSE-2.0.txt
  POM_LICENSE_DIST=repo

  POM_SCM_URL=https://github.com/miro93/sportmonks-java-api-client
  POM_SCM_CONNECTION=scm:git:https://github.com/miro93/sportmonks-java-api-client.git
  POM_SCM_DEV_CONNECTION=scm:git:ssh://git@github.com/miro93/sportmonks-java-api-client.git

  POM_DEVELOPER_ID=miro93
  POM_DEVELOPER_NAME=miro93
  POM_DEVELOPER_URL=https://github.com/miro93
  ```
  (Do NOT add a developer email. Keep Gradle daemon/JVM args out unless needed.)
- CREATE `core/gradle.properties` — `POM_ARTIFACT_ID=sportmonks-core`, `POM_NAME=SportMonks Java Client — Core`, `POM_DESCRIPTION=Sport-agnostic core (HTTP transport, auth, JSON codec, retry, pagination) for the SportMonks Java client.`
- CREATE `football/gradle.properties` — `POM_ARTIFACT_ID=sportmonks-football`, `POM_NAME=SportMonks Java Client — Football`, `POM_DESCRIPTION=Football (soccer) endpoints and models for the SportMonks Java client.`
- MODIFY `core/build.gradle.kts` and `football/build.gradle.kts`:
  - Apply the plugin in `plugins { ... alias(libs.plugins.vanniktech.mavenPublish) }`.
  - REMOVE the literal `group = ...` and `version = ...` lines (now driven by `GROUP`/`VERSION_NAME` in `gradle.properties`).
  - Add an explicit publishing config so the javadoc jar is generated deterministically, e.g.:
    ```kotlin
    import com.vanniktech.maven.publish.JavaLibrary
    import com.vanniktech.maven.publish.JavadocJar

    mavenPublishing {
        configure(JavaLibrary(javadocJar = JavadocJar.Javadoc(), sourcesJar = true))
    }
    ```
    (Confirm the exact import paths/API for 0.36.0 against the plugin docs — https://vanniktech.github.io/gradle-maven-publish-plugin/central/ — fetchable; adjust if the API differs.)
- CREATE `LICENSE` at repo root — the full **Apache License 2.0** text (standard, unmodified).

**Notes / gotchas:**
- For a `0.1.0-SNAPSHOT` version, vanniktech does NOT sign and does NOT need credentials, so local builds and `publishToMavenLocal` must work with no GPG key / no Sonatype creds present.
- Signing for releases uses in-memory keys via env (`ORG_GRADLE_PROJECT_signingInMemoryKey`, `...signingInMemoryKeyPassword`) and Central creds via `ORG_GRADLE_PROJECT_mavenCentralUsername`/`...Password` — these are provided by the release workflow from Secrets, NOT configured here.
- `///` Markdown JavaDoc (JEP 467, final in JDK 23) must render under the JDK 25 `javadoc` tool — the javadoc jar generation exercises this.

**Verification:**
- `./gradlew build` green (all 204 existing tests still pass).
- `./gradlew publishToMavenLocal` succeeds with NO signing keys present, and produces for BOTH modules: the main jar, `-sources.jar`, and `-javadoc.jar` under `~/.m2/repository/io/github/miro93/sportmonks/sportmonks-{core,football}/0.1.0-SNAPSHOT/` (verify the artifactIds + the three jars exist; inspect the generated `.pom` for name/description/license/scm/developer and confirm NO email is present).
- Confirm the generated coordinates are `io.github.miro93.sportmonks:sportmonks-core` and `:sportmonks-football`.

---

## Task 2: CI workflow — build + test on JDK 25 (#29)

**File:** CREATE `.github/workflows/ci.yml`.

Workflow `CI`: triggers on `push` to `main` and on `pull_request`. Single job `build` on `ubuntu-latest`:
1. `actions/checkout@v4`
2. `actions/setup-java@v4` with `distribution: temurin`, `java-version: 25`
3. `gradle/actions/setup-gradle@v4` (dependency + build cache)
4. `./gradlew build --no-daemon`

Keep it minimal and correct. Use `permissions: contents: read`. Pin action major versions as above. The job's success is the PR status check.

**Verification:** YAML is well-formed (parse it with a YAML tool, e.g. `python3 -c "import yaml,sys; yaml.safe_load(open('.github/workflows/ci.yml'))"`). Confirm the gradle invocation matches the verified working command. (Actions can't run locally — correctness is by inspection + YAML lint.)

---

## Task 3: Release + Javadoc-Pages workflows (#31 + #30)

**Files:** CREATE `.github/workflows/release.yml` and `.github/workflows/javadoc.yml`.

**`release.yml`** — `Release` workflow, trigger `on: push: tags: ['v*']`. Job on `ubuntu-latest`, `permissions: contents: read`:
1. checkout + setup-java (Temurin 25) + setup-gradle.
2. Derive the version from the tag: `VERSION=${GITHUB_REF_NAME#v}` (e.g. tag `v0.1.0` → `0.1.0`).
3. Run `./gradlew publishToMavenCentral --no-daemon -PVERSION_NAME="$VERSION"` (use the vanniktech aggregated task; with `SONATYPE_AUTOMATIC_RELEASE=false` this uploads + creates the deployment without auto-releasing, so the maintainer presses "Publish" in the Portal). Provide env from secrets:
   - `ORG_GRADLE_PROJECT_mavenCentralUsername: ${{ secrets.MAVEN_CENTRAL_USERNAME }}`
   - `ORG_GRADLE_PROJECT_mavenCentralPassword: ${{ secrets.MAVEN_CENTRAL_PASSWORD }}`
   - `ORG_GRADLE_PROJECT_signingInMemoryKey: ${{ secrets.SIGNING_KEY }}`
   - `ORG_GRADLE_PROJECT_signingInMemoryKeyPassword: ${{ secrets.SIGNING_KEY_PASSWORD }}`
   Document these four required secret names in a short comment at the top of the file.

**`javadoc.yml`** — `Javadoc` workflow, trigger `on: release: types: [published]` AND `workflow_dispatch` (manual). `permissions: contents: read, pages: write, id-token: write`. Job:
1. checkout + setup-java (Temurin 25) + setup-gradle.
2. `./gradlew javadoc --no-daemon` (each module emits `build/docs/javadoc`).
3. Assemble a site dir: copy `core/build/docs/javadoc` → `site/core/`, `football/build/docs/javadoc` → `site/football/`, and write a minimal `site/index.html` linking to `core/` and `football/`.
4. `actions/configure-pages@v5`, `actions/upload-pages-artifact@v3` (path `site`), `actions/deploy-pages@v4` (use the standard two-step pages job pattern with the `github-pages` environment).

Keep both workflows minimal and correct; pin action major versions.

**Verification:** both YAML files parse cleanly (YAML lint). `./gradlew javadoc` runs green locally and produces `build/docs/javadoc/index.html` for each module (this also re-confirms `///` comments render). Confirm no secret VALUES are present in the files — only `${{ secrets.* }}` references.

---

## Task 4: README + usage guide (#30)

**File:** CREATE `README.md` at repo root.

Sections:
- **Title + one-line description**; CI badge (`![CI](https://github.com/miro93/sportmonks-java-api-client/actions/workflows/ci.yml/badge.svg)`); license badge (Apache-2.0).
- **Requirements:** Java 25+.
- **Installation:** Gradle Kotlin DSL + Groovy + Maven snippets for BOTH artifacts (`io.github.miro93.sportmonks:sportmonks-football:0.1.0` pulls `core` transitively; show core too). Note: not yet on Maven Central until the first release is published — phrase accordingly (e.g. "once released").
- **Quickstart:** build a `FootballClient` (token via env `SPORTMONKS_API_TOKEN` / `ApiToken.fromEnv()` or `ApiToken.of("...")` — **use a placeholder, never a real token**), a sync `byId` call with includes/filters/select, an async `getAsync` call, and the lazy auto-paginated `.stream()` example. Mirror the spec's "Modèle d'usage cible" but verify method names against the ACTUAL code (read `FootballClient`, `FixturesEndpoint`, `RequestSpec`/the request types, `ApiResponse`/result type, `ApiToken`) — do not invent methods.
- **Includes / filters / select / sort / pagination:** brief explanation matching the real builder API.
- **Error handling:** the sealed `SportmonksException` hierarchy (verify the actual type names in `core`).
- **Available endpoints:** concise list grouped by milestone (fixtures, livescores, leagues, seasons, stages, rounds, schedules, teams, players, coaches, squads, transfers, standings, topscorers).
- **Modules:** `core` (sport-agnostic) + `football`; how to add a sport.
- **Javadoc:** link to the GitHub Pages site (`https://miro93.github.io/sportmonks-java-api-client/`).
- **License:** Apache-2.0.

**Accuracy requirement:** every code snippet must compile against the real API. Read the actual source for exact class/method names before writing snippets. Keep all examples free of real tokens/emails/paths.

**Verification:** snippets cross-checked against real signatures (cite the files). Markdown well-formed. No personal data.

---

## Definition of done

- [ ] All tasks complete; `./gradlew build` SUCCESSFUL (204 tests still green).
- [ ] `./gradlew publishToMavenLocal` produces signed-free SNAPSHOT artifacts (jar + sources + javadoc) for both modules with correct coordinates + POM metadata, no creds needed.
- [ ] `./gradlew javadoc` green for both modules.
- [ ] All workflow YAML files parse; no secret values committed; only `${{ secrets.* }}` references.
- [ ] README snippets match the real API surface.
- [ ] No personal data committed (no real email/token/local path); POM developer is the `miro93` handle only.
- [ ] `LICENSE` (Apache-2.0) present and referenced by the POM.
- [ ] PR opened against `main` closing #29, #30, #31.
