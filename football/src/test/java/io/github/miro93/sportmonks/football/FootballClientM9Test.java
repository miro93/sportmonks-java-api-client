package io.github.miro93.sportmonks.football;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class FootballClientM9Test {

    private FootballClient client(String baseUrl) {
        return FootballClient.builder()
                .apiToken(ApiToken.of("tok"))
                .baseUrl(baseUrl)
                .build();
    }

    @Test
    void exposesM9Endpoints(WireMockRuntimeInfo wm) {
        var client = client(wm.getHttpBaseUrl());
        assertThat(client.bookmakers()).isNotNull();
        assertThat(client.markets()).isNotNull();
        assertThat(client.preMatchOdds()).isNotNull();
        assertThat(client.inplayOdds()).isNotNull();
    }

    @Test
    void preMatchOddsByFixtureDecodesAndSendsAuth(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/odds/pre-match/fixtures/18533878")).willReturn(okJson("""
                { "data": [{ "id": 1, "fixture_id": 18533878, "value": "1.48" }] }
                """)));

        var odds = client(wm.getHttpBaseUrl()).preMatchOdds().byFixture(18533878L).get().data();

        assertThat(odds).hasSize(1);
        assertThat(odds.getFirst().value()).isEqualTo("1.48");
        verify(getRequestedFor(urlPathEqualTo("/odds/pre-match/fixtures/18533878"))
                .withHeader("Authorization", equalTo("tok")));
    }

    @Test
    void bookmakersByFixtureDecodesAndSendsAuth(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/bookmakers/fixtures/18533878")).willReturn(okJson("""
                { "data": [{ "id": 34, "name": "bet365" }] }
                """)));

        var books = client(wm.getHttpBaseUrl()).bookmakers().byFixture(18533878L).get().data();

        assertThat(books).hasSize(1);
        assertThat(books.getFirst().id()).isEqualTo(34L);
        verify(getRequestedFor(urlPathEqualTo("/bookmakers/fixtures/18533878"))
                .withHeader("Authorization", equalTo("tok")));
    }
}
