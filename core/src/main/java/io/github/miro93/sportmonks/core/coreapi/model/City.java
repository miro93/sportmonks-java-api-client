package io.github.miro93.sportmonks.core.coreapi.model;

import io.helidon.json.binding.Json;

/// A city from the SportMonks Core API. {@code id} is always present; every
/// other field may be {@code null}: {@code countryId}, {@code region} (the
/// region id — the API names this field {@code region}, not {@code region_id}),
/// {@code name}, the geo coordinates {@code latitude}/{@code longitude}
/// (returned as strings) and {@code geonameid}.
@Json.Entity
public record City(
        long id,
        @Json.Property("country_id") Long countryId,
        Long region,
        String name,
        String latitude,
        String longitude,
        Long geonameid) {
}
