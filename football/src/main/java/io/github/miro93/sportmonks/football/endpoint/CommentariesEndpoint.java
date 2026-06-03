package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.football.model.Commentary;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks {@code /commentaries} endpoints.
public final class CommentariesEndpoint {

    private final ApiExecutor executor;
    private final DataType<List<Commentary>> list;

    /// Creates the endpoint, building the {@link Commentary} list decoder from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the list response type
    public CommentariesEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.list = codec.listType(Commentary.class);
    }

    /// Requests every commentary, paginated.
    ///
    /// @return a collection request for all commentaries
    public CollectionRequest<Commentary> all() {
        return collection("commentaries");
    }

    /// Requests all commentaries for a given fixture.
    ///
    /// @param fixtureId the fixture id to filter by
    /// @return a collection request for the matching commentaries
    public CollectionRequest<Commentary> byFixture(long fixtureId) {
        return collection("commentaries/fixtures/" + fixtureId);
    }

    private CollectionRequest<Commentary> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
