package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.football.model.Standing;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks {@code /standings} endpoints.
///
/// <p>Standings represent the league table position of each participant
/// for a given season, round, or live league context. All methods return
/// a {@link CollectionRequest} that can be enriched with includes or filters
/// before execution.
public final class StandingsEndpoint {

    private final ApiExecutor executor;
    private final DataType<List<Standing>> list;

    /// Creates the endpoint, building the {@link Standing} list decoder from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the list response type
    public StandingsEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.list = codec.listType(Standing.class);
    }

    /// Requests all standings across all seasons and leagues.
    ///
    /// @return a collection request for all standings
    public CollectionRequest<Standing> all() {
        return collection("standings");
    }

    /// Requests standings filtered to a specific season.
    ///
    /// @param seasonId the season id
    /// @return a collection request for standings in that season
    public CollectionRequest<Standing> bySeason(long seasonId) {
        return collection("standings/seasons/" + seasonId);
    }

    /// Requests standings filtered to a specific round.
    ///
    /// @param roundId the round id
    /// @return a collection request for standings in that round
    public CollectionRequest<Standing> byRound(long roundId) {
        return collection("standings/rounds/" + roundId);
    }

    /// Requests correction standings for a specific season.
    ///
    /// @param seasonId the season id
    /// @return a collection request for correction standings in that season
    public CollectionRequest<Standing> correctionsBySeason(long seasonId) {
        return collection("standings/corrections/seasons/" + seasonId);
    }

    /// Requests live standings for a specific league.
    ///
    /// @param leagueId the league id
    /// @return a collection request for the live standings of that league
    public CollectionRequest<Standing> liveByLeague(long leagueId) {
        return collection("standings/leagues/" + leagueId + "/live");
    }

    private CollectionRequest<Standing> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
