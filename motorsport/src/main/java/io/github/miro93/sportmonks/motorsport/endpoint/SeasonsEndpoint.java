package io.github.miro93.sportmonks.motorsport.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.HelidonJsonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.SingleResourceRequest;
import io.github.miro93.sportmonks.core.request.internal.RequestSpec;
import io.github.miro93.sportmonks.motorsport.model.Season;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks Motorsport {@code /seasons} endpoints.
public final class SeasonsEndpoint {

    private final ApiExecutor executor;
    private final DataType<Season> single;
    private final DataType<List<Season>> list;

    /// Creates the endpoint, building the {@link Season} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the response types
    public SeasonsEndpoint(ApiExecutor executor, HelidonJsonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.single = codec.type(Season.class);
        this.list = codec.listType(Season.class);
    }

    /// Requests every season.
    ///
    /// @return a collection request
    public CollectionRequest<Season> all() {
        return new CollectionRequest<>(executor, RequestSpec.builder("seasons"), list);
    }

    /// Requests a single season by its id.
    ///
    /// @param id the id
    /// @return a single-resource request
    public SingleResourceRequest<Season> byId(long id) {
        return new SingleResourceRequest<>(executor, RequestSpec.builder("seasons/" + id), single);
    }
}
