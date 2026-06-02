package io.github.miro93.sportmonks.football.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.core.request.SingleResourceRequest;
import io.github.miro93.sportmonks.football.model.Coach;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks {@code /coaches} endpoints.
public final class CoachesEndpoint {

    private final ApiExecutor executor;
    private final DataType<Coach> single;
    private final DataType<List<Coach>> list;

    /// Creates the endpoint, building the {@link Coach} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the single/list response types
    public CoachesEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.single = codec.type(Coach.class);
        this.list = codec.listType(Coach.class);
    }

    /// Requests every coach, paginated.
    ///
    /// @return a collection request for all coaches
    public CollectionRequest<Coach> all() {
        return collection("coaches");
    }

    /// Requests a single coach by its id.
    ///
    /// @param id the coach id
    /// @return a single-resource request for that coach
    public SingleResourceRequest<Coach> byId(long id) {
        return new SingleResourceRequest<>(executor, RequestSpec.builder("coaches/" + id), single);
    }

    /// Requests all coaches for a given country.
    ///
    /// @param countryId the country id to filter by
    /// @return a collection request for the matching coaches
    public CollectionRequest<Coach> byCountry(long countryId) {
        return collection("coaches/countries/" + countryId);
    }

    /// Searches coaches by name.
    ///
    /// @param name the search term (must not be {@code null})
    /// @return a collection request for the matching coaches
    /// @throws NullPointerException if {@code name} is {@code null}
    public CollectionRequest<Coach> search(String name) {
        Objects.requireNonNull(name, "name");
        return collection("coaches/search/" + name);
    }

    /// Requests the latest coaches added to the platform.
    ///
    /// @return a collection request for the latest coaches
    public CollectionRequest<Coach> latest() {
        return collection("coaches/latest");
    }

    private CollectionRequest<Coach> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
