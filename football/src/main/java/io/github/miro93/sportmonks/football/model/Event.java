package io.github.miro93.sportmonks.football.model;

/// A single in-match event (goal, card, substitution, ...).
public record Event(
        long id,
        Long fixtureId,
        Long periodId,
        Long participantId,
        Long typeId,
        Long subTypeId,
        Long playerId,
        Long relatedPlayerId,
        String playerName,
        String relatedPlayerName,
        String result,
        String info,
        String addition,
        Integer minute,
        Integer extraMinute,
        Boolean injured,
        Boolean onBench,
        Long coachId,
        Integer sortOrder) {
}
