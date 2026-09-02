package io.github.miro93.sportmonks.football.model;

import io.helidon.json.binding.Json;

/// A single premium expected-lineup entry from the SportMonks football API: the
/// predicted placement of one player in a fixture's lineup, available before the
/// official lineup is published. {@code id} is always present; every other field
/// may be {@code null}. {@code formationField} and {@code formationPosition} are
/// {@code String} because the API returns these placement markers as strings.
@Json.Entity
public record ExpectedLineup(
        long id,
        @Json.Property("sport_id") Long sportId,
        @Json.Property("fixture_id") Long fixtureId,
        @Json.Property("player_id") Long playerId,
        @Json.Property("team_id") Long teamId,
        @Json.Property("type_id") Long typeId,
        @Json.Property("player_name") String playerName,
        @Json.Property("jersey_number") Integer jerseyNumber,
        @Json.Property("position_id") Long positionId,
        @Json.Property("detailed_position_id") Long detailedPositionId,
        @Json.Property("formation_field") String formationField,
        @Json.Property("formation_position") String formationPosition) {
}
