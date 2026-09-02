package io.github.miro93.sportmonks.football.model;

import io.helidon.json.JsonObject;
import io.helidon.json.JsonValue;
import io.helidon.json.binding.Json;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/// A single detail entry inside a {@link Statistic}. {@code id} is always
/// present; every other field may be {@code null}. {@code value} is a free-form
/// object whose keys depend on {@code typeId} (e.g. {@code {total}},
/// {@code {home,away}}, {@code {average,highest,lowest}}); numeric values decode
/// as {@code Integer} when whole, {@code Double} when they have a fractional
/// part.
///
/// ponytail: `value` is decoded by hand from the raw {@link JsonValue} tree
/// instead of letting Helidon bind straight to {@code Map<String, Object>}.
/// Helidon 4.5.4's `Object`-typed (and `Double`-typed) map-value converter
/// mis-parses a bare (no decimal point) JSON integer literal that isn't the
/// object's last entry — confirmed with {@code {"home":5,"away":7}}, which is
/// exactly this field's shape. Narrowing to {@code Map<String, Integer>} was
/// tried and rejected: it doesn't throw on a decimal value, it silently
/// truncates it (`2.5` -> `2`), which is data corruption, not a workaround.
/// {@code Map<String, Double>} was also tried and still hits the same
/// non-last-bare-integer bug. Revert to a plain {@code Map<String, Object>}
/// component once the upstream bug is fixed.
@Json.Entity
public record StatisticDetail(
        long id,
        @Json.Property("type_id") Long typeId,
        @Json.Property("value") JsonValue rawValue) {

    /// The decoded {@code value} payload; {@code null} if the field was absent.
    public Map<String, Object> value() {
        return rawValue == null ? null : toMap(rawValue.asObject());
    }

    private static Map<String, Object> toMap(JsonObject object) {
        Map<String, Object> result = new LinkedHashMap<>();
        object.keysAsStrings().forEach(key -> result.put(key, toObject(object.value(key).orElseThrow())));
        return result;
    }

    private static Object toObject(JsonValue value) {
        return switch (value.type()) {
            case NUMBER -> toNumber(value.asNumber().bigDecimalValue());
            case STRING -> value.asString().value();
            case BOOLEAN -> value.asBoolean().value();
            case OBJECT -> toMap(value.asObject());
            case ARRAY -> value.asArray().values().stream().map(StatisticDetail::toObject).toList();
            case NULL, UNKNOWN -> null;
        };
    }

    private static Object toNumber(BigDecimal value) {
        BigDecimal stripped = value.stripTrailingZeros();
        if (stripped.scale() > 0) {
            return stripped.doubleValue();
        }
        long asLong = stripped.longValueExact();
        return asLong == (int) asLong ? (Object) (int) asLong : (Object) asLong;
    }
}
