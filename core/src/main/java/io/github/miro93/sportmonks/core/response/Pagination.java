package io.github.miro93.sportmonks.core.response;

/// SportMonks pagination block. Field names map to snake_case JSON via the codec.
public record Pagination(int count, int perPage, int currentPage, String nextPage, boolean hasMore) {
}
