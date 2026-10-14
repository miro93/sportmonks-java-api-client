package io.github.miro93.sportmonks.motorsport.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.HelidonJsonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.SingleResourceRequest;
import io.github.miro93.sportmonks.core.request.internal.RequestSpec;
import io.github.miro93.sportmonks.motorsport.model.Venue;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks Motorsport {@code /venues} endpoints (tracks).
public final class VenuesEndpoint {

    private final ApiExecutor executor;
    private final DataType<Venue> single;
    private final DataType<List<Venue>> list;

    /// Creates the endpoint, building the {@link Venue} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the response types
    public VenuesEndpoint(ApiExecutor executor, HelidonJsonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.single = codec.type(Venue.class);
        this.list = codec.listType(Venue.class);
    }

    /// Requests every track.
    ///
    /// @return a collection request
    public CollectionRequest<Venue> all() {
        return new CollectionRequest<>(executor, RequestSpec.builder("venues"), list);
    }

    /// Requests a single track by its id.
    ///
    /// @param id the id
    /// @return a single-resource request
    public SingleResourceRequest<Venue> byId(long id) {
        return new SingleResourceRequest<>(executor, RequestSpec.builder("venues/" + id), single);
    }

    /// Requests the tracks of a season.
    ///
    /// @param seasonId the season id
    /// @return a collection request
    public CollectionRequest<Venue> bySeason(long seasonId) {
        return new CollectionRequest<>(executor, RequestSpec.builder("venues/seasons/" + seasonId), list);
    }

    /// Searches tracks by name.
    ///
    /// @param name the name
    /// @return a collection request
    public CollectionRequest<Venue> search(String name) {
        Objects.requireNonNull(name, "name");
        return new CollectionRequest<>(executor, RequestSpec.builder("venues/search/" + name), list);
    }
}
