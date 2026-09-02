package io.github.miro93.sportmonks.football.model;

import io.helidon.json.binding.Json;

/// A squad member (player currently or historically associated with a team).
/// {@code id} is always present. Foreign-key {@code Long} fields
/// ({@code transferId}, {@code playerId}, {@code teamId}, {@code positionId},
/// {@code detailedPositionId}) and optional scalars ({@code jerseyNumber},
/// {@code start}, {@code end}) may be {@code null}. The relation field
/// ({@code player}) is {@code null} unless requested via includes.
@Json.Entity
public record Squad(
        long id,
        @Json.Property("transfer_id") Long transferId,
        @Json.Property("player_id") Long playerId,
        @Json.Property("team_id") Long teamId,
        @Json.Property("position_id") Long positionId,
        @Json.Property("detailed_position_id") Long detailedPositionId,
        @Json.Property("jersey_number") Integer jerseyNumber,
        String start,
        String end,
        Player player) {
}
