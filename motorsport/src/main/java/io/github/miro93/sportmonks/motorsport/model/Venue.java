package io.github.miro93.sportmonks.motorsport.model;

import io.helidon.json.binding.Json;
import java.util.List;

/// A track. {@code id} is always present; every other field may be {@code null}.
/// {@code nationalTeam} is unused by the Motorsport API.
@Json.Entity
public record Venue(
        long id,
        @Json.Property("country_id") Long countryId,
        @Json.Property("city_id") Long cityId,
        String name,
        String address,
        String zipcode,
        String latitude,
        String longitude,
        Integer capacity,
        @Json.Property("image_path") String imagePath,
        @Json.Property("city_name") String cityName,
        String surface,
        @Json.Property("national_team") Boolean nationalTeam,
        List<Fixture> fixtures) {
}
