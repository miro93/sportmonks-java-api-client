package io.github.miro93.sportmonks.motorsport;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.error.NotFoundException;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest
class MotorsportClientTest {

    private MotorsportClient client(String baseUrl) {
        return MotorsportClient.builder()
                .apiToken(ApiToken.of("tok-77"))
                .baseUrl(baseUrl + "/motorsport")
                .coreBaseUrl(baseUrl + "/core")
                .build();
    }

    @Test
    void exposesEveryEndpointGroupAndCore(WireMockRuntimeInfo wm) {
        var client = client(wm.getHttpBaseUrl());
        assertThat(client.drivers()).isNotNull();
        assertThat(client.fixtures()).isNotNull();
        assertThat(client.laps()).isNotNull();
        assertThat(client.pitstops()).isNotNull();
        assertThat(client.stints()).isNotNull();
        assertThat(client.livescores()).isNotNull();
        assertThat(client.leagues()).isNotNull();
        assertThat(client.raceResults()).isNotNull();
        assertThat(client.schedules()).isNotNull();
        assertThat(client.seasons()).isNotNull();
        assertThat(client.stages()).isNotNull();
        assertThat(client.standings()).isNotNull();
        assertThat(client.states()).isNotNull();
        assertThat(client.teams()).isNotNull();
        assertThat(client.venues()).isNotNull();
        assertThat(client.core()).isNotNull();
    }

    @Test
    void endToEndCallSendsAuthDecodesAndTracksRateLimit(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/motorsport/drivers/1")).willReturn(okJson("""
                { "data": { "id": 1, "display_name": "Max Verstappen" },
                  "rate_limit": { "remaining": 2999, "resets_in_seconds": 3600, "requested_entity": "driver" } }
                """)));
        stubFor(get(urlPathEqualTo("/core/countries/38")).willReturn(okJson("{ \"data\": { \"id\": 38, \"name\": \"Netherlands\" } }")));

        var client = client(wm.getHttpBaseUrl());

        assertThat(client.drivers().byId(1L).get().data().displayName()).isEqualTo("Max Verstappen");
        assertThat(client.core().countries().byId(38L).get().data().name()).isEqualTo("Netherlands");
        assertThat(client.rateLimits()).containsKey("drivers");
        verify(getRequestedFor(urlPathEqualTo("/motorsport/drivers/1")).withHeader("Authorization", equalTo("tok-77")));
    }

    @Test
    void mapsErrorsAndRejectsConflictingTimeouts(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/motorsport/teams/404")).willReturn(aResponse().withStatus(404).withBody("nope")));

        assertThatThrownBy(() -> client(wm.getHttpBaseUrl()).teams().byId(404L).get()).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> MotorsportClient.builder().apiToken(ApiToken.of("t"))
                .httpClient(HttpClient.newHttpClient()).connectTimeout(Duration.ofSeconds(1)).build())
                .isInstanceOf(IllegalStateException.class);
    }
}
