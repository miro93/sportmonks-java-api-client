package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.football.model.Odd;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks pre-match odds endpoints ({@code /odds/pre-match}).
public final class PreMatchOddsEndpoint {

    private final ApiExecutor executor;
    private final DataType<List<Odd>> list;

    /// Creates the endpoint, building the {@link Odd} list decoder from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the list response type
    public PreMatchOddsEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.list = codec.listType(Odd.class);
    }

    /// Requests every pre-match odd, paginated.
    ///
    /// @return a collection request for all pre-match odds
    public CollectionRequest<Odd> all() {
        return collection("odds/pre-match");
    }

    /// Requests all pre-match odds for a given fixture.
    ///
    /// @param fixtureId the fixture id
    /// @return a collection request for the matching odds
    public CollectionRequest<Odd> byFixture(long fixtureId) {
        return collection("odds/pre-match/fixtures/" + fixtureId);
    }

    /// Requests pre-match odds for a fixture from a specific bookmaker.
    ///
    /// @param fixtureId   the fixture id
    /// @param bookmakerId the bookmaker id
    /// @return a collection request for the matching odds
    public CollectionRequest<Odd> byFixtureAndBookmaker(long fixtureId, long bookmakerId) {
        return collection("odds/pre-match/fixtures/" + fixtureId + "/bookmakers/" + bookmakerId);
    }

    /// Requests pre-match odds for a fixture in a specific market.
    ///
    /// @param fixtureId the fixture id
    /// @param marketId  the market id
    /// @return a collection request for the matching odds
    public CollectionRequest<Odd> byFixtureAndMarket(long fixtureId, long marketId) {
        return collection("odds/pre-match/fixtures/" + fixtureId + "/markets/" + marketId);
    }

    /// Requests the most recently updated pre-match odds.
    ///
    /// @return a collection request for the latest pre-match odds
    public CollectionRequest<Odd> latest() {
        return collection("odds/pre-match/latest");
    }

    private CollectionRequest<Odd> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
