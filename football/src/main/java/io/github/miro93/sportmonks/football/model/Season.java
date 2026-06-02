package io.github.miro93.sportmonks.football.model;

import java.util.List;

/// A competition season. Scalar fields ({@code id} through {@code standingMethod})
/// are always present. The relation fields ({@code league}, {@code stages}) are
/// {@code null} unless requested via includes.
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
