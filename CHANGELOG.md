# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.1.0](https://github.com/miro93/sportmonks-java-api-client/compare/v2.0.0...v2.1.0) (2026-06-05)


### Features

* complete builder configuration (connectTimeout + retryable statuses) ([#83](https://github.com/miro93/sportmonks-java-api-client/issues/83)) ([5c5a07f](https://github.com/miro93/sportmonks-java-api-client/commit/5c5a07f8943ab9b686f952510621e6cadb750318))
* expose configurable HttpClient on client builders ([#81](https://github.com/miro93/sportmonks-java-api-client/issues/81)) ([484f9ac](https://github.com/miro93/sportmonks-java-api-client/commit/484f9ac6bb373f7fad0daae97d14415608c99643))

## [2.0.0](https://github.com/miro93/sportmonks-java-api-client/compare/v1.0.0...v2.0.0) (2026-06-04)


### ⚠ BREAKING CHANGES

* previously-public internal types are now encapsulated and unavailable to consumers; the artifacts are now named JPMS modules (io.github.miro93.sportmonks.core / .football).

### Features

* GraalVM native-image readiness ([#79](https://github.com/miro93/sportmonks-java-api-client/issues/79)) ([8680532](https://github.com/miro93/sportmonks-java-api-client/commit/8680532b82af717cf113e9be2d8c2e3f043ba9f7))
* JPMS modularization (max encapsulation) — 2.0.0 ([#77](https://github.com/miro93/sportmonks-java-api-client/issues/77)) ([95acda8](https://github.com/miro93/sportmonks-java-api-client/commit/95acda80e08ec64d2bea6a690e4bea30b0817611))

## [1.0.0](https://github.com/miro93/sportmonks-java-api-client/compare/v0.6.0...v1.0.0) (2026-06-04)


### Features

* predictions (probabilities / value bets / predictability) ([#74](https://github.com/miro93/sportmonks-java-api-client/issues/74)) ([97fce59](https://github.com/miro93/sportmonks-java-api-client/commit/97fce59b871e8f7d364bca1128c38c2bde12caf9))
* statistics + expected (xG) ([#76](https://github.com/miro93/sportmonks-java-api-client/issues/76)) ([d72d65f](https://github.com/miro93/sportmonks-java-api-client/commit/d72d65f643c50918f73cda65cc23fb7549698504))


### Miscellaneous Chores

* release 1.0.0 ([c1443bd](https://github.com/miro93/sportmonks-java-api-client/commit/c1443bd96cd4d1fd9d707a3a77244c1bcba715a1))

## [0.6.0](https://github.com/miro93/sportmonks-java-api-client/compare/v0.5.0...v0.6.0) (2026-06-04)


### Features

* premium expected lineups (by team / by player) ([#72](https://github.com/miro93/sportmonks-java-api-client/issues/72)) ([ef2f9da](https://github.com/miro93/sportmonks-java-api-client/commit/ef2f9da8f5bd38e974c99c5d7d5b003a14e0f8ed))

## [0.5.0](https://github.com/miro93/sportmonks-java-api-client/compare/v0.4.0...v0.5.0) (2026-06-04)


### Features

* premium odds feed (Premium pre-match/History/Markets/Bookmakers) ([#71](https://github.com/miro93/sportmonks-java-api-client/issues/71)) ([72d07a2](https://github.com/miro93/sportmonks-java-api-client/commit/72d07a2cf4be168b1f20067c1b5001ff403561d7))
* standard odds feed (Bookmakers/Markets/Pre-match/Inplay odds) ([#69](https://github.com/miro93/sportmonks-java-api-client/issues/69)) ([fa4b7ef](https://github.com/miro93/sportmonks-java-api-client/commit/fa4b7ef9a2d1789fe96e6692642c0188a554c3e6))

## [0.4.0](https://github.com/miro93/sportmonks-java-api-client/compare/v0.3.0...v0.4.0) (2026-06-03)


### Features

* football referentials (States/Venues/Referees/TV Stations/Commentaries) ([#67](https://github.com/miro93/sportmonks-java-api-client/issues/67)) ([a2b8482](https://github.com/miro93/sportmonks-java-api-client/commit/a2b84820decbc80e5deb495260d46df34ca0d25e))

## [0.3.0](https://github.com/miro93/sportmonks-java-api-client/compare/v0.2.0...v0.3.0) (2026-06-03)


### Features

* Core API foundation (CoreClient + continents/countries/regions/cities/types) ([#65](https://github.com/miro93/sportmonks-java-api-client/issues/65)) ([ca8c232](https://github.com/miro93/sportmonks-java-api-client/commit/ca8c232a7d8e68f4ebbd110cd05139c8f06e9d87))

## [Unreleased]

## [0.2.0] - 2026-06-03

### Changed
- Migrated the JSON layer to **Jackson 3** (`tools.jackson` 3.1.4). The transitive Jackson
  dependency now uses the `tools.jackson.*` coordinates — relevant if you also depend on
  Jackson directly. No public API of this library changed.
- Bumped JUnit to 6.1.0, AssertJ to 3.27.7, WireMock to 3.13.2 and Gradle to 9.5.1.
- Bumped all GitHub Actions to current majors and pinned them by commit SHA.

### Performance
- Decode HTTP responses straight from raw bytes instead of an intermediate `String`
  (Jackson's byte parser is faster and avoids allocating large strings for big payloads).
- Each endpoint now reuses a pre-built, thread-safe Jackson `ObjectReader`.

### Fixed
- Re-enabled lenient handling of `null`/absent primitives, which Jackson 3 made strict by
  default (an absent boolean such as `placeholder` again decodes to its default).

### Internal
- Added `serialVersionUID` to every exception type.
- Scoped the submitted dependency graph to runtime dependencies only.

## [0.1.0] - 2026-06-03

### Added
- Initial public release, published to Maven Central.
- Sport-agnostic `core` module: JDK `HttpClient` transport, API-token authentication,
  Jackson codec, retry with exponential backoff, lazy auto-pagination, a sealed error
  hierarchy, and synchronous + asynchronous (`CompletableFuture`, virtual threads) execution.
- `football` module endpoints: fixtures, livescores, leagues, seasons, stages, rounds,
  schedules, teams, players, coaches, squads, transfers, standings and topscorers.

[Unreleased]: https://github.com/miro93/sportmonks-java-api-client/compare/v0.2.0...HEAD
[0.2.0]: https://github.com/miro93/sportmonks-java-api-client/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/miro93/sportmonks-java-api-client/releases/tag/v0.1.0
