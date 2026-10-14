package io.github.miro93.sportmonks.core;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.error.RateLimitException;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.HelidonJsonCodec;
import io.github.miro93.sportmonks.core.ratelimit.RateLimitPolicy;
import io.github.miro93.sportmonks.core.ratelimit.RateLimitTracker;
import io.github.miro93.sportmonks.core.request.internal.RequestSpec;
import io.github.miro93.sportmonks.core.retry.internal.Sleeper;
import io.helidon.json.binding.Json;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest
class ApiExecutorRateLimitTest {

    @Json.Entity
    record Team(long id) {
    }

    private final HelidonJsonCodec codec = new HelidonJsonCodec();
    private final List<Duration> sleeps = new ArrayList<>();

    private ApiExecutor executor(String baseUrl, RateLimitPolicy policy) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        return new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl,
                Executors.newVirtualThreadPerTaskExecutor(), new RateLimitTracker(), policy, sleeps::add);
    }

    private static void stubWithRemaining(int remaining) {
        stubFor(get(urlPathEqualTo("/teams/1")).willReturn(okJson("""
                { "data": { "id": 1 },
                  "rate_limit": { "remaining": %d, "resets_in_seconds": 120, "requested_entity": "team" } }
                """.formatted(remaining))));
    }

    @Test
    void recordsRateLimitPerEntity(WireMockRuntimeInfo wm) {
        stubWithRemaining(2999);
        ApiExecutor executor = executor(wm.getHttpBaseUrl(), RateLimitPolicy.NONE);

        executor.execute(RequestSpec.builder("teams/1").build(), codec.type(Team.class));

        assertThat(executor.rateLimits().snapshot()).containsOnlyKeys("teams");
        assertThat(executor.rateLimits().latest("teams")).map(l -> l.remaining()).contains(2999);
    }

    @Test
    void failPolicyRejectsTheNextRequestBelowThreshold(WireMockRuntimeInfo wm) {
        stubWithRemaining(3);
        ApiExecutor executor = executor(wm.getHttpBaseUrl(), RateLimitPolicy.failBelow(5));
        RequestSpec spec = RequestSpec.builder("teams/1").build();

        executor.execute(spec, codec.type(Team.class));

        assertThatThrownBy(() -> executor.execute(spec, codec.type(Team.class)))
                .isInstanceOf(RateLimitException.class)
                .satisfies(e -> assertThat(((RateLimitException) e).retryAfter()).hasValueSatisfying(
                        d -> assertThat(d).isBetween(Duration.ofSeconds(110), Duration.ofSeconds(120))));
        verify(1, getRequestedFor(urlPathEqualTo("/teams/1")));
    }

    @Test
    void waitPolicySleepsUntilResetThenSends(WireMockRuntimeInfo wm) {
        stubWithRemaining(3);
        ApiExecutor executor = executor(wm.getHttpBaseUrl(), RateLimitPolicy.waitBelow(5));
        RequestSpec spec = RequestSpec.builder("teams/1").build();

        executor.execute(spec, codec.type(Team.class));
        executor.execute(spec, codec.type(Team.class));

        assertThat(sleeps).hasSize(1);
        assertThat(sleeps.getFirst()).isBetween(Duration.ofSeconds(110), Duration.ofSeconds(120));
        verify(2, getRequestedFor(urlPathEqualTo("/teams/1")));
    }

    @Test
    void otherEntitiesAreNotAffected(WireMockRuntimeInfo wm) {
        stubWithRemaining(0);
        stubFor(get(urlPathEqualTo("/players/1")).willReturn(okJson("{ \"data\": { \"id\": 1 } }")));
        ApiExecutor executor = executor(wm.getHttpBaseUrl(), RateLimitPolicy.failBelow(5));

        executor.execute(RequestSpec.builder("teams/1").build(), codec.type(Team.class));
        executor.execute(RequestSpec.builder("players/1").build(), codec.type(Team.class));

        verify(1, getRequestedFor(urlPathEqualTo("/players/1")));
    }
}
