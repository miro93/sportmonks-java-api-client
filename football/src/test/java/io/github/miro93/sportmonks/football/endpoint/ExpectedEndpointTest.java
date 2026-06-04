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
class ExpectedEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private ExpectedEndpoint expected(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new ExpectedEndpoint(executor, codec);
    }

    @Test
    void fixturesHitsExpectedFixturesPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/expected/fixtures")).willReturn(okJson("""
                { "data": [ { "id": 7001, "fixture_id": 18533878, "location": "home", "data": { "value": 1.85 } } ] }
                """)));

        var response = expected(wm.getHttpBaseUrl()).fixtures().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().location()).isEqualTo("home");
        assertThat(response.data().getFirst().data()).containsEntry("value", 1.85);
    }

    @Test
    void lineupsHitsExpectedLineupsPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/expected/lineups")).willReturn(okJson("""
                { "data": [ { "id": 7002, "fixture_id": 18533878, "participant_id": 172 } ] }
                """)));

        var response = expected(wm.getHttpBaseUrl()).lineups().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().participantId()).isEqualTo(172L);
    }
}
