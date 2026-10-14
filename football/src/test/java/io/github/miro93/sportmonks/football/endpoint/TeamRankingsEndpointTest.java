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
    class TeamRankingsEndpointTest {

        private final HelidonJsonCodec codec = new HelidonJsonCodec();

        private TeamRankingsEndpoint endpoint(String baseUrl) {
            var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
            var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
            return new TeamRankingsEndpoint(executor, codec);
        }

    @Test
    void allHitsRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/team-rankings")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        var response = endpoint(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(2);
    }

    @Test
    void byTeamHitsTeamsPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/team-rankings/teams/14754")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        var response = endpoint(wm.getHttpBaseUrl()).byTeam(14754L).get();

        assertThat(response.data()).hasSize(2);
    }

    @Test
    void byDateHitsIsoDatePath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/team-rankings/date/2005-07-02")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        var response = endpoint(wm.getHttpBaseUrl()).byDate(LocalDate.of(2005, 7, 2)).get();

        assertThat(response.data()).hasSize(2);
    }
}
