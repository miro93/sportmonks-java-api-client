package io.github.miro93.sportmonks.football.model;

import io.helidon.json.JsonValue;
import io.helidon.json.binding.Json;

import java.util.Map;

/// A predicted-probability record from the SportMonks predictions feed
/// (probabilities endpoints). {@code id} is always present; every other field
/// may be {@code null}. {@code predictions} is a free-form object whose keys
/// depend on {@code typeId} (e.g. {@code {yes,no}}, {@code {home,draw,away}},
/// correct-score maps); numeric values decode as {@code Integer} when whole,
/// {@code Double} when they have a fractional part.
///
/// `predictions` is decoded via {@link FreeFormJson} from a raw {@link JsonValue}
/// component rather than a plain {@code Map<String, Object>} — see that class's
/// javadoc for why (a Helidon 4.5.4 bug, shared by three other records in this
/// package).
@Json.Entity
public record Prediction(
        long id,
        @Json.Property("fixture_id") Long fixtureId,
        @Json.Property("type_id") Long typeId,
        @Json.Property("predictions") JsonValue rawPredictions) {

    /// The decoded {@code predictions} payload; {@code null} if the field was absent.
    public Map<String, Object> predictions() {
        return FreeFormJson.toMap(rawPredictions);
    }
}
