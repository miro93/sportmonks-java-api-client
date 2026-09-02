package io.github.miro93.sportmonks.football.model;

import io.helidon.json.binding.Json;

/// A single detail entry within a standing (e.g. goals scored, matches played).
/// {@code id} is always present. All other fields ({@code standingType},
/// {@code standingId}, {@code typeId}, {@code value}) may be {@code null}.
@Json.Entity
public record StandingDetail(
        long id,
        @Json.Property("standing_type") String standingType,
        @Json.Property("standing_id") Long standingId,
        @Json.Property("type_id") Long typeId,
        Integer value) {
}
