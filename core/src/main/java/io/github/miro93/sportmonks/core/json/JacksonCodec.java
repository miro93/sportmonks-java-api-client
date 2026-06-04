package io.github.miro93.sportmonks.core.json;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;
import io.github.miro93.sportmonks.core.response.ApiResponse;

import java.util.List;

/// Wraps a snake_case-aware Jackson {@link JsonMapper} and decodes the SportMonks
/// envelope into a typed {@link ApiResponse}.
public final class JacksonCodec {

    private final JsonMapper mapper;

    /// Creates a codec with a snake_case naming strategy and lenient handling of
    /// unknown properties and of `null`/absent primitives.
    public JacksonCodec() {
        this.mapper = JsonMapper.builder()
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                // Jackson 3 flipped this default to true; keep the lenient v2 behaviour so an
                // absent primitive (e.g. a missing boolean `placeholder`) decodes to its default.
                .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
                .build();
    }

    /// Build the {@link DataType} for a single-resource {@code data} payload.
    /// The returned token carries the generic parameter {@code T} so that
    /// {@link #decode(String, DataType)} can infer the response type.
    public <T> DataType<T> type(Class<T> dataClass) {
        return responseReader(mapper.getTypeFactory().constructType(dataClass));
    }

    /// Build the {@link DataType} for a {@code List<T>} {@code data} payload.
    public <T> DataType<List<T>> listType(Class<T> dataClass) {
        return responseReader(mapper.getTypeFactory().constructCollectionType(List.class, dataClass));
    }

    /// Decode a SportMonks JSON envelope using a typed {@link DataType} token.
    public <T> ApiResponse<T> decode(String json, DataType<T> dataType) {
        try {
            return dataType.reader().readValue(json);
        } catch (JacksonException e) {
            throw new CodecException("Failed to decode SportMonks response", e);
        }
    }

    /// Decode a SportMonks JSON envelope directly from raw UTF-8 bytes (the transport's
    /// preferred path: Jackson parses bytes faster and avoids an intermediate `String`).
    public <T> ApiResponse<T> decode(byte[] json, DataType<T> dataType) {
        try {
            return dataType.reader().readValue(json);
        } catch (JacksonException e) {
            throw new CodecException("Failed to decode SportMonks response", e);
        }
    }

    /// Resolves the full `ApiResponse<dataType>` envelope type once and pre-builds a
    /// reusable, thread-safe {@link tools.jackson.databind.ObjectReader} for it, so the
    /// parametric type and value deserializer are not rebuilt on every {@link #decode}.
    private <T> DataType<T> responseReader(JavaType dataType) {
        JavaType responseType = mapper.getTypeFactory()
                .constructParametricType(ApiResponse.class, dataType);
        return new DataType<>(mapper.readerFor(responseType));
    }
}
