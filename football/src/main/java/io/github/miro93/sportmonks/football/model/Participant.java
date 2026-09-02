package io.github.miro93.sportmonks.football.model;

import io.helidon.json.binding.Json;

/// A team taking part in a fixture. {@code meta} is present when the participant
/// is loaded through a fixture include.
@Json.Entity
public record Participant(
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
        ParticipantMeta meta) {
}
