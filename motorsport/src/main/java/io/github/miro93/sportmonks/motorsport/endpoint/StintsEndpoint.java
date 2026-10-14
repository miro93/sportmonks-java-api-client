package io.github.miro93.sportmonks.motorsport.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.HelidonJsonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.internal.RequestSpec;
import io.github.miro93.sportmonks.motorsport.model.Stint;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks Motorsport {@code /fixtures/{id}/stints} endpoints.
public final class StintsEndpoint {

    private final ApiExecutor executor;
    private final DataType<List<Stint>> list;

    /// Creates the endpoint, building the {@link Stint} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the response types
    public StintsEndpoint(ApiExecutor executor, HelidonJsonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.list = codec.listType(Stint.class);
    }

    /// Requests every stints entry of a session.
    ///
    /// @param fixtureId the fixture id
    /// @return a collection request
    public CollectionRequest<Stint> byFixture(long fixtureId) {
        return new CollectionRequest<>(executor, RequestSpec.builder("fixtures/" + fixtureId + "/stints"), list);
    }

    /// Requests the stints of one driver in a session.
    ///
    /// @param fixtureId the fixture id
    /// @param driverId the driver id
    /// @return a collection request
    public CollectionRequest<Stint> byFixtureAndDriver(long fixtureId, long driverId) {
        return new CollectionRequest<>(executor, RequestSpec.builder("fixtures/" + fixtureId + "/stints/drivers/" + driverId), list);
    }

    /// Requests the stints of a session by stint number.
    ///
    /// @param fixtureId the fixture id
    /// @param stintNumber the stintNumber
    /// @return a collection request
    public CollectionRequest<Stint> byFixtureAndNumber(long fixtureId, int stintNumber) {
        return new CollectionRequest<>(executor, RequestSpec.builder("fixtures/" + fixtureId + "/stints/" + stintNumber), list);
    }

    /// Requests the latest stints of a session.
    ///
    /// @param fixtureId the fixture id
    /// @return a collection request
    public CollectionRequest<Stint> latestByFixture(long fixtureId) {
        return new CollectionRequest<>(executor, RequestSpec.builder("fixtures/" + fixtureId + "/stints/latest"), list);
    }
}
