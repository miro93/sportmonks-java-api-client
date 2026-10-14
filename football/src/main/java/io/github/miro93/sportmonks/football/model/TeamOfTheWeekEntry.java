package io.github.miro93.sportmonks.football.model;

import io.helidon.json.binding.Json;

/// One player slot in a team of the week (beta). {@code id} is always present; every
/// other scalar may be {@code null}. {@code rating} is returned as a string by the API.
/// The relation fields ({@code player}, {@code team}, {@code fixture}, {@code round})
/// are {@code null} unless requested via includes.
@Json.Entity
public record TeamOfTheWeekEntry(
        long id,
        @Json.Property("player_id") Long playerId,
        @Json.Property("fixture_id") Long fixtureId,
        @Json.Property("round_id") Long roundId,
        @Json.Property("team_id") Long teamId,
        String rating,
        @Json.Property("formation_position") Integer formationPosition,
        String formation,
        Player player,
        Team team,
        Fixture fixture,
        Round round) {
}
