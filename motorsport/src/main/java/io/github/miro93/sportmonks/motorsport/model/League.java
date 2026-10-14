package io.github.miro93.sportmonks.motorsport.model;

import io.helidon.json.binding.Json;
import java.util.List;

/// A motorsport league (e.g. Formula 1). {@code id} is always present; every other
/// field may be {@code null}. {@code hasJerseys} is unused by the Motorsport API.
@Json.Entity
public record League(
        long id,
        @Json.Property("sport_id") Long sportId,
        @Json.Property("country_id") Long countryId,
        String name,
        Integer active,
        @Json.Property("image_path") String imagePath,
        String type,
        @Json.Property("sub_type") String subType,
        @Json.Property("last_played_at") String lastPlayedAt,
        Integer category,
        @Json.Property("has_jerseys") Boolean hasJerseys,
        Season currentSeason,
        List<Season> seasons,
        List<Stage> stages) {
}
