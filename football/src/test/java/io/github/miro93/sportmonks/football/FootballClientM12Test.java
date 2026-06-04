package io.github.miro93.sportmonks.football;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class FootballClientM12Test {

    private FootballClient client(String baseUrl) {
        return FootballClient.builder()
                .apiToken(ApiToken.of("tok"))
                .baseUrl(baseUrl)
                .build();
    }

    @Test
    void exposesPredictions(WireMockRuntimeInfo wm) {
        assertThat(client(wm.getHttpBaseUrl()).predictions()).isNotNull();
    }

    @Test
    void valueBetsByFixtureDecodesAndSendsAuth(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/predictions/value-bets/fixtures/18533878")).willReturn(okJson("""
                { "data": [{ "id": 2, "fixture_id": 18533878, "predictions": { "bet": "Home" } }] }
                """)));

        var bets = client(wm.getHttpBaseUrl()).predictions().valueBetsByFixture(18533878L).get().data();

        assertThat(bets).hasSize(1);
        assertThat(bets.getFirst().predictions().bet()).isEqualTo("Home");
        verify(getRequestedFor(urlPathEqualTo("/predictions/value-bets/fixtures/18533878"))
                .withHeader("Authorization", equalTo("tok")));
    }
}
