package io.github.miro93.sportmonks.football.model;

import io.helidon.json.binding.Json;
import java.util.List;

/// A statistics record from the SportMonks statistics endpoints (season by
/// participant, stage, round). A single unified envelope: the participant ids
/// ({@code playerId}/{@code coachId}/{@code teamId}/{@code refereeId}) and scope
/// ids ({@code seasonId}/{@code stageId}/{@code roundId}) that do not apply to
/// the requested context are {@code null}. {@code id} is always present; every
/// other field may be {@code null}. {@code details} carries the individual
/// statistic values.
@Json.Entity
public record Statistic(
        long id,
        @Json.Property("player_id") Long playerId,
        @Json.Property("coach_id") Long coachId,
        @Json.Property("team_id") Long teamId,
        @Json.Property("referee_id") Long refereeId,
        @Json.Property("season_id") Long seasonId,
        @Json.Property("stage_id") Long stageId,
        @Json.Property("round_id") Long roundId,
        @Json.Property("has_values") Boolean hasValues,
        @Json.Property("position_id") Long positionId,
        @Json.Property("jersey_number") Integer jerseyNumber,
        List<StatisticDetail> details) {
}
