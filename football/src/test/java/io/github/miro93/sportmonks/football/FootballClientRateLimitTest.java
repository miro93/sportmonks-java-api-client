package io.github.miro93.sportmonks.football;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.error.RateLimitException;
import io.github.miro93.sportmonks.core.ratelimit.RateLimitPolicy;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest
class FootballClientRateLimitTest {

    @Test
    void exposesQuotaAcrossBaseUrlsAndGuardsRequests(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/football/fixtures/1")).willReturn(okJson("""
                { "data": { "id": 1 }, "rate_limit": { "remaining": 1, "resets_in_seconds": 900, "requested_entity": "fixture" } }
                """)));
        stubFor(get(urlPathEqualTo("/core/countries/1")).willReturn(okJson("""
                { "data": { "id": 1 }, "rate_limit": { "remaining": 2500, "resets_in_seconds": 900, "requested_entity": "country" } }
                """)));
        var client = FootballClient.builder()
                .apiToken(ApiToken.of("tok"))
                .baseUrl(wm.getHttpBaseUrl() + "/football")
                .coreBaseUrl(wm.getHttpBaseUrl() + "/core")
                .rateLimitPolicy(RateLimitPolicy.failBelow(10))
                .build();

        client.fixtures().byId(1L).get();
        client.core().countries().byId(1L).get();

        assertThat(client.rateLimits()).containsOnlyKeys("fixtures", "countries");
        assertThat(client.rateLimits().get("fixtures").remaining()).isEqualTo(1);
        assertThatThrownBy(() -> client.fixtures().byId(1L).get()).isInstanceOf(RateLimitException.class);
        assertThat(client.core().countries().byId(1L).get().data().id()).isEqualTo(1L);
    }
}
