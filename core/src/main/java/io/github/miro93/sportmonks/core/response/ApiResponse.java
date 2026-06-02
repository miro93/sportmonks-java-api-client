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
