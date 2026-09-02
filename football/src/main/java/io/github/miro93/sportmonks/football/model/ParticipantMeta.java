package io.github.miro93.sportmonks.football.model;

import io.helidon.json.binding.Json;

/// Per-fixture metadata attached to a participant when included via a fixture.
@Json.Entity
public record ParticipantMeta(String location, Boolean winner, Integer position) {
}
