package io.github.miro93.sportmonks.football.model;

/// A betting market (e.g. Fulltime Result) from the SportMonks football API.
/// {@code id} is always present; {@code legacyId}, {@code name},
/// {@code developerName} and {@code hasWinningCalculations} may be {@code null}.
public record Market(
        long id,
        Long legacyId,
        String name,
        String developerName,
        Boolean hasWinningCalculations) {
}
