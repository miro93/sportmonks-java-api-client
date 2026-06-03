package io.github.miro93.sportmonks.football.model;

/// A match referee from the SportMonks football API. {@code id} is always
/// present; every other field may be {@code null}. {@code cityId} is typed as
/// {@code String} because the API returns {@code city_id} as a string value
/// (same as {@link Coach}).
public record Referee(
        long id,
        Long sportId,
        Long countryId,
        String cityId,
        String commonName,
        String firstname,
        String lastname,
        String name,
        String displayName,
        String imagePath,
        Integer height,
        Integer weight,
        String dateOfBirth,
        String gender) {
}
