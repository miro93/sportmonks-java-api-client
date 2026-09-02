package io.github.miro93.sportmonks.core.response;

import java.util.Optional;

/// Typed wrapper around the SportMonks response envelope.
/// {@code pagination} is present only for collection endpoints; {@code rateLimit}
/// is present on successful calls.
///
/// Not itself a `@Json.Entity`: Helidon 4.5.4's json-codegen cannot generate a
/// converter for a record whose component type *is* the record's own type
/// parameter (it emits the type variable verbatim into generated code, which
/// does not compile). {@link io.github.miro93.sportmonks.core.json.HelidonJsonCodec}
/// decodes the wire envelope into an internal, non-generic record and assembles
/// this type by hand.
public record ApiResponse<T>(T data, Pagination pagination, RateLimit rateLimit, String timezone) {

    public Optional<Pagination> paginationOpt() {
        return Optional.ofNullable(pagination);
    }

    public Optional<RateLimit> rateLimitOpt() {
        return Optional.ofNullable(rateLimit);
    }
}
