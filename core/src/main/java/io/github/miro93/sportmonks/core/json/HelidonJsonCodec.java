package io.github.miro93.sportmonks.core.json;

import io.helidon.common.GenericType;
import io.helidon.json.JsonException;
import io.helidon.json.binding.JsonBinding;
import io.github.miro93.sportmonks.core.response.ApiResponse;
import io.github.miro93.sportmonks.core.response.Pagination;
import io.github.miro93.sportmonks.core.response.RateLimit;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/// Wraps a Helidon JSON {@link JsonBinding} (compile-time binding, no reflection)
/// and decodes the SportMonks envelope into a typed {@link ApiResponse}.
/// snake_case mapping is declared per field via `@Json.Property` on the records.
///
/// The envelope members are located with a structural byte scan ({@link JsonTopLevel}) and
/// each one ({@code data}, {@code pagination}, {@code rate_limit}, {@code timezone}) is handed
/// to the binding as its own byte slice, so every byte of the response is parsed exactly once.
/// Helidon 4.5.x cannot decode a generic {@code ApiResponse<T>} in one pass (generic-record
/// codegen bug) nor re-walk an already parsed {@code JsonValue}
/// ({@code JsonBinding.deserialize(JsonValue, GenericType)} throws on objects and arrays in
/// 4.5.4 and 4.5.5), which is why neither of the obvious one-liners is used.
///
/// A second, unrelated Helidon 4.5.4 JSON binding bug affects free-form
/// {@code Map<String, Object>} fields on the football-module side — see
/// {@code io.github.miro93.sportmonks.football.model.FreeFormJson}'s javadoc.
public final class HelidonJsonCodec {

    private final JsonBinding binding;

    public HelidonJsonCodec() {
        this.binding = JsonBinding.create();
    }

    /// Build the {@link DataType} for a single-resource `data` payload.
    public <T> DataType<T> type(Class<T> dataClass) {
        return new DataType<>(GenericType.create(dataClass));
    }

    /// Build the {@link DataType} for a `List<T>` `data` payload.
    public <T> DataType<List<T>> listType(Class<T> dataClass) {
        return new DataType<>(genericListType(dataClass));
    }

    /// Build the {@link DataType} for a response that has no {@code data} envelope: the
    /// whole body is decoded as {@code T} and the resulting {@link ApiResponse} carries no
    /// pagination, rate limit or timezone (e.g. {@code transfer-rumours/config}).
    public <T> DataType<T> rawType(Class<T> dataClass) {
        return new DataType<>(GenericType.create(dataClass), true);
    }

    /// Build the {@link DataType} for a `data` payload that is a JSON object keyed by
    /// arbitrary strings (e.g. the entity-keyed {@code types/entities} response).
    public <T> DataType<Map<String, T>> mapType(Class<T> valueClass) {
        return new DataType<>(GenericType.<Map<String, T>>create(parameterized(Map.class, String.class, valueClass)));
    }

    /// Decode a SportMonks JSON envelope using a typed {@link DataType} token.
    public <T> ApiResponse<T> decode(String json, DataType<T> dataType) {
        return decode(json.getBytes(StandardCharsets.UTF_8), dataType);
    }

    /// Decode directly from raw UTF-8 bytes (the transport's preferred path).
    public <T> ApiResponse<T> decode(byte[] json, DataType<T> dataType) {
        try {
            if (dataType.raw()) {
                return new ApiResponse<>(binding.deserialize(json, dataType.genericType()), null, null, null);
            }
            Map<String, JsonTopLevel.Range> members = JsonTopLevel.scan(json);
            T data = member(json, members.get("data"), dataType.genericType());
            Pagination pagination = member(json, members.get("pagination"), GenericType.create(Pagination.class));
            RateLimit rateLimit = member(json, members.get("rate_limit"), GenericType.create(RateLimit.class));
            String timezone = member(json, members.get("timezone"), GenericType.create(String.class));
            return new ApiResponse<>(data, pagination, rateLimit, timezone);
        } catch (JsonException | IllegalArgumentException e) {
            throw new CodecException("Failed to decode SportMonks response", e);
        }
    }

    /// Decodes one envelope member from its byte slice; absent or {@code null} members decode to {@code null}.
    private <T> T member(byte[] json, JsonTopLevel.Range range, GenericType<T> type) {
        if (range == null || isNullLiteral(json, range)) {
            return null;
        }
        return binding.deserialize(Arrays.copyOfRange(json, range.start(), range.end()), type);
    }

    private static boolean isNullLiteral(byte[] json, JsonTopLevel.Range range) {
        return range.end() - range.start() == 4 && json[range.start()] == 'n' && json[range.start() + 1] == 'u'
                && json[range.start() + 2] == 'l' && json[range.start() + 3] == 'l';
    }

    private static <T> GenericType<List<T>> genericListType(Class<T> dataClass) {
        return GenericType.<List<T>>create(parameterized(List.class, dataClass));
    }

    private static ParameterizedType parameterized(Class<?> raw, Type... arguments) {
        return new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return arguments.clone();
            }

            @Override
            public Type getRawType() {
                return raw;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };
    }
}
