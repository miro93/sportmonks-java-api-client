package io.github.miro93.sportmonks.motorsport.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.HelidonJsonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.internal.RequestSpec;
import io.github.miro93.sportmonks.motorsport.model.Stage;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks Motorsport {@code /schedules} endpoint.
public final class SchedulesEndpoint {

    private final ApiExecutor executor;
    private final DataType<List<Stage>> list;

    /// Creates the endpoint, building the {@link Stage} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the response types
    public SchedulesEndpoint(ApiExecutor executor, HelidonJsonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.list = codec.listType(Stage.class);
    }

    /// Requests the race weekends of a season with their sessions.
    ///
    /// @param seasonId the season id
    /// @return a collection request
    public CollectionRequest<Stage> bySeason(long seasonId) {
        return new CollectionRequest<>(executor, RequestSpec.builder("schedules/seasons/" + seasonId), list);
    }
}
