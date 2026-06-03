package io.github.miro93.sportmonks.core.coreapi.endpoint;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.coreapi.model.Region;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest
class RegionsEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private RegionsEndpoint regions(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new RegionsEndpoint(executor, codec);
    }

    @Test
    void allHitsRegionsRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/regions")).willReturn(okJson("""
                { "data": [ { "id": 10, "country_id": 320, "name": "Glasgow" } ] }
                """)));

        var response = regions(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().countryId()).isEqualTo(320L);
    }

    @Test
    void byIdHitsTheCorrectPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlEqualTo("/regions/10")).willReturn(okJson("""
                { "data": { "id": 10, "country_id": 320, "name": "Glasgow" } }
                """)));

        Region region = regions(wm.getHttpBaseUrl()).byId(10L).get().data();

        assertThat(region.id()).isEqualTo(10L);
        assertThat(region.name()).isEqualTo("Glasgow");
    }

    @Test
    void searchHitsSearchPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/regions/search/Glas")).willReturn(okJson("""
                { "data": [ { "id": 10, "name": "Glasgow" } ] }
                """)));

        var response = regions(wm.getHttpBaseUrl()).search("Glas").get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().id()).isEqualTo(10L);
    }

    @Test
    void searchRejectsNull(WireMockRuntimeInfo wm) {
        assertThatThrownBy(() -> regions(wm.getHttpBaseUrl()).search(null))
                .isInstanceOf(NullPointerException.class);
    }
}
