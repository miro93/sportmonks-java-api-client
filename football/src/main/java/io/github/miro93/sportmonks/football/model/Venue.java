package io.github.miro93.sportmonks.football.model;

import io.helidon.json.binding.Json;

/// A venue (stadium) from the SportMonks football API. {@code id} is always
/// present; every other field may be {@code null}. {@code cityId} is typed as
/// {@code String} because the API returns {@code city_id} as a string value
/// (same as {@link Coach}); {@code latitude}/{@code longitude} are also strings.
@Json.Entity
public record Venue(
        long id,
        @Json.Property("country_id") Long countryId,
        @Json.Property("city_id") String cityId,
        String name,
        String address,
        String zipcode,
        String latitude,
        String longitude,
        Integer capacity,
        @Json.Property("image_path") String imagePath,
        @Json.Property("city_name") String cityName,
        String surface,
        @Json.Property("national_team") Boolean nationalTeam) {
}
