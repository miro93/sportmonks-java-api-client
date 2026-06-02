package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.football.model.Squad;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks {@code /squads} endpoints.
///
/// <p>A squad is the list of players associated with a team in a given context
/// (current team roster or a season-scoped squad). Each entry uses the
/// {@link Squad} model with an optional nested {@code player} relation
/// populated via includes.
public final class SquadsEndpoint {

    private final ApiExecutor executor;
    private final DataType<List<Squad>> list;

    /// Creates the endpoint, building the {@link Squad} list decoder from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the list response type
    public SquadsEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.list = codec.listType(Squad.class);
    }

    /// Requests the squad for a given team.
    ///
    /// @param teamId the team id
    /// @return a collection request for the squad members of that team
    public CollectionRequest<Squad> byTeam(long teamId) {
        return collection("squads/teams/" + teamId);
    }

    /// Requests the squad for a given team filtered to a specific season.
    ///
    /// @param seasonId the season id
    /// @param teamId   the team id
    /// @return a collection request for the squad members of that team in the season
    public CollectionRequest<Squad> bySeasonAndTeam(long seasonId, long teamId) {
        return collection("squads/seasons/" + seasonId + "/teams/" + teamId);
    }

    private CollectionRequest<Squad> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
