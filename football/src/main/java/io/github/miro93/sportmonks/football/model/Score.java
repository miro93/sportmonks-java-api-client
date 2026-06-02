package io.github.miro93.sportmonks.football.model;

/// A score line for a fixture (e.g. CURRENT, HT, FT) for one participant.
public record Score(
        long id,
        Long fixtureId,
        Long typeId,
        Long participantId,
        ScoreDetail score,
        String description) {
}
