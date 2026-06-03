package io.github.miro93.sportmonks.core.coreapi.endpoint;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.coreapi.model.Country;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest
class CountriesEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private CountriesEndpoint countries(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new CountriesEndpoint(executor, codec);
    }

    @Test
    void allHitsCountriesRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/countries")).willReturn(okJson("""
                { "data": [ { "id": 320, "name": "Scotland" } ] }
                """)));

        var response = countries(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().name()).isEqualTo("Scotland");
    }

    @Test
    void byIdHitsTheCorrectPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlEqualTo("/countries/320")).willReturn(okJson("""
                { "data": { "id": 320, "continent_id": 1, "name": "Scotland", "iso2": "GB", "iso3": "GBR" } }
                """)));

        Country country = countries(wm.getHttpBaseUrl()).byId(320L).get().data();

        assertThat(country.id()).isEqualTo(320L);
        assertThat(country.continentId()).isEqualTo(1L);
        assertThat(country.iso2()).isEqualTo("GB");
    }

    @Test
    void searchHitsSearchPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/countries/search/Scot")).willReturn(okJson("""
                { "data": [ { "id": 320, "name": "Scotland" } ] }
                """)));

        var response = countries(wm.getHttpBaseUrl()).search("Scot").get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().id()).isEqualTo(320L);
    }

    @Test
    void searchRejectsNull(WireMockRuntimeInfo wm) {
        assertThatThrownBy(() -> countries(wm.getHttpBaseUrl()).search(null))
                .isInstanceOf(NullPointerException.class);
    }
}
