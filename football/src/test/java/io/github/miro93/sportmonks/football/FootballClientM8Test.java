package io.github.miro93.sportmonks.football;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class FootballClientM8Test {

    private FootballClient client(String baseUrl) {
        return FootballClient.builder()
                .apiToken(ApiToken.of("tok"))
                .baseUrl(baseUrl)
                .build();
    }

    @Test
    void exposesM8Endpoints(WireMockRuntimeInfo wm) {
        var client = client(wm.getHttpBaseUrl());
        assertThat(client.states()).isNotNull();
        assertThat(client.venues()).isNotNull();
        assertThat(client.referees()).isNotNull();
        assertThat(client.tvStations()).isNotNull();
        assertThat(client.commentaries()).isNotNull();
    }

    @Test
    void venuesBySeasonDecodesAndSendsAuth(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/venues/seasons/19735")).willReturn(okJson("""
                { "data": [{ "id": 8909, "name": "Celtic Park" }] }
                """)));

        var venues = client(wm.getHttpBaseUrl()).venues().bySeason(19735L).get().data();

        assertThat(venues).hasSize(1);
        assertThat(venues.getFirst().id()).isEqualTo(8909L);
        verify(getRequestedFor(urlPathEqualTo("/venues/seasons/19735"))
                .withHeader("Authorization", equalTo("tok")));
    }

    @Test
    void commentariesByFixtureDecodesAndSendsAuth(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/commentaries/fixtures/18535517")).willReturn(okJson("""
                { "data": [{ "id": "c-1", "fixture_id": 18535517, "comment": "Kick-off" }] }
                """)));

        var comments = client(wm.getHttpBaseUrl()).commentaries().byFixture(18535517L).get().data();

        assertThat(comments).hasSize(1);
        assertThat(comments.getFirst().id()).isEqualTo("c-1");
        verify(getRequestedFor(urlPathEqualTo("/commentaries/fixtures/18535517"))
                .withHeader("Authorization", equalTo("tok")));
    }
}
