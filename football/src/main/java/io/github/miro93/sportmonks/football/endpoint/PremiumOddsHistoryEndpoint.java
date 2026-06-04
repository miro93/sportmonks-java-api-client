package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.internal.RequestSpec;
import io.github.miro93.sportmonks.football.model.HistoricalOdd;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks premium pre-match odds history endpoints
/// ({@code /odds/premium/history}).
public final class PremiumOddsHistoryEndpoint {

    private final ApiExecutor executor;
    private final DataType<List<HistoricalOdd>> list;

    /// Creates the endpoint, building the {@link HistoricalOdd} list decoder from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the list response type
    public PremiumOddsHistoryEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.list = codec.listType(HistoricalOdd.class);
    }

    /// Requests the full history of premium pre-match odds, paginated.
    ///
    /// @return a collection request for all historical premium odds
    public CollectionRequest<HistoricalOdd> all() {
        return collection("odds/premium/history");
    }

    /// Requests historical premium odds updated within a time range, expressed
    /// as UNIX timestamps (seconds).
    ///
    /// @param fromEpochSeconds start of the range, as a UNIX timestamp in seconds
    /// @param toEpochSeconds   end of the range, as a UNIX timestamp in seconds
    /// @return a collection request for the historical premium odds updated in the range
    public CollectionRequest<HistoricalOdd> updatedBetween(long fromEpochSeconds, long toEpochSeconds) {
        return collection("odds/premium/history/updated/between/" + fromEpochSeconds + "/" + toEpochSeconds);
    }

    private CollectionRequest<HistoricalOdd> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
