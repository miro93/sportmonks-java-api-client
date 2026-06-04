package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.football.model.Expected;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks expected-goals (xG) endpoints ({@code /expected}):
/// team-level xG per fixture and player-level xG per lineup.
public final class ExpectedEndpoint {

    private final ApiExecutor executor;
    private final DataType<List<Expected>> list;

    /// Creates the endpoint, building the {@link Expected} list decoder from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the list response type
    public ExpectedEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.list = codec.listType(Expected.class);
    }

    /// Requests the team-level expected-goals records aggregated per fixture.
    ///
    /// @return a collection request for the fixture-level xG records
    public CollectionRequest<Expected> fixtures() {
        return collection("expected/fixtures");
    }

    /// Requests the player-level expected-goals records per lineup.
    ///
    /// @return a collection request for the lineup-level xG records
    public CollectionRequest<Expected> lineups() {
        return collection("expected/lineups");
    }

    private CollectionRequest<Expected> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
