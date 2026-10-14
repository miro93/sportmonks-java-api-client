package io.github.miro93.sportmonks.motorsport.model;

import io.helidon.json.binding.Json;
import java.util.List;

/// A motorsport team (constructor). {@code id} is always present; every other field may
/// be {@code null}. {@code venueId} is unused by the Motorsport API.
@Json.Entity
public record Team(
        long id,
        @Json.Property("sport_id") Long sportId,
        @Json.Property("country_id") Long countryId,
        @Json.Property("venue_id") Long venueId,
        String gender,
        String name,
        @Json.Property("short_code") String shortCode,
        @Json.Property("image_path") String imagePath,
        Integer founded,
        String type,
        boolean placeholder,
        @Json.Property("last_played_at") String lastPlayedAt,
        List<Driver> drivers,
        List<Season> seasons) {
}
