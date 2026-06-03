package io.github.miro93.sportmonks.football.endpoint;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.football.model.Standing;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class StandingsEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private StandingsEndpoint standings(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new StandingsEndpoint(executor, codec);
    }

    @Test
    void allHitsStandingsPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/standings")).willReturn(okJson("""
                {
                  "data": [
                    {
                      "id": 1001,
                      "position": 1,
                      "points": 24
                    }
                  ]
                }
                """)));

        var response = standings(wm.getHttpBaseUrl())
                .all()
                .get();

        assertThat(response.data()).hasSize(1);
        Standing first = response.data().getFirst();
        assertThat(first.id()).isEqualTo(1001L);
        assertThat(first.position()).isEqualTo(1);
        assertThat(first.points()).isEqualTo(24);
    }

    @Test
    void bySeasonHitsSeasonPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/standings/seasons/19735")).willReturn(okJson("""
                {
                  "data": [
                    {
                      "id": 2001,
                      "season_id": 19735,
                      "position": 2,
                      "points": 18
                    },
                    {
                      "id": 2002,
                      "season_id": 19735,
                      "position": 3,
                      "points": 15
                    }
                  ]
                }
                """)));

        var response = standings(wm.getHttpBaseUrl())
                .bySeason(19735L)
                .get();

        assertThat(response.data()).hasSize(2);
        Standing first = response.data().getFirst();
        assertThat(first.id()).isEqualTo(2001L);
        assertThat(first.position()).isEqualTo(2);
        assertThat(first.points()).isEqualTo(18);
    }

    @Test
    void byRoundHitsRoundPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/standings/rounds/275092")).willReturn(okJson("""
                {
                  "data": [
                    {
                      "id": 3001,
                      "round_id": 275092,
                      "position": 1,
                      "points": 30
                    }
                  ]
                }
                """)));

        var response = standings(wm.getHttpBaseUrl())
                .byRound(275092L)
                .get();

        assertThat(response.data()).hasSize(1);
        Standing first = response.data().getFirst();
        assertThat(first.id()).isEqualTo(3001L);
        assertThat(first.position()).isEqualTo(1);
        assertThat(first.points()).isEqualTo(30);
    }

    @Test
    void correctionsBySeasonHitsCorrectionsPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/standings/corrections/seasons/19735")).willReturn(okJson("""
                {
                  "data": [
                    {
                      "id": 4001,
                      "season_id": 19735,
                      "position": 5,
                      "points": 12,
                      "result": "correction"
                    }
                  ]
                }
                """)));

        var response = standings(wm.getHttpBaseUrl())
                .correctionsBySeason(19735L)
                .get();

        assertThat(response.data()).hasSize(1);
        Standing first = response.data().getFirst();
        assertThat(first.id()).isEqualTo(4001L);
        assertThat(first.position()).isEqualTo(5);
        assertThat(first.points()).isEqualTo(12);
        assertThat(first.result()).isEqualTo("correction");
    }

    @Test
    void liveByLeagueHitsLivePath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/standings/leagues/501/live")).willReturn(okJson("""
                {
                  "data": [
                    {
                      "id": 5001,
                      "league_id": 501,
                      "position": 1,
                      "points": 45
                    }
                  ]
                }
                """)));

        var response = standings(wm.getHttpBaseUrl())
                .liveByLeague(501L)
                .get();

        assertThat(response.data()).hasSize(1);
        Standing first = response.data().getFirst();
        assertThat(first.id()).isEqualTo(5001L);
        assertThat(first.position()).isEqualTo(1);
        assertThat(first.points()).isEqualTo(45);
    }

    @Test
    void bySeasonUsesGetAsync(WireMockRuntimeInfo wm) throws Exception {
        stubFor(get(urlPathEqualTo("/standings/seasons/19735")).willReturn(okJson("""
                {
                  "data": [
                    {
                      "id": 6001,
                      "season_id": 19735,
                      "position": 4,
                      "points": 21
                    },
                    {
                      "id": 6002,
                      "season_id": 19735,
                      "position": 5,
                      "points": 19
                    }
                  ]
                }
                """)));

        var future = standings(wm.getHttpBaseUrl())
                .bySeason(19735L)
                .getAsync();

        var response = future.get();
        assertThat(response.data()).hasSize(2);
        assertThat(response.data().getFirst().id()).isEqualTo(6001L);
        assertThat(response.data().getFirst().points()).isEqualTo(21);
    }
}
