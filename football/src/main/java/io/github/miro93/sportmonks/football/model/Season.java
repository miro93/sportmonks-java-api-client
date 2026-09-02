package io.github.miro93.sportmonks.football.model;

import io.helidon.json.binding.Json;
import java.util.List;

/// A competition season. {@code id} is always present. Foreign-key {@code Long}
/// fields ({@code sportId}, {@code leagueId}, {@code tieBreakerRuleId}) and other
/// optional scalars ({@code startingAt}, {@code endingAt}, {@code standingsRecalculatedAt},
/// {@code standingMethod}) may be {@code null}. Boolean flags and
/// {@code gamesInCurrentWeek} may also be {@code null}. The relation fields
/// ({@code league}, {@code stages}) are {@code null} unless requested via includes.
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
        @Json.Property("standing_method") String standingMethod,
        League league,
        List<Stage> stages) {
}
