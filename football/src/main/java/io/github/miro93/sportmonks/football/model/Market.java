package io.github.miro93.sportmonks.football.model;

import io.helidon.json.binding.Json;

/// A betting market (e.g. Fulltime Result) from the SportMonks football API.
/// {@code id} is always present; {@code legacyId}, {@code name},
/// {@code developerName} and {@code hasWinningCalculations} may be {@code null}.
@Json.Entity
public record Market(
        long id,
        @Json.Property("legacy_id") Long legacyId,
        String name,
        @Json.Property("developer_name") String developerName,
        @Json.Property("has_winning_calculations") Boolean hasWinningCalculations) {
}
