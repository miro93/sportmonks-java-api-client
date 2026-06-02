package io.github.miro93.sportmonks.football.endpoint;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.football.model.Squad;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class SquadsEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private SquadsEndpoint squads(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new SquadsEndpoint(executor, codec);
    }

    @Test
    void byTeamHitsTeamPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/squads/teams/53")).willReturn(okJson("""
                { "data": [ { "id": 1 } ] }
                """)));

        var response = squads(wm.getHttpBaseUrl())
                .byTeam(53L)
                .get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().get(0).id()).isEqualTo(1L);
    }

    @Test
    void byTeamDecodesNestedPlayer(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/squads/teams/53")).willReturn(okJson("""
                {
                  "data": [
                    {
                      "id": 1,
                      "jersey_number": 9,
                      "player": { "id": 101, "display_name": "John Doe" }
                    }
                  ]
                }
                """)));

        var response = squads(wm.getHttpBaseUrl())
                .byTeam(53L)
                .get();

        assertThat(response.data()).hasSize(1);
        Squad member = response.data().get(0);
        assertThat(member.jerseyNumber()).isEqualTo(9);
        assertThat(member.player()).isNotNull();
        assertThat(member.player().id()).isEqualTo(101L);
        assertThat(member.player().displayName()).isEqualTo("John Doe");
    }

    @Test
    void bySeasonAndTeamHitsNestedPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/squads/seasons/19686/teams/53")).willReturn(okJson("""
                { "data": [ { "id": 2 }, { "id": 3 } ] }
                """)));

        var response = squads(wm.getHttpBaseUrl())
                .bySeasonAndTeam(19686L, 53L)
                .get();

        assertThat(response.data()).hasSize(2);
        assertThat(response.data().get(0).id()).isEqualTo(2L);
        assertThat(response.data().get(1).id()).isEqualTo(3L);
    }

    @Test
    void bySeasonAndTeamUsesGetAsync(WireMockRuntimeInfo wm) throws Exception {
        stubFor(get(urlPathEqualTo("/squads/seasons/19686/teams/53")).willReturn(okJson("""
                {
                  "data": [
                    { "id": 10 },
                    { "id": 11 }
                  ]
                }
                """)));

        var future = squads(wm.getHttpBaseUrl())
                .bySeasonAndTeam(19686L, 53L)
                .getAsync();

        var response = future.get();
        assertThat(response.data()).hasSize(2);
        assertThat(response.data().get(0).id()).isEqualTo(10L);
        assertThat(response.data().get(1).id()).isEqualTo(11L);
    }

    @Test
    void byTeamUsesGetAsync(WireMockRuntimeInfo wm) throws Exception {
        stubFor(get(urlPathEqualTo("/squads/teams/62")).willReturn(okJson("""
                { "data": [ { "id": 20 }, { "id": 21 } ] }
                """)));

        var future = squads(wm.getHttpBaseUrl())
                .byTeam(62L)
                .getAsync();

        var response = future.get();
        assertThat(response.data()).hasSize(2);
        assertThat(response.data().get(0).id()).isEqualTo(20L);
    }
}
