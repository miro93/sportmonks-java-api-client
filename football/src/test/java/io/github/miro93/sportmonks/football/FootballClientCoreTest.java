package io.github.miro93.sportmonks.football;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class FootballClientCoreTest {

    @Test
    void exposesCoreClient(WireMockRuntimeInfo wm) {
        var client = FootballClient.builder()
                .apiToken(ApiToken.of("tok"))
                .baseUrl(wm.getHttpBaseUrl())
                .build();

        assertThat(client.core()).isNotNull();
        assertThat(client.core().countries()).isNotNull();
    }

    @Test
    void coreUsesConfiguredCoreBaseUrlAndSendsAuth(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/countries")).willReturn(okJson("""
                { "data": [ { "id": 320, "name": "Scotland" } ] }
                """)));

        var client = FootballClient.builder()
                .apiToken(ApiToken.of("tok"))
                .baseUrl(wm.getHttpBaseUrl())
                .coreBaseUrl(wm.getHttpBaseUrl())
                .build();

        var response = client.core().countries().all().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().name()).isEqualTo("Scotland");
        verify(getRequestedFor(urlPathEqualTo("/countries"))
                .withHeader("Authorization", equalTo("tok")));
    }
}
