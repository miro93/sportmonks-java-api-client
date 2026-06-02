package io.github.miro93.sportmonks.football.endpoint;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.football.model.League;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.LocalDate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest
class LeaguesEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private LeaguesEndpoint leagues(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new LeaguesEndpoint(executor, codec);
    }

    @Test
    void allHitsLeaguesRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/leagues")).willReturn(okJson("""
                { "data": [ { "id": 1, "name": "Premier League" } ] }
                """)));

        var response = leagues(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().get(0).name()).isEqualTo("Premier League");
    }

    @Test
    void byIdHitsTheCorrectPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlEqualTo("/leagues/271"))
                .willReturn(okJson("""
                        { "data": { "id": 271, "name": "Superliga" } }
                        """)));

        League league = leagues(wm.getHttpBaseUrl())
                .byId(271L)
                .get()
                .data();

        assertThat(league.id()).isEqualTo(271L);
        assertThat(league.name()).isEqualTo("Superliga");
    }

    @Test
    void byIdWithIncludesHitsCorrectPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlEqualTo("/leagues/271?include=seasons"))
                .willReturn(okJson("""
                        { "data": { "id": 271, "name": "Superliga" } }
                        """)));

        League league = leagues(wm.getHttpBaseUrl())
                .byId(271L)
                .include("seasons")
                .get()
                .data();

        assertThat(league.id()).isEqualTo(271L);
    }

    @Test
    void byCountryHitsCountriesPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/leagues/countries/320")).willReturn(okJson("""
                { "data": [ { "id": 271, "name": "Superliga" } ] }
                """)));

        var response = leagues(wm.getHttpBaseUrl())
                .byCountry(320L)
                .get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().get(0).id()).isEqualTo(271L);
    }

    @Test
    void searchHitsSearchPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/leagues/search/Premier")).willReturn(okJson("""
                { "data": [ { "id": 8, "name": "Premier League" } ] }
                """)));

        var response = leagues(wm.getHttpBaseUrl())
                .search("Premier")
                .get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().get(0).name()).isEqualTo("Premier League");
    }

    @Test
    void searchRejectsNull(WireMockRuntimeInfo wm) {
        assertThatThrownBy(() -> leagues(wm.getHttpBaseUrl()).search(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void byDateRejectsNull(WireMockRuntimeInfo wm) {
        assertThatThrownBy(() -> leagues(wm.getHttpBaseUrl()).byDate(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void liveHitsLivePath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/leagues/live")).willReturn(okJson("""
                { "data": [ { "id": 8, "name": "Premier League" }, { "id": 564, "name": "La Liga" } ] }
                """)));

        var response = leagues(wm.getHttpBaseUrl())
                .live()
                .get();

        assertThat(response.data()).hasSize(2);
    }

    @Test
    void byDateHitsFixturesDatePath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/leagues/fixtures/2024-09-01")).willReturn(okJson("""
                { "data": [ { "id": 8, "name": "Premier League" } ] }
                """)));

        var response = leagues(wm.getHttpBaseUrl())
                .byDate(LocalDate.of(2024, 9, 1))
                .get();

        assertThat(response.data()).hasSize(1);
    }

    @Test
    void byDateUsesGetAsync(WireMockRuntimeInfo wm) throws Exception {
        stubFor(get(urlPathEqualTo("/leagues/fixtures/2024-12-25")).willReturn(okJson("""
                { "data": [ { "id": 271, "name": "Superliga" } ] }
                """)));

        var future = leagues(wm.getHttpBaseUrl())
                .byDate(LocalDate.of(2024, 12, 25))
                .getAsync();

        var response = future.get();
        assertThat(response.data()).hasSize(1);
        assertThat(response.data().get(0).id()).isEqualTo(271L);
    }
}
