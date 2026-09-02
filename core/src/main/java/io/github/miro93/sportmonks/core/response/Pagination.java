package io.github.miro93.sportmonks.core.response;

import io.helidon.json.binding.Json;

/// SportMonks pagination block. Field names map to snake_case JSON via the codec.
@Json.Entity
public record Pagination(int count,
                         @Json.Property("per_page") int perPage,
                         @Json.Property("current_page") int currentPage,
                         @Json.Property("next_page") String nextPage,
                         @Json.Property("has_more") boolean hasMore) {
}
