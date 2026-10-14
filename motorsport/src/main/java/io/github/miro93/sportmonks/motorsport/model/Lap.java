package io.github.miro93.sportmonks.motorsport.model;

import io.helidon.json.binding.Json;

/// One lap of one driver in a session. Every field may be {@code null}.
@Json.Entity
public record Lap(
        Long id,
        @Json.Property("fixture_id") Long fixtureId,
        @Json.Property("lap_number") Integer lapNumber,
        @Json.Property("driver_number") Integer driverNumber,
        @Json.Property("participant_id") Long participantId,
        @Json.Property("is_latest") Boolean isLatest,
        Fixture fixture) {
}
