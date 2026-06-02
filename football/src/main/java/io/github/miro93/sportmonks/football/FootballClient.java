package io.github.miro93.sportmonks.football;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.http.HttpTransport;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.retry.RetryPolicy;
import io.github.miro93.sportmonks.core.retry.RetryingTransport;
import io.github.miro93.sportmonks.core.retry.Sleeper;
import io.github.miro93.sportmonks.football.endpoint.FixturesEndpoint;
import io.github.miro93.sportmonks.football.endpoint.LivescoresEndpoint;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Objects;

/// Entry point for the SportMonks football API. Build via {@link #builder()}.
public final class FootballClient {

    public static final String DEFAULT_BASE_URL = "https://api.sportmonks.com/v3/football";

    private final FixturesEndpoint fixtures;
    private final LivescoresEndpoint livescores;

    private FootballClient(FixturesEndpoint fixtures, LivescoresEndpoint livescores) {
        this.fixtures = fixtures;
        this.livescores = livescores;
    }

    public static Builder builder() {
        return new Builder();
    }

    public FixturesEndpoint fixtures() {
        return fixtures;
    }

    public LivescoresEndpoint livescores() {
        return livescores;
    }

    public static final class Builder {
        private ApiToken apiToken;
        private RetryPolicy retryPolicy = RetryPolicy.defaults();
        private String baseUrl = DEFAULT_BASE_URL;
        private Duration requestTimeout = Duration.ofSeconds(30);

        private Builder() {
        }

        public Builder apiToken(ApiToken apiToken) {
            this.apiToken = apiToken;
            return this;
        }

        public Builder retryPolicy(RetryPolicy retryPolicy) {
            this.retryPolicy = Objects.requireNonNull(retryPolicy, "retryPolicy");
            return this;
        }

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl");
            return this;
        }

        public Builder requestTimeout(Duration requestTimeout) {
            this.requestTimeout = Objects.requireNonNull(requestTimeout, "requestTimeout");
            return this;
        }

        public FootballClient build() {
            Objects.requireNonNull(apiToken, "apiToken is required");
            HttpTransport base = new JdkHttpTransport(HttpClient.newHttpClient(), requestTimeout);
            HttpTransport transport = new RetryingTransport(base, retryPolicy, Sleeper.REAL);
            JacksonCodec codec = new JacksonCodec();
            ApiExecutor executor = new ApiExecutor(transport, codec, apiToken, baseUrl);
            return new FootballClient(
                    new FixturesEndpoint(executor, codec),
                    new LivescoresEndpoint(executor, codec));
        }
    }
}
