package io.github.miro93.sportmonks.core.coreapi.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.coreapi.model.Country;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.internal.RequestSpec;
import io.github.miro93.sportmonks.core.request.SingleResourceRequest;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks Core API {@code /countries} endpoints.
public final class CountriesEndpoint {

    private final ApiExecutor executor;
    private final DataType<Country> single;
    private final DataType<List<Country>> list;

    /// Creates the endpoint, building the {@link Country} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the single/list response types
    public CountriesEndpoint(ApiExecutor executor, JacksonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.single = codec.type(Country.class);
        this.list = codec.listType(Country.class);
    }

    /// Requests every country, paginated.
    ///
    /// @return a collection request for all countries
    public CollectionRequest<Country> all() {
        return collection("countries");
    }

    /// Requests a single country by its id.
    ///
    /// @param id the country id
    /// @return a single-resource request for that country
    public SingleResourceRequest<Country> byId(long id) {
        return new SingleResourceRequest<>(executor, RequestSpec.builder("countries/" + id), single);
    }

    /// Searches countries by name.
    ///
    /// @param name the search term (must not be {@code null})
    /// @return a collection request for the matching countries
    /// @throws NullPointerException if {@code name} is {@code null}
    public CollectionRequest<Country> search(String name) {
        Objects.requireNonNull(name, "name");
        return collection("countries/search/" + name);
    }

    private CollectionRequest<Country> collection(String path) {
        return new CollectionRequest<>(executor, RequestSpec.builder(path), list);
    }
}
