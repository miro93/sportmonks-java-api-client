package io.github.miro93.sportmonks.football.endpoint;

    import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
    import com.github.tomakehurst.wiremock.junit5.WireMockTest;
    import io.github.miro93.sportmonks.core.ApiExecutor;
    import io.github.miro93.sportmonks.core.auth.ApiToken;
    import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
    import io.github.miro93.sportmonks.core.json.HelidonJsonCodec;
    import org.junit.jupiter.api.Test;

    import java.net.http.HttpClient;
    import java.time.Duration;
    import java.time.LocalDate;

    import static com.github.tomakehurst.wiremock.client.WireMock.*;
    import static org.assertj.core.api.Assertions.assertThat;

    @WireMockTest
    class TransferRumoursEndpointTest {

        private final HelidonJsonCodec codec = new HelidonJsonCodec();

        private TransferRumoursEndpoint endpoint(String baseUrl) {
            var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
            var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
            return new TransferRumoursEndpoint(executor, codec);
        }

    @Test
    void allHitsRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/transfer-rumours")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        var response = endpoint(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(2);
    }

    @Test
    void byIdHitsIdPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/transfer-rumours/5")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        var response = endpoint(wm.getHttpBaseUrl()).byId(5L).get();

        assertThat(response.data()).hasSize(2);
    }

    @Test
    void byDateRangeHitsBetweenPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/transfer-rumours/between/2025-06-01/2025-06-30")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        var response = endpoint(wm.getHttpBaseUrl()).byDateRange(LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 30)).get();

        assertThat(response.data()).hasSize(2);
    }

    @Test
    void byTeamHitsTeamsPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/transfer-rumours/teams/8")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        var response = endpoint(wm.getHttpBaseUrl()).byTeam(8L).get();

        assertThat(response.data()).hasSize(2);
    }

    @Test
    void byPlayerHitsPlayersPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/transfer-rumours/players/34053")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        var response = endpoint(wm.getHttpBaseUrl()).byPlayer(34053L).get();

        assertThat(response.data()).hasSize(2);
    }
}
