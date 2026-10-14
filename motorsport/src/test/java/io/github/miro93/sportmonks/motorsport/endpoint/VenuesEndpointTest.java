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
class VenuesEndpointTest {

    private final HelidonJsonCodec codec = new HelidonJsonCodec();

    private VenuesEndpoint endpoint(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new VenuesEndpoint(executor, codec);
    }

    @Test
    void allHitsRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/venues")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        assertThat(endpoint(wm.getHttpBaseUrl()).all().get().data()).hasSize(2);
    }

    @Test
    void byIdHitsIdPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/venues/343575")).willReturn(okJson("""
                { "data": { "id": 7 } }
                """)));

        assertThat(endpoint(wm.getHttpBaseUrl()).byId(343575L).get().data().id()).isEqualTo(7L);
    }

    @Test
    void bySeasonHitsSeasonsPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/venues/seasons/26733")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        assertThat(endpoint(wm.getHttpBaseUrl()).bySeason(26733L).get().data()).hasSize(2);
    }

    @Test
    void searchHitsSearchPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/venues/search/Hungaroring")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        assertThat(endpoint(wm.getHttpBaseUrl()).search("Hungaroring").get().data()).hasSize(2);
    }
}
