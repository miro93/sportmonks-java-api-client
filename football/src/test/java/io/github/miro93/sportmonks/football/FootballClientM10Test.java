package io.github.miro93.sportmonks.football;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class FootballClientM10Test {

    private FootballClient client(String baseUrl) {
        return FootballClient.builder()
                .apiToken(ApiToken.of("tok"))
                .baseUrl(baseUrl)
                .build();
    }

    @Test
    void exposesM10Endpoints(WireMockRuntimeInfo wm) {
        var client = client(wm.getHttpBaseUrl());
        assertThat(client.premiumOdds()).isNotNull();
        assertThat(client.premiumOddsHistory()).isNotNull();
        assertThat(client.premiumMarkets()).isNotNull();
        assertThat(client.premiumBookmakers()).isNotNull();
    }

    @Test
    void premiumOddsByFixtureUsesFootballExecutorAndSendsAuth(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/odds/premium/fixtures/18533878")).willReturn(okJson("""
                { "data": [{ "id": 1, "fixture_id": 18533878, "value": "1.48" }] }
                """)));

        var odds = client(wm.getHttpBaseUrl()).premiumOdds().byFixture(18533878L).get().data();

        assertThat(odds).hasSize(1);
        assertThat(odds.getFirst().value()).isEqualTo("1.48");
        verify(getRequestedFor(urlPathEqualTo("/odds/premium/fixtures/18533878"))
                .withHeader("Authorization", equalTo("tok")));
    }

    @Test
    void premiumMarketsUsesConfiguredOddsBaseUrlAndSendsAuth(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/markets/premium")).willReturn(okJson("""
                { "data": [{ "id": 1, "name": "Fulltime Result" }] }
                """)));

        var client = FootballClient.builder()
                .apiToken(ApiToken.of("tok"))
                .baseUrl(wm.getHttpBaseUrl())
                .oddsBaseUrl(wm.getHttpBaseUrl())
                .build();

        var markets = client.premiumMarkets().all().get().data();

        assertThat(markets).hasSize(1);
        assertThat(markets.getFirst().name()).isEqualTo("Fulltime Result");
        verify(getRequestedFor(urlPathEqualTo("/markets/premium"))
                .withHeader("Authorization", equalTo("tok")));
    }

    @Test
    void premiumBookmakersUsesConfiguredOddsBaseUrlAndSendsAuth(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/bookmakers/premium")).willReturn(okJson("""
                { "data": [{ "id": 34, "name": "bet365" }] }
                """)));

        var client = FootballClient.builder()
                .apiToken(ApiToken.of("tok"))
                .baseUrl(wm.getHttpBaseUrl())
                .oddsBaseUrl(wm.getHttpBaseUrl())
                .build();

        var bookmakers = client.premiumBookmakers().all().get().data();

        assertThat(bookmakers).hasSize(1);
        assertThat(bookmakers.getFirst().name()).isEqualTo("bet365");
        verify(getRequestedFor(urlPathEqualTo("/bookmakers/premium"))
                .withHeader("Authorization", equalTo("tok")));
    }
}
