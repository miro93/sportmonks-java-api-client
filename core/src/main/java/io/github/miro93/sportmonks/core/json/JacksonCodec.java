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

    public JacksonCodec() {
        this.mapper = JsonMapper.builder()
                .addModule(new BlackbirdModule())
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .build();
    }

    /// Build the {@link JavaType} for a single-resource {@code data} payload.
    public JavaType type(Class<?> dataClass) {
        return mapper.getTypeFactory().constructType(dataClass);
    }

    /// Build the {@link JavaType} for a {@code List<dataClass>} {@code data} payload.
    public JavaType listType(Class<?> dataClass) {
        return mapper.getTypeFactory().constructCollectionType(List.class, dataClass);
    }

    public <T> ApiResponse<T> decode(String json, JavaType dataType) {
        JavaType responseType = mapper.getTypeFactory()
                .constructParametricType(ApiResponse.class, dataType);
        try {
            return mapper.readValue(json, responseType);
        } catch (JacksonException e) {
            throw new CodecException("Failed to decode SportMonks response", e);
        }
    }
}
