package io.github.miro93.sportmonks.football.model;

import io.helidon.json.binding.Json;
import java.util.List;

/// A football league. Scalar fields ({@code id} through {@code hasJerseys}) are
/// always present, except {@code sportId} and {@code countryId} which may be
/// {@code null} for international competitions. The relation field
/// ({@code seasons}) is {@code null} unless requested via includes.
@Json.Entity
public record League(
        long id,
        @Json.Property("sport_id") Long sportId,
        @Json.Property("country_id") Long countryId,
        String name,
        Boolean active,
        @Json.Property("short_code") String shortCode,
        @Json.Property("image_path") String imagePath,
        String type,
        @Json.Property("sub_type") String subType,
        @Json.Property("last_played_at") String lastPlayedAt,
        Integer category,
        @Json.Property("has_jerseys") Boolean hasJerseys,
        List<Season> seasons) {
}
