package io.github.miro93.sportmonks.football.model;

/// A betting bookmaker from the SportMonks football API. {@code id} is always
/// present; {@code legacyId} and {@code name} may be {@code null}.
public record Bookmaker(
        long id,
        Long legacyId,
        String name) {
}
