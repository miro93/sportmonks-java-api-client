package io.github.miro93.sportmonks.football.model;

import io.helidon.json.JsonObject;
import io.helidon.json.JsonValue;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/// Hand-rolled {@link JsonValue}-tree to {@code Map<String, Object>} conversion, shared by
/// every record in this package with a free-form JSON payload field ({@link StatisticDetail},
/// {@link Prediction}, {@link Expected}, {@link Predictability}).
///
/// ponytail: exists because Helidon 4.5.4's `Object`-typed (and `Double`-typed) map-value
/// converter mis-parses JSON integer literals. Precisely, via `JsonBinding.deserialize`: a
/// single-digit bare (no decimal point) integer literal throws even alone/as the object's only
/// or last entry (e.g. `{"home":5}`); multi-digit bare integers happen to parse without
/// throwing, but come back as `Double` (`33.0`), not `Integer` — either way, "numeric values
/// decode as Integer when whole, Double when fractional" (this class's actual, correct
/// behavior) does not hold for that converter. No upstream issue filed as of 2026-09 (searched
/// `helidon-io/helidon` for "map"/"JsonBinding Map"/"MapBindingFactory"/"integer"+"double"+json
/// — no match). Working around it by capturing the field as a raw {@link JsonValue} and walking
/// it here avoids the buggy converter entirely (it's never invoked). Delete this class and go
/// back to plain {@code Map<String, Object>} components if a later Helidon version fixes it.
final class FreeFormJson {

    private FreeFormJson() {
    }

    /// Converts a raw JSON object tree to {@code Map<String, Object>}; {@code null} if absent.
    static Map<String, Object> toMap(JsonValue raw) {
        return raw == null ? null : toMap(raw.asObject());
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
            case ARRAY -> value.asArray().values().stream().map(FreeFormJson::toObject).toList();
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
