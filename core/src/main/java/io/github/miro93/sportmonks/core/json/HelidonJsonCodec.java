package io.github.miro93.sportmonks.core.json;

import io.helidon.common.GenericType;
import io.helidon.json.JsonException;
import io.helidon.json.JsonParser;
import io.helidon.json.binding.JsonBinding;
import io.github.miro93.sportmonks.core.response.ApiResponse;
import io.github.miro93.sportmonks.core.response.Pagination;
import io.github.miro93.sportmonks.core.response.RateLimit;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/// Wraps a Helidon JSON {@link JsonBinding} (compile-time binding, no reflection)
/// and decodes the SportMonks envelope into a typed {@link ApiResponse}.
/// snake_case mapping is declared per field via `@Json.Property` on the records.
///
/// The envelope is read in one streaming pass with Helidon's {@link JsonParser}: each member
/// ({@code data}, {@code pagination}, {@code rate_limit}, {@code timezone}) is handed to the
/// binding from the parser's current position and every other member is skipped, so each byte
/// of the response is parsed exactly once and nothing is copied. A generic
/// {@code ApiResponse<T>} entity is not an option: Helidon's codegen cannot bind generic records.
///
/// A separate Helidon JSON binding bug affects free-form {@code Map<String, Object>} fields on the
/// football-module side — see {@code io.github.miro93.sportmonks.football.model.FreeFormJson}'s javadoc.
public final class HelidonJsonCodec {

    private static final GenericType<Pagination> PAGINATION = GenericType.create(Pagination.class);
    private static final GenericType<RateLimit> RATE_LIMIT = GenericType.create(RateLimit.class);

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
            JsonParser parser = JsonParser.create(json);
            if (parser.currentByte() != '{') {
                throw parser.createException("Expected a JSON object at the top level", parser.currentByte());
            }
            T data = null;
            Pagination pagination = null;
            RateLimit rateLimit = null;
            String timezone = null;
            byte token = parser.nextToken();
            while (token != '}') {
                if (token != '"') {
                    throw parser.createException("Expected a member name", token);
                }
                String key = parser.readString();
                token = parser.nextToken();
                if (token != ':') {
                    throw parser.createException("Expected ':' after the member name", token);
                }
                parser.nextToken();
                switch (key) {
                    case "data" -> data = member(parser, dataType.genericType());
                    case "pagination" -> pagination = member(parser, PAGINATION);
                    case "rate_limit" -> rateLimit = member(parser, RATE_LIMIT);
                    case "timezone" -> timezone = member(parser, GenericType.STRING);
                    default -> parser.skip();
                }
                token = parser.nextToken();
                if (token == ',') {
                    token = parser.nextToken();
                } else if (token != '}') {
                    throw parser.createException("Expected ',' or '}' after a member", token);
                }
            }
            return new ApiResponse<>(data, pagination, rateLimit, timezone);
        } catch (JsonException | IllegalArgumentException e) {
            throw new CodecException("Failed to decode SportMonks response", e);
        }
    }

    /// Decodes the member at the parser's position; a JSON {@code null} decodes to {@code null}.
    private <T> T member(JsonParser parser, GenericType<T> type) {
        return parser.checkNull() ? null : binding.deserialize(parser, type);
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
