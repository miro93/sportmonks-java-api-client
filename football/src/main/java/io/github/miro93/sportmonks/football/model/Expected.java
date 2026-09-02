package io.github.miro93.sportmonks.football.model;

import io.helidon.json.JsonValue;
import io.helidon.json.binding.Json;

import java.util.Map;

/// An expected-goals (xG) record from the SportMonks expected endpoints
/// ({@code /expected/fixtures} for team-level, {@code /expected/lineups} for
/// player-level). {@code id} is always present; every other field may be
/// {@code null}. {@code location} is {@code "home"} or {@code "away"}.
/// {@code data} is a free-form object (typically {@code {value}}) whose shape
/// depends on {@code typeId}; numeric values decode as {@code Integer} when
/// whole, {@code Double} when they have a fractional part.
///
/// `data` is decoded via {@link FreeFormJson} from a raw {@link JsonValue}
/// component rather than a plain {@code Map<String, Object>} — see that class's
/// javadoc for why (a Helidon 4.5.4 bug, shared by three other records in this
/// package).
@Json.Entity
public record Expected(
        long id,
        @Json.Property("fixture_id") Long fixtureId,
        @Json.Property("type_id") Long typeId,
        @Json.Property("participant_id") Long participantId,
        String location,
        @Json.Property("data") JsonValue rawData) {

    /// The decoded {@code data} payload; {@code null} if the field was absent.
    public Map<String, Object> data() {
        return FreeFormJson.toMap(rawData);
    }
}
