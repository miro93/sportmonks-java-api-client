package io.github.miro93.sportmonks.motorsport.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.HelidonJsonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.internal.RequestSpec;
import io.github.miro93.sportmonks.motorsport.model.Stage;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks Motorsport {@code /results} endpoints: the race weekends of a
/// season with their sessions and the lineup (grid position, result details) of one driver
/// or team.
public final class RaceResultsEndpoint {

    private final ApiExecutor executor;
    private final DataType<List<Stage>> list;

    /// Creates the endpoint, building the {@link Stage} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the response types
    public RaceResultsEndpoint(ApiExecutor executor, HelidonJsonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.list = codec.listType(Stage.class);
    }

    /// Requests the results of a driver over a season.
    ///
    /// @param seasonId the season id
    /// @param driverId the driver id
    /// @return a collection request
    public CollectionRequest<Stage> bySeasonAndDriver(long seasonId, long driverId) {
        return new CollectionRequest<>(executor, RequestSpec.builder("results/seasons/" + seasonId + "/drivers/" + driverId), list);
    }

    /// Requests the results of a team over a season.
    ///
    /// @param seasonId the season id
    /// @param teamId the team id
    /// @return a collection request
    public CollectionRequest<Stage> bySeasonAndTeam(long seasonId, long teamId) {
        return new CollectionRequest<>(executor, RequestSpec.builder("results/seasons/" + seasonId + "/teams/" + teamId), list);
    }
}
