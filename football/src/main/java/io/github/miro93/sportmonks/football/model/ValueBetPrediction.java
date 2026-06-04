package io.github.miro93.sportmonks.football.model;

/// The typed payload of a {@link ValueBet}: a single value-bet recommendation.
/// Every field may be {@code null}. {@code odd}, {@code stake} and
/// {@code fairOdd} are {@code String} because the API returns these
/// numeric-looking values as strings (project numbers-as-strings convention).
public record ValueBetPrediction(
        String bet,
        String bookmaker,
        String odd,
        Boolean isValue,
        String stake,
        String fairOdd) {
}
