package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.core.request.SingleResourceRequest;
import io.github.miro93.sportmonks.football.model.Fixture;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/// Access to the SportMonks {@code /fixtures} endpoints.
public final class FixturesEndpoint {

    private final ApiExecutor executor;
    private final DataType<Fixture> single;
    private final DataType<List<Fixture>> list;

    public FixturesEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.single = codec.type(Fixture.class);
        this.list = codec.listType(Fixture.class);
    }

    public CollectionRequest<Fixture> all() {
        return collection("fixtures");
    }

    public SingleResourceRequest<Fixture> byId(long id) {
        return new SingleResourceRequest<>(executor, RequestSpec.builder("fixtures/" + id), single);
    }

    public CollectionRequest<Fixture> byMultipleIds(long... ids) {
        if (ids.length == 0) {
            throw new IllegalArgumentException("byMultipleIds requires at least one id");
        }
        String csv = LongStream.of(ids).mapToObj(Long::toString).collect(Collectors.joining(","));
        return collection("fixtures/multi/" + csv);
    }

    public CollectionRequest<Fixture> byDate(LocalDate date) {
        return collection("fixtures/date/" + date);
    }

    public CollectionRequest<Fixture> byDateRange(LocalDate start, LocalDate end) {
        return collection("fixtures/between/" + start + "/" + end);
    }

    public CollectionRequest<Fixture> byDateRangeForTeam(LocalDate start, LocalDate end, long teamId) {
        return collection("fixtures/between/" + start + "/" + end + "/" + teamId);
    }

    public CollectionRequest<Fixture> headToHead(long teamId1, long teamId2) {
        return collection("fixtures/head-to-head/" + teamId1 + "/" + teamId2);
    }

    public CollectionRequest<Fixture> search(String name) {
        Objects.requireNonNull(name, "name");
        return collection("fixtures/search/" + name);
    }

    private CollectionRequest<Fixture> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
