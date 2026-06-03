package io.github.miro93.sportmonks.football.endpoint;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.football.model.TvStation;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class TvStationsEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private TvStationsEndpoint tvStations(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new TvStationsEndpoint(executor, codec);
    }

    @Test
    void allHitsTvStationsRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/tv-stations")).willReturn(okJson("""
                { "data": [ { "id": 5, "name": "Sky Sports", "url": "https://sky.com", "image_path": "https://cdn/sky.png" } ] }
                """)));

        var response = tvStations(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().name()).isEqualTo("Sky Sports");
    }

    @Test
    void byIdHitsTheCorrectPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlEqualTo("/tv-stations/5")).willReturn(okJson("""
                { "data": { "id": 5, "name": "Sky Sports" } }
                """)));

        TvStation station = tvStations(wm.getHttpBaseUrl()).byId(5L).get().data();

        assertThat(station.id()).isEqualTo(5L);
        assertThat(station.name()).isEqualTo("Sky Sports");
    }

    @Test
    void byFixtureHitsFixturesPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/tv-stations/fixtures/18535517")).willReturn(okJson("""
                { "data": [ { "id": 5, "name": "Sky Sports" } ] }
                """)));

        var response = tvStations(wm.getHttpBaseUrl()).byFixture(18535517L).get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().id()).isEqualTo(5L);
    }
}
