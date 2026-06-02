package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.core.request.SingleResourceRequest;
import io.github.miro93.sportmonks.football.model.Transfer;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/// Access to the SportMonks {@code /transfers} endpoints.
///
/// <p>Transfers record player movements between clubs. Methods cover the full
/// listing, single-resource lookup, the latest batch, date-range filtering,
/// and per-team or per-player views. Each entry uses the {@link Transfer}
/// model with optional nested relations ({@code player}, {@code fromTeam},
/// {@code toTeam}) populated via includes.
public final class TransfersEndpoint {

    private final ApiExecutor executor;
    private final DataType<Transfer> single;
    private final DataType<List<Transfer>> list;

    /// Creates the endpoint, building the {@link Transfer} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the single/list response types
    public TransfersEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.single = codec.type(Transfer.class);
        this.list = codec.listType(Transfer.class);
    }

    /// Requests every transfer, paginated.
    ///
    /// @return a collection request for all transfers
    public CollectionRequest<Transfer> all() {
        return collection("transfers");
    }

    /// Requests a single transfer by its id.
    ///
    /// @param id the transfer id
    /// @return a single-resource request for that transfer
    public SingleResourceRequest<Transfer> byId(long id) {
        return new SingleResourceRequest<>(executor, RequestSpec.builder("transfers/" + id), single);
    }

    /// Requests the most recent transfers.
    ///
    /// @return a collection request for the latest transfers
    public CollectionRequest<Transfer> latest() {
        return collection("transfers/latest");
    }

    /// Requests transfers that occurred within an inclusive date range.
    ///
    /// @param start the first date (inclusive, must not be {@code null})
    /// @param end   the last date (inclusive, must not be {@code null})
    /// @return a collection request for transfers in the range
    /// @throws NullPointerException if {@code start} or {@code end} is {@code null}
    public CollectionRequest<Transfer> byDateRange(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");
        return collection("transfers/between/" + start + "/" + end);
    }

    /// Requests all transfers involving a given team.
    ///
    /// @param teamId the team id to filter by
    /// @return a collection request for transfers involving that team
    public CollectionRequest<Transfer> byTeam(long teamId) {
        return collection("transfers/teams/" + teamId);
    }

    /// Requests all transfers involving a given player.
    ///
    /// @param playerId the player id to filter by
    /// @return a collection request for transfers involving that player
    public CollectionRequest<Transfer> byPlayer(long playerId) {
        return collection("transfers/players/" + playerId);
    }

    private CollectionRequest<Transfer> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
