package io.github.miro93.sportmonks.core.response;

import io.helidon.json.binding.Json;

/// SportMonks {@code rate_limit} block returned on every successful response.
@Json.Entity
public record RateLimit(int remaining,
                        @Json.Property("resets_in_seconds") int resetsInSeconds,
                        @Json.Property("requested_entity") String requestedEntity) {
}
