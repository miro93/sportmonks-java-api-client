package io.github.miro93.sportmonks.football.endpoint;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.football.model.Season;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest
class SeasonsEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private SeasonsEndpoint seasons(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new SeasonsEndpoint(executor, codec);
    }

    @Test
    void allHitsSeasonsRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/seasons")).willReturn(okJson("""
                { "data": [ { "id": 1, "name": "2023/2024" } ] }
                """)));

        var response = seasons(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().get(0).name()).isEqualTo("2023/2024");
    }

    @Test
    void byIdHitsTheCorrectPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlEqualTo("/seasons/19686"))
                .willReturn(okJson("""
                        { "data": { "id": 19686, "name": "2023/2024" } }
                        """)));

        Season season = seasons(wm.getHttpBaseUrl())
                .byId(19686L)
                .get()
                .data();

        assertThat(season.id()).isEqualTo(19686L);
        assertThat(season.name()).isEqualTo("2023/2024");
    }

    @Test
    void byIdWithIncludesHitsCorrectPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlEqualTo("/seasons/19686?include=stages"))
                .willReturn(okJson("""
                        { "data": { "id": 19686, "name": "2023/2024" } }
                        """)));

        Season season = seasons(wm.getHttpBaseUrl())
                .byId(19686L)
                .include("stages")
                .get()
                .data();

        assertThat(season.id()).isEqualTo(19686L);
    }

    @Test
    void byTeamHitsTeamsPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/seasons/teams/53")).willReturn(okJson("""
                { "data": [ { "id": 19686, "name": "2023/2024" } ] }
                """)));

        var response = seasons(wm.getHttpBaseUrl())
                .byTeam(53L)
                .get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().get(0).id()).isEqualTo(19686L);
    }

    @Test
    void searchHitsSearchPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/seasons/search/2023")).willReturn(okJson("""
                { "data": [ { "id": 19686, "name": "2023/2024" } ] }
                """)));

        var response = seasons(wm.getHttpBaseUrl())
                .search("2023")
                .get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().get(0).name()).isEqualTo("2023/2024");
    }

    @Test
    void searchRejectsNull(WireMockRuntimeInfo wm) {
        assertThatThrownBy(() -> seasons(wm.getHttpBaseUrl()).search(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void byTeamUsesGetAsync(WireMockRuntimeInfo wm) throws Exception {
        stubFor(get(urlPathEqualTo("/seasons/teams/62")).willReturn(okJson("""
                { "data": [ { "id": 19686, "name": "2023/2024" }, { "id": 19687, "name": "2022/2023" } ] }
                """)));

        var future = seasons(wm.getHttpBaseUrl())
                .byTeam(62L)
                .getAsync();

        var response = future.get();
        assertThat(response.data()).hasSize(2);
        assertThat(response.data().get(0).id()).isEqualTo(19686L);
    }
}
