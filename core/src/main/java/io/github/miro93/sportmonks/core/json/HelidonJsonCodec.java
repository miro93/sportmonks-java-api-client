package io.github.miro93.sportmonks.core.json;

import io.helidon.common.GenericType;
import io.helidon.json.JsonException;
import io.helidon.json.binding.JsonBinding;
import io.github.miro93.sportmonks.core.response.ApiResponse;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/// Wraps a Helidon JSON {@link JsonBinding} (compile-time binding, no reflection)
/// and decodes the SportMonks envelope into a typed {@link ApiResponse}.
/// snake_case mapping is declared per field via `@Json.Property` on the records.
///
/// The envelope is parsed generically first, via {@link Envelope} (`data` held as a
/// raw {@code JsonValue}), and the payload is then decoded separately into `T`/
/// `List<T>` — see {@link Envelope} for why `ApiResponse<T>` is not decoded directly.
///
/// ponytail: the `data` subtree is re-serialized to a `String` and re-parsed once per
/// decode (see {@link #toResponse}) instead of binding straight from the captured
/// {@code JsonValue} — `JsonBinding.deserialize(JsonValue, GenericType)` exists but
/// throws `JsonException` ("Expected ',' or '}'" / "No more JSON Values available") on
/// both objects and arrays in Helidon 4.5.4; its `JsonValueParser` doesn't correctly
/// re-walk an already-parsed tree. Switch to that call directly if a later Helidon
/// version fixes it, or once the generic-record codegen bug is fixed and `ApiResponse<T>`
/// can be decoded in one pass.
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

    /// Decode a SportMonks JSON envelope using a typed {@link DataType} token.
    public <T> ApiResponse<T> decode(String json, DataType<T> dataType) {
        try {
            return toResponse(binding.deserialize(json, Envelope.class), dataType);
        } catch (JsonException e) {
            throw new CodecException("Failed to decode SportMonks response", e);
        }
    }

    /// Decode directly from raw UTF-8 bytes (the transport's preferred path).
    public <T> ApiResponse<T> decode(byte[] json, DataType<T> dataType) {
        try {
            return toResponse(binding.deserialize(json, Envelope.class), dataType);
        } catch (JsonException e) {
            throw new CodecException("Failed to decode SportMonks response", e);
        }
    }

    private <T> ApiResponse<T> toResponse(Envelope envelope, DataType<T> dataType) {
        T data = envelope.data() == null ? null : binding.deserialize(envelope.data().toString(), dataType.genericType());
        return new ApiResponse<>(data, envelope.pagination(), envelope.rateLimit(), envelope.timezone());
    }

    private static <T> GenericType<List<T>> genericListType(Class<T> dataClass) {
        return GenericType.<List<T>>create(parameterized(List.class, dataClass));
    }

    private static ParameterizedType parameterized(Class<?> raw, Type argument) {
        return new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[] {argument};
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
