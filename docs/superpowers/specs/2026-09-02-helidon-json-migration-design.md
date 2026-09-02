# Helidon JSON migration (replace Jackson) — design

Date: 2026-09-02
Status: approved (chat) — target release 3.0.0

## Goal

Replace Jackson (`tools.jackson.core:jackson-databind`) with Helidon JSON
(`io.helidon.json:helidon-json-binding` 4.5.4, compile-time binding, zero
reflection) as the only JSON codec of the library. Published artifactIds
(`sportmonks-core`, `sportmonks-football`) and group stay unchanged; the
API break (Jackson types removed from the public surface) ships as 3.0.0.

## Facts established (verified against helidon-io/helidon sources, 4.5.x)

- Coordinates: `io.helidon.json:helidon-json-binding:4.5.4`; annotation
  processor path needs `io.helidon.codegen:helidon-codegen-apt` +
  `io.helidon.json:helidon-json-codegen`.
- Records are supported: `@Json.Entity public record ...`.
- Nested generics deserialize via `io.helidon.common.GenericType`
  (e.g. `ApiResponse<List<City>>`); programmatic parameterized types work
  (`JsonBindingTypes.listType` does exactly this).
- Unknown JSON properties are ignored by default (opt-in `@Json.FailOnUnknown`).
- No global naming strategy (codegen options are nulls/unknown/order only):
  snake_case must be per-field `@Json.Property("...")`.
- API is `@Api.Preview` (incubating): may change across Helidon 4.x. Accepted.
- Heavier transitive footprint than jackson-databind alone (helidon-common,
  helidon-config, helidon-service-registry, helidon-builder-api). Accepted.

## Changes

### 1. Dependencies

- `gradle/libs.versions.toml`: drop `jackson`; add `helidon = "4.5.4"` with
  `helidon-json-binding`, `helidon-json-codegen`, `helidon-codegen-apt`.
- `core`: `api(helidon-json-binding)`, `annotationProcessor(codegen-apt + json-codegen)`.
- `football`: `annotationProcessor(codegen-apt + json-codegen)` (its own records).

### 2. Codec (`core/json/`)

- `JacksonCodec` → `HelidonJsonCodec`, same public surface:
  `type(Class<T>)`, `listType(Class<T>)`, `decode(String, DataType<T>)`,
  `decode(byte[], DataType<T>)`. Backed by
  `JsonBinding.deserialize(bytes, GenericType<ApiResponse<T>>)`.
- `DataType<T>` wraps a pre-resolved `GenericType<ApiResponse<T>>`
  (built from a programmatic `ParameterizedType`) instead of an `ObjectReader`.
- `CodecException` cause becomes Helidon's `JsonBindingException`.
- `ApiExecutor` (and any client builder wiring) takes `HelidonJsonCodec`.

### 3. Models (~41 records in core + football)

- Add `@Json.Entity` to every record decoded from JSON, including the
  envelope types (`ApiResponse`, `Pagination`, `RateLimit`).
- Add `@Json.Property("snake_case_name")` on every multi-word component
  (`perPage`, `hasMore`, `countryId`, `imagePath`, ...).
- Risk to validate FIRST by test: absent primitive in JSON (e.g. missing
  `boolean hasMore`) must decode to the Java default, matching the previous
  lenient Jackson config (`FAIL_ON_NULL_FOR_PRIMITIVES=false`). If Helidon
  fails instead, switch those record components to wrapper types.

### 4. Modules / native / publication

- `module-info.java` (core, football): `requires tools.jackson.databind` →
  `requires io.helidon.json.binding;` (+ `io.helidon.common` if needed).
- `native-smoke`: unchanged as the proof; drop any Jackson reachability
  metadata. Compile-time binding needs no reflection config.
- Version: conventional commit `feat!:` so release-please cuts 3.0.0.

### 5. Tests

- Existing WireMock/endpoint/decoding tests are the behavioural spec: they
  must pass unchanged (except codec-class renames in test code).
- TDD order on the codec: absent-primitive + snake_case decoding test first,
  then implementation.

## Out of scope

- No codec abstraction/interface (concrete class swap, same as today).
- No artifact renames, no serialization features (client only reads JSON).
