package io.github.miro93.sportmonks.football;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class FootballClientM13Test {

    private FootballClient client(String baseUrl) {
        return FootballClient.builder()
                .apiToken(ApiToken.of("tok"))
                .baseUrl(baseUrl)
                .build();
    }

    @Test
    void exposesM13Endpoints(WireMockRuntimeInfo wm) {
        var client = client(wm.getHttpBaseUrl());
        assertThat(client.statistics()).isNotNull();
        assertThat(client.expected()).isNotNull();
    }

    @Test
    void seasonByTeamDecodesAndSendsAuth(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/statistics/seasons/teams/1")).willReturn(okJson("""
                { "data": [{ "id": 5001, "team_id": 1 }] }
                """)));

        var stats = client(wm.getHttpBaseUrl()).statistics().seasonByTeam(1L).get().data();

        assertThat(stats).hasSize(1);
        assertThat(stats.getFirst().teamId()).isEqualTo(1L);
        verify(getRequestedFor(urlPathEqualTo("/statistics/seasons/teams/1"))
                .withHeader("Authorization", equalTo("tok")));
    }

    @Test
    void expectedFixturesDecodesAndSendsAuth(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/expected/fixtures")).willReturn(okJson("""
                { "data": [{ "id": 7001, "location": "home" }] }
                """)));

        var xg = client(wm.getHttpBaseUrl()).expected().fixtures().get().data();

        assertThat(xg).hasSize(1);
        assertThat(xg.getFirst().location()).isEqualTo("home");
        verify(getRequestedFor(urlPathEqualTo("/expected/fixtures"))
                .withHeader("Authorization", equalTo("tok")));
    }
}
