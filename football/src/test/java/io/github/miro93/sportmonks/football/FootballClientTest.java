package io.github.miro93.sportmonks.football;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.error.NotFoundException;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest
class FootballClientTest {

    private FootballClient client(String baseUrl) {
        return FootballClient.builder()
                .apiToken(ApiToken.of("tok-77"))
                .baseUrl(baseUrl)
                .build();
    }

    @Test
    void exposesFixturesAndLivescoresEndpoints(WireMockRuntimeInfo wm) {
        var client = client(wm.getHttpBaseUrl());
        assertThat(client.fixtures()).isNotNull();
        assertThat(client.livescores()).isNotNull();
    }

    @Test
    void endToEndFixtureCallSendsAuthAndDecodes(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/fixtures/1")).willReturn(okJson("""
                { "data": { "id": 1, "name": "A vs B" } }
                """)));

        var fixture = client(wm.getHttpBaseUrl()).fixtures().byId(1L).get().data();

        assertThat(fixture.name()).isEqualTo("A vs B");
        verify(getRequestedFor(urlPathEqualTo("/fixtures/1"))
                .withHeader("Authorization", equalTo("tok-77")));
    }

    @Test
    void builderRequiresApiToken() {
        assertThatThrownBy(() -> FootballClient.builder().build())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void defaultBaseUrlIsSportmonksFootballV3() {
        assertThat(FootballClient.DEFAULT_BASE_URL)
                .isEqualTo("https://api.sportmonks.com/v3/football");
    }

    @Test
    void notFoundPropagatesAsTypedException(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/fixtures/999"))
                .willReturn(aResponse().withStatus(404).withBody("missing")));

        assertThatThrownBy(() -> client(wm.getHttpBaseUrl()).fixtures().byId(999L).get())
                .isInstanceOf(NotFoundException.class);
    }
}
