package io.github.miro93.sportmonks.football.model;

import io.helidon.json.binding.Json;
import java.util.List;

/// A competition round within a stage. {@code id} is always present. Foreign-key
/// {@code Long} fields ({@code sportId}, {@code leagueId}, {@code seasonId},
/// {@code stageId}, {@code groupId}) and other optional scalars ({@code startingAt},
/// {@code endingAt}) may be {@code null}. Boolean flags and
/// {@code gamesInCurrentWeek} may also be {@code null}. The relation field
/// ({@code fixtures}) is {@code null} unless requested via includes.
@Json.Entity
public record Round(
        long id,
        @Json.Property("sport_id") Long sportId,
        @Json.Property("league_id") Long leagueId,
        @Json.Property("season_id") Long seasonId,
        @Json.Property("stage_id") Long stageId,
        @Json.Property("group_id") Long groupId,
        String name,
        Boolean finished,
        Boolean pending,
        @Json.Property("is_current") Boolean isCurrent,
        @Json.Property("starting_at") String startingAt,
        @Json.Property("ending_at") String endingAt,
        @Json.Property("games_in_current_week") Boolean gamesInCurrentWeek,
        List<Fixture> fixtures) {
}
