package io.github.miro93.sportmonks.motorsport.model;

import io.helidon.json.binding.Json;
import java.util.List;

/// A motorsport session (practice, qualifying, sprint, race). {@code id} is always
/// present; the relation fields are {@code null} unless requested via includes
/// ({@code lineups} is also populated by the race-results endpoints). {@code groupId},
/// {@code aggregateId}, {@code roundId}, {@code hasOdds} and {@code hasPremiumOdds} are
/// unused by the Motorsport API.
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
        @Json.Property("has_premium_odds") boolean hasPremiumOdds,
        @Json.Property("starting_at_timestamp") Long startingAtTimestamp,
        League league,
        Season season,
        Stage stage,
        State state,
        Venue venue,
        List<Team> participants,
        List<Lineup> lineups,
        List<Lap> laps,
        List<Pitstop> pitstops,
        List<Stint> stints) {
}
