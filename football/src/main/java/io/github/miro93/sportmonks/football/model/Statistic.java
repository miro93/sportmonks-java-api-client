package io.github.miro93.sportmonks.football.model;

import java.util.List;

/// A statistics record from the SportMonks statistics endpoints (season by
/// participant, stage, round). A single unified envelope: the participant ids
/// ({@code playerId}/{@code coachId}/{@code teamId}/{@code refereeId}) and scope
/// ids ({@code seasonId}/{@code stageId}/{@code roundId}) that do not apply to
/// the requested context are {@code null}. {@code id} is always present; every
/// other field may be {@code null}. {@code details} carries the individual
/// statistic values.
public record Statistic(
        long id,
        Long playerId,
        Long coachId,
        Long teamId,
        Long refereeId,
        Long seasonId,
        Long stageId,
        Long roundId,
        Boolean hasValues,
        Long positionId,
        Integer jerseyNumber,
        List<StatisticDetail> details) {
}
