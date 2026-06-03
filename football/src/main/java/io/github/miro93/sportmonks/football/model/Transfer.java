package io.github.miro93.sportmonks.football.model;

/// A player transfer between clubs. {@code id} is always present. Foreign-key
/// {@code Long} fields ({@code sportId}, {@code playerId}, {@code typeId},
/// {@code fromTeamId}, {@code toTeamId}, {@code positionId},
/// {@code detailedPositionId}) and optional scalars ({@code date},
/// {@code careerEnded}, {@code completed}, {@code amount}, {@code completedAt})
/// may be {@code null}. The relation fields ({@code player}, {@code fromTeam},
/// {@code toTeam}) are {@code null} unless requested via includes.
public record Transfer(
        long id,
        Long sportId,
        Long playerId,
        Long typeId,
        Long fromTeamId,
        Long toTeamId,
        Long positionId,
        Long detailedPositionId,
        String date,
        Boolean careerEnded,
        Boolean completed,
        String amount,
        String completedAt,
        Player player,
        Team fromTeam,
        Team toTeam) {
}
