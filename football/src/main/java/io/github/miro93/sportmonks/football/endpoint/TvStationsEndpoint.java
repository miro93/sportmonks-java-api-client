package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.core.request.SingleResourceRequest;
import io.github.miro93.sportmonks.football.model.TvStation;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks {@code /tv-stations} endpoints.
public final class TvStationsEndpoint {

    private final ApiExecutor executor;
    private final DataType<TvStation> single;
    private final DataType<List<TvStation>> list;

    /// Creates the endpoint, building the {@link TvStation} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the single/list response types
    public TvStationsEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.single = codec.type(TvStation.class);
        this.list = codec.listType(TvStation.class);
    }

    /// Requests every TV station, paginated.
    ///
    /// @return a collection request for all TV stations
    public CollectionRequest<TvStation> all() {
        return collection("tv-stations");
    }

    /// Requests a single TV station by its id.
    ///
    /// @param id the TV station id
    /// @return a single-resource request for that TV station
    public SingleResourceRequest<TvStation> byId(long id) {
        return new SingleResourceRequest<>(executor, RequestSpec.builder("tv-stations/" + id), single);
    }

    /// Requests all TV stations broadcasting a given fixture.
    ///
    /// @param fixtureId the fixture id to filter by
    /// @return a collection request for the matching TV stations
    public CollectionRequest<TvStation> byFixture(long fixtureId) {
        return collection("tv-stations/fixtures/" + fixtureId);
    }

    private CollectionRequest<TvStation> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
