package io.github.miro93.sportmonks.football.endpoint;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.football.model.Round;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest
class RoundsEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private RoundsEndpoint rounds(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new RoundsEndpoint(executor, codec);
    }

    @Test
    void allHitsRoundsRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/rounds")).willReturn(okJson("""
                { "data": [ { "id": 1, "name": "Round 1" } ] }
                """)));

        var response = rounds(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().get(0).name()).isEqualTo("Round 1");
    }

    @Test
    void byIdHitsTheCorrectPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlEqualTo("/rounds/271"))
                .willReturn(okJson("""
                        { "data": { "id": 271, "name": "Round 5" } }
                        """)));

        Round round = rounds(wm.getHttpBaseUrl())
                .byId(271L)
                .get()
                .data();

        assertThat(round.id()).isEqualTo(271L);
        assertThat(round.name()).isEqualTo("Round 5");
    }

    @Test
    void byIdWithIncludesHitsCorrectPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlEqualTo("/rounds/271?include=fixtures"))
                .willReturn(okJson("""
                        { "data": { "id": 271, "name": "Round 5" } }
                        """)));

        Round round = rounds(wm.getHttpBaseUrl())
                .byId(271L)
                .include("fixtures")
                .get()
                .data();

        assertThat(round.id()).isEqualTo(271L);
    }

    @Test
    void bySeasonHitsSeasonsPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/rounds/seasons/19686")).willReturn(okJson("""
                { "data": [ { "id": 271, "name": "Round 1" } ] }
                """)));

        var response = rounds(wm.getHttpBaseUrl())
                .bySeason(19686L)
                .get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().get(0).id()).isEqualTo(271L);
    }

    @Test
    void searchHitsSearchPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/rounds/search/Round")).willReturn(okJson("""
                { "data": [ { "id": 42, "name": "Round 1" } ] }
                """)));

        var response = rounds(wm.getHttpBaseUrl())
                .search("Round")
                .get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().get(0).name()).isEqualTo("Round 1");
    }

    @Test
    void searchRejectsNull(WireMockRuntimeInfo wm) {
        assertThatThrownBy(() -> rounds(wm.getHttpBaseUrl()).search(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void bySeasonUsesGetAsync(WireMockRuntimeInfo wm) throws Exception {
        stubFor(get(urlPathEqualTo("/rounds/seasons/19688")).willReturn(okJson("""
                { "data": [ { "id": 300, "name": "Round 1" }, { "id": 301, "name": "Round 2" } ] }
                """)));

        var future = rounds(wm.getHttpBaseUrl())
                .bySeason(19688L)
                .getAsync();

        var response = future.get();
        assertThat(response.data()).hasSize(2);
        assertThat(response.data().get(0).id()).isEqualTo(300L);
    }
}
