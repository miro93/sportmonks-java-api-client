package io.github.miro93.sportmonks.core.coreapi.model;

/// A country from the SportMonks Core API. {@code id} is always present; every
/// other field may be {@code null}: the {@code continentId} foreign key, the
/// names ({@code name}, {@code officialName}, {@code fifaName}), the ISO codes
/// ({@code iso2}, {@code iso3}), the geo coordinates {@code latitude}/
/// {@code longitude} (returned as strings by the API), {@code geonameid},
/// {@code borders} and {@code imagePath}.
public record Country(
        long id,
        Long continentId,
        String name,
        String officialName,
        String fifaName,
        String iso2,
        String iso3,
        String latitude,
        String longitude,
        Long geonameid,
        String borders,
        String imagePath) {
}
