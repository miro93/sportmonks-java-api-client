package io.github.miro93.sportmonks.football.endpoint;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.football.model.Coach;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest
class CoachesEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private CoachesEndpoint coaches(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new CoachesEndpoint(executor, codec);
    }

    @Test
    void allHitsCoachesRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/coaches")).willReturn(okJson("""
                { "data": [ { "id": 1, "name": "Pep Guardiola" } ] }
                """)));

        var response = coaches(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().get(0).name()).isEqualTo("Pep Guardiola");
    }

    @Test
    void byIdHitsTheCorrectPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlEqualTo("/coaches/5430"))
                .willReturn(okJson("""
                        { "data": { "id": 5430, "name": "Pep Guardiola" } }
                        """)));

        Coach coach = coaches(wm.getHttpBaseUrl())
                .byId(5430L)
                .get()
                .data();

        assertThat(coach.id()).isEqualTo(5430L);
        assertThat(coach.name()).isEqualTo("Pep Guardiola");
    }

    @Test
    void byIdWithIncludesHitsCorrectPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlEqualTo("/coaches/5430?include=teams"))
                .willReturn(okJson("""
                        { "data": { "id": 5430, "name": "Pep Guardiola" } }
                        """)));

        Coach coach = coaches(wm.getHttpBaseUrl())
                .byId(5430L)
                .include("teams")
                .get()
                .data();

        assertThat(coach.id()).isEqualTo(5430L);
    }

    @Test
    void byCountryHitsCountriesPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/coaches/countries/320")).willReturn(okJson("""
                { "data": [ { "id": 5430, "name": "Pep Guardiola" } ] }
                """)));

        var response = coaches(wm.getHttpBaseUrl())
                .byCountry(320L)
                .get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().get(0).id()).isEqualTo(5430L);
    }

    @Test
    void searchHitsSearchPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/coaches/search/Guardiola")).willReturn(okJson("""
                { "data": [ { "id": 5430, "name": "Pep Guardiola" } ] }
                """)));

        var response = coaches(wm.getHttpBaseUrl())
                .search("Guardiola")
                .get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().get(0).name()).isEqualTo("Pep Guardiola");
    }

    @Test
    void searchRejectsNull(WireMockRuntimeInfo wm) {
        assertThatThrownBy(() -> coaches(wm.getHttpBaseUrl()).search(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void latestHitsLatestPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/coaches/latest")).willReturn(okJson("""
                { "data": [ { "id": 88881, "name": "New Coach" }, { "id": 88882, "name": "Another Coach" } ] }
                """)));

        var response = coaches(wm.getHttpBaseUrl())
                .latest()
                .get();

        assertThat(response.data()).hasSize(2);
    }

    @Test
    void byCountryUsesGetAsync(WireMockRuntimeInfo wm) throws Exception {
        stubFor(get(urlPathEqualTo("/coaches/countries/47")).willReturn(okJson("""
                { "data": [ { "id": 5430, "name": "Pep Guardiola" }, { "id": 5431, "name": "Luis Enrique" } ] }
                """)));

        var future = coaches(wm.getHttpBaseUrl())
                .byCountry(47L)
                .getAsync();

        var response = future.get();
        assertThat(response.data()).hasSize(2);
        assertThat(response.data().get(0).id()).isEqualTo(5430L);
    }
}
