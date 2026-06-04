package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.football.model.Market;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks premium markets endpoint ({@code /markets/premium}
/// on the odds base URL).
public final class PremiumMarketsEndpoint {

    private final ApiExecutor executor;
    private final DataType<List<Market>> list;

    /// Creates the endpoint, building the {@link Market} list decoder from {@code codec}.
    ///
    /// @param executor the executor used to run requests (configured with the odds base URL)
    /// @param codec    the codec used to derive the list response type
    public PremiumMarketsEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.list = codec.listType(Market.class);
    }

    /// Requests every premium market, paginated.
    ///
    /// @return a collection request for all premium markets
    public CollectionRequest<Market> all() {
        return new CollectionRequest<>(executor, RequestSpec.builder("markets/premium"), list);
    }
}
