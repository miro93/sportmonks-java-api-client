package io.github.miro93.sportmonks.football.model;

import io.helidon.json.binding.Json;

/// A player transfer between clubs. {@code id} is always present. Foreign-key
/// {@code Long} fields ({@code sportId}, {@code playerId}, {@code typeId},
/// {@code fromTeamId}, {@code toTeamId}, {@code positionId},
/// {@code detailedPositionId}) and optional scalars ({@code date},
/// {@code careerEnded}, {@code completed}, {@code amount}, {@code completedAt})
/// may be {@code null}. The relation fields ({@code player}, {@code fromTeam},
/// {@code toTeam}) are {@code null} unless requested via includes.
@Json.Entity
public record Transfer(
        long id,
        @Json.Property("sport_id") Long sportId,
        @Json.Property("player_id") Long playerId,
        @Json.Property("type_id") Long typeId,
        @Json.Property("from_team_id") Long fromTeamId,
        @Json.Property("to_team_id") Long toTeamId,
        @Json.Property("position_id") Long positionId,
        @Json.Property("detailed_position_id") Long detailedPositionId,
        String date,
        @Json.Property("career_ended") Boolean careerEnded,
        Boolean completed,
        String amount,
        @Json.Property("completed_at") String completedAt,
        Player player,
        @Json.Property("from_team") Team fromTeam,
        @Json.Property("to_team") Team toTeam) {
}
