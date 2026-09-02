package io.github.miro93.sportmonks.football.model;

import io.helidon.json.binding.Json;

/// A betting bookmaker from the SportMonks football API. {@code id} is always
/// present; {@code legacyId} and {@code name} may be {@code null}.
@Json.Entity
public record Bookmaker(
        long id,
        @Json.Property("legacy_id") Long legacyId,
        String name) {
}
