package io.github.miro93.sportmonks.core.coreapi.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.coreapi.model.Region;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.internal.RequestSpec;
import io.github.miro93.sportmonks.core.request.SingleResourceRequest;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks Core API {@code /regions} endpoints.
public final class RegionsEndpoint {

    private final ApiExecutor executor;
    private final DataType<Region> single;
    private final DataType<List<Region>> list;

    /// Creates the endpoint, building the {@link Region} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the single/list response types
    public RegionsEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.single = codec.type(Region.class);
        this.list = codec.listType(Region.class);
    }

    /// Requests every region, paginated.
    ///
    /// @return a collection request for all regions
    public CollectionRequest<Region> all() {
        return collection("regions");
    }

    /// Requests a single region by its id.
    ///
    /// @param id the region id
    /// @return a single-resource request for that region
    public SingleResourceRequest<Region> byId(long id) {
        return new SingleResourceRequest<>(executor, RequestSpec.builder("regions/" + id), single);
    }

    /// Searches regions by name.
    ///
    /// @param name the search term (must not be {@code null})
    /// @return a collection request for the matching regions
    /// @throws NullPointerException if {@code name} is {@code null}
    public CollectionRequest<Region> search(String name) {
        Objects.requireNonNull(name, "name");
        return collection("regions/search/" + name);
    }

    private CollectionRequest<Region> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
