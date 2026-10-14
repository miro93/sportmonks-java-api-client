package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.HelidonJsonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.internal.RequestSpec;
import io.github.miro93.sportmonks.football.model.TeamOfTheWeekEntry;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks {@code /team-of-the-week} endpoints (beta).
public final class TeamOfTheWeekEndpoint {

    private final ApiExecutor executor;
    private final DataType<List<TeamOfTheWeekEntry>> list;

    /// Creates the endpoint, building the {@link TeamOfTheWeekEntry} list decoder from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the list response type
    public TeamOfTheWeekEndpoint(ApiExecutor executor, HelidonJsonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.list = codec.listType(TeamOfTheWeekEntry.class);
    }

    /// Requests every team-of-the-week entry, paginated.
    ///
    /// @return a collection request for all entries
    public CollectionRequest<TeamOfTheWeekEntry> all() {
        return collection("team-of-the-week");
    }

    /// Requests the team of the week of a given round. Not paginated.
    ///
    /// @param roundId the round id
    /// @return a collection request for that round's entries
    public CollectionRequest<TeamOfTheWeekEntry> byRound(long roundId) {
        return collection("team-of-the-week/rounds/" + roundId);
    }

    /// Requests the latest team of the week of a given league. Not paginated.
    ///
    /// @param leagueId the league id
    /// @return a collection request for that league's latest entries
    public CollectionRequest<TeamOfTheWeekEntry> latestByLeague(long leagueId) {
        return collection("team-of-the-week/leagues/" + leagueId + "/latest");
    }

    private CollectionRequest<TeamOfTheWeekEntry> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
