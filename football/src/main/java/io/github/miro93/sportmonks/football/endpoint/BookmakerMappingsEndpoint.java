package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.HelidonJsonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.internal.RequestSpec;
import io.github.miro93.sportmonks.football.model.BookmakerMapping;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks bookmaker match-id mappings
/// ({@code bookmakers/fixtures/{id}/mapping}), served from the {@code /v3/odds} base URL.
public final class BookmakerMappingsEndpoint {

    private final ApiExecutor executor;
    private final DataType<List<BookmakerMapping>> list;

    /// Creates the endpoint, building the {@link BookmakerMapping} list decoder from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the list response type
    public BookmakerMappingsEndpoint(ApiExecutor executor, HelidonJsonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.list = codec.listType(BookmakerMapping.class);
    }

    /// Requests the bookmaker event ids mapped to a given fixture. Not paginated.
    ///
    /// @param fixtureId the fixture id
    /// @return a collection request for that fixture's bookmaker mappings
    public CollectionRequest<BookmakerMapping> byFixture(long fixtureId) {
        return collection("bookmakers/fixtures/" + fixtureId + "/mapping");
    }

    private CollectionRequest<BookmakerMapping> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
