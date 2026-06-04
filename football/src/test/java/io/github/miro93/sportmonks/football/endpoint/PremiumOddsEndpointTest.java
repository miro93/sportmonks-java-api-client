package io.github.miro93.sportmonks.football.endpoint;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class PremiumOddsEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private PremiumOddsEndpoint odds(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new PremiumOddsEndpoint(executor, codec);
    }

    @Test
    void allHitsPremiumRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/odds/premium")).willReturn(okJson("""
                { "data": [ { "id": 1, "fixture_id": 18533878, "value": "1.48" } ] }
                """)));

        var response = odds(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().value()).isEqualTo("1.48");
    }

    @Test
    void byFixtureHitsFixturesPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/odds/premium/fixtures/18533878")).willReturn(okJson("""
                { "data": [ { "id": 1, "fixture_id": 18533878, "value": "1.48" } ] }
                """)));

        var response = odds(wm.getHttpBaseUrl()).byFixture(18533878L).get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().fixtureId()).isEqualTo(18533878L);
    }

    @Test
    void byFixtureAndBookmakerHitsCompositePath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/odds/premium/fixtures/18533878/bookmakers/34")).willReturn(okJson("""
                { "data": [ { "id": 1, "bookmaker_id": 34, "value": "1.48" } ] }
                """)));

        var response = odds(wm.getHttpBaseUrl()).byFixtureAndBookmaker(18533878L, 34L).get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().bookmakerId()).isEqualTo(34L);
    }

    @Test
    void byFixtureAndMarketHitsCompositePath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/odds/premium/fixtures/18533878/markets/1")).willReturn(okJson("""
                { "data": [ { "id": 1, "market_id": 1, "value": "1.48" } ] }
                """)));

        var response = odds(wm.getHttpBaseUrl()).byFixtureAndMarket(18533878L, 1L).get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().marketId()).isEqualTo(1L);
    }

    @Test
    void updatedBetweenHitsTimeRangePath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/odds/premium/updated/between/1767225600/1767225900")).willReturn(okJson("""
                { "data": [ { "id": 1, "value": "1.48" } ] }
                """)));

        var response = odds(wm.getHttpBaseUrl()).updatedBetween(1767225600L, 1767225900L).get();

        assertThat(response.data()).hasSize(1);
    }
}
