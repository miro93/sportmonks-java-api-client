# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [4.0.0](https://github.com/miro93/sportmonks-java-api-client/compare/v3.0.1...v4.0.0) (2026-10-15)


### ⚠ BREAKING CHANGES

* **deps:** the published jars target Java 27.

### Features

* **core:** cap the response body size read by the transport ([d1875bf](https://github.com/miro93/sportmonks-java-api-client/commit/d1875bff7fa1ed400d51bc632aea51ea15f981e9)), closes [#141](https://github.com/miro93/sportmonks-java-api-client/issues/141)
* **core:** optional TTL cache for reference data ([9f70304](https://github.com/miro93/sportmonks-java-api-client/commit/9f703043b8e79aa05acccb81ecb9a73220a819f7)), closes [#144](https://github.com/miro93/sportmonks-java-api-client/issues/144)
* **core:** rate-limit tracking and optional client-side guard ([a2e6113](https://github.com/miro93/sportmonks-java-api-client/commit/a2e6113169d007b504c9f7f20c5b0ab85aec02c3)), closes [#122](https://github.com/miro93/sportmonks-java-api-client/issues/122)
* **core:** request gzip responses and decompress them in the transport ([a6a525a](https://github.com/miro93/sportmonks-java-api-client/commit/a6a525ad68abc6591a87caea286a51899d372b21)), closes [#138](https://github.com/miro93/sportmonks-java-api-client/issues/138)
* **core:** RequestListener hook for logging and metrics ([7a27b0f](https://github.com/miro93/sportmonks-java-api-client/commit/7a27b0f43d75203101f119b4ccfb1fc735de5f78)), closes [#123](https://github.com/miro93/sportmonks-java-api-client/issues/123)
* **core:** sortBy/order query options, plus per_page, locale and timezone ([#128](https://github.com/miro93/sportmonks-java-api-client/issues/128)) ([b6408e1](https://github.com/miro93/sportmonks-java-api-client/commit/b6408e127601d18b58141e8b84a666966e69fa1d)), closes [#117](https://github.com/miro93/sportmonks-java-api-client/issues/117) [#118](https://github.com/miro93/sportmonks-java-api-client/issues/118)
* **core:** support cursor-based pagination (next_cursor) ([#110](https://github.com/miro93/sportmonks-java-api-client/issues/110)) ([562288e](https://github.com/miro93/sportmonks-java-api-client/commit/562288ef9444992cc9b6b3c578a9bfcd81c99cba)), closes [#103](https://github.com/miro93/sportmonks-java-api-client/issues/103)
* cover the remaining SportMonks endpoint groups ([#109](https://github.com/miro93/sportmonks-java-api-client/issues/109)) ([#116](https://github.com/miro93/sportmonks-java-api-client/issues/116)) ([9324254](https://github.com/miro93/sportmonks-java-api-client/commit/9324254ae1002137889989784673028229f28563))
* decode envelope-less responses and expose transfer-rumours/config ([afac506](https://github.com/miro93/sportmonks-java-api-client/commit/afac506be5afeabfa86046ccb65ded5607b07727)), closes [#119](https://github.com/miro93/sportmonks-java-api-client/issues/119)
* **football:** add Event.rescinded for card events ([#111](https://github.com/miro93/sportmonks-java-api-client/issues/111)) ([7047396](https://github.com/miro93/sportmonks-java-api-client/commit/7047396904de50f23bde6e6fb27f4c9363e2de25)), closes [#104](https://github.com/miro93/sportmonks-java-api-client/issues/104)
* **football:** add latest(), upcomingByMarket(), upcomingByTvStation(), pastByTvStation() to FixturesEndpoint ([#112](https://github.com/miro93/sportmonks-java-api-client/issues/112)) ([eb69de1](https://github.com/miro93/sportmonks-java-api-client/commit/eb69de18675452ac394b6dd3620674747cf45bc0)), closes [#105](https://github.com/miro93/sportmonks-java-api-client/issues/105)
* **football:** add live probabilities endpoints to PredictionsEndpoint ([#113](https://github.com/miro93/sportmonks-java-api-client/issues/113)) ([047bf3a](https://github.com/miro93/sportmonks-java-api-client/commit/047bf3a5da8c7cd57248fd1b8edca49f1d41a4b9)), closes [#106](https://github.com/miro93/sportmonks-java-api-client/issues/106)
* **football:** add SeasonsEndpoint.brackets() for knockout tournament brackets ([#115](https://github.com/miro93/sportmonks-java-api-client/issues/115)) ([f3e9326](https://github.com/miro93/sportmonks-java-api-client/commit/f3e93266b2046c4540db5184ce374171b3d5eb71)), closes [#108](https://github.com/miro93/sportmonks-java-api-client/issues/108)
* **football:** add SquadsEndpoint.extendedByTeam() and Player.inSquad ([#114](https://github.com/miro93/sportmonks-java-api-client/issues/114)) ([fffcdaf](https://github.com/miro93/sportmonks-java-api-client/commit/fffcdafd16fff3a19050328e3f23fdc0ad38885e)), closes [#107](https://github.com/miro93/sportmonks-java-api-client/issues/107)
* **football:** Includes constants for the documented include names ([1929e93](https://github.com/miro93/sportmonks-java-api-client/commit/1929e93caeab2c7a2910b1f7dab920f12f33b2cb)), closes [#124](https://github.com/miro93/sportmonks-java-api-client/issues/124)
* **football:** typed date accessors on the models ([208b94b](https://github.com/miro93/sportmonks-java-api-client/commit/208b94b7691fd3f259b7ea5bb3d0fe389e211557)), closes [#120](https://github.com/miro93/sportmonks-java-api-client/issues/120)
* **motorsport:** new module for the SportMonks Motorsport API v3 ([24b1214](https://github.com/miro93/sportmonks-java-api-client/commit/24b1214fc852940e45f0c782543a0cc9e876004e)), closes [#127](https://github.com/miro93/sportmonks-java-api-client/issues/127)


### Bug Fixes

* **core:** default HttpClient no longer follows redirects ([aeb7cc8](https://github.com/miro93/sportmonks-java-api-client/commit/aeb7cc88b3a3d20d72b29ecccceb3d5b42ec61c5)), closes [#140](https://github.com/miro93/sportmonks-java-api-client/issues/140)
* **core:** refuse plain http:// base URLs outside loopback ([d668bf4](https://github.com/miro93/sportmonks-java-api-client/commit/d668bf4b7a78f2aed3d5db75ec0de3bf031abe37)), closes [#139](https://github.com/miro93/sportmonks-java-api-client/issues/139)


### Performance Improvements

* **core:** decode the envelope in one JsonParser pass, share Retry-After parsing ([e250eb4](https://github.com/miro93/sportmonks-java-api-client/commit/e250eb44016742f5316da42d6a8ac1e3c8cba1b1))
* **core:** one-page lookahead in CollectionRequest.stream() ([f83f87d](https://github.com/miro93/sportmonks-java-api-client/commit/f83f87d2038cbedda22592904996266705fd6411)), closes [#142](https://github.com/miro93/sportmonks-java-api-client/issues/142)
* **core:** parse each response exactly once in HelidonJsonCodec ([12a8680](https://github.com/miro93/sportmonks-java-api-client/commit/12a86801a0f06a00bef96a25b469c7dc4bc256ef)), closes [#143](https://github.com/miro93/sportmonks-java-api-client/issues/143)


### Build System

* **deps:** Java 27 toolchain and Helidon 27.0.0 ([9e6fdf7](https://github.com/miro93/sportmonks-java-api-client/commit/9e6fdf73d2d4766416bf45cd5c04119d0efece8e))

## [3.0.1](https://github.com/miro93/sportmonks-java-api-client/compare/v3.0.0...v3.0.1) (2026-10-14)


### Bug Fixes

* **core:** export `java.net.http` transitively (both client builders expose `HttpClient`) and build request headers once per executor ([#100](https://github.com/miro93/sportmonks-java-api-client/issues/100)) ([bc06ef0](https://github.com/miro93/sportmonks-java-api-client/commit/bc06ef0))

## [3.0.0](https://github.com/miro93/sportmonks-java-api-client/compare/v2.1.0...v3.0.0) (2026-09-03)


### ⚠ BREAKING CHANGES

* replace Jackson with Helidon JSON ([#96](https://github.com/miro93/sportmonks-java-api-client/issues/96))

### Features

* replace Jackson with Helidon JSON ([#96](https://github.com/miro93/sportmonks-java-api-client/issues/96)) ([29dfe55](https://github.com/miro93/sportmonks-java-api-client/commit/29dfe55f4ee59b17bb3786def9d850b0d3411905))

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
