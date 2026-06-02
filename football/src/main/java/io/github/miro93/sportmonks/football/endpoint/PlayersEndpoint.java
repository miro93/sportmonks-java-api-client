package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.core.request.SingleResourceRequest;
import io.github.miro93.sportmonks.football.model.Player;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks {@code /players} endpoints.
public final class PlayersEndpoint {

    private final ApiExecutor executor;
    private final DataType<Player> single;
    private final DataType<List<Player>> list;

    /// Creates the endpoint, building the {@link Player} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the single/list response types
    public PlayersEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.single = codec.type(Player.class);
        this.list = codec.listType(Player.class);
    }

    /// Requests every player, paginated.
    ///
    /// @return a collection request for all players
    public CollectionRequest<Player> all() {
        return collection("players");
    }

    /// Requests a single player by its id.
    ///
    /// @param id the player id
    /// @return a single-resource request for that player
    public SingleResourceRequest<Player> byId(long id) {
        return new SingleResourceRequest<>(executor, RequestSpec.builder("players/" + id), single);
    }

    /// Requests all players for a given country.
    ///
    /// @param countryId the country id to filter by
    /// @return a collection request for the matching players
    public CollectionRequest<Player> byCountry(long countryId) {
        return collection("players/countries/" + countryId);
    }

    /// Searches players by name.
    ///
    /// @param name the search term (must not be {@code null})
    /// @return a collection request for the matching players
    /// @throws NullPointerException if {@code name} is {@code null}
    public CollectionRequest<Player> search(String name) {
        Objects.requireNonNull(name, "name");
        return collection("players/search/" + name);
    }

    /// Requests the latest players added to the platform.
    ///
    /// @return a collection request for the latest players
    public CollectionRequest<Player> latest() {
        return collection("players/latest");
    }

    private CollectionRequest<Player> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
