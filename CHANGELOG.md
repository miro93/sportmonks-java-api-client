# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
