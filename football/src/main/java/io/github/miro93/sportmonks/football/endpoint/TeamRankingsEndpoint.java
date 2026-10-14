package io.github.miro93.sportmonks.football.endpoint;

    import io.github.miro93.sportmonks.core.ApiExecutor;
    import io.github.miro93.sportmonks.core.json.DataType;
    import io.github.miro93.sportmonks.core.json.HelidonJsonCodec;
    import io.github.miro93.sportmonks.core.request.CollectionRequest;
    import io.github.miro93.sportmonks.core.request.internal.RequestSpec;
    import io.github.miro93.sportmonks.football.model.TeamRanking;

import java.time.LocalDate;
    import java.util.List;
    import java.util.Objects;

    /// Access to the SportMonks {@code /team-rankings} endpoints (beta).
    public final class TeamRankingsEndpoint {

        private final ApiExecutor executor;
        private final DataType<List<TeamRanking>> list;

        /// Creates the endpoint, building the {@link TeamRanking} list decoder from {@code codec}.
        ///
        /// @param executor the executor used to run requests
        /// @param codec    the codec used to derive the list response type
        public TeamRankingsEndpoint(ApiExecutor executor, HelidonJsonCodec codec) {
            this.executor = Objects.requireNonNull(executor, "executor");
            Objects.requireNonNull(codec, "codec");
            this.list = codec.listType(TeamRanking.class);
        }

        /// Requests every team ranking entry, paginated.
    ///
    /// @return a collection request for all rankings
    public CollectionRequest<TeamRanking> all() {
        return collection("team-rankings");
    }

        /// Requests the ranking history of a given team.
    ///
    /// @param teamId the team id
    /// @return a collection request for that team's rankings
    public CollectionRequest<TeamRanking> byTeam(long teamId) {
        return collection("team-rankings/teams/" + teamId);
    }

        /// Requests the rankings as of a given date.
    ///
    /// @param date the ranking date
    /// @return a collection request for that date's rankings
    public CollectionRequest<TeamRanking> byDate(LocalDate date) {
        return collection("team-rankings/date/" + date);
    }

        private CollectionRequest<TeamRanking> collection(String path) {
            return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
        }
    }
