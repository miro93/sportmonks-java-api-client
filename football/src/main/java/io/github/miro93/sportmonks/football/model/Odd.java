package io.github.miro93.sportmonks.football.model;

import io.helidon.json.binding.Json;

/// A single betting odd from the SportMonks football API (shared by the
/// pre-match and in-play feeds). {@code id} is always present; every other field
/// may be {@code null}. The numeric-looking fields — {@code value},
/// {@code probability}, {@code dp3}, {@code fractional}, {@code american},
/// {@code total} and {@code handicap} — are {@code String} because the API
/// returns them as strings (e.g. {@code "1.48"}, {@code "67.57%"}).
@Json.Entity
public record Odd(
        long id,
        @Json.Property("fixture_id") Long fixtureId,
        @Json.Property("market_id") Long marketId,
        @Json.Property("bookmaker_id") Long bookmakerId,
        String label,
        String value,
        String name,
        @Json.Property("sort_order") Integer sortOrder,
        @Json.Property("market_description") String marketDescription,
        String probability,
        String dp3,
        String fractional,
        String american,
        Boolean winning,
        Boolean stopped,
        String total,
        String handicap,
        String participants,
        @Json.Property("created_at") String createdAt,
        @Json.Property("updated_at") String updatedAt,
        @Json.Property("original_label") String originalLabel,
        @Json.Property("latest_bookmaker_update") String latestBookmakerUpdate) {
}
