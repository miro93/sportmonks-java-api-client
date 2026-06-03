package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.core.request.SingleResourceRequest;
import io.github.miro93.sportmonks.football.model.Bookmaker;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks {@code /bookmakers} endpoints.
public final class BookmakersEndpoint {

    private final ApiExecutor executor;
    private final DataType<Bookmaker> single;
    private final DataType<List<Bookmaker>> list;

    /// Creates the endpoint, building the {@link Bookmaker} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the single/list response types
    public BookmakersEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.single = codec.type(Bookmaker.class);
        this.list = codec.listType(Bookmaker.class);
    }

    /// Requests every bookmaker, paginated.
    ///
    /// @return a collection request for all bookmakers
    public CollectionRequest<Bookmaker> all() {
        return collection("bookmakers");
    }

    /// Requests a single bookmaker by its id.
    ///
    /// @param id the bookmaker id
    /// @return a single-resource request for that bookmaker
    public SingleResourceRequest<Bookmaker> byId(long id) {
        return new SingleResourceRequest<>(executor, RequestSpec.builder("bookmakers/" + id), single);
    }

    /// Searches bookmakers by name.
    ///
    /// @param name the search term (must not be {@code null})
    /// @return a collection request for the matching bookmakers
    /// @throws NullPointerException if {@code name} is {@code null}
    public CollectionRequest<Bookmaker> search(String name) {
        Objects.requireNonNull(name, "name");
        return collection("bookmakers/search/" + name);
    }

    /// Requests all bookmakers offering odds for a given fixture.
    ///
    /// @param fixtureId the fixture id to filter by
    /// @return a collection request for the matching bookmakers
    public CollectionRequest<Bookmaker> byFixture(long fixtureId) {
        return collection("bookmakers/fixtures/" + fixtureId);
    }

    private CollectionRequest<Bookmaker> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
