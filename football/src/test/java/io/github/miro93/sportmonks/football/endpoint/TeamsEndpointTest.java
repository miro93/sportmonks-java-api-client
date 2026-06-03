package io.github.miro93.sportmonks.football.endpoint;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.football.model.Team;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest
class TeamsEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private TeamsEndpoint teams(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new TeamsEndpoint(executor, codec);
    }

    @Test
    void allHitsTeamsRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/teams")).willReturn(okJson("""
                { "data": [ { "id": 1, "placeholder": false } ] }
                """)));

        var response = teams(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().get(0).id()).isEqualTo(1L);
    }

    @Test
    void byIdHitsTheCorrectPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlEqualTo("/teams/53"))
                .willReturn(okJson("""
                        { "data": { "id": 53, "name": "Celtic", "placeholder": false } }
                        """)));

        Team team = teams(wm.getHttpBaseUrl())
                .byId(53L)
                .get()
                .data();

        assertThat(team.id()).isEqualTo(53L);
        assertThat(team.name()).isEqualTo("Celtic");
    }

    @Test
    void byIdWithIncludesHitsCorrectPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlEqualTo("/teams/53?include=squad"))
                .willReturn(okJson("""
                        { "data": { "id": 53, "name": "Celtic", "placeholder": false } }
                        """)));

        Team team = teams(wm.getHttpBaseUrl())
                .byId(53L)
                .include("squad")
                .get()
                .data();

        assertThat(team.id()).isEqualTo(53L);
    }

    @Test
    void byMultipleIdsHitsMultiPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/teams/multi/1,2,3")).willReturn(okJson("""
                { "data": [ { "id": 1, "placeholder": false }, { "id": 2, "placeholder": false }, { "id": 3, "placeholder": false } ] }
                """)));

        var response = teams(wm.getHttpBaseUrl())
                .byMultipleIds(1L, 2L, 3L)
                .get();

        assertThat(response.data()).hasSize(3);
        assertThat(response.data().get(0).id()).isEqualTo(1L);
    }

    @Test
    void bySeasonHitsSeasonsPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/teams/seasons/19686")).willReturn(okJson("""
                { "data": [ { "id": 53, "name": "Celtic", "placeholder": false } ] }
                """)));

        var response = teams(wm.getHttpBaseUrl())
                .bySeason(19686L)
                .get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().get(0).id()).isEqualTo(53L);
    }

    @Test
    void byCountryHitsCountriesPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/teams/countries/1161")).willReturn(okJson("""
                { "data": [ { "id": 53, "name": "Celtic", "placeholder": false } ] }
                """)));

        var response = teams(wm.getHttpBaseUrl())
                .byCountry(1161L)
                .get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().get(0).name()).isEqualTo("Celtic");
    }

    @Test
    void searchHitsSearchPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/teams/search/Celtic")).willReturn(okJson("""
                { "data": [ { "id": 53, "name": "Celtic", "placeholder": false } ] }
                """)));

        var response = teams(wm.getHttpBaseUrl())
                .search("Celtic")
                .get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().get(0).name()).isEqualTo("Celtic");
    }

    @Test
    void searchRejectsNull(WireMockRuntimeInfo wm) {
        assertThatThrownBy(() -> teams(wm.getHttpBaseUrl()).search(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void byMultipleIdsRejectsEmpty(WireMockRuntimeInfo wm) {
        assertThatThrownBy(() -> teams(wm.getHttpBaseUrl()).byMultipleIds())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void bySeasonUsesGetAsync(WireMockRuntimeInfo wm) throws Exception {
        stubFor(get(urlPathEqualTo("/teams/seasons/19686")).willReturn(okJson("""
                { "data": [ { "id": 53, "name": "Celtic", "placeholder": false }, { "id": 62, "name": "Rangers", "placeholder": false } ] }
                """)));

        var future = teams(wm.getHttpBaseUrl())
                .bySeason(19686L)
                .getAsync();

        var response = future.get();
        assertThat(response.data()).hasSize(2);
        assertThat(response.data().get(0).id()).isEqualTo(53L);
    }
}
