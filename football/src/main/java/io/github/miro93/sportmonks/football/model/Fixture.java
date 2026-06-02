package io.github.miro93.sportmonks.football.model;

import java.util.List;

/// A football fixture. Scalar fields are always present; the relation fields
/// ({@code participants}, {@code scores}, {@code state}, {@code events}) are
/// {@code null} unless requested via includes.
public record Fixture(
        long id,
        Long sportId,
        Long leagueId,
        Long seasonId,
        Long stageId,
        Long groupId,
        Long aggregateId,
        Long roundId,
        Integer stateId,
        Long venueId,
        String name,
        String startingAt,
        String resultInfo,
        String leg,
        String details,
        Integer length,
        boolean placeholder,
        boolean hasOdds,
        Long startingAtTimestamp,
        List<Participant> participants,
        List<Score> scores,
        State state,
        List<Event> events) {
}
