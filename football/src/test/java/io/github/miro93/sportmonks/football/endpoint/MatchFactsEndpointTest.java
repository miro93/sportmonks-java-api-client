package io.github.miro93.sportmonks.football.endpoint;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.HelidonJsonCodec;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.LocalDate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class MatchFactsEndpointTest {

    private final HelidonJsonCodec codec = new HelidonJsonCodec();

    private MatchFactsEndpoint endpoint(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new MatchFactsEndpoint(executor, codec);
    }

    @Test
    void allHitsRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/match-facts")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        var response = endpoint(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(2);
    }

    @Test
    void byFixtureHitsFixtureIdPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/match-facts/19427187")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        var response = endpoint(wm.getHttpBaseUrl()).byFixture(19427187L).get();

        assertThat(response.data()).hasSize(2);
    }

    @Test
    void byDateRangeHitsFixturesBetweenPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/match-facts/fixtures/between/2025-01-01/2025-01-07")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        var response = endpoint(wm.getHttpBaseUrl()).byDateRange(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 7)).get();

        assertThat(response.data()).hasSize(2);
    }

    @Test
    void byLeagueHitsLeaguesPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/match-facts/leagues/8")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        var response = endpoint(wm.getHttpBaseUrl()).byLeague(8L).get();

        assertThat(response.data()).hasSize(2);
    }
}
