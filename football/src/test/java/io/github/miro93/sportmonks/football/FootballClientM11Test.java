package io.github.miro93.sportmonks.football;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class FootballClientM11Test {

    private FootballClient client(String baseUrl) {
        return FootballClient.builder()
                .apiToken(ApiToken.of("tok"))
                .baseUrl(baseUrl)
                .build();
    }

    @Test
    void exposesExpectedLineups(WireMockRuntimeInfo wm) {
        assertThat(client(wm.getHttpBaseUrl()).expectedLineups()).isNotNull();
    }

    @Test
    void expectedLineupsByTeamDecodesAndSendsAuth(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/expected-lineups/teams/1")).willReturn(okJson("""
                { "data": [{ "id": 1, "team_id": 1, "player_name": "Virgil van Dijk" }] }
                """)));

        var lineups = client(wm.getHttpBaseUrl()).expectedLineups().byTeam(1L).get().data();

        assertThat(lineups).hasSize(1);
        assertThat(lineups.getFirst().playerName()).isEqualTo("Virgil van Dijk");
        verify(getRequestedFor(urlPathEqualTo("/expected-lineups/teams/1"))
                .withHeader("Authorization", equalTo("tok")));
    }
}
