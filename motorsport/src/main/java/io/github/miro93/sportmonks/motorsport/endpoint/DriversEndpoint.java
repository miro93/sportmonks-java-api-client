package io.github.miro93.sportmonks.motorsport.endpoint;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.HelidonJsonCodec;
import io.github.miro93.sportmonks.core.request.CollectionRequest;
import io.github.miro93.sportmonks.core.request.SingleResourceRequest;
import io.github.miro93.sportmonks.core.request.internal.RequestSpec;
import io.github.miro93.sportmonks.motorsport.model.Driver;

import java.util.List;
import java.util.Objects;

/// Access to the SportMonks Motorsport {@code /drivers} endpoints.
public final class DriversEndpoint {

    private final ApiExecutor executor;
    private final DataType<Driver> single;
    private final DataType<List<Driver>> list;

    /// Creates the endpoint, building the {@link Driver} decoders from {@code codec}.
    ///
    /// @param executor the executor used to run requests
    /// @param codec    the codec used to derive the response types
    public DriversEndpoint(ApiExecutor executor, HelidonJsonCodec codec) {
        this.executor = Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.single = codec.type(Driver.class);
        this.list = codec.listType(Driver.class);
    }

    /// Requests every driver.
    ///
    /// @return a collection request
    public CollectionRequest<Driver> all() {
        return new CollectionRequest<>(executor, RequestSpec.builder("drivers"), list);
    }

    /// Requests a single driver by its id.
    ///
    /// @param id the id
    /// @return a single-resource request
    public SingleResourceRequest<Driver> byId(long id) {
        return new SingleResourceRequest<>(executor, RequestSpec.builder("drivers/" + id), single);
    }

    /// Requests the drivers of a country.
    ///
    /// @param countryId the country id
    /// @return a collection request
    public CollectionRequest<Driver> byCountry(long countryId) {
        return new CollectionRequest<>(executor, RequestSpec.builder("drivers/countries/" + countryId), list);
    }

    /// Requests the drivers of a season.
    ///
    /// @param seasonId the season id
    /// @return a collection request
    public CollectionRequest<Driver> bySeason(long seasonId) {
        return new CollectionRequest<>(executor, RequestSpec.builder("drivers/seasons/" + seasonId), list);
    }

    /// Searches drivers by name.
    ///
    /// @param name the name
    /// @return a collection request
    public CollectionRequest<Driver> search(String name) {
        Objects.requireNonNull(name, "name");
        return new CollectionRequest<>(executor, RequestSpec.builder("drivers/search/" + name), list);
    }

    /// Requests the drivers updated recently.
    ///
    /// @return a collection request
    public CollectionRequest<Driver> latest() {
        return new CollectionRequest<>(executor, RequestSpec.builder("drivers/latest"), list);
    }
}
