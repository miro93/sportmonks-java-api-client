package io.github.miro93.sportmonks.core.coreapi.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.coreapi.model.Continent;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.internal.RequestSpec;
import io.github.miro93.sportmonks.core.request.SingleResourceRequest;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks Core API {@code /continents} endpoints.
public final class ContinentsEndpoint {

    private final ApiExecutor executor;
    private final DataType<Continent> single;
    private final DataType<List<Continent>> list;

    /// Creates the endpoint, building the {@link Continent} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the single/list response types
    public ContinentsEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.single = codec.type(Continent.class);
        this.list = codec.listType(Continent.class);
    }

    /// Requests every continent, paginated.
    ///
    /// @return a collection request for all continents
    public CollectionRequest<Continent> all() {
        return new CollectionRequest<>(executor, RequestSpec.builder("continents"), list);
    }

    /// Requests a single continent by its id.
    ///
    /// @param id the continent id
    /// @return a single-resource request for that continent
    public SingleResourceRequest<Continent> byId(long id) {
        return new SingleResourceRequest<>(executor, RequestSpec.builder("continents/" + id), single);
    }
}
