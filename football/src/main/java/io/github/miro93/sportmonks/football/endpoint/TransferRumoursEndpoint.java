package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.HelidonJsonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.SingleResourceRequest;
import io.github.miro93.sportmonks.core.request.internal.RequestSpec;
import io.github.miro93.sportmonks.football.model.TransferRumour;
import io.github.miro93.sportmonks.football.model.TransferRumourConfig;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/// Access to the SportMonks {@code /transfer-rumours} endpoints.
public final class TransferRumoursEndpoint {

    private final ApiExecutor executor;
    private final DataType<List<TransferRumour>> list;
    private final DataType<TransferRumourConfig> config;

    /// Creates the endpoint, building the {@link TransferRumour} list decoder from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the list response type
    public TransferRumoursEndpoint(ApiExecutor executor, HelidonJsonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.list = codec.listType(TransferRumour.class);
        this.config = codec.rawType(TransferRumourConfig.class);
    }

    /// Requests every transfer rumour, paginated.
    ///
    /// @return a collection request for all transfer rumours
    public CollectionRequest<TransferRumour> all() {
        return collection("transfer-rumours");
    }

    /// Requests a transfer rumour by its id. The API documents the payload as a
    /// {@code data} array, hence the collection request.
    ///
    /// @param id the transfer rumour id
    /// @return a collection request for that rumour
    public CollectionRequest<TransferRumour> byId(long id) {
        return collection("transfer-rumours/" + id);
    }

    /// Requests the transfer rumours dated within an inclusive range (at most 31 days).
    ///
    /// @param start the first date (inclusive)
    /// @param end   the last date (inclusive)
    /// @return a collection request for rumours in the range
    public CollectionRequest<TransferRumour> byDateRange(LocalDate start, LocalDate end) {
        return collection("transfer-rumours/between/" + start + "/" + end);
    }

    /// Requests the transfer rumours involving a given team.
    ///
    /// @param teamId the team id
    /// @return a collection request for that team's rumours
    public CollectionRequest<TransferRumour> byTeam(long teamId) {
        return collection("transfer-rumours/teams/" + teamId);
    }

    /// Requests the transfer rumours involving a given player.
    ///
    /// @param playerId the player id
    /// @return a collection request for that player's rumours
    public CollectionRequest<TransferRumour> byPlayer(long playerId) {
        return collection("transfer-rumours/players/" + playerId);
    }

    /// Requests the reference values for fee types, transfer types and rumour statuses.
    /// The response has no {@code data} envelope, so the returned {@code ApiResponse}
    /// carries no pagination or rate-limit block.
    ///
    /// @return a single-resource request for the config
    public SingleResourceRequest<TransferRumourConfig> config() {
        return new SingleResourceRequest<>(executor, RequestSpec.builder("transfer-rumours/config"), config);
    }

    private CollectionRequest<TransferRumour> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
