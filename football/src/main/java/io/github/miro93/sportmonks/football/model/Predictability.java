package io.github.miro93.sportmonks.football.model;

import io.helidon.json.binding.Json;
import java.util.Map;

/// A league-level predictability record from the SportMonks predictions feed
/// (predictability endpoint). {@code id} is always present; every other field
/// may be {@code null}. The variable payload is league-scoped and lives under
/// the {@code data} key (not {@code predictions}); it maps market names to
/// reliability metrics, exposed as a raw {@code Map<String, Object>}.
@Json.Entity
public record Predictability(
        long id,
        @Json.Property("league_id") Long leagueId,
        @Json.Property("type_id") Long typeId,
        Map<String, Object> data) {
}
