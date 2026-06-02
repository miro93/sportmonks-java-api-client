package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.core.request.SingleResourceRequest;
import io.github.miro93.sportmonks.football.model.Season;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks {@code /seasons} endpoints.
public final class SeasonsEndpoint {

    private final ApiExecutor executor;
    private final DataType<Season> single;
    private final DataType<List<Season>> list;

    /// Creates the endpoint, building the {@link Season} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the single/list response types
    public SeasonsEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.single = codec.type(Season.class);
        this.list = codec.listType(Season.class);
    }

    /// Requests every season, paginated.
    ///
    /// @return a collection request for all seasons
    public CollectionRequest<Season> all() {
        return collection("seasons");
    }

    /// Requests a single season by its id.
    ///
    /// @param id the season id
    /// @return a single-resource request for that season
    public SingleResourceRequest<Season> byId(long id) {
        return new SingleResourceRequest<>(executor, RequestSpec.builder("seasons/" + id), single);
    }

    /// Requests all seasons for a given team.
    ///
    /// @param teamId the team id to filter by
    /// @return a collection request for the matching seasons
    public CollectionRequest<Season> byTeam(long teamId) {
        return collection("seasons/teams/" + teamId);
    }

    /// Searches seasons by name.
    ///
    /// @param name the search term (must not be {@code null})
    /// @return a collection request for the matching seasons
    public CollectionRequest<Season> search(String name) {
        Objects.requireNonNull(name, "name");
        return collection("seasons/search/" + name);
    }

    private CollectionRequest<Season> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
