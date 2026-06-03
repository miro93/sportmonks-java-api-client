package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.football.model.Topscorer;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks {@code /topscorers} endpoints.
///
/// <p>Top-scorer entries represent the goal-scoring ranking of players
/// within a given season or stage. All methods return a
/// {@link CollectionRequest} that can be enriched with includes or filters
/// before execution.
public final class TopscorersEndpoint {

    private final ApiExecutor executor;
    private final DataType<List<Topscorer>> list;

    /// Creates the endpoint, building the {@link Topscorer} list decoder from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the list response type
    public TopscorersEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.list = codec.listType(Topscorer.class);
    }

    /// Requests top-scorers filtered to a specific season.
    ///
    /// @param seasonId the season id
    /// @return a collection request for top-scorers in that season
    public CollectionRequest<Topscorer> bySeason(long seasonId) {
        return collection("topscorers/seasons/" + seasonId);
    }

    /// Requests top-scorers filtered to a specific stage.
    ///
    /// @param stageId the stage id
    /// @return a collection request for top-scorers in that stage
    public CollectionRequest<Topscorer> byStage(long stageId) {
        return collection("topscorers/stages/" + stageId);
    }

    private CollectionRequest<Topscorer> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
