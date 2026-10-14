package io.github.miro93.sportmonks.core.response;

import io.helidon.json.binding.Json;

/// SportMonks pagination block. Field names map to snake_case JSON via the codec.
///
/// {@code nextCursor} is the opaque token to pass back as {@code cursor} to fetch the next
/// page (cursor-based pagination, {@code null} on the last page or on legacy servers).
/// {@code nextPage} is the legacy offset-based link, kept for backward compatibility.
@Json.Entity
public record Pagination(int count,
                         @Json.Property("per_page") int perPage,
                         @Json.Property("current_page") int currentPage,
                         @Json.Property("next_page") String nextPage,
                         @Json.Property("next_cursor") String nextCursor,
                         @Json.Property("has_more") boolean hasMore) {
}
