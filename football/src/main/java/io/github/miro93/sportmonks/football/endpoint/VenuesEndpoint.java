package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.internal.RequestSpec;
import io.github.miro93.sportmonks.core.request.SingleResourceRequest;
import io.github.miro93.sportmonks.football.model.Venue;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks {@code /venues} endpoints.
public final class VenuesEndpoint {

    private final ApiExecutor executor;
    private final DataType<Venue> single;
    private final DataType<List<Venue>> list;

    /// Creates the endpoint, building the {@link Venue} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the single/list response types
    public VenuesEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.single = codec.type(Venue.class);
        this.list = codec.listType(Venue.class);
    }

    /// Requests every venue, paginated.
    ///
    /// @return a collection request for all venues
    public CollectionRequest<Venue> all() {
        return collection("venues");
    }

    /// Requests a single venue by its id.
    ///
    /// @param id the venue id
    /// @return a single-resource request for that venue
    public SingleResourceRequest<Venue> byId(long id) {
        return new SingleResourceRequest<>(executor, RequestSpec.builder("venues/" + id), single);
    }

    /// Requests all venues for a given season.
    ///
    /// @param seasonId the season id to filter by
    /// @return a collection request for the matching venues
    public CollectionRequest<Venue> bySeason(long seasonId) {
        return collection("venues/seasons/" + seasonId);
    }

    /// Searches venues by name.
    ///
    /// @param name the search term (must not be {@code null})
    /// @return a collection request for the matching venues
    /// @throws NullPointerException if {@code name} is {@code null}
    public CollectionRequest<Venue> search(String name) {
        Objects.requireNonNull(name, "name");
        return collection("venues/search/" + name);
    }

    private CollectionRequest<Venue> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
