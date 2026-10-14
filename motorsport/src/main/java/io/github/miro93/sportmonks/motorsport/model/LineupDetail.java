package io.github.miro93.sportmonks.motorsport.model;

import io.helidon.json.JsonValue;
import io.helidon.json.binding.Json;
import java.util.Map;

/// A typed detail row of a {@link Lineup} (finishing position, points, fastest lap, ...).
/// {@code data} is a free-form object whose keys depend on {@code typeId}, decoded via
/// {@link FreeFormJson}.
@Json.Entity
public record LineupDetail(
        Long id,
        @Json.Property("fixture_id") Long fixtureId,
        @Json.Property("player_id") Long playerId,
        @Json.Property("team_id") Long teamId,
        @Json.Property("lineup_id") Long lineupId,
        @Json.Property("type_id") Long typeId,
        @Json.Property("data") JsonValue rawData) {

    /// The decoded {@code data} payload; {@code null} if the field was absent.
    public Map<String, Object> data() {
        return FreeFormJson.toMap(rawData);
    }
}
