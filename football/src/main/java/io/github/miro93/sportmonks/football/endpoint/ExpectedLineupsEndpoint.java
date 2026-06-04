package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.football.model.ExpectedLineup;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks premium expected lineups endpoints
/// ({@code /expected-lineups}): pre-match predicted lineups by team and by player.
public final class ExpectedLineupsEndpoint {

    private final ApiExecutor executor;
    private final DataType<List<ExpectedLineup>> list;

    /// Creates the endpoint, building the {@link ExpectedLineup} list decoder from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the list response type
    public ExpectedLineupsEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.list = codec.listType(ExpectedLineup.class);
    }

    /// Requests the expected lineup entries for a given team.
    ///
    /// @param teamId the team id
    /// @return a collection request for the matching expected lineup entries
    public CollectionRequest<ExpectedLineup> byTeam(long teamId) {
        return collection("expected-lineups/teams/" + teamId);
    }

    /// Requests the expected lineup entries for a given player.
    ///
    /// @param playerId the player id
    /// @return a collection request for the matching expected lineup entries
    public CollectionRequest<ExpectedLineup> byPlayer(long playerId) {
        return collection("expected-lineups/players/" + playerId);
    }

    private CollectionRequest<ExpectedLineup> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
