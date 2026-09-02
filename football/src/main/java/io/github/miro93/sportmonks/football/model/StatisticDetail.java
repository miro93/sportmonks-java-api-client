package io.github.miro93.sportmonks.football.model;

import io.helidon.json.JsonValue;
import io.helidon.json.binding.Json;

import java.util.Map;

/// A single detail entry inside a {@link Statistic}. {@code id} is always
/// present; every other field may be {@code null}. {@code value} is a free-form
/// object whose keys depend on {@code typeId} (e.g. {@code {total}},
/// {@code {home,away}}, {@code {average,highest,lowest}}); numeric values decode
/// as {@code Integer} when whole, {@code Double} when they have a fractional
/// part.
///
/// `value` is decoded via {@link FreeFormJson} from a raw {@link JsonValue}
/// component rather than a plain {@code Map<String, Object>} — see that class's
/// javadoc for why (a Helidon 4.5.4 bug, shared by three other records in this
/// package).
@Json.Entity
public record StatisticDetail(
        long id,
        @Json.Property("type_id") Long typeId,
        @Json.Property("value") JsonValue rawValue) {

    /// The decoded {@code value} payload; {@code null} if the field was absent.
    public Map<String, Object> value() {
        return FreeFormJson.toMap(rawValue);
    }
}
