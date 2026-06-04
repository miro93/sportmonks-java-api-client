package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.internal.RequestSpec;
import io.github.miro93.sportmonks.core.request.SingleResourceRequest;
import io.github.miro93.sportmonks.football.model.Round;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks {@code /rounds} endpoints.
public final class RoundsEndpoint {

    private final ApiExecutor executor;
    private final DataType<Round> single;
    private final DataType<List<Round>> list;

    /// Creates the endpoint, building the {@link Round} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the single/list response types
    public RoundsEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.single = codec.type(Round.class);
        this.list = codec.listType(Round.class);
    }

    /// Requests every round, paginated.
    ///
    /// @return a collection request for all rounds
    public CollectionRequest<Round> all() {
        return collection("rounds");
    }

    /// Requests a single round by its id.
    ///
    /// @param id the round id
    /// @return a single-resource request for that round
    public SingleResourceRequest<Round> byId(long id) {
        return new SingleResourceRequest<>(executor, RequestSpec.builder("rounds/" + id), single);
    }

    /// Requests all rounds for a given season.
    ///
    /// @param seasonId the season id to filter by
    /// @return a collection request for the matching rounds
    public CollectionRequest<Round> bySeason(long seasonId) {
        return collection("rounds/seasons/" + seasonId);
    }

    /// Searches rounds by name.
    ///
    /// @param name the search term (must not be {@code null})
    /// @return a collection request for the matching rounds
    public CollectionRequest<Round> search(String name) {
        Objects.requireNonNull(name, "name");
        return collection("rounds/search/" + name);
    }

    private CollectionRequest<Round> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
