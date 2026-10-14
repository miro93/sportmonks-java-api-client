package io.github.miro93.sportmonks.motorsport.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.HelidonJsonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.internal.RequestSpec;
import io.github.miro93.sportmonks.motorsport.model.Fixture;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks Motorsport {@code /livescores} endpoint.
public final class LivescoresEndpoint {

    private final ApiExecutor executor;
    private final DataType<List<Fixture>> list;

    /// Creates the endpoint, building the {@link Fixture} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the response types
    public LivescoresEndpoint(ApiExecutor executor, HelidonJsonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.list = codec.listType(Fixture.class);
    }

    /// Requests the sessions currently live.
    ///
    /// @return a collection request
    public CollectionRequest<Fixture> all() {
        return new CollectionRequest<>(executor, RequestSpec.builder("livescores"), list);
    }
}
