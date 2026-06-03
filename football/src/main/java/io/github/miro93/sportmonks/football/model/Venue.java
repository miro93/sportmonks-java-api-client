package io.github.miro93.sportmonks.football.model;

/// A venue (stadium) from the SportMonks football API. {@code id} is always
/// present; every other field may be {@code null}. {@code cityId} is typed as
/// {@code String} because the API returns {@code city_id} as a string value
/// (same as {@link Coach}); {@code latitude}/{@code longitude} are also strings.
public record Venue(
        long id,
        Long countryId,
        String cityId,
        String name,
        String address,
        String zipcode,
        String latitude,
        String longitude,
        Integer capacity,
        String imagePath,
        String cityName,
        String surface,
        Boolean nationalTeam) {
}
