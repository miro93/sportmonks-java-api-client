package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.football.model.Odd;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks in-play (live) odds endpoints ({@code /odds/inplay}).
public final class InplayOddsEndpoint {

    private final ApiExecutor executor;
    private final DataType<List<Odd>> list;

    /// Creates the endpoint, building the {@link Odd} list decoder from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the list response type
    public InplayOddsEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.list = codec.listType(Odd.class);
    }

    /// Requests every in-play odd, paginated.
    ///
    /// @return a collection request for all in-play odds
    public CollectionRequest<Odd> all() {
        return collection("odds/inplay");
    }

    /// Requests all in-play odds for a given fixture.
    ///
    /// @param fixtureId the fixture id
    /// @return a collection request for the matching odds
    public CollectionRequest<Odd> byFixture(long fixtureId) {
        return collection("odds/inplay/fixtures/" + fixtureId);
    }

    /// Requests in-play odds for a fixture from a specific bookmaker.
    ///
    /// @param fixtureId   the fixture id
    /// @param bookmakerId the bookmaker id
    /// @return a collection request for the matching odds
    public CollectionRequest<Odd> byFixtureAndBookmaker(long fixtureId, long bookmakerId) {
        return collection("odds/inplay/fixtures/" + fixtureId + "/bookmakers/" + bookmakerId);
    }

    /// Requests in-play odds for a fixture in a specific market.
    ///
    /// @param fixtureId the fixture id
    /// @param marketId  the market id
    /// @return a collection request for the matching odds
    public CollectionRequest<Odd> byFixtureAndMarket(long fixtureId, long marketId) {
        return collection("odds/inplay/fixtures/" + fixtureId + "/markets/" + marketId);
    }

    /// Requests the most recently updated in-play odds.
    ///
    /// @return a collection request for the latest in-play odds
    public CollectionRequest<Odd> latest() {
        return collection("odds/inplay/latest");
    }

    private CollectionRequest<Odd> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
