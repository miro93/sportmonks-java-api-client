package io.github.miro93.sportmonks.football.model;

import io.helidon.json.binding.Json;

/// The typed payload of a {@link ValueBet}: a single value-bet recommendation.
/// Every field may be {@code null}. {@code odd}, {@code stake} and
/// {@code fairOdd} are {@code String} because the API returns these
/// numeric-looking values as strings (project numbers-as-strings convention).
@Json.Entity
public record ValueBetPrediction(
        String bet,
        String bookmaker,
        String odd,
        @Json.Property("is_value") Boolean isValue,
        String stake,
        @Json.Property("fair_odd") String fairOdd) {
}
