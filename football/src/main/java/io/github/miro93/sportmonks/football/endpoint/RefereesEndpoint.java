package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.internal.RequestSpec;
import io.github.miro93.sportmonks.core.request.SingleResourceRequest;
import io.github.miro93.sportmonks.football.model.Referee;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks {@code /referees} endpoints.
public final class RefereesEndpoint {

    private final ApiExecutor executor;
    private final DataType<Referee> single;
    private final DataType<List<Referee>> list;

    /// Creates the endpoint, building the {@link Referee} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the single/list response types
    public RefereesEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.single = codec.type(Referee.class);
        this.list = codec.listType(Referee.class);
    }

    /// Requests every referee, paginated.
    ///
    /// @return a collection request for all referees
    public CollectionRequest<Referee> all() {
        return collection("referees");
    }

    /// Requests a single referee by its id.
    ///
    /// @param id the referee id
    /// @return a single-resource request for that referee
    public SingleResourceRequest<Referee> byId(long id) {
        return new SingleResourceRequest<>(executor, RequestSpec.builder("referees/" + id), single);
    }

    /// Requests all referees for a given country.
    ///
    /// @param countryId the country id to filter by
    /// @return a collection request for the matching referees
    public CollectionRequest<Referee> byCountry(long countryId) {
        return collection("referees/countries/" + countryId);
    }

    /// Requests all referees for a given season.
    ///
    /// @param seasonId the season id to filter by
    /// @return a collection request for the matching referees
    public CollectionRequest<Referee> bySeason(long seasonId) {
        return collection("referees/seasons/" + seasonId);
    }

    /// Searches referees by name.
    ///
    /// @param name the search term (must not be {@code null})
    /// @return a collection request for the matching referees
    /// @throws NullPointerException if {@code name} is {@code null}
    public CollectionRequest<Referee> search(String name) {
        Objects.requireNonNull(name, "name");
        return collection("referees/search/" + name);
    }

    private CollectionRequest<Referee> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
