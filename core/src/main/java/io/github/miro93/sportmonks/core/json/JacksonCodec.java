package io.github.miro93.sportmonks.core.json;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.module.blackbird.BlackbirdModule;
import io.github.miro93.sportmonks.core.response.ApiResponse;

import java.util.List;

/// Wraps a snake_case-aware Jackson {@link JsonMapper} (with the Blackbird module)
/// and decodes the SportMonks envelope into a typed {@link ApiResponse}.
public final class JacksonCodec {

    private final JsonMapper mapper;

    /// Creates a codec with a snake_case naming strategy, the Blackbird module,
    /// and lenient handling of unknown properties.
    public JacksonCodec() {
        this.mapper = JsonMapper.builder()
                .addModule(new BlackbirdModule())
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .build();
    }

    /// Build the {@link DataType} for a single-resource {@code data} payload.
    /// The returned token carries the generic parameter {@code T} so that
    /// {@link #decode(String, DataType)} can infer the response type.
    public <T> DataType<T> type(Class<T> dataClass) {
        return new DataType<>(mapper.getTypeFactory().constructType(dataClass));
    }

    /// Build the {@link DataType} for a {@code List<T>} {@code data} payload.
    public <T> DataType<List<T>> listType(Class<T> dataClass) {
        return new DataType<>(mapper.getTypeFactory().constructCollectionType(List.class, dataClass));
    }

    /// Decode a SportMonks JSON envelope using a typed {@link DataType} token.
    public <T> ApiResponse<T> decode(String json, DataType<T> dataType) {
        return decodeJavaType(json, dataType.javaType());
    }

    @SuppressWarnings("unchecked")
    private <T> ApiResponse<T> decodeJavaType(String json, JavaType dataType) {
        JavaType responseType = mapper.getTypeFactory()
                .constructParametricType(ApiResponse.class, dataType);
        try {
            return (ApiResponse<T>) mapper.readValue(json, responseType);
        } catch (JacksonException e) {
            throw new CodecException("Failed to decode SportMonks response", e);
        }
    }
}
