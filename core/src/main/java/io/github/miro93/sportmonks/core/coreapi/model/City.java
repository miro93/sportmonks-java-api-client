package io.github.miro93.sportmonks.core.coreapi.model;

/// A city from the SportMonks Core API. {@code id} is always present; every
/// other field may be {@code null}: {@code countryId}, {@code region} (the
/// region id — the API names this field {@code region}, not {@code region_id}),
/// {@code name}, the geo coordinates {@code latitude}/{@code longitude}
/// (returned as strings) and {@code geonameid}.
public record City(
        long id,
        Long countryId,
        Long region,
        String name,
        String latitude,
        String longitude,
        Long geonameid) {
}
