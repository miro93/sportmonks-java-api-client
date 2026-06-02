package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.core.request.SingleResourceRequest;
import io.github.miro93.sportmonks.football.model.League;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/// Access to the SportMonks {@code /leagues} endpoints.
public final class LeaguesEndpoint {

    private final ApiExecutor executor;
    private final DataType<League> single;
    private final DataType<List<League>> list;

    /// Creates the endpoint, building the {@link League} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the single/list response types
    public LeaguesEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.single = codec.type(League.class);
        this.list = codec.listType(League.class);
    }

    /// Requests every league, paginated.
    ///
    /// @return a collection request for all leagues
    public CollectionRequest<League> all() {
        return collection("leagues");
    }

    /// Requests a single league by its id.
    ///
    /// @param id the league id
    /// @return a single-resource request for that league
    public SingleResourceRequest<League> byId(long id) {
        return new SingleResourceRequest<>(executor, RequestSpec.builder("leagues/" + id), single);
    }

    /// Requests all leagues for a given country.
    ///
    /// @param countryId the country id to filter by
    /// @return a collection request for the matching leagues
    public CollectionRequest<League> byCountry(long countryId) {
        return collection("leagues/countries/" + countryId);
    }

    /// Searches leagues by name.
    ///
    /// @param name the search term (must not be {@code null})
    /// @return a collection request for the matching leagues
    public CollectionRequest<League> search(String name) {
        Objects.requireNonNull(name, "name");
        return collection("leagues/search/" + name);
    }

    /// Requests all leagues that currently have live fixtures.
    ///
    /// @return a collection request for live leagues
    public CollectionRequest<League> live() {
        return collection("leagues/live");
    }

    /// Requests all leagues that have fixtures on a given date.
    ///
    /// @param date the calendar date
    /// @return a collection request for leagues with fixtures on that date
    public CollectionRequest<League> byDate(LocalDate date) {
        return collection("leagues/fixtures/" + date);
    }

    private CollectionRequest<League> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
