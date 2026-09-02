package io.github.miro93.sportmonks.football.model;

import io.helidon.json.binding.Json;

/// A football coach. {@code id} is always present. Foreign-key {@code Long}
/// fields ({@code playerId}, {@code sportId}, {@code countryId},
/// {@code nationalityId}) and
/// optional scalars ({@code cityId}, {@code commonName}, {@code firstname},
/// {@code lastname}, {@code name}, {@code displayName}, {@code imagePath},
/// {@code height}, {@code weight}, {@code dateOfBirth}, {@code gender}) may be
/// {@code null}. Note: {@code cityId} is typed as {@code String} because the
/// API returns {@code city_id} as a string value. No relation fields are
/// exposed on this record.
@Json.Entity
public record Coach(
        long id,
        @Json.Property("player_id") Long playerId,
        @Json.Property("sport_id") Long sportId,
        @Json.Property("country_id") Long countryId,
        @Json.Property("nationality_id") Long nationalityId,
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
