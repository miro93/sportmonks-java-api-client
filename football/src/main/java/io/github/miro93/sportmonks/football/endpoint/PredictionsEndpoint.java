package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.internal.RequestSpec;
import io.github.miro93.sportmonks.football.model.Predictability;
import io.github.miro93.sportmonks.football.model.Prediction;
import io.github.miro93.sportmonks.football.model.ValueBet;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks predictions endpoints ({@code /predictions}):
/// probabilities and value bets by fixture, plus predictability by league.
public final class PredictionsEndpoint {

    private final ApiExecutor executor;
    private final DataType<List<Prediction>> predictionList;
    private final DataType<List<ValueBet>> valueBetList;
    private final DataType<List<Predictability>> predictabilityList;

    /// Creates the endpoint, building the list decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the list response types
    public PredictionsEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.predictionList = codec.listType(Prediction.class);
        this.valueBetList = codec.listType(ValueBet.class);
        this.predictabilityList = codec.listType(Predictability.class);
    }

    /// Requests every predicted-probability record, paginated.
    ///
    /// @return a collection request for all probability predictions
    public CollectionRequest<Prediction> probabilities() {
        return new CollectionRequest<>(executor, RequestSpec.builder("predictions/probabilities"), predictionList);
    }

    /// Requests the probability predictions for a given fixture.
    ///
    /// @param fixtureId the fixture id
    /// @return a collection request for the matching probability predictions
    public CollectionRequest<Prediction> probabilitiesByFixture(long fixtureId) {
        return new CollectionRequest<>(executor, RequestSpec.builder("predictions/probabilities/fixtures/" + fixtureId), predictionList);
    }

    /// Requests every value-bet record, paginated.
    ///
    /// @return a collection request for all value bets
    public CollectionRequest<ValueBet> valueBets() {
        return new CollectionRequest<>(executor, RequestSpec.builder("predictions/value-bets"), valueBetList);
    }

    /// Requests the value bets for a given fixture.
    ///
    /// @param fixtureId the fixture id
    /// @return a collection request for the matching value bets
    public CollectionRequest<ValueBet> valueBetsByFixture(long fixtureId) {
        return new CollectionRequest<>(executor, RequestSpec.builder("predictions/value-bets/fixtures/" + fixtureId), valueBetList);
    }

    /// Requests the predictability records for a given league.
    ///
    /// @param leagueId the league id
    /// @return a collection request for the matching predictability records
    public CollectionRequest<Predictability> predictabilityByLeague(long leagueId) {
        return new CollectionRequest<>(executor, RequestSpec.builder("predictions/predictability/leagues/" + leagueId), predictabilityList);
    }
}
