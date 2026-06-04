package io.github.miro93.sportmonks.football.model;

/// A value-bet record from the SportMonks predictions feed (value-bets
/// endpoints). {@code id} is always present; every other field may be
/// {@code null}. Unlike {@link Prediction}, the {@code predictions} payload has
/// a stable shape and is typed as {@link ValueBetPrediction}.
public record ValueBet(
        long id,
        Long fixtureId,
        Long typeId,
        ValueBetPrediction predictions) {
}
