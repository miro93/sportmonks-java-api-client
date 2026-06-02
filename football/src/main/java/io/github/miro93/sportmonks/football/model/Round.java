package io.github.miro93.sportmonks.football.model;

import java.util.List;

/// A competition round within a stage. {@code id} is always present. Foreign-key
/// {@code Long} fields ({@code sportId}, {@code leagueId}, {@code seasonId},
/// {@code stageId}, {@code groupId}) and other optional scalars ({@code startingAt},
/// {@code endingAt}) may be {@code null}. Boolean flags and
/// {@code gamesInCurrentWeek} may also be {@code null}. The relation field
/// ({@code fixtures}) is {@code null} unless requested via includes.
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
