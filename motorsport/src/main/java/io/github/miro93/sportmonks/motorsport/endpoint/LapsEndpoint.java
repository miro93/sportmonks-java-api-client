package io.github.miro93.sportmonks.motorsport.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.HelidonJsonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.internal.RequestSpec;
import io.github.miro93.sportmonks.motorsport.model.Lap;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks Motorsport {@code /fixtures/{id}/laps} endpoints.
public final class LapsEndpoint {

    private final ApiExecutor executor;
    private final DataType<List<Lap>> list;

    /// Creates the endpoint, building the {@link Lap} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the response types
    public LapsEndpoint(ApiExecutor executor, HelidonJsonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.list = codec.listType(Lap.class);
    }

    /// Requests every laps entry of a session.
    ///
    /// @param fixtureId the fixture id
    /// @return a collection request
    public CollectionRequest<Lap> byFixture(long fixtureId) {
        return new CollectionRequest<>(executor, RequestSpec.builder("fixtures/" + fixtureId + "/laps"), list);
    }

    /// Requests the laps of one driver in a session.
    ///
    /// @param fixtureId the fixture id
    /// @param driverId the driver id
    /// @return a collection request
    public CollectionRequest<Lap> byFixtureAndDriver(long fixtureId, long driverId) {
        return new CollectionRequest<>(executor, RequestSpec.builder("fixtures/" + fixtureId + "/laps/drivers/" + driverId), list);
    }

    /// Requests the laps of a session by lap number.
    ///
    /// @param fixtureId the fixture id
    /// @param lapNumber the lapNumber
    /// @return a collection request
    public CollectionRequest<Lap> byFixtureAndNumber(long fixtureId, int lapNumber) {
        return new CollectionRequest<>(executor, RequestSpec.builder("fixtures/" + fixtureId + "/laps/" + lapNumber), list);
    }

    /// Requests the latest laps of a session.
    ///
    /// @param fixtureId the fixture id
    /// @return a collection request
    public CollectionRequest<Lap> latestByFixture(long fixtureId) {
        return new CollectionRequest<>(executor, RequestSpec.builder("fixtures/" + fixtureId + "/laps/latest"), list);
    }
}
