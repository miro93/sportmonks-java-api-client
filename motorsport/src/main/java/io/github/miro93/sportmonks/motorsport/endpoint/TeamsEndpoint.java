package io.github.miro93.sportmonks.motorsport.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.HelidonJsonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.SingleResourceRequest;
import io.github.miro93.sportmonks.core.request.internal.RequestSpec;
import io.github.miro93.sportmonks.motorsport.model.Team;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks Motorsport {@code /teams} endpoints.
public final class TeamsEndpoint {

    private final ApiExecutor executor;
    private final DataType<Team> single;
    private final DataType<List<Team>> list;

    /// Creates the endpoint, building the {@link Team} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the response types
    public TeamsEndpoint(ApiExecutor executor, HelidonJsonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.single = codec.type(Team.class);
        this.list = codec.listType(Team.class);
    }

    /// Requests every team.
    ///
    /// @return a collection request
    public CollectionRequest<Team> all() {
        return new CollectionRequest<>(executor, RequestSpec.builder("teams"), list);
    }

    /// Requests a single team by its id.
    ///
    /// @param id the id
    /// @return a single-resource request
    public SingleResourceRequest<Team> byId(long id) {
        return new SingleResourceRequest<>(executor, RequestSpec.builder("teams/" + id), single);
    }

    /// Requests the teams of a country.
    ///
    /// @param countryId the country id
    /// @return a collection request
    public CollectionRequest<Team> byCountry(long countryId) {
        return new CollectionRequest<>(executor, RequestSpec.builder("teams/countries/" + countryId), list);
    }

    /// Requests the teams of a season.
    ///
    /// @param seasonId the season id
    /// @return a collection request
    public CollectionRequest<Team> bySeason(long seasonId) {
        return new CollectionRequest<>(executor, RequestSpec.builder("teams/seasons/" + seasonId), list);
    }

    /// Searches teams by name.
    ///
    /// @param name the name
    /// @return a collection request
    public CollectionRequest<Team> search(String name) {
        Objects.requireNonNull(name, "name");
        return new CollectionRequest<>(executor, RequestSpec.builder("teams/search/" + name), list);
    }
}
