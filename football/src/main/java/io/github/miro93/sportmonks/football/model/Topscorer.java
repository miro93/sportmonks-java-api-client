package io.github.miro93.sportmonks.football.model;

import io.helidon.json.binding.Json;

/// A top-scorer entry for a season/stage. {@code id} is always present.
/// Foreign-key {@code Long} fields ({@code seasonId}, {@code stageId},
/// {@code playerId}, {@code participantId}, {@code typeId}) and optional
/// scalars ({@code position}, {@code total}) may be {@code null}. The
/// relation fields ({@code player}, {@code participant}) are {@code null}
/// unless requested via includes.
@Json.Entity
public record Topscorer(
        long id,
        @Json.Property("season_id") Long seasonId,
        @Json.Property("stage_id") Long stageId,
        @Json.Property("player_id") Long playerId,
        @Json.Property("participant_id") Long participantId,
        @Json.Property("type_id") Long typeId,
        Integer position,
        Integer total,
        Player player,
        Team participant) {
}
