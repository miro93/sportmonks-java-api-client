package io.github.miro93.sportmonks.core;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.cache.CachePolicy;
import io.github.miro93.sportmonks.core.cache.InMemoryResponseCache;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.HelidonJsonCodec;
import io.github.miro93.sportmonks.core.ratelimit.RateLimitPolicy;
import io.github.miro93.sportmonks.core.ratelimit.RateLimitTracker;
import io.github.miro93.sportmonks.core.request.internal.RequestSpec;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class ApiExecutorCacheTest {

    private final HelidonJsonCodec codec = new HelidonJsonCodec();

    private ApiExecutor executor(String baseUrl, CachePolicy policy) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        return new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl, new RateLimitTracker(), RateLimitPolicy.NONE, policy);
    }

    @Test
    void servesCachedEntitiesWithoutCallingTheApiAgain(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/types")).willReturn(okJson("""
                { "data": [ { "id": 1, "name": "Goal" } ],
                  "rate_limit": { "remaining": 2999, "resets_in_seconds": 3600, "requested_entity": "type" } }
                """)));
        stubFor(get(urlPathEqualTo("/fixtures")).willReturn(okJson("{ \"data\": [ { \"id\": 7 } ] }")));
        ApiExecutor executor = executor(wm.getHttpBaseUrl(),
                CachePolicy.inMemory(100, Map.of("types", Duration.ofHours(1))));
        var types = codec.listType(ApiExecutorTest.Team.class);

        executor.execute(RequestSpec.builder("types").build(), types);
        executor.execute(RequestSpec.builder("types").build(), types);
        executor.execute(RequestSpec.builder("types").include("x").build(), types);   // different URI
        executor.execute(RequestSpec.builder("fixtures").build(), types);
        executor.execute(RequestSpec.builder("fixtures").build(), types);

        verify(1, getRequestedFor(urlEqualTo("/types")));
        verify(1, getRequestedFor(urlEqualTo("/types?include=x")));
        verify(2, getRequestedFor(urlEqualTo("/fixtures")));
        assertThat(executor.rateLimits().latest("types")).map(l -> l.remaining()).contains(2999);
    }

    @Test
    void doesNotCacheErrors(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/types")).willReturn(aResponse().withStatus(500).withBody("boom")));
        var cache = new InMemoryResponseCache(10);
        ApiExecutor executor = executor(wm.getHttpBaseUrl(), new CachePolicy(cache, Map.of("types", Duration.ofHours(1))));

        try {
            executor.execute(RequestSpec.builder("types").build(), codec.listType(ApiExecutorTest.Team.class));
        } catch (RuntimeException expected) {
            // mapped error
        }

        assertThat(cache.size()).isZero();
    }
}
