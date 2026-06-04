package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.internal.RequestSpec;
import io.github.miro93.sportmonks.football.model.Stage;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks {@code /schedules} endpoints.
///
/// <p>A schedule is a stage tree that nests rounds and their fixtures;
/// it reuses the {@link Stage} model (with optional {@code rounds} → {@code fixtures}
/// populated via includes).
public final class SchedulesEndpoint {

    private final ApiExecutor executor;
    private final DataType<List<Stage>> list;

    /// Creates the endpoint, building the {@link Stage} list decoder from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the list response type
    public SchedulesEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.list = codec.listType(Stage.class);
    }

    /// Requests the schedule for a given season.
    ///
    /// @param seasonId the season id
    /// @return a collection request for the stages in that season's schedule
    public CollectionRequest<Stage> bySeason(long seasonId) {
        return collection("schedules/seasons/" + seasonId);
    }

    /// Requests the schedule for a given team.
    ///
    /// @param teamId the team id
    /// @return a collection request for the stages in that team's schedule
    public CollectionRequest<Stage> byTeam(long teamId) {
        return collection("schedules/teams/" + teamId);
    }

    /// Requests the schedule for a given season filtered to a specific team.
    ///
    /// @param seasonId the season id
    /// @param teamId   the team id
    /// @return a collection request for the matching stages
    public CollectionRequest<Stage> bySeasonAndTeam(long seasonId, long teamId) {
        return collection("schedules/seasons/" + seasonId + "/teams/" + teamId);
    }

    private CollectionRequest<Stage> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
