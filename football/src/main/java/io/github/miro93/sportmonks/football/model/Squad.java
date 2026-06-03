package io.github.miro93.sportmonks.football.model;

/// A squad member (player currently or historically associated with a team).
/// {@code id} is always present. Foreign-key {@code Long} fields
/// ({@code transferId}, {@code playerId}, {@code teamId}, {@code positionId},
/// {@code detailedPositionId}) and optional scalars ({@code jerseyNumber},
/// {@code start}, {@code end}) may be {@code null}. The relation field
/// ({@code player}) is {@code null} unless requested via includes.
public record Squad(
        long id,
        Long transferId,
        Long playerId,
        Long teamId,
        Long positionId,
        Long detailedPositionId,
        Integer jerseyNumber,
        String start,
        String end,
        Player player) {
}
