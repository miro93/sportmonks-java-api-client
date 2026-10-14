package io.github.miro93.sportmonks.motorsport.model;

import io.helidon.json.binding.Json;

/// A session state (not started, live, finished, ...). {@code id} is always present.
@Json.Entity
public record State(
        long id,
        String state,
        String name,
        @Json.Property("short_name") String shortName,
        @Json.Property("developer_name") String developerName) {
}
