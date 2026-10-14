package io.github.miro93.sportmonks.motorsport.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.HelidonJsonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.SingleResourceRequest;
import io.github.miro93.sportmonks.core.request.internal.RequestSpec;
import io.github.miro93.sportmonks.motorsport.model.Fixture;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/// Access to the SportMonks Motorsport {@code /fixtures} endpoints (sessions).
public final class FixturesEndpoint {

    private final ApiExecutor executor;
    private final DataType<Fixture> single;
    private final DataType<List<Fixture>> list;

    /// Creates the endpoint, building the {@link Fixture} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the response types
    public FixturesEndpoint(ApiExecutor executor, HelidonJsonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.single = codec.type(Fixture.class);
        this.list = codec.listType(Fixture.class);
    }

    /// Requests every session.
    ///
    /// @return a collection request
    public CollectionRequest<Fixture> all() {
        return new CollectionRequest<>(executor, RequestSpec.builder("fixtures"), list);
    }

    /// Requests a single session by its id.
    ///
    /// @param id the id
    /// @return a single-resource request
    public SingleResourceRequest<Fixture> byId(long id) {
        return new SingleResourceRequest<>(executor, RequestSpec.builder("fixtures/" + id), single);
    }

    /// Requests several sessions by their ids (at most 50).
    ///
    /// @param ids the ids
    /// @return a collection request
    public CollectionRequest<Fixture> byMultipleIds(long... ids) {
        return new CollectionRequest<>(executor, RequestSpec.builder("fixtures/multi/" + LongStream.of(ids).mapToObj(Long::toString).collect(Collectors.joining(","))), list);
    }

    /// Requests the sessions of a calendar date.
    ///
    /// @param date the date
    /// @return a collection request
    public CollectionRequest<Fixture> byDate(LocalDate date) {
        return new CollectionRequest<>(executor, RequestSpec.builder("fixtures/date/" + date), list);
    }

    /// Requests the sessions within an inclusive date range.
    ///
    /// @param start the start
    /// @param end the end
    /// @return a collection request
    public CollectionRequest<Fixture> byDateRange(LocalDate start, LocalDate end) {
        return new CollectionRequest<>(executor, RequestSpec.builder("fixtures/between/" + start + "/" + end), list);
    }

    /// Requests the sessions updated recently.
    ///
    /// @return a collection request
    public CollectionRequest<Fixture> latest() {
        return new CollectionRequest<>(executor, RequestSpec.builder("fixtures/latest"), list);
    }
}
