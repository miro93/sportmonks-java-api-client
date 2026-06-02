package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.football.model.Fixture;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks {@code /livescores} endpoints. All return fixtures.
public final class LivescoresEndpoint {

    private final ApiExecutor executor;
    private final DataType<List<Fixture>> list;

    public LivescoresEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.list = codec.listType(Fixture.class);
    }

    /// Fixtures currently in play.
    public CollectionRequest<Fixture> inplay() {
        return collection("livescores/inplay");
    }

    /// Fixtures within the 15-minute window around kickoff.
    public CollectionRequest<Fixture> all() {
        return collection("livescores");
    }

    /// Fixtures updated within the last 10 seconds.
    public CollectionRequest<Fixture> latest() {
        return collection("livescores/latest");
    }

    private CollectionRequest<Fixture> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
