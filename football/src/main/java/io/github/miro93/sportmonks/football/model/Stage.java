package io.github.miro93.sportmonks.football.model;

import java.util.List;

/// A competition stage (e.g. group stage, knockout round). Scalar fields
/// ({@code id} through {@code gamesInCurrentWeek}) are always present. The
/// relation field ({@code rounds}) is {@code null} unless requested via includes.
public record Stage(
        long id,
        Long sportId,
        Long leagueId,
        Long seasonId,
        Long typeId,
        String name,
        Integer sortOrder,
        Boolean finished,
        Boolean pending,
        Boolean isCurrent,
        String startingAt,
        String endingAt,
        Boolean gamesInCurrentWeek,
        List<Round> rounds) {
}
