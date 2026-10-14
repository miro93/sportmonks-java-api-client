package io.github.miro93.sportmonks.motorsport.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.HelidonJsonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.SingleResourceRequest;
import io.github.miro93.sportmonks.core.request.internal.RequestSpec;
import io.github.miro93.sportmonks.motorsport.model.Stage;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks Motorsport {@code /stages} endpoints (race weekends).
public final class StagesEndpoint {

    private final ApiExecutor executor;
    private final DataType<Stage> single;
    private final DataType<List<Stage>> list;

    /// Creates the endpoint, building the {@link Stage} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the response types
    public StagesEndpoint(ApiExecutor executor, HelidonJsonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.single = codec.type(Stage.class);
        this.list = codec.listType(Stage.class);
    }

    /// Requests every race weekend.
    ///
    /// @return a collection request
    public CollectionRequest<Stage> all() {
        return new CollectionRequest<>(executor, RequestSpec.builder("stages"), list);
    }

    /// Requests a single race weekend by its id.
    ///
    /// @param id the id
    /// @return a single-resource request
    public SingleResourceRequest<Stage> byId(long id) {
        return new SingleResourceRequest<>(executor, RequestSpec.builder("stages/" + id), single);
    }

    /// Requests the race weekends of a season.
    ///
    /// @param seasonId the season id
    /// @return a collection request
    public CollectionRequest<Stage> bySeason(long seasonId) {
        return new CollectionRequest<>(executor, RequestSpec.builder("stages/seasons/" + seasonId), list);
    }

    /// Searches race weekends by name.
    ///
    /// @param name the name
    /// @return a collection request
    public CollectionRequest<Stage> search(String name) {
        Objects.requireNonNull(name, "name");
        return new CollectionRequest<>(executor, RequestSpec.builder("stages/search/" + name), list);
    }
}
