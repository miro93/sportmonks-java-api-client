package io.github.miro93.sportmonks.core.coreapi.endpoint;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.coreapi.model.City;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest
class CitiesEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private CitiesEndpoint cities(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new CitiesEndpoint(executor, codec);
    }

    @Test
    void allHitsCitiesRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/cities")).willReturn(okJson("""
                { "data": [ { "id": 100, "country_id": 320, "region": 10, "name": "Glasgow" } ] }
                """)));

        var response = cities(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().countryId()).isEqualTo(320L);
        assertThat(response.data().getFirst().region()).isEqualTo(10L);
    }

    @Test
    void byIdHitsTheCorrectPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlEqualTo("/cities/100")).willReturn(okJson("""
                { "data": { "id": 100, "country_id": 320, "name": "Glasgow" } }
                """)));

        City city = cities(wm.getHttpBaseUrl()).byId(100L).get().data();

        assertThat(city.id()).isEqualTo(100L);
        assertThat(city.name()).isEqualTo("Glasgow");
    }

    @Test
    void searchHitsSearchPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/cities/search/Glas")).willReturn(okJson("""
                { "data": [ { "id": 100, "name": "Glasgow" } ] }
                """)));

        var response = cities(wm.getHttpBaseUrl()).search("Glas").get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().id()).isEqualTo(100L);
    }

    @Test
    void searchRejectsNull(WireMockRuntimeInfo wm) {
        assertThatThrownBy(() -> cities(wm.getHttpBaseUrl()).search(null))
                .isInstanceOf(NullPointerException.class);
    }
}
