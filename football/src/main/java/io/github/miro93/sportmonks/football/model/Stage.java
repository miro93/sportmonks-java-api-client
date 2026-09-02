package io.github.miro93.sportmonks.football.model;

import io.helidon.json.binding.Json;
import java.util.List;

/// A competition stage (e.g. group stage, knockout round). {@code id} is always
/// present. Foreign-key {@code Long} fields ({@code sportId}, {@code leagueId},
/// {@code seasonId}, {@code typeId}) and other optional scalars ({@code sortOrder},
/// {@code startingAt}, {@code endingAt}) may be {@code null}. Boolean flags and
/// {@code gamesInCurrentWeek} may also be {@code null}. The relation field
/// ({@code rounds}) is {@code null} unless requested via includes.
@Json.Entity
public record Stage(
        long id,
        @Json.Property("sport_id") Long sportId,
        @Json.Property("league_id") Long leagueId,
        @Json.Property("season_id") Long seasonId,
        @Json.Property("type_id") Long typeId,
        String name,
        @Json.Property("sort_order") Integer sortOrder,
        Boolean finished,
        Boolean pending,
        @Json.Property("is_current") Boolean isCurrent,
        @Json.Property("starting_at") String startingAt,
        @Json.Property("ending_at") String endingAt,
        @Json.Property("games_in_current_week") Boolean gamesInCurrentWeek,
        List<Round> rounds) {
}
