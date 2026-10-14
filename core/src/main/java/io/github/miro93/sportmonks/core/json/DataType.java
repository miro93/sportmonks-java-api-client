package io.github.miro93.sportmonks.core.json;

import io.helidon.common.GenericType;

/// An opaque, type-safe token describing the `data` payload type of a SportMonks
/// response. Obtain instances from {@link HelidonJsonCodec#type(Class)} or
/// {@link HelidonJsonCodec#listType(Class)} — the wrapped {@link GenericType} is an
/// internal detail and not part of the public API.
///
/// The payload's {@link GenericType} is resolved once, when the token is created,
/// and reused for every decode. Instances are immutable and thread-safe; create
/// one per endpoint and reuse it across requests.
public final class DataType<T> {

    private final GenericType<T> genericType;
    private final boolean raw;

    DataType(GenericType<T> genericType) {
        this(genericType, false);
    }

    DataType(GenericType<T> genericType, boolean raw) {
        this.genericType = genericType;
        this.raw = raw;
    }

    GenericType<T> genericType() {
        return genericType;
    }

    /// Whether the payload is the whole response body rather than its {@code data} member.
    boolean raw() {
        return raw;
    }
}
