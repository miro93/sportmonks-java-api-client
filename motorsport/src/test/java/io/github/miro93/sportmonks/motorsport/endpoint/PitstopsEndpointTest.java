package io.github.miro93.sportmonks.motorsport.endpoint;

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
class PitstopsEndpointTest {

    private final HelidonJsonCodec codec = new HelidonJsonCodec();

    private PitstopsEndpoint endpoint(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new PitstopsEndpoint(executor, codec);
    }

    @Test
    void byFixtureHitsFixturePath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/fixtures/19408487/pitstops")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        assertThat(endpoint(wm.getHttpBaseUrl()).byFixture(19408487L).get().data()).hasSize(2);
    }

    @Test
    void byFixtureAndDriverHitsDriversPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/fixtures/19408487/pitstops/drivers/37920800")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        assertThat(endpoint(wm.getHttpBaseUrl()).byFixtureAndDriver(19408487L, 37920800L).get().data()).hasSize(2);
    }

    @Test
    void byFixtureAndNumberHitsNumberPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/fixtures/19408487/pitstops/3")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        assertThat(endpoint(wm.getHttpBaseUrl()).byFixtureAndNumber(19408487L, 3).get().data()).hasSize(2);
    }

    @Test
    void latestByFixtureHitsLatestPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/fixtures/19408487/pitstops/latest")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        assertThat(endpoint(wm.getHttpBaseUrl()).latestByFixture(19408487L).get().data()).hasSize(2);
    }
}
