package io.github.miro93.sportmonks.motorsport;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.coreapi.CoreClient;
import io.github.miro93.sportmonks.core.http.HttpTransport;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.http.ListeningTransport;
import io.github.miro93.sportmonks.core.json.HelidonJsonCodec;
import io.github.miro93.sportmonks.core.ratelimit.RateLimitPolicy;
import io.github.miro93.sportmonks.core.ratelimit.RateLimitTracker;
import io.github.miro93.sportmonks.core.request.RequestListener;
import io.github.miro93.sportmonks.core.response.RateLimit;
import io.github.miro93.sportmonks.core.retry.RetryPolicy;
import io.github.miro93.sportmonks.core.retry.internal.RetryingTransport;
import io.github.miro93.sportmonks.core.retry.internal.Sleeper;
import io.github.miro93.sportmonks.motorsport.endpoint.DriversEndpoint;
import io.github.miro93.sportmonks.motorsport.endpoint.FixturesEndpoint;
import io.github.miro93.sportmonks.motorsport.endpoint.LapsEndpoint;
import io.github.miro93.sportmonks.motorsport.endpoint.PitstopsEndpoint;
import io.github.miro93.sportmonks.motorsport.endpoint.StintsEndpoint;
import io.github.miro93.sportmonks.motorsport.endpoint.LivescoresEndpoint;
import io.github.miro93.sportmonks.motorsport.endpoint.LeaguesEndpoint;
import io.github.miro93.sportmonks.motorsport.endpoint.RaceResultsEndpoint;
import io.github.miro93.sportmonks.motorsport.endpoint.SchedulesEndpoint;
import io.github.miro93.sportmonks.motorsport.endpoint.SeasonsEndpoint;
import io.github.miro93.sportmonks.motorsport.endpoint.StagesEndpoint;
import io.github.miro93.sportmonks.motorsport.endpoint.StandingsEndpoint;
import io.github.miro93.sportmonks.motorsport.endpoint.StatesEndpoint;
import io.github.miro93.sportmonks.motorsport.endpoint.TeamsEndpoint;
import io.github.miro93.sportmonks.motorsport.endpoint.VenuesEndpoint;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/// Entry point for the SportMonks Motorsport API v3 ({@code /v3/motorsport}): one accessor
/// per endpoint group, plus the embedded Core API client. Build it with {@link #builder()};
/// instances are immutable and thread-safe.
public final class MotorsportClient {

    /// The production Motorsport API base URL.
    public static final String DEFAULT_BASE_URL = "https://api.sportmonks.com/v3/motorsport";

    private final DriversEndpoint drivers;
    private final FixturesEndpoint fixtures;
    private final LapsEndpoint laps;
    private final PitstopsEndpoint pitstops;
    private final StintsEndpoint stints;
    private final LivescoresEndpoint livescores;
    private final LeaguesEndpoint leagues;
    private final RaceResultsEndpoint raceResults;
    private final SchedulesEndpoint schedules;
    private final SeasonsEndpoint seasons;
    private final StagesEndpoint stages;
    private final StandingsEndpoint standings;
    private final StatesEndpoint states;
    private final TeamsEndpoint teams;
    private final VenuesEndpoint venues;
    private final CoreClient core;

    private MotorsportClient(ApiExecutor executor, HelidonJsonCodec codec, CoreClient core) {
        this.drivers = new DriversEndpoint(executor, codec);
        this.fixtures = new FixturesEndpoint(executor, codec);
        this.laps = new LapsEndpoint(executor, codec);
        this.pitstops = new PitstopsEndpoint(executor, codec);
        this.stints = new StintsEndpoint(executor, codec);
        this.livescores = new LivescoresEndpoint(executor, codec);
        this.leagues = new LeaguesEndpoint(executor, codec);
        this.raceResults = new RaceResultsEndpoint(executor, codec);
        this.schedules = new SchedulesEndpoint(executor, codec);
        this.seasons = new SeasonsEndpoint(executor, codec);
        this.stages = new StagesEndpoint(executor, codec);
        this.standings = new StandingsEndpoint(executor, codec);
        this.states = new StatesEndpoint(executor, codec);
        this.teams = new TeamsEndpoint(executor, codec);
        this.venues = new VenuesEndpoint(executor, codec);
        this.core = core;
    }

    /// Starts a new builder.
    ///
    /// @return a fresh builder
    public static Builder builder() {
        return new Builder();
    }

    /// Returns the {@code /drivers} endpoint accessor.
    ///
    /// @return the {@link DriversEndpoint}
    public DriversEndpoint drivers() {
        return drivers;
    }

    /// Returns the {@code /fixtures} endpoint accessor.
    ///
    /// @return the {@link FixturesEndpoint}
    public FixturesEndpoint fixtures() {
        return fixtures;
    }

    /// Returns the {@code /laps} endpoint accessor.
    ///
    /// @return the {@link LapsEndpoint}
    public LapsEndpoint laps() {
        return laps;
    }

    /// Returns the {@code /pitstops} endpoint accessor.
    ///
    /// @return the {@link PitstopsEndpoint}
    public PitstopsEndpoint pitstops() {
        return pitstops;
    }

    /// Returns the {@code /stints} endpoint accessor.
    ///
    /// @return the {@link StintsEndpoint}
    public StintsEndpoint stints() {
        return stints;
    }

    /// Returns the {@code /livescores} endpoint accessor.
    ///
    /// @return the {@link LivescoresEndpoint}
    public LivescoresEndpoint livescores() {
        return livescores;
    }

    /// Returns the {@code /leagues} endpoint accessor.
    ///
    /// @return the {@link LeaguesEndpoint}
    public LeaguesEndpoint leagues() {
        return leagues;
    }

    /// Returns the {@code /raceResults} endpoint accessor.
    ///
    /// @return the {@link RaceResultsEndpoint}
    public RaceResultsEndpoint raceResults() {
        return raceResults;
    }

    /// Returns the {@code /schedules} endpoint accessor.
    ///
    /// @return the {@link SchedulesEndpoint}
    public SchedulesEndpoint schedules() {
        return schedules;
    }

    /// Returns the {@code /seasons} endpoint accessor.
    ///
    /// @return the {@link SeasonsEndpoint}
    public SeasonsEndpoint seasons() {
        return seasons;
    }

    /// Returns the {@code /stages} endpoint accessor.
    ///
    /// @return the {@link StagesEndpoint}
    public StagesEndpoint stages() {
        return stages;
    }

    /// Returns the {@code /standings} endpoint accessor.
    ///
    /// @return the {@link StandingsEndpoint}
    public StandingsEndpoint standings() {
        return standings;
    }

    /// Returns the {@code /states} endpoint accessor.
    ///
    /// @return the {@link StatesEndpoint}
    public StatesEndpoint states() {
        return states;
    }

    /// Returns the {@code /teams} endpoint accessor.
    ///
    /// @return the {@link TeamsEndpoint}
    public TeamsEndpoint teams() {
        return teams;
    }

    /// Returns the {@code /venues} endpoint accessor.
    ///
    /// @return the {@link VenuesEndpoint}
    public VenuesEndpoint venues() {
        return venues;
    }

    /// Returns the SportMonks Core API client (continents, countries, regions, cities,
    /// types) backed by the same credentials and transport.
    ///
    /// @return the embedded {@link CoreClient}
    public CoreClient core() {
        return core;
    }

    /// The last {@code rate_limit} block seen per entity (first path segment) across the
    /// motorsport and core base URLs, for windows that have not reset yet.
    ///
    /// @return a snapshot keyed by entity
    public Map<String, RateLimit> rateLimits() {
        return core.rateLimits();
    }

    /// Fluent builder for {@link MotorsportClient}; mirrors the football builder.
    public static final class Builder {
        private ApiToken apiToken;
        private RetryPolicy retryPolicy = RetryPolicy.defaults();
        private String baseUrl = DEFAULT_BASE_URL;
        private String coreBaseUrl = CoreClient.DEFAULT_BASE_URL;
        private HttpClient httpClient;
        private Duration connectTimeout;
        private RequestListener listener = RequestListener.NOOP;
        private RateLimitPolicy rateLimitPolicy = RateLimitPolicy.NONE;
        private Duration requestTimeout = JdkHttpTransport.DEFAULT_REQUEST_TIMEOUT;
        private long maxResponseSize = JdkHttpTransport.DEFAULT_MAX_RESPONSE_SIZE;

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

        /// Overrides the default retry policy (3 attempts on 429 / 5xx).
        ///
        /// @param retryPolicy the policy
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

        /// Overrides the Core API base URL (defaults to {@link CoreClient#DEFAULT_BASE_URL}).
        ///
        /// @param coreBaseUrl the Core API base URL
        /// @return this builder
        public Builder coreBaseUrl(String coreBaseUrl) {
            this.coreBaseUrl = Objects.requireNonNull(coreBaseUrl, "coreBaseUrl");
            return this;
        }

        /// Sets the request-to-response deadline (defaults to 30 seconds).
        ///
        /// @param requestTimeout the timeout
        /// @return this builder
        public Builder requestTimeout(Duration requestTimeout) {
            this.requestTimeout = Objects.requireNonNull(requestTimeout, "requestTimeout");
            return this;
        }

        /// Caps the response body size read into memory (defaults to
        /// {@link JdkHttpTransport#DEFAULT_MAX_RESPONSE_SIZE}, 32 MiB); larger bodies fail
        /// with a {@code TransportException}.
        ///
        /// @param maxResponseSize the cap in bytes (positive)
        /// @return this builder
        public Builder maxResponseSize(long maxResponseSize) {
            if (maxResponseSize <= 0) {
                throw new IllegalArgumentException("maxResponseSize must be positive, got " + maxResponseSize);
            }
            this.maxResponseSize = maxResponseSize;
            return this;
        }

        /// Supplies a custom {@link HttpClient}; mutually exclusive with {@link #connectTimeout}.
        ///
        /// @param httpClient the client
        /// @return this builder
        public Builder httpClient(HttpClient httpClient) {
            this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
            return this;
        }

        /// Sets the connect timeout of the built-in default client; mutually exclusive with
        /// {@link #httpClient}.
        ///
        /// @param connectTimeout the timeout
        /// @return this builder
        public Builder connectTimeout(Duration connectTimeout) {
            this.connectTimeout = Objects.requireNonNull(connectTimeout, "connectTimeout");
            return this;
        }

        /// Observes every HTTP attempt (each retry separately) for logging or metrics.
        ///
        /// @param listener the listener
        /// @return this builder
        public Builder listener(RequestListener listener) {
            this.listener = Objects.requireNonNull(listener, "listener");
            return this;
        }

        /// Client-side quota guard shared by the motorsport and core executors.
        ///
        /// @param rateLimitPolicy the policy
        /// @return this builder
        public Builder rateLimitPolicy(RateLimitPolicy rateLimitPolicy) {
            this.rateLimitPolicy = Objects.requireNonNull(rateLimitPolicy, "rateLimitPolicy");
            return this;
        }

        /// Builds the client.
        ///
        /// @return the configured client
        /// @throws NullPointerException if no API token was set
        /// @throws IllegalStateException if both {@link #connectTimeout(Duration)} and {@link #httpClient(HttpClient)} are set
        public MotorsportClient build() {
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
            HttpTransport base = new JdkHttpTransport(client, requestTimeout, maxResponseSize);
            HttpTransport transport = new RetryingTransport(
                    new ListeningTransport(base, listener), retryPolicy, Sleeper.REAL);
            HelidonJsonCodec codec = new HelidonJsonCodec();
            RateLimitTracker rateLimits = new RateLimitTracker();
            ApiExecutor executor = new ApiExecutor(transport, codec, apiToken, baseUrl, rateLimits, rateLimitPolicy);
            ApiExecutor coreExecutor = new ApiExecutor(transport, codec, apiToken, coreBaseUrl, rateLimits, rateLimitPolicy);
            return new MotorsportClient(executor, codec, new CoreClient(coreExecutor, codec));
        }
    }
}
