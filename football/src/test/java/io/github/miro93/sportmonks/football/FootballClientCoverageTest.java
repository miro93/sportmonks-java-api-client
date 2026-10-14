package io.github.miro93.sportmonks.football;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class FootballClientCoverageTest {

    private FootballClient client(String baseUrl) {
        return FootballClient.builder()
                .apiToken(ApiToken.of("tok"))
                .baseUrl(baseUrl + "/football")
                .oddsBaseUrl(baseUrl + "/odds")
                .build();
    }

    @Test
    void exposesCoverageGapEndpoints(WireMockRuntimeInfo wm) {
        var client = client(wm.getHttpBaseUrl());
        assertThat(client.rivals()).isNotNull();
        assertThat(client.transferRumours()).isNotNull();
        assertThat(client.news()).isNotNull();
        assertThat(client.teamRankings()).isNotNull();
        assertThat(client.teamOfTheWeek()).isNotNull();
        assertThat(client.matchFacts()).isNotNull();
        assertThat(client.bookmakerMappings()).isNotNull();
    }

    @Test
    void rivalsUseFootballBaseUrlAndSendAuth(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/football/rivals/teams/53")).willReturn(okJson("""
                { "data": [ { "team_id": 53, "rival_id": 62 } ] }
                """)));

        var rivals = client(wm.getHttpBaseUrl()).rivals().byTeam(53L).get().data();

        assertThat(rivals.getFirst().rivalId()).isEqualTo(62L);
        verify(getRequestedFor(urlPathEqualTo("/football/rivals/teams/53"))
                .withHeader("Authorization", equalTo("tok")));
    }

    @Test
    void bookmakerMappingsUseOddsBaseUrl(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/odds/bookmakers/fixtures/19427243/mapping")).willReturn(okJson("""
                { "data": [ { "fixture_id": 19427243, "bookmaker_id": 2 } ] }
                """)));

        var mappings = client(wm.getHttpBaseUrl()).bookmakerMappings().byFixture(19427243L).get().data();

        assertThat(mappings.getFirst().bookmakerId()).isEqualTo(2L);
        verify(0, getRequestedFor(urlPathMatching("/football/.*")));
    }
}
