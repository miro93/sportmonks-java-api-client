package io.github.miro93.sportmonks.core.json;

import com.fasterxml.jackson.databind.JavaType;

/// A thin typed wrapper around a Jackson {@link JavaType} that carries the
/// Java generic parameter {@code T}, allowing callers to chain
/// {@code codec.decode(json, codec.type(Foo.class)).data()} without an
/// explicit cast.
public record DataType<T>(JavaType javaType) {
}
