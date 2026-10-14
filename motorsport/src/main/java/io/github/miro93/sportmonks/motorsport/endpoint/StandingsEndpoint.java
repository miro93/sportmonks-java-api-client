package io.github.miro93.sportmonks.motorsport.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.HelidonJsonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.internal.RequestSpec;
import io.github.miro93.sportmonks.motorsport.model.Standing;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks Motorsport {@code /standings} endpoints.
public final class StandingsEndpoint {

    private final ApiExecutor executor;
    private final DataType<List<Standing>> list;

    /// Creates the endpoint, building the {@link Standing} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the response types
    public StandingsEndpoint(ApiExecutor executor, HelidonJsonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.list = codec.listType(Standing.class);
    }

    /// Requests every driver standing.
    ///
    /// @return a collection request
    public CollectionRequest<Standing> drivers() {
        return new CollectionRequest<>(executor, RequestSpec.builder("standings/drivers"), list);
    }

    /// Requests every team standing.
    ///
    /// @return a collection request
    public CollectionRequest<Standing> teams() {
        return new CollectionRequest<>(executor, RequestSpec.builder("standings/teams"), list);
    }

    /// Requests the driver standings of a season.
    ///
    /// @param seasonId the season id
    /// @return a collection request
    public CollectionRequest<Standing> driversBySeason(long seasonId) {
        return new CollectionRequest<>(executor, RequestSpec.builder("standings/drivers/seasons/" + seasonId), list);
    }

    /// Requests the team standings of a season.
    ///
    /// @param seasonId the season id
    /// @return a collection request
    public CollectionRequest<Standing> teamsBySeason(long seasonId) {
        return new CollectionRequest<>(executor, RequestSpec.builder("standings/teams/seasons/" + seasonId), list);
    }
}
