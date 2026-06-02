package io.github.miro93.sportmonks.core.json;

import com.fasterxml.jackson.databind.JavaType;

/// An opaque, type-safe token describing the {@code data} payload type of a
/// SportMonks response. Obtain instances from {@link JacksonCodec#type(Class)} or
/// {@link JacksonCodec#listType(Class)} — the wrapped Jackson type is an internal
/// detail and is not part of the public API.
///
/// Instances are not thread-safe; use one instance per logical request.
public final class DataType<T> {

    private final JavaType javaType;

    DataType(JavaType javaType) {
        this.javaType = javaType;
    }

    JavaType javaType() {
        return javaType;
    }
}
