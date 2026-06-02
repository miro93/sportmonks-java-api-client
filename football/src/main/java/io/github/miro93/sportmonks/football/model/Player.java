package io.github.miro93.sportmonks.football.model;

/// A football player. {@code id} is always present. Foreign-key {@code Long}
/// fields ({@code sportId}, {@code countryId}, {@code nationalityId},
/// {@code positionId}, {@code detailedPositionId}, {@code typeId}) and optional
/// scalars ({@code cityId}, {@code commonName}, {@code firstname}, {@code lastname},
/// {@code name}, {@code displayName}, {@code imagePath}, {@code height},
/// {@code weight}, {@code dateOfBirth}, {@code gender}) may be {@code null}.
/// Note: {@code cityId} is typed as {@code String} because the API returns
/// {@code city_id} as a string value. No relation fields are exposed on this record.
public record Player(
        long id,
        Long sportId,
        Long countryId,
        Long nationalityId,
        String cityId,
        Long positionId,
        Long detailedPositionId,
        Long typeId,
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
