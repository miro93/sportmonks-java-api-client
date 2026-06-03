package io.github.miro93.sportmonks.football.model;

/// A TV station from the SportMonks football API. {@code id} is always present;
/// {@code name}, {@code url} and {@code imagePath} may be {@code null}.
public record TvStation(
        long id,
        String name,
        String url,
        String imagePath) {
}
