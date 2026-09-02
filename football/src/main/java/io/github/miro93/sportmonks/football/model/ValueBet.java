package io.github.miro93.sportmonks.football.model;

import io.helidon.json.binding.Json;

/// A value-bet record from the SportMonks predictions feed (value-bets
/// endpoints). {@code id} is always present; every other field may be
/// {@code null}. Unlike {@link Prediction}, the {@code predictions} payload has
/// a stable shape and is typed as {@link ValueBetPrediction}.
@Json.Entity
public record ValueBet(
        long id,
        @Json.Property("fixture_id") Long fixtureId,
        @Json.Property("type_id") Long typeId,
        ValueBetPrediction predictions) {
}
