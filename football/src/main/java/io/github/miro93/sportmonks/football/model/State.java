package io.github.miro93.sportmonks.football.model;

import io.helidon.json.binding.Json;

/// The lifecycle state of a fixture (NS, INPLAY, FT, ...).
@Json.Entity
public record State(
        long id,
        String state,
        String name,
        @Json.Property("short_name") String shortName,
        @Json.Property("developer_name") String developerName) {
}
