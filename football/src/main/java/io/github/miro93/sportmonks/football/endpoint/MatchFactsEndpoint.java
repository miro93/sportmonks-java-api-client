package io.github.miro93.sportmonks.football.endpoint;

    import io.github.miro93.sportmonks.core.ApiExecutor;
    import io.github.miro93.sportmonks.core.json.DataType;
    import io.github.miro93.sportmonks.core.json.HelidonJsonCodec;
    import io.github.miro93.sportmonks.core.request.CollectionRequest;
    import io.github.miro93.sportmonks.core.request.internal.RequestSpec;
    import io.github.miro93.sportmonks.football.model.MatchFact;

import java.time.LocalDate;
    import java.util.List;
    import java.util.Objects;

    /// Access to the SportMonks {@code /match-facts} endpoints.
    public final class MatchFactsEndpoint {

        private final ApiExecutor executor;
        private final DataType<List<MatchFact>> list;

        /// Creates the endpoint, building the {@link MatchFact} list decoder from {@code codec}.
        ///
        /// @param executor the executor used to run requests
        /// @param codec    the codec used to derive the list response type
        public MatchFactsEndpoint(ApiExecutor executor, HelidonJsonCodec codec) {
            this.executor = Objects.requireNonNull(executor, "executor");
            Objects.requireNonNull(codec, "codec");
            this.list = codec.listType(MatchFact.class);
        }

        /// Requests every available match fact, paginated.
    ///
    /// @return a collection request for all match facts
    public CollectionRequest<MatchFact> all() {
        return collection("match-facts");
    }

        /// Requests the match facts of a given fixture.
    ///
    /// @param fixtureId the fixture id
    /// @return a collection request for that fixture's match facts
    public CollectionRequest<MatchFact> byFixture(long fixtureId) {
        return collection("match-facts/" + fixtureId);
    }

        /// Requests the match facts of the fixtures within an inclusive date range.
    ///
    /// @param start the first date (inclusive)
    /// @param end   the last date (inclusive)
    /// @return a collection request for match facts in the range
    public CollectionRequest<MatchFact> byDateRange(LocalDate start, LocalDate end) {
        return collection("match-facts/fixtures/between/" + start + "/" + end);
    }

        /// Requests the match facts of a given league.
    ///
    /// @param leagueId the league id
    /// @return a collection request for that league's match facts
    public CollectionRequest<MatchFact> byLeague(long leagueId) {
        return collection("match-facts/leagues/" + leagueId);
    }

        private CollectionRequest<MatchFact> collection(String path) {
            return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
        }
    }
