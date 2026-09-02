package io.github.miro93.sportmonks.football.model;

import io.helidon.json.binding.Json;

/// A score line for a fixture (e.g. CURRENT, HT, FT) for one participant.
@Json.Entity
public record Score(
        long id,
        @Json.Property("fixture_id") Long fixtureId,
        @Json.Property("type_id") Long typeId,
        @Json.Property("participant_id") Long participantId,
        ScoreDetail score,
        String description) {
}
