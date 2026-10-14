package io.github.miro93.sportmonks.motorsport.model;

import io.helidon.json.binding.Json;
import java.util.List;

/// A driver entry of a session (grid position, car number) with its typed detail rows.
/// {@code id} is always present; every other field may be {@code null}.
@Json.Entity
public record Lineup(
        long id,
        @Json.Property("sport_id") Long sportId,
        @Json.Property("fixture_id") Long fixtureId,
        @Json.Property("player_id") Long playerId,
        @Json.Property("team_id") Long teamId,
        @Json.Property("position_id") Long positionId,
        @Json.Property("type_id") Long typeId,
        @Json.Property("grid_position") Integer gridPosition,
        @Json.Property("driver_name") String driverName,
        @Json.Property("driver_number") Integer driverNumber,
        List<LineupDetail> details) {
}
