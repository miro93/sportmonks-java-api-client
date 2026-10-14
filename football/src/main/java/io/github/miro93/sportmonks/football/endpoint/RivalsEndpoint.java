package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.HelidonJsonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.internal.RequestSpec;
import io.github.miro93.sportmonks.football.model.Rival;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks {@code /rivals} endpoints.
public final class RivalsEndpoint {

    private final ApiExecutor executor;
    private final DataType<List<Rival>> list;

    /// Creates the endpoint, building the {@link Rival} list decoder from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the list response type
    public RivalsEndpoint(ApiExecutor executor, HelidonJsonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.list = codec.listType(Rival.class);
    }

    /// Requests every rivalry within the subscription, paginated.
///
/// @return a collection request for all rivalries
public CollectionRequest<Rival> all() {
    return collection("rivals");
}

    /// Requests the rivals of a given team. Not paginated.
///
/// @param teamId the team id
/// @return a collection request for that team's rivals
public CollectionRequest<Rival> byTeam(long teamId) {
    return collection("rivals/teams/" + teamId);
}

    private CollectionRequest<Rival> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
