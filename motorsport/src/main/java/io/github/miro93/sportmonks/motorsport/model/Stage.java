package io.github.miro93.sportmonks.motorsport.model;

import io.helidon.json.binding.Json;
import java.util.List;

/// A race weekend (Grand Prix) of a season. {@code id} is always present; every other
/// field may be {@code null}. {@code fixtures} is populated by the schedule and
/// race-results endpoints and by the {@code fixtures} include.
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
        @Json.Property("is_current") Boolean isCurrent,
        @Json.Property("starting_at") String startingAt,
        @Json.Property("ending_at") String endingAt,
        @Json.Property("games_in_current_week") Boolean gamesInCurrentWeek,
        @Json.Property("tie_breaker_rule_id") Long tieBreakerRuleId,
        League league,
        Season season,
        List<Fixture> fixtures) {
}
