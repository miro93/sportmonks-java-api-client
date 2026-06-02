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

    /// Creates the endpoint, building the {@link Fixture} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the single/list response types
    public FixturesEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.single = codec.type(Fixture.class);
        this.list = codec.listType(Fixture.class);
    }

    /// Requests every fixture, paginated.
    ///
    /// @return a collection request for all fixtures
    public CollectionRequest<Fixture> all() {
        return collection("fixtures");
    }

    /// Requests a single fixture by its id.
    ///
    /// @param id the fixture id
    /// @return a single-resource request for that fixture
    public SingleResourceRequest<Fixture> byId(long id) {
        return new SingleResourceRequest<>(executor, RequestSpec.builder("fixtures/" + id), single);
    }

    /// Requests several fixtures by their ids in a single call.
    ///
    /// @param ids one or more fixture ids
    /// @return a collection request for the matching fixtures
    /// @throws IllegalArgumentException if no ids are supplied
    public CollectionRequest<Fixture> byMultipleIds(long... ids) {
        if (ids.length == 0) {
            throw new IllegalArgumentException("byMultipleIds requires at least one id");
        }
        String csv = LongStream.of(ids).mapToObj(Long::toString).collect(Collectors.joining(","));
        return collection("fixtures/multi/" + csv);
    }

    /// Requests the fixtures played on a given date.
    ///
    /// @param date the calendar date
    /// @return a collection request for that date's fixtures
    public CollectionRequest<Fixture> byDate(LocalDate date) {
        return collection("fixtures/date/" + date);
    }

    /// Requests the fixtures played within an inclusive date range.
    ///
    /// @param start the first date (inclusive)
    /// @param end   the last date (inclusive)
    /// @return a collection request for fixtures in the range
    public CollectionRequest<Fixture> byDateRange(LocalDate start, LocalDate end) {
        return collection("fixtures/between/" + start + "/" + end);
    }

    /// Requests a team's fixtures within an inclusive date range.
    ///
    /// @param start  the first date (inclusive)
    /// @param end    the last date (inclusive)
    /// @param teamId the team id to filter by
    /// @return a collection request for the team's fixtures in the range
    public CollectionRequest<Fixture> byDateRangeForTeam(LocalDate start, LocalDate end, long teamId) {
        return collection("fixtures/between/" + start + "/" + end + "/" + teamId);
    }

    /// Requests the head-to-head fixtures between two teams.
    ///
    /// @param teamId1 the first team id
    /// @param teamId2 the second team id
    /// @return a collection request for fixtures between the two teams
    public CollectionRequest<Fixture> headToHead(long teamId1, long teamId2) {
        return collection("fixtures/head-to-head/" + teamId1 + "/" + teamId2);
    }

    /// Searches fixtures by name.
    ///
    /// @param name the search term (must not be {@code null})
    /// @return a collection request for the matching fixtures
    public CollectionRequest<Fixture> search(String name) {
        Objects.requireNonNull(name, "name");
        return collection("fixtures/search/" + name);
    }

    private CollectionRequest<Fixture> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
