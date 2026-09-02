package io.github.miro93.sportmonks.football.model;

import io.helidon.json.binding.Json;
import java.util.List;

/// A football fixture. Scalar fields are always present; the relation fields
/// ({@code participants}, {@code scores}, {@code state}, {@code events}) are
/// {@code null} unless requested via includes.
@Json.Entity
public record Fixture(
        long id,
        @Json.Property("sport_id") Long sportId,
        @Json.Property("league_id") Long leagueId,
        @Json.Property("season_id") Long seasonId,
        @Json.Property("stage_id") Long stageId,
        @Json.Property("group_id") Long groupId,
        @Json.Property("aggregate_id") Long aggregateId,
        @Json.Property("round_id") Long roundId,
        @Json.Property("state_id") Integer stateId,
        @Json.Property("venue_id") Long venueId,
        String name,
        @Json.Property("starting_at") String startingAt,
        @Json.Property("result_info") String resultInfo,
        String leg,
        String details,
        Integer length,
        boolean placeholder,
        @Json.Property("has_odds") boolean hasOdds,
        @Json.Property("starting_at_timestamp") Long startingAtTimestamp,
        List<Participant> participants,
        List<Score> scores,
        State state,
        List<Event> events) {
}
