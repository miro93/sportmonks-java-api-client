package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.core.request.SingleResourceRequest;
import io.github.miro93.sportmonks.football.model.State;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks {@code /states} endpoints.
public final class StatesEndpoint {

    private final ApiExecutor executor;
    private final DataType<State> single;
    private final DataType<List<State>> list;

    /// Creates the endpoint, building the {@link State} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the single/list response types
    public StatesEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.single = codec.type(State.class);
        this.list = codec.listType(State.class);
    }

    /// Requests every fixture state, paginated.
    ///
    /// @return a collection request for all states
    public CollectionRequest<State> all() {
        return new CollectionRequest<>(executor, RequestSpec.builder("states"), list);
    }

    /// Requests a single state by its id.
    ///
    /// @param id the state id
    /// @return a single-resource request for that state
    public SingleResourceRequest<State> byId(long id) {
        return new SingleResourceRequest<>(executor, RequestSpec.builder("states/" + id), single);
    }
}
