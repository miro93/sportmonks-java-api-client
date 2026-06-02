package io.github.miro93.sportmonks.football.model;

import java.util.List;

/// A competition round within a stage. Scalar fields ({@code id} through
/// {@code endingAt}) are always present; {@code gamesInCurrentWeek} may be
/// {@code null}. The relation field ({@code fixtures}) is {@code null} unless
/// requested via includes.
public record Round(
        long id,
        Long sportId,
        Long leagueId,
        Long seasonId,
        Long stageId,
        Long groupId,
        String name,
        Boolean finished,
        Boolean pending,
        Boolean isCurrent,
        String startingAt,
        String endingAt,
        Boolean gamesInCurrentWeek,
        List<Fixture> fixtures) {
}
