package io.github.miro93.sportmonks.football.model;

/// A top-scorer entry for a season/stage. {@code id} is always present.
/// Foreign-key {@code Long} fields ({@code seasonId}, {@code stageId},
/// {@code playerId}, {@code participantId}, {@code typeId}) and optional
/// scalars ({@code position}, {@code total}) may be {@code null}. The
/// relation fields ({@code player}, {@code participant}) are {@code null}
/// unless requested via includes.
public record Topscorer(
        long id,
        Long seasonId,
        Long stageId,
        Long playerId,
        Long participantId,
        Long typeId,
        Integer position,
        Integer total,
        Player player,
        Team participant) {
}
