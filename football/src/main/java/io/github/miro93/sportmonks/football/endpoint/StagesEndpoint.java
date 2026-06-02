package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.core.request.SingleResourceRequest;
import io.github.miro93.sportmonks.football.model.Stage;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks {@code /stages} endpoints.
public final class StagesEndpoint {

    private final ApiExecutor executor;
    private final DataType<Stage> single;
    private final DataType<List<Stage>> list;

    /// Creates the endpoint, building the {@link Stage} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the single/list response types
    public StagesEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.single = codec.type(Stage.class);
        this.list = codec.listType(Stage.class);
    }

    /// Requests every stage, paginated.
    ///
    /// @return a collection request for all stages
    public CollectionRequest<Stage> all() {
        return collection("stages");
    }

    /// Requests a single stage by its id.
    ///
    /// @param id the stage id
    /// @return a single-resource request for that stage
    public SingleResourceRequest<Stage> byId(long id) {
        return new SingleResourceRequest<>(executor, RequestSpec.builder("stages/" + id), single);
    }

    /// Requests all stages for a given season.
    ///
    /// @param seasonId the season id to filter by
    /// @return a collection request for the matching stages
    public CollectionRequest<Stage> bySeason(long seasonId) {
        return collection("stages/seasons/" + seasonId);
    }

    /// Searches stages by name.
    ///
    /// @param name the search term (must not be {@code null})
    /// @return a collection request for the matching stages
    public CollectionRequest<Stage> search(String name) {
        Objects.requireNonNull(name, "name");
        return collection("stages/search/" + name);
    }

    private CollectionRequest<Stage> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
