package io.github.miro93.sportmonks.football.model;

import io.helidon.json.binding.Json;

import java.util.List;

/// One knockout stage of a {@link Bracket} with its fixtures.
@Json.Entity
public record BracketStage(
        @Json.Property("stage_id") Long stageId,
        @Json.Property("stage_name") String stageName,
        List<Fixture> fixtures) {
}
