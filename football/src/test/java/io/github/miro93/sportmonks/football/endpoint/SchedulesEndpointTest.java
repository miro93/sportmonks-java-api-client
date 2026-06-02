package io.github.miro93.sportmonks.football.endpoint;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.football.model.Stage;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class SchedulesEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private SchedulesEndpoint schedules(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new SchedulesEndpoint(executor, codec);
    }

    @Test
    void bySeasonHitsSeasonPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/schedules/seasons/19686")).willReturn(okJson("""
                { "data": [ { "id": 1, "name": "Regular Season" } ] }
                """)));

        var response = schedules(wm.getHttpBaseUrl())
                .bySeason(19686L)
                .get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().get(0).name()).isEqualTo("Regular Season");
    }

    @Test
    void bySeasonDecodesNestedRoundsAndFixtures(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/schedules/seasons/19686")).willReturn(okJson("""
                {
                  "data": [
                    {
                      "id": 1,
                      "name": "Regular Season",
                      "rounds": [
                        {
                          "id": 10,
                          "name": "Round 1",
                          "fixtures": [
                            { "id": 100, "name": "Team A vs Team B", "placeholder": false, "hasOdds": true }
                          ]
                        }
                      ]
                    }
                  ]
                }
                """)));

        var response = schedules(wm.getHttpBaseUrl())
                .bySeason(19686L)
                .get();

        assertThat(response.data()).hasSize(1);
        Stage stage = response.data().get(0);
        assertThat(stage.rounds()).hasSize(1);
        assertThat(stage.rounds().get(0).id()).isEqualTo(10L);
        assertThat(stage.rounds().get(0).name()).isEqualTo("Round 1");
        assertThat(stage.rounds().get(0).fixtures()).hasSize(1);
        assertThat(stage.rounds().get(0).fixtures().get(0).id()).isEqualTo(100L);
    }

    @Test
    void byTeamHitsTeamPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/schedules/teams/53")).willReturn(okJson("""
                { "data": [ { "id": 2, "name": "Regular Season" } ] }
                """)));

        var response = schedules(wm.getHttpBaseUrl())
                .byTeam(53L)
                .get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().get(0).id()).isEqualTo(2L);
    }

    @Test
    void bySeasonAndTeamHitsNestedPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/schedules/seasons/19686/teams/53")).willReturn(okJson("""
                { "data": [ { "id": 3, "name": "Regular Season" } ] }
                """)));

        var response = schedules(wm.getHttpBaseUrl())
                .bySeasonAndTeam(19686L, 53L)
                .get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().get(0).id()).isEqualTo(3L);
    }

    @Test
    void byTeamUsesGetAsync(WireMockRuntimeInfo wm) throws Exception {
        stubFor(get(urlPathEqualTo("/schedules/teams/62")).willReturn(okJson("""
                { "data": [ { "id": 10, "name": "Regular Season" }, { "id": 11, "name": "Playoffs" } ] }
                """)));

        var future = schedules(wm.getHttpBaseUrl())
                .byTeam(62L)
                .getAsync();

        var response = future.get();
        assertThat(response.data()).hasSize(2);
        assertThat(response.data().get(0).id()).isEqualTo(10L);
    }
}
