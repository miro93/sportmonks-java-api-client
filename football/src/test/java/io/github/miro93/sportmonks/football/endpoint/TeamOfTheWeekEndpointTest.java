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
class TeamOfTheWeekEndpointTest {

    private final HelidonJsonCodec codec = new HelidonJsonCodec();

    private TeamOfTheWeekEndpoint endpoint(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new TeamOfTheWeekEndpoint(executor, codec);
    }

    @Test
    void allHitsRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/team-of-the-week")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        var response = endpoint(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(2);
    }

    @Test
    void byRoundHitsRoundsPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/team-of-the-week/rounds/318161")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        var response = endpoint(wm.getHttpBaseUrl()).byRound(318161L).get();

        assertThat(response.data()).hasSize(2);
    }

    @Test
    void latestByLeagueHitsLeaguesLatestPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/team-of-the-week/leagues/8/latest")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        var response = endpoint(wm.getHttpBaseUrl()).latestByLeague(8L).get();

        assertThat(response.data()).hasSize(2);
    }
}
