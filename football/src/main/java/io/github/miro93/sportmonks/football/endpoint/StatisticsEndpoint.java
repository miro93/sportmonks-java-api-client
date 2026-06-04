package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.football.model.Statistic;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks statistics endpoints ({@code /statistics}): season
/// statistics by participant (team, player, coach, referee), plus stage and
/// round statistics.
public final class StatisticsEndpoint {

    private final ApiExecutor executor;
    private final DataType<List<Statistic>> list;

    /// Creates the endpoint, building the {@link Statistic} list decoder from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the list response type
    public StatisticsEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.list = codec.listType(Statistic.class);
    }

    /// Requests the season statistics for a given team.
    ///
    /// @param teamId the team id
    /// @return a collection request for the matching statistics
    public CollectionRequest<Statistic> seasonByTeam(long teamId) {
        return collection("statistics/seasons/teams/" + teamId);
    }

    /// Requests the season statistics for a given player.
    ///
    /// @param playerId the player id
    /// @return a collection request for the matching statistics
    public CollectionRequest<Statistic> seasonByPlayer(long playerId) {
        return collection("statistics/seasons/players/" + playerId);
    }

    /// Requests the season statistics for a given coach.
    ///
    /// @param coachId the coach id
    /// @return a collection request for the matching statistics
    public CollectionRequest<Statistic> seasonByCoach(long coachId) {
        return collection("statistics/seasons/coaches/" + coachId);
    }

    /// Requests the season statistics for a given referee.
    ///
    /// @param refereeId the referee id
    /// @return a collection request for the matching statistics
    public CollectionRequest<Statistic> seasonByReferee(long refereeId) {
        return collection("statistics/seasons/referees/" + refereeId);
    }

    /// Requests the statistics for a given stage.
    ///
    /// @param stageId the stage id
    /// @return a collection request for the matching statistics
    public CollectionRequest<Statistic> byStage(long stageId) {
        return collection("statistics/stages/" + stageId);
    }

    /// Requests the statistics for a given round.
    ///
    /// @param roundId the round id
    /// @return a collection request for the matching statistics
    public CollectionRequest<Statistic> byRound(long roundId) {
        return collection("statistics/rounds/" + roundId);
    }

    private CollectionRequest<Statistic> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
