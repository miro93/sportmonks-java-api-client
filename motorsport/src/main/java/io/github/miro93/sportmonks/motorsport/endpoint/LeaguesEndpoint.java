package io.github.miro93.sportmonks.motorsport.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.HelidonJsonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.SingleResourceRequest;
import io.github.miro93.sportmonks.core.request.internal.RequestSpec;
import io.github.miro93.sportmonks.motorsport.model.League;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/// Access to the SportMonks Motorsport {@code /leagues} endpoints.
public final class LeaguesEndpoint {

    private final ApiExecutor executor;
    private final DataType<League> single;
    private final DataType<List<League>> list;

    /// Creates the endpoint, building the {@link League} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the response types
    public LeaguesEndpoint(ApiExecutor executor, HelidonJsonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.single = codec.type(League.class);
        this.list = codec.listType(League.class);
    }

    /// Requests every league.
    ///
    /// @return a collection request
    public CollectionRequest<League> all() {
        return new CollectionRequest<>(executor, RequestSpec.builder("leagues"), list);
    }

    /// Requests a single league by its id.
    ///
    /// @param id the id
    /// @return a single-resource request
    public SingleResourceRequest<League> byId(long id) {
        return new SingleResourceRequest<>(executor, RequestSpec.builder("leagues/" + id), single);
    }

    /// Requests the leagues of a country.
    ///
    /// @param countryId the country id
    /// @return a collection request
    public CollectionRequest<League> byCountry(long countryId) {
        return new CollectionRequest<>(executor, RequestSpec.builder("leagues/countries/" + countryId), list);
    }

    /// Requests the leagues with sessions on a date.
    ///
    /// @param date the date
    /// @return a collection request
    public CollectionRequest<League> byDate(LocalDate date) {
        return new CollectionRequest<>(executor, RequestSpec.builder("leagues/date/" + date), list);
    }

    /// Requests the leagues with live sessions.
    ///
    /// @return a collection request
    public CollectionRequest<League> live() {
        return new CollectionRequest<>(executor, RequestSpec.builder("leagues/live"), list);
    }

    /// Searches leagues by name.
    ///
    /// @param name the name
    /// @return a collection request
    public CollectionRequest<League> search(String name) {
        Objects.requireNonNull(name, "name");
        return new CollectionRequest<>(executor, RequestSpec.builder("leagues/search/" + name), list);
    }

    /// Requests the leagues a team took part in.
    ///
    /// @param teamId the team id
    /// @return a collection request
    public CollectionRequest<League> byTeam(long teamId) {
        return new CollectionRequest<>(executor, RequestSpec.builder("leagues/teams/" + teamId), list);
    }

    /// Requests the leagues a team currently takes part in.
    ///
    /// @param teamId the team id
    /// @return a collection request
    public CollectionRequest<League> currentByTeam(long teamId) {
        return new CollectionRequest<>(executor, RequestSpec.builder("leagues/teams/" + teamId + "/current"), list);
    }
}
