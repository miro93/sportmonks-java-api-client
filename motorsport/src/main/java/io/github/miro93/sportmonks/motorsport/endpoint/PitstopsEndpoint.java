package io.github.miro93.sportmonks.motorsport.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.HelidonJsonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.internal.RequestSpec;
import io.github.miro93.sportmonks.motorsport.model.Pitstop;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks Motorsport {@code /fixtures/{id}/pitstops} endpoints.
public final class PitstopsEndpoint {

    private final ApiExecutor executor;
    private final DataType<List<Pitstop>> list;

    /// Creates the endpoint, building the {@link Pitstop} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the response types
    public PitstopsEndpoint(ApiExecutor executor, HelidonJsonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.list = codec.listType(Pitstop.class);
    }

    /// Requests every pit stops entry of a session.
    ///
    /// @param fixtureId the fixture id
    /// @return a collection request
    public CollectionRequest<Pitstop> byFixture(long fixtureId) {
        return new CollectionRequest<>(executor, RequestSpec.builder("fixtures/" + fixtureId + "/pitstops"), list);
    }

    /// Requests the pit stops of one driver in a session.
    ///
    /// @param fixtureId the fixture id
    /// @param driverId the driver id
    /// @return a collection request
    public CollectionRequest<Pitstop> byFixtureAndDriver(long fixtureId, long driverId) {
        return new CollectionRequest<>(executor, RequestSpec.builder("fixtures/" + fixtureId + "/pitstops/drivers/" + driverId), list);
    }

    /// Requests the pit stops of a session by lap number.
    ///
    /// @param fixtureId the fixture id
    /// @param lapNumber the lapNumber
    /// @return a collection request
    public CollectionRequest<Pitstop> byFixtureAndNumber(long fixtureId, int lapNumber) {
        return new CollectionRequest<>(executor, RequestSpec.builder("fixtures/" + fixtureId + "/pitstops/" + lapNumber), list);
    }

    /// Requests the latest pit stops of a session.
    ///
    /// @param fixtureId the fixture id
    /// @return a collection request
    public CollectionRequest<Pitstop> latestByFixture(long fixtureId) {
        return new CollectionRequest<>(executor, RequestSpec.builder("fixtures/" + fixtureId + "/pitstops/latest"), list);
    }
}
