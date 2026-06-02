package io.github.miro93.sportmonks.core.response;

/// SportMonks {@code rate_limit} block returned on every successful response.
public record RateLimit(int remaining, int resetsInSeconds, String requestedEntity) {
}
