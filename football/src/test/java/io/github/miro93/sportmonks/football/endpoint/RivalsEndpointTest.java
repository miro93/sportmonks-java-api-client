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
    class RivalsEndpointTest {

        private final HelidonJsonCodec codec = new HelidonJsonCodec();

        private RivalsEndpoint endpoint(String baseUrl) {
            var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
            var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
            return new RivalsEndpoint(executor, codec);
        }

    @Test
    void allHitsRivalsRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/rivals")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        var response = endpoint(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(2);
    }

    @Test
    void byTeamHitsTeamsPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/rivals/teams/53")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        var response = endpoint(wm.getHttpBaseUrl()).byTeam(53L).get();

        assertThat(response.data()).hasSize(2);
    }
}
