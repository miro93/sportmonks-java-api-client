package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.football.model.PremiumOdd;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks premium pre-match odds endpoints
/// ({@code /odds/premium}).
public final class PremiumOddsEndpoint {

    private final ApiExecutor executor;
    private final DataType<List<PremiumOdd>> list;

    /// Creates the endpoint, building the {@link PremiumOdd} list decoder from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the list response type
    public PremiumOddsEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.list = codec.listType(PremiumOdd.class);
    }

    /// Requests every premium pre-match odd, paginated.
    ///
    /// @return a collection request for all premium odds
    public CollectionRequest<PremiumOdd> all() {
        return collection("odds/premium");
    }

    /// Requests all premium odds for a given fixture.
    ///
    /// @param fixtureId the fixture id
    /// @return a collection request for the matching premium odds
    public CollectionRequest<PremiumOdd> byFixture(long fixtureId) {
        return collection("odds/premium/fixtures/" + fixtureId);
    }

    /// Requests premium odds for a fixture from a specific bookmaker.
    ///
    /// @param fixtureId   the fixture id
    /// @param bookmakerId the bookmaker id
    /// @return a collection request for the matching premium odds
    public CollectionRequest<PremiumOdd> byFixtureAndBookmaker(long fixtureId, long bookmakerId) {
        return collection("odds/premium/fixtures/" + fixtureId + "/bookmakers/" + bookmakerId);
    }

    /// Requests premium odds for a fixture in a specific market.
    ///
    /// @param fixtureId the fixture id
    /// @param marketId  the market id
    /// @return a collection request for the matching premium odds
    public CollectionRequest<PremiumOdd> byFixtureAndMarket(long fixtureId, long marketId) {
        return collection("odds/premium/fixtures/" + fixtureId + "/markets/" + marketId);
    }

    /// Requests premium odds updated within a time range, expressed as UNIX
    /// timestamps (seconds).
    ///
    /// @param fromEpochSeconds start of the range, as a UNIX timestamp in seconds
    /// @param toEpochSeconds   end of the range, as a UNIX timestamp in seconds
    /// @return a collection request for the premium odds updated in the range
    public CollectionRequest<PremiumOdd> updatedBetween(long fromEpochSeconds, long toEpochSeconds) {
        return collection("odds/premium/updated/between/" + fromEpochSeconds + "/" + toEpochSeconds);
    }

    private CollectionRequest<PremiumOdd> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
