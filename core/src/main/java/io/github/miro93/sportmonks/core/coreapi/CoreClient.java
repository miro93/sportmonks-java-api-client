package io.github.miro93.sportmonks.core.coreapi;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.coreapi.endpoint.CitiesEndpoint;
import io.github.miro93.sportmonks.core.coreapi.endpoint.ContinentsEndpoint;
import io.github.miro93.sportmonks.core.coreapi.endpoint.CountriesEndpoint;
import io.github.miro93.sportmonks.core.coreapi.endpoint.RegionsEndpoint;
import io.github.miro93.sportmonks.core.coreapi.endpoint.TypesEndpoint;
import io.github.miro93.sportmonks.core.http.HttpTransport;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.retry.RetryPolicy;
import io.github.miro93.sportmonks.core.retry.internal.RetryingTransport;
import io.github.miro93.sportmonks.core.retry.internal.Sleeper;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Objects;

/// Entry point for the SportMonks Core API (cross-sport resources: continents,
/// countries, regions, cities, types). Build via {@link #builder()}, or obtain
/// one from a football client via {@code FootballClient.core()}.
public final class CoreClient {

    /// The default base URL for the SportMonks Core API.
    public static final String DEFAULT_BASE_URL = "https://api.sportmonks.com/v3/core";

    private final ContinentsEndpoint continents;
    private final CountriesEndpoint countries;
    private final RegionsEndpoint regions;
    private final CitiesEndpoint cities;
    private final TypesEndpoint types;

    /// Creates a client wiring the Core API endpoints onto the given executor.
    /// Shared by {@link Builder#build()} and by {@code FootballClient} so a
    /// football client can expose a Core client over the same transport/token.
    ///
    /// @param executor the executor (already configured with the Core base URL)
    /// @param codec    the codec used to derive response types
    public CoreClient(ApiExecutor executor, JacksonCodec codec) {
        Objects.requireNonNull(executor, "executor");
        Objects.requireNonNull(codec, "codec");
        this.continents = new ContinentsEndpoint(executor, codec);
        this.countries = new CountriesEndpoint(executor, codec);
        this.regions = new RegionsEndpoint(executor, codec);
        this.cities = new CitiesEndpoint(executor, codec);
        this.types = new TypesEndpoint(executor, codec);
    }

    /// Creates a new builder for a {@link CoreClient}.
    ///
    /// @return a fresh builder
    public static Builder builder() {
        return new Builder();
    }

    /// Returns the continents endpoint.
    ///
    /// @return the {@code /continents} endpoint accessor
    public ContinentsEndpoint continents() {
        return continents;
    }

    /// Returns the countries endpoint.
    ///
    /// @return the {@code /countries} endpoint accessor
    public CountriesEndpoint countries() {
        return countries;
    }

    /// Returns the regions endpoint.
    ///
    /// @return the {@code /regions} endpoint accessor
    public RegionsEndpoint regions() {
        return regions;
    }

    /// Returns the cities endpoint.
    ///
    /// @return the {@code /cities} endpoint accessor
    public CitiesEndpoint cities() {
        return cities;
    }

    /// Returns the types endpoint.
    ///
    /// @return the {@code /types} endpoint accessor
    public TypesEndpoint types() {
        return types;
    }

    /// Fluent builder for {@link CoreClient}. The API token is required; the
    /// retry policy, base URL, request timeout, and HTTP client default to sensible values.
    public static final class Builder {
        private ApiToken apiToken;
        private RetryPolicy retryPolicy = RetryPolicy.defaults();
        private String baseUrl = DEFAULT_BASE_URL;
        private HttpClient httpClient;
        private Duration connectTimeout;
        private Duration requestTimeout = JdkHttpTransport.DEFAULT_REQUEST_TIMEOUT;

        private Builder() {
        }

        /// Sets the SportMonks API token used to authenticate requests (required).
        ///
        /// @param apiToken the API token
        /// @return this builder
        public Builder apiToken(ApiToken apiToken) {
            this.apiToken = apiToken;
            return this;
        }

        /// Overrides the retry policy applied to transient failures.
        ///
        /// @param retryPolicy the retry policy to use
        /// @return this builder
        public Builder retryPolicy(RetryPolicy retryPolicy) {
            this.retryPolicy = Objects.requireNonNull(retryPolicy, "retryPolicy");
            return this;
        }

        /// Overrides the API base URL (defaults to {@link #DEFAULT_BASE_URL}).
        ///
        /// @param baseUrl the base URL
        /// @return this builder
        public Builder baseUrl(String baseUrl) {
            this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl");
            return this;
        }

        /// Overrides the per-request timeout (defaults to {@link JdkHttpTransport#DEFAULT_REQUEST_TIMEOUT}).
        ///
        /// @param requestTimeout the request timeout
        /// @return this builder
        public Builder requestTimeout(Duration requestTimeout) {
            this.requestTimeout = Objects.requireNonNull(requestTimeout, "requestTimeout");
            return this;
        }

        /// Overrides the underlying JDK {@link HttpClient} used for all requests.
        /// When not set, a default client is used (explicit 10s connect timeout, NORMAL redirects).
        /// This is distinct from {@link #requestTimeout(Duration)}: the client's connect timeout
        /// bounds connection establishment, while requestTimeout bounds the request→response deadline.
        ///
        /// @param httpClient the HTTP client to use
        /// @return this builder
        public Builder httpClient(HttpClient httpClient) {
            this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
            return this;
        }

        /// Sets the connection-establishment timeout for the built-in default {@link HttpClient}.
        /// Only meaningful when no custom {@link #httpClient(HttpClient)} is supplied — a
        /// user-provided client carries its own (immutable) connect timeout. Supplying BOTH is
        /// rejected at {@link #build()}. When unset, defaults to
        /// {@link JdkHttpTransport#DEFAULT_CONNECT_TIMEOUT}.
        ///
        /// @param connectTimeout the connect timeout (non-null)
        /// @return this builder
        public Builder connectTimeout(Duration connectTimeout) {
            this.connectTimeout = Objects.requireNonNull(connectTimeout, "connectTimeout");
            return this;
        }

        /// Builds the configured {@link CoreClient}.
        ///
        /// @return a ready-to-use client
        /// @throws NullPointerException if no API token was set
        /// @throws IllegalStateException if both {@link #connectTimeout(Duration)} and {@link #httpClient(HttpClient)} are set
        public CoreClient build() {
            Objects.requireNonNull(apiToken, "apiToken is required");
            if (httpClient != null && connectTimeout != null) {
                throw new IllegalStateException(
                        "connectTimeout() and httpClient() are mutually exclusive: a supplied "
                        + "HttpClient carries its own connect timeout");
            }
            HttpClient client = (httpClient != null)
                    ? httpClient
                    : JdkHttpTransport.newDefaultClient(
                          connectTimeout != null ? connectTimeout : JdkHttpTransport.DEFAULT_CONNECT_TIMEOUT);
            HttpTransport base = new JdkHttpTransport(client, requestTimeout);
            HttpTransport transport = new RetryingTransport(base, retryPolicy, Sleeper.REAL);
            JacksonCodec codec = new JacksonCodec();
            ApiExecutor executor = new ApiExecutor(transport, codec, apiToken, baseUrl);
            return new CoreClient(executor, codec);
        }
    }
}
