package io.github.miro93.sportmonks.football.model;

import io.helidon.json.JsonValue;
import io.helidon.json.binding.Json;

import java.util.Map;

/// A league-level predictability record from the SportMonks predictions feed
/// (predictability endpoint). {@code id} is always present; every other field
/// may be {@code null}. The variable payload is league-scoped and lives under
/// the {@code data} key (not {@code predictions}); it maps market names to
/// reliability metrics; numeric values decode as {@code Integer} when whole,
/// {@code Double} when they have a fractional part.
///
/// `data` is decoded via {@link FreeFormJson} from a raw {@link JsonValue}
/// component rather than a plain {@code Map<String, Object>} — see that class's
/// javadoc for why (a Helidon 4.5.4 bug, shared by three other records in this
/// package).
@Json.Entity
public record Predictability(
        long id,
        @Json.Property("league_id") Long leagueId,
        @Json.Property("type_id") Long typeId,
        @Json.Property("data") JsonValue rawData) {

    /// The decoded {@code data} payload; {@code null} if the field was absent.
    public Map<String, Object> data() {
        return FreeFormJson.toMap(rawData);
    }
}
