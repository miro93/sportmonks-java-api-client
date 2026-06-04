package io.github.miro93.sportmonks.football.model;

/// A single premium expected-lineup entry from the SportMonks football API: the
/// predicted placement of one player in a fixture's lineup, available before the
/// official lineup is published. {@code id} is always present; every other field
/// may be {@code null}. {@code formationField} and {@code formationPosition} are
/// {@code String} because the API returns these placement markers as strings.
public record ExpectedLineup(
        long id,
        Long sportId,
        Long fixtureId,
        Long playerId,
        Long teamId,
        Long typeId,
        String playerName,
        Integer jerseyNumber,
        Long positionId,
        Long detailedPositionId,
        String formationField,
        String formationPosition) {
}
