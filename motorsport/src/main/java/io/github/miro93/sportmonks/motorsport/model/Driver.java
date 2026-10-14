package io.github.miro93.sportmonks.motorsport.model;

import io.helidon.json.binding.Json;
import java.util.List;

/// A motorsport driver. {@code id} is always present; every other field may be
/// {@code null}. {@code detailedPositionId} and {@code typeId} are unused by the API.
@Json.Entity
public record Driver(
        long id,
        @Json.Property("sport_id") Long sportId,
        @Json.Property("country_id") Long countryId,
        @Json.Property("nationality_id") Long nationalityId,
        @Json.Property("city_id") Long cityId,
        @Json.Property("position_id") Long positionId,
        @Json.Property("detailed_position_id") Long detailedPositionId,
        @Json.Property("type_id") Long typeId,
        @Json.Property("common_name") String commonName,
        String firstname,
        String lastname,
        String name,
        @Json.Property("display_name") String displayName,
        @Json.Property("image_path") String imagePath,
        Integer height,
        Integer weight,
        @Json.Property("date_of_birth") String dateOfBirth,
        String gender,
        List<Team> teams) {
}
