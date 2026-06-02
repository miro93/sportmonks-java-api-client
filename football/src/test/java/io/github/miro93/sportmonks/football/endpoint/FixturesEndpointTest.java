package io.github.miro93.sportmonks.football.endpoint;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.football.model.Fixture;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.LocalDate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest
class FixturesEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private FixturesEndpoint fixtures(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new FixturesEndpoint(executor, codec);
    }

    @Test
    void byIdHitsTheCorrectPathWithIncludes(WireMockRuntimeInfo wm) {
        stubFor(get(urlEqualTo("/fixtures/18535517?include=participants;scores"))
                .willReturn(okJson("""
                        { "data": { "id": 18535517, "name": "Celtic vs Rangers" } }
                        """)));

        Fixture fixture = fixtures(wm.getHttpBaseUrl())
                .byId(18535517L)
                .include("participants", "scores")
                .get()
                .data();

        assertThat(fixture.id()).isEqualTo(18535517L);
    }

    @Test
    void byDateHitsDatePath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/fixtures/date/2024-09-01")).willReturn(okJson("""
                { "data": [ { "id": 1, "name": "A vs B" } ] }
                """)));

        var response = fixtures(wm.getHttpBaseUrl())
                .byDate(LocalDate.of(2024, 9, 1))
                .get();

        assertThat(response.data()).hasSize(1);
    }

    @Test
    void byDateRangeHitsBetweenPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/fixtures/between/2024-09-01/2024-09-07")).willReturn(okJson("""
                { "data": [ { "id": 1, "name": "A vs B" }, { "id": 2, "name": "C vs D" } ] }
                """)));

        var response = fixtures(wm.getHttpBaseUrl())
                .byDateRange(LocalDate.of(2024, 9, 1), LocalDate.of(2024, 9, 7))
                .get();

        assertThat(response.data()).hasSize(2);
    }

    @Test
    void byDateRangeForTeamHitsBetweenTeamPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/fixtures/between/2024-09-01/2024-09-07/53")).willReturn(okJson("""
                { "data": [ { "id": 1, "name": "A vs B" } ] }
                """)));

        var response = fixtures(wm.getHttpBaseUrl())
                .byDateRangeForTeam(LocalDate.of(2024, 9, 1), LocalDate.of(2024, 9, 7), 53L)
                .get();

        assertThat(response.data()).hasSize(1);
    }

    @Test
    void byMultipleIdsHitsMultiPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/fixtures/multi/1,2,3")).willReturn(okJson("""
                { "data": [ { "id": 1, "name": "A" }, { "id": 2, "name": "B" }, { "id": 3, "name": "C" } ] }
                """)));

        var response = fixtures(wm.getHttpBaseUrl())
                .byMultipleIds(1L, 2L, 3L)
                .get();

        assertThat(response.data()).hasSize(3);
    }

    @Test
    void headToHeadHitsHeadToHeadPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/fixtures/head-to-head/53/62")).willReturn(okJson("""
                { "data": [ { "id": 1, "name": "Celtic vs Rangers" } ] }
                """)));

        var response = fixtures(wm.getHttpBaseUrl())
                .headToHead(53L, 62L)
                .get();

        assertThat(response.data()).hasSize(1);
    }

    @Test
    void searchHitsSearchPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/fixtures/search/Celtic")).willReturn(okJson("""
                { "data": [ { "id": 1, "name": "Celtic vs Rangers" } ] }
                """)));

        var response = fixtures(wm.getHttpBaseUrl())
                .search("Celtic")
                .get();

        assertThat(response.data()).hasSize(1);
    }

    @Test
    void searchRejectsNull(WireMockRuntimeInfo wm) {
        assertThatThrownBy(() -> fixtures(wm.getHttpBaseUrl()).search(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void byMultipleIdsRejectsEmpty(WireMockRuntimeInfo wm) {
        assertThatThrownBy(() -> fixtures(wm.getHttpBaseUrl()).byMultipleIds())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void allHitsFixturesRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/fixtures")).willReturn(okJson("""
                { "data": [ { "id": 1, "name": "A vs B" } ] }
                """)));

        var response = fixtures(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(1);
    }
}
