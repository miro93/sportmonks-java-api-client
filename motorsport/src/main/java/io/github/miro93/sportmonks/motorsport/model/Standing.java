package io.github.miro93.sportmonks.motorsport.model;

import io.helidon.json.binding.Json;

/// A driver or team championship standing. {@code id} is always present; every other
/// field may be {@code null}. {@code participantId} is a driver id on the driver
/// standings and a team id on the team standings.
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
        League league,
        Season season,
        Stage stage) {
}
