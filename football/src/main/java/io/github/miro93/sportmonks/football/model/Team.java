package io.github.miro93.sportmonks.football.model;

import io.helidon.json.binding.Json;
import java.util.List;

/// A football team. {@code id} is always present; {@code placeholder} is always
/// present (defaults to {@code false}). Foreign-key {@code Long} fields
/// ({@code sportId}, {@code countryId}, {@code venueId}) and other optional
/// scalars ({@code gender}, {@code name}, {@code shortCode}, {@code imagePath},
/// {@code founded}, {@code type}, {@code lastPlayedAt}) may be {@code null}.
/// The relation field ({@code squad}) is {@code null} unless requested via includes.
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
        List<Squad> squad) {
}
