package io.github.miro93.sportmonks.football.model;

import io.helidon.json.binding.Json;

/// A match referee from the SportMonks football API. {@code id} is always
/// present; every other field may be {@code null}. {@code cityId} is typed as
/// {@code String} because the API returns {@code city_id} as a string value
/// (same as {@link Coach}).
@Json.Entity
public record Referee(
        long id,
        @Json.Property("sport_id") Long sportId,
        @Json.Property("country_id") Long countryId,
        @Json.Property("city_id") String cityId,
        @Json.Property("common_name") String commonName,
        String firstname,
        String lastname,
        String name,
        @Json.Property("display_name") String displayName,
        @Json.Property("image_path") String imagePath,
        Integer height,
        Integer weight,
        @Json.Property("date_of_birth") String dateOfBirth,
        String gender) {
}
