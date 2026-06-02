package io.github.miro93.sportmonks.football.model;

import java.util.List;

/// A competition stage (e.g. group stage, knockout round). {@code id} is always
/// present. Foreign-key {@code Long} fields ({@code sportId}, {@code leagueId},
/// {@code seasonId}, {@code typeId}) and other optional scalars ({@code sortOrder},
/// {@code startingAt}, {@code endingAt}) may be {@code null}. Boolean flags and
/// {@code gamesInCurrentWeek} may also be {@code null}. The relation field
/// ({@code rounds}) is {@code null} unless requested via includes.
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
