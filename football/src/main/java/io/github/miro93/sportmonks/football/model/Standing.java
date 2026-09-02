package io.github.miro93.sportmonks.football.model;

import io.helidon.json.binding.Json;
import java.util.List;

/// A league/stage standing entry for a participant. {@code id} is always present.
/// Foreign-key {@code Long} fields ({@code participantId}, {@code sportId},
/// {@code leagueId}, {@code seasonId}, {@code stageId}, {@code groupId},
/// {@code roundId}, {@code standingRuleId}) and optional scalars
/// ({@code position}, {@code result}, {@code points}) may be {@code null}.
/// The relation fields ({@code participant}, {@code details}) are {@code null}
/// unless requested via includes.
@Json.Entity
public record Standing(
        long id,
        @Json.Property("participant_id") Long participantId,
        @Json.Property("sport_id") Long sportId,
        @Json.Property("league_id") Long leagueId,
        @Json.Property("season_id") Long seasonId,
        @Json.Property("stage_id") Long stageId,
        @Json.Property("group_id") Long groupId,
        @Json.Property("round_id") Long roundId,
        @Json.Property("standing_rule_id") Long standingRuleId,
        Integer position,
        String result,
        Integer points,
        Team participant,
        List<StandingDetail> details) {
}
