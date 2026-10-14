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
class FixturesEndpointTest {

    private final HelidonJsonCodec codec = new HelidonJsonCodec();

    private FixturesEndpoint endpoint(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new FixturesEndpoint(executor, codec);
    }

    @Test
    void allHitsRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/fixtures")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        assertThat(endpoint(wm.getHttpBaseUrl()).all().get().data()).hasSize(2);
    }

    @Test
    void byIdHitsIdPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/fixtures/19408487")).willReturn(okJson("""
                { "data": { "id": 7 } }
                """)));

        assertThat(endpoint(wm.getHttpBaseUrl()).byId(19408487L).get().data().id()).isEqualTo(7L);
    }

    @Test
    void byMultipleIdsHitsMultiPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/fixtures/multi/1,2")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        assertThat(endpoint(wm.getHttpBaseUrl()).byMultipleIds(1L, 2L).get().data()).hasSize(2);
    }

    @Test
    void byDateHitsDatePath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/fixtures/date/2026-03-07")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        assertThat(endpoint(wm.getHttpBaseUrl()).byDate(LocalDate.of(2026, 3, 7)).get().data()).hasSize(2);
    }

    @Test
    void byDateRangeHitsBetweenPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/fixtures/between/2026-03-06/2026-03-07")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        assertThat(endpoint(wm.getHttpBaseUrl()).byDateRange(LocalDate.of(2026, 3, 6), LocalDate.of(2026, 3, 7)).get().data()).hasSize(2);
    }

    @Test
    void latestHitsLatestPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/fixtures/latest")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        assertThat(endpoint(wm.getHttpBaseUrl()).latest().get().data()).hasSize(2);
    }
}
