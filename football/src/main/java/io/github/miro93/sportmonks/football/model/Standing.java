package io.github.miro93.sportmonks.football.model;

import java.util.List;

/// A league/stage standing entry for a participant. {@code id} is always present.
/// Foreign-key {@code Long} fields ({@code participantId}, {@code sportId},
/// {@code leagueId}, {@code seasonId}, {@code stageId}, {@code groupId},
/// {@code roundId}, {@code standingRuleId}) and optional scalars
/// ({@code position}, {@code result}, {@code points}) may be {@code null}.
/// The relation fields ({@code participant}, {@code details}) are {@code null}
/// unless requested via includes.
public record Standing(
        long id,
        Long participantId,
        Long sportId,
        Long leagueId,
        Long seasonId,
        Long stageId,
        Long groupId,
        Long roundId,
        Long standingRuleId,
        Integer position,
        String result,
        Integer points,
        Team participant,
        List<StandingDetail> details) {
}
