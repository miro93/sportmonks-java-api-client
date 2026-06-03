package io.github.miro93.sportmonks.core.coreapi.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.coreapi.model.City;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.core.request.SingleResourceRequest;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks Core API {@code /cities} endpoints.
public final class CitiesEndpoint {

    private final ApiExecutor executor;
    private final DataType<City> single;
    private final DataType<List<City>> list;

    /// Creates the endpoint, building the {@link City} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the single/list response types
    public CitiesEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.single = codec.type(City.class);
        this.list = codec.listType(City.class);
    }

    /// Requests every city, paginated.
    ///
    /// @return a collection request for all cities
    public CollectionRequest<City> all() {
        return collection("cities");
    }

    /// Requests a single city by its id.
    ///
    /// @param id the city id
    /// @return a single-resource request for that city
    public SingleResourceRequest<City> byId(long id) {
        return new SingleResourceRequest<>(executor, RequestSpec.builder("cities/" + id), single);
    }

    /// Searches cities by name.
    ///
    /// @param name the search term (must not be {@code null})
    /// @return a collection request for the matching cities
    /// @throws NullPointerException if {@code name} is {@code null}
    public CollectionRequest<City> search(String name) {
        Objects.requireNonNull(name, "name");
        return collection("cities/search/" + name);
    }

    private CollectionRequest<City> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
