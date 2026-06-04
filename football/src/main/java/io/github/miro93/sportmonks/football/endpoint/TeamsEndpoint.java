package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.internal.RequestSpec;
import io.github.miro93.sportmonks.core.request.SingleResourceRequest;
import io.github.miro93.sportmonks.football.model.Team;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/// Access to the SportMonks {@code /teams} endpoints.
public final class TeamsEndpoint {

    private final ApiExecutor executor;
    private final DataType<Team> single;
    private final DataType<List<Team>> list;

    /// Creates the endpoint, building the {@link Team} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the single/list response types
    public TeamsEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.single = codec.type(Team.class);
        this.list = codec.listType(Team.class);
    }

    /// Requests every team, paginated.
    ///
    /// @return a collection request for all teams
    public CollectionRequest<Team> all() {
        return collection("teams");
    }

    /// Requests a single team by its id.
    ///
    /// @param id the team id
    /// @return a single-resource request for that team
    public SingleResourceRequest<Team> byId(long id) {
        return new SingleResourceRequest<>(executor, RequestSpec.builder("teams/" + id), single);
    }

    /// Requests several teams by their ids in a single call.
    ///
    /// @param ids one or more team ids
    /// @return a collection request for the matching teams
    /// @throws IllegalArgumentException if no ids are supplied
    public CollectionRequest<Team> byMultipleIds(long... ids) {
        if (ids.length == 0) {
            throw new IllegalArgumentException("byMultipleIds requires at least one id");
        }
        String csv = LongStream.of(ids).mapToObj(Long::toString).collect(Collectors.joining(","));
        return collection("teams/multi/" + csv);
    }

    /// Requests all teams for a given season.
    ///
    /// @param seasonId the season id to filter by
    /// @return a collection request for the matching teams
    public CollectionRequest<Team> bySeason(long seasonId) {
        return collection("teams/seasons/" + seasonId);
    }

    /// Requests all teams for a given country.
    ///
    /// @param countryId the country id to filter by
    /// @return a collection request for the matching teams
    public CollectionRequest<Team> byCountry(long countryId) {
        return collection("teams/countries/" + countryId);
    }

    /// Searches teams by name.
    ///
    /// @param name the search term (must not be {@code null})
    /// @return a collection request for the matching teams
    /// @throws NullPointerException if {@code name} is {@code null}
    public CollectionRequest<Team> search(String name) {
        Objects.requireNonNull(name, "name");
        return collection("teams/search/" + name);
    }

    private CollectionRequest<Team> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
