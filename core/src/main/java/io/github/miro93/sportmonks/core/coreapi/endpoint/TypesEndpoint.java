package io.github.miro93.sportmonks.core.coreapi.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.coreapi.model.Type;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.core.request.SingleResourceRequest;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks Core API {@code /types} endpoints.
public final class TypesEndpoint {

    private final ApiExecutor executor;
    private final DataType<Type> single;
    private final DataType<List<Type>> list;

    /// Creates the endpoint, building the {@link Type} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the single/list response types
    public TypesEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.single = codec.type(Type.class);
        this.list = codec.listType(Type.class);
    }

    /// Requests every type, paginated.
    ///
    /// @return a collection request for all types
    public CollectionRequest<Type> all() {
        return new CollectionRequest<>(executor, RequestSpec.builder("types"), list);
    }

    /// Requests a single type by its id.
    ///
    /// @param id the type id
    /// @return a single-resource request for that type
    public SingleResourceRequest<Type> byId(long id) {
        return new SingleResourceRequest<>(executor, RequestSpec.builder("types/" + id), single);
    }
}
