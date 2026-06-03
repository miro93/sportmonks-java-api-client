package io.github.miro93.sportmonks.core.json;

import tools.jackson.databind.ObjectReader;

/// An opaque, type-safe token describing the `data` payload type of a SportMonks
/// response. Obtain instances from {@link JacksonCodec#type(Class)} or
/// {@link JacksonCodec#listType(Class)} — the wrapped Jackson {@link ObjectReader} is an
/// internal detail and is not part of the public API.
///
/// The reader for the full `ApiResponse<T>` envelope is resolved once, when the token is
/// created, and reused for every decode. Instances are immutable and thread-safe; create
/// one per endpoint and reuse it across requests.
public final class DataType<T> {

    private final ObjectReader reader;

    DataType(ObjectReader reader) {
        this.reader = reader;
    }

    ObjectReader reader() {
        return reader;
    }
}
