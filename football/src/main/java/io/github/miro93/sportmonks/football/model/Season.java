package io.github.miro93.sportmonks.football.model;

import java.util.List;

/// A competition season. {@code id} is always present. Foreign-key {@code Long}
/// fields ({@code sportId}, {@code leagueId}, {@code tieBreakerRuleId}) and other
/// optional scalars ({@code startingAt}, {@code endingAt}, {@code standingsRecalculatedAt},
/// {@code standingMethod}) may be {@code null}. Boolean flags and
/// {@code gamesInCurrentWeek} may also be {@code null}. The relation fields
/// ({@code league}, {@code stages}) are {@code null} unless requested via includes.
public record Season(
        long id,
        Long sportId,
        Long leagueId,
        Long tieBreakerRuleId,
        String name,
        Boolean finished,
        Boolean pending,
        Boolean isCurrent,
        String startingAt,
        String endingAt,
        String standingsRecalculatedAt,
        Boolean gamesInCurrentWeek,
        String standingMethod,
        League league,
        List<Stage> stages) {
}
