package io.github.miro93.sportmonks.football.model;

/// Per-fixture metadata attached to a participant when included via a fixture.
public record ParticipantMeta(String location, Boolean winner, Integer position) {
}
