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
class PredictionsEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private PredictionsEndpoint predictions(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new PredictionsEndpoint(executor, codec);
    }

    @Test
    void probabilitiesHitsProbabilitiesRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/predictions/probabilities")).willReturn(okJson("""
                { "data": [ { "id": 1, "fixture_id": 18533878, "predictions": { "yes": 0.6 } } ] }
                """)));

        var response = predictions(wm.getHttpBaseUrl()).probabilities().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().predictions()).containsEntry("yes", 0.6);
    }

    @Test
    void probabilitiesByFixtureHitsFixturesPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/predictions/probabilities/fixtures/18533878")).willReturn(okJson("""
                { "data": [ { "id": 1, "fixture_id": 18533878 } ] }
                """)));

        var response = predictions(wm.getHttpBaseUrl()).probabilitiesByFixture(18533878L).get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().fixtureId()).isEqualTo(18533878L);
    }

    @Test
    void valueBetsHitsValueBetsRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/predictions/value-bets")).willReturn(okJson("""
                { "data": [ { "id": 2, "fixture_id": 18533878, "predictions": { "bet": "Home", "is_value": true } } ] }
                """)));

        var response = predictions(wm.getHttpBaseUrl()).valueBets().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().predictions().bet()).isEqualTo("Home");
    }

    @Test
    void valueBetsByFixtureHitsFixturesPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/predictions/value-bets/fixtures/18533878")).willReturn(okJson("""
                { "data": [ { "id": 2, "fixture_id": 18533878 } ] }
                """)));

        var response = predictions(wm.getHttpBaseUrl()).valueBetsByFixture(18533878L).get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().fixtureId()).isEqualTo(18533878L);
    }

    @Test
    void predictabilityByLeagueHitsLeaguesPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/predictions/predictability/leagues/8")).willReturn(okJson("""
                { "data": [ { "id": 3, "league_id": 8, "data": { "fulltime_result": 0.75 } } ] }
                """)));

        var response = predictions(wm.getHttpBaseUrl()).predictabilityByLeague(8L).get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().leagueId()).isEqualTo(8L);
        assertThat(response.data().getFirst().data()).containsEntry("fulltime_result", 0.75);
    }
}
