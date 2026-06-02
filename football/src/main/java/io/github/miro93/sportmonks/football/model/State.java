package io.github.miro93.sportmonks.football.model;

/// The lifecycle state of a fixture (NS, INPLAY, FT, ...).
public record State(
        long id,
        String state,
        String name,
        String shortName,
        String developerName) {
}
