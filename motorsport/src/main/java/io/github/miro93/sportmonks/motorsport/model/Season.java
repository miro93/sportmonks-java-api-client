package io.github.miro93.sportmonks.motorsport.model;

import io.helidon.json.binding.Json;
import java.util.List;

/// A season of a league. {@code id} is always present; every other field may be
/// {@code null}. {@code finished}, {@code pending} and {@code isCurrent} are documented as
/// integers but decoded as booleans here when the API sends booleans.
@Json.Entity
public record Season(
        long id,
        @Json.Property("sport_id") Long sportId,
        @Json.Property("league_id") Long leagueId,
        @Json.Property("tie_breaker_rule_id") Long tieBreakerRuleId,
        String name,
        Boolean finished,
        Boolean pending,
        @Json.Property("is_current") Boolean isCurrent,
        @Json.Property("starting_at") String startingAt,
        @Json.Property("ending_at") String endingAt,
        @Json.Property("standings_recalculated_at") String standingsRecalculatedAt,
        @Json.Property("games_in_current_week") Boolean gamesInCurrentWeek,
        League league,
        List<Stage> stages,
        List<Team> teams,
        List<Fixture> fixtures) {
}
