package io.github.miro93.sportmonks.football;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.coreapi.CoreClient;
import io.github.miro93.sportmonks.core.http.HttpTransport;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.retry.RetryPolicy;
import io.github.miro93.sportmonks.core.retry.RetryingTransport;
import io.github.miro93.sportmonks.core.retry.Sleeper;
import io.github.miro93.sportmonks.football.endpoint.CoachesEndpoint;
import io.github.miro93.sportmonks.football.endpoint.FixturesEndpoint;
import io.github.miro93.sportmonks.football.endpoint.LeaguesEndpoint;
import io.github.miro93.sportmonks.football.endpoint.LivescoresEndpoint;
import io.github.miro93.sportmonks.football.endpoint.PlayersEndpoint;
import io.github.miro93.sportmonks.football.endpoint.RoundsEndpoint;
import io.github.miro93.sportmonks.football.endpoint.SchedulesEndpoint;
import io.github.miro93.sportmonks.football.endpoint.SeasonsEndpoint;
import io.github.miro93.sportmonks.football.endpoint.SquadsEndpoint;
import io.github.miro93.sportmonks.football.endpoint.StagesEndpoint;
import io.github.miro93.sportmonks.football.endpoint.StandingsEndpoint;
import io.github.miro93.sportmonks.football.endpoint.TeamsEndpoint;
import io.github.miro93.sportmonks.football.endpoint.TopscorersEndpoint;
import io.github.miro93.sportmonks.football.endpoint.TransfersEndpoint;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Objects;

/// Entry point for the SportMonks football API. Build via {@link #builder()}.
public final class FootballClient {

    public static final String DEFAULT_BASE_URL = "https://api.sportmonks.com/v3/football";

    private final FixturesEndpoint fixtures;
    private final LivescoresEndpoint livescores;
    private final LeaguesEndpoint leagues;
    private final SeasonsEndpoint seasons;
    private final StagesEndpoint stages;
    private final RoundsEndpoint rounds;
    private final SchedulesEndpoint schedules;
    private final TeamsEndpoint teams;
    private final PlayersEndpoint players;
    private final CoachesEndpoint coaches;
    private final SquadsEndpoint squads;
    private final TransfersEndpoint transfers;
    private final StandingsEndpoint standings;
    private final TopscorersEndpoint topscorers;
    private final CoreClient core;

    private FootballClient(
            FixturesEndpoint fixtures,
            LivescoresEndpoint livescores,
            LeaguesEndpoint leagues,
            SeasonsEndpoint seasons,
            StagesEndpoint stages,
            RoundsEndpoint rounds,
            SchedulesEndpoint schedules,
            TeamsEndpoint teams,
            PlayersEndpoint players,
            CoachesEndpoint coaches,
            SquadsEndpoint squads,
            TransfersEndpoint transfers,
            StandingsEndpoint standings,
            TopscorersEndpoint topscorers,
            CoreClient core) {
        this.fixtures = fixtures;
        this.livescores = livescores;
        this.leagues = leagues;
        this.seasons = seasons;
        this.stages = stages;
        this.rounds = rounds;
        this.schedules = schedules;
        this.teams = teams;
        this.players = players;
        this.coaches = coaches;
        this.squads = squads;
        this.transfers = transfers;
        this.standings = standings;
        this.topscorers = topscorers;
        this.core = core;
    }

    /// Creates a new builder for a {@link FootballClient}.
    ///
    /// @return a fresh builder
    public static Builder builder() {
        return new Builder();
    }

    /// Returns the fixtures endpoint.
    ///
    /// @return the {@code /fixtures} endpoint accessor
    public FixturesEndpoint fixtures() {
        return fixtures;
    }

    /// Returns the livescores endpoint.
    ///
    /// @return the {@code /livescores} endpoint accessor
    public LivescoresEndpoint livescores() {
        return livescores;
    }

    /// Returns the leagues endpoint.
    ///
    /// @return the {@code /leagues} endpoint accessor
    public LeaguesEndpoint leagues() {
        return leagues;
    }

    /// Returns the seasons endpoint.
    ///
    /// @return the {@code /seasons} endpoint accessor
    public SeasonsEndpoint seasons() {
        return seasons;
    }

    /// Returns the stages endpoint.
    ///
    /// @return the {@code /stages} endpoint accessor
    public StagesEndpoint stages() {
        return stages;
    }

    /// Returns the rounds endpoint.
    ///
    /// @return the {@code /rounds} endpoint accessor
    public RoundsEndpoint rounds() {
        return rounds;
    }

    /// Returns the schedules endpoint.
    ///
    /// @return the {@code /schedules} endpoint accessor
    public SchedulesEndpoint schedules() {
        return schedules;
    }

    /// Returns the teams endpoint.
    ///
    /// @return the {@code /teams} endpoint accessor
    public TeamsEndpoint teams() {
        return teams;
    }

    /// Returns the players endpoint.
    ///
    /// @return the {@code /players} endpoint accessor
    public PlayersEndpoint players() {
        return players;
    }

    /// Returns the coaches endpoint.
    ///
    /// @return the {@code /coaches} endpoint accessor
    public CoachesEndpoint coaches() {
        return coaches;
    }

    /// Returns the squads endpoint.
    ///
    /// @return the {@code /squads} endpoint accessor
    public SquadsEndpoint squads() {
        return squads;
    }

    /// Returns the transfers endpoint.
    ///
    /// @return the {@code /transfers} endpoint accessor
    public TransfersEndpoint transfers() {
        return transfers;
    }

    /// Returns the standings endpoint.
    ///
    /// @return the {@code /standings} endpoint accessor
    public StandingsEndpoint standings() {
        return standings;
    }

    /// Returns the topscorers endpoint.
    ///
    /// @return the {@code /topscorers} endpoint accessor
    public TopscorersEndpoint topscorers() {
        return topscorers;
    }

    /// Returns the SportMonks Core API client (continents, countries, regions,
    /// cities, types) backed by the same credentials and transport.
    ///
    /// @return the embedded {@link CoreClient}
    public CoreClient core() {
        return core;
    }

    /// Fluent builder for {@link FootballClient}. The API token is required; the
    /// retry policy, base URL and request timeout default to sensible values.
    public static final class Builder {
        private ApiToken apiToken;
        private RetryPolicy retryPolicy = RetryPolicy.defaults();
        private String baseUrl = DEFAULT_BASE_URL;
        private String coreBaseUrl = CoreClient.DEFAULT_BASE_URL;
        private Duration requestTimeout = Duration.ofSeconds(30);

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

        /// Overrides the SportMonks Core API base URL used by {@link FootballClient#core()}
        /// (defaults to {@link CoreClient#DEFAULT_BASE_URL}).
        ///
        /// @param coreBaseUrl the Core API base URL
        /// @return this builder
        public Builder coreBaseUrl(String coreBaseUrl) {
            this.coreBaseUrl = Objects.requireNonNull(coreBaseUrl, "coreBaseUrl");
            return this;
        }

        /// Overrides the per-request timeout (defaults to 30 seconds).
        ///
        /// @param requestTimeout the request timeout
        /// @return this builder
        public Builder requestTimeout(Duration requestTimeout) {
            this.requestTimeout = Objects.requireNonNull(requestTimeout, "requestTimeout");
            return this;
        }

        /// Builds the configured {@link FootballClient}.
        ///
        /// @return a ready-to-use client
        /// @throws NullPointerException if no API token was set
        public FootballClient build() {
            Objects.requireNonNull(apiToken, "apiToken is required");
            HttpTransport base = new JdkHttpTransport(HttpClient.newHttpClient(), requestTimeout);
            HttpTransport transport = new RetryingTransport(base, retryPolicy, Sleeper.REAL);
            JacksonCodec codec = new JacksonCodec();
            ApiExecutor executor = new ApiExecutor(transport, codec, apiToken, baseUrl);
            ApiExecutor coreExecutor = new ApiExecutor(transport, codec, apiToken, coreBaseUrl);
            CoreClient core = new CoreClient(coreExecutor, codec);
            return new FootballClient(
                    new FixturesEndpoint(executor, codec),
                    new LivescoresEndpoint(executor, codec),
                    new LeaguesEndpoint(executor, codec),
                    new SeasonsEndpoint(executor, codec),
                    new StagesEndpoint(executor, codec),
                    new RoundsEndpoint(executor, codec),
                    new SchedulesEndpoint(executor, codec),
                    new TeamsEndpoint(executor, codec),
                    new PlayersEndpoint(executor, codec),
                    new CoachesEndpoint(executor, codec),
                    new SquadsEndpoint(executor, codec),
                    new TransfersEndpoint(executor, codec),
                    new StandingsEndpoint(executor, codec),
                    new TopscorersEndpoint(executor, codec),
                    core);
        }
    }
}
