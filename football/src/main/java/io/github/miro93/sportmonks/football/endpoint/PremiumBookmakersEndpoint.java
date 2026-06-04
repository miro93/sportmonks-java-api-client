package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.internal.RequestSpec;
import io.github.miro93.sportmonks.football.model.Bookmaker;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks premium bookmakers endpoint
/// ({@code /bookmakers/premium} on the odds base URL).
public final class PremiumBookmakersEndpoint {

    private final ApiExecutor executor;
    private final DataType<List<Bookmaker>> list;

    /// Creates the endpoint, building the {@link Bookmaker} list decoder from {@code codec}.
    ///
    /// @param executor the executor used to run requests (configured with the odds base URL)
    /// @param codec    the codec used to derive the list response type
    public PremiumBookmakersEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.list = codec.listType(Bookmaker.class);
    }

    /// Requests every premium bookmaker, paginated.
    ///
    /// @return a collection request for all premium bookmakers
    public CollectionRequest<Bookmaker> all() {
        return new CollectionRequest<>(executor, RequestSpec.builder("bookmakers/premium"), list);
    }
}
