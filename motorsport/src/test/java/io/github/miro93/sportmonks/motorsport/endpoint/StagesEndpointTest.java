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
class StagesEndpointTest {

    private final HelidonJsonCodec codec = new HelidonJsonCodec();

    private StagesEndpoint endpoint(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new StagesEndpoint(executor, codec);
    }

    @Test
    void allHitsRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/stages")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        assertThat(endpoint(wm.getHttpBaseUrl()).all().get().data()).hasSize(2);
    }

    @Test
    void byIdHitsIdPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/stages/77479219")).willReturn(okJson("""
                { "data": { "id": 7 } }
                """)));

        assertThat(endpoint(wm.getHttpBaseUrl()).byId(77479219L).get().data().id()).isEqualTo(7L);
    }

    @Test
    void bySeasonHitsSeasonsPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/stages/seasons/26733")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        assertThat(endpoint(wm.getHttpBaseUrl()).bySeason(26733L).get().data()).hasSize(2);
    }

    @Test
    void searchHitsSearchPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/stages/search/Monaco")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        assertThat(endpoint(wm.getHttpBaseUrl()).search("Monaco").get().data()).hasSize(2);
    }
}
