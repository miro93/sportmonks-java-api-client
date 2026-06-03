package io.github.miro93.sportmonks.core.coreapi.endpoint;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.coreapi.model.Continent;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class ContinentsEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private ContinentsEndpoint continents(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new ContinentsEndpoint(executor, codec);
    }

    @Test
    void allHitsContinentsRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/continents")).willReturn(okJson("""
                { "data": [ { "id": 1, "name": "Europe", "code": "EU" } ] }
                """)));

        var response = continents(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().name()).isEqualTo("Europe");
        assertThat(response.data().getFirst().code()).isEqualTo("EU");
    }

    @Test
    void byIdHitsTheCorrectPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlEqualTo("/continents/1")).willReturn(okJson("""
                { "data": { "id": 1, "name": "Europe", "code": "EU" } }
                """)));

        Continent continent = continents(wm.getHttpBaseUrl()).byId(1L).get().data();

        assertThat(continent.id()).isEqualTo(1L);
        assertThat(continent.name()).isEqualTo("Europe");
    }

    @Test
    void byIdWithIncludesHitsCorrectPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlEqualTo("/continents/1?include=countries")).willReturn(okJson("""
                { "data": { "id": 1, "name": "Europe", "code": "EU" } }
                """)));

        Continent continent = continents(wm.getHttpBaseUrl()).byId(1L).include("countries").get().data();

        assertThat(continent.id()).isEqualTo(1L);
    }
}
