package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.core.request.SingleResourceRequest;
import io.github.miro93.sportmonks.football.model.Market;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks {@code /markets} endpoints.
public final class MarketsEndpoint {

    private final ApiExecutor executor;
    private final DataType<Market> single;
    private final DataType<List<Market>> list;

    /// Creates the endpoint, building the {@link Market} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the single/list response types
    public MarketsEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.single = codec.type(Market.class);
        this.list = codec.listType(Market.class);
    }

    /// Requests every market, paginated.
    ///
    /// @return a collection request for all markets
    public CollectionRequest<Market> all() {
        return collection("markets");
    }

    /// Requests a single market by its id.
    ///
    /// @param id the market id
    /// @return a single-resource request for that market
    public SingleResourceRequest<Market> byId(long id) {
        return new SingleResourceRequest<>(executor, RequestSpec.builder("markets/" + id), single);
    }

    /// Searches markets by name.
    ///
    /// @param name the search term (must not be {@code null})
    /// @return a collection request for the matching markets
    /// @throws NullPointerException if {@code name} is {@code null}
    public CollectionRequest<Market> search(String name) {
        Objects.requireNonNull(name, "name");
        return collection("markets/search/" + name);
    }

    private CollectionRequest<Market> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
