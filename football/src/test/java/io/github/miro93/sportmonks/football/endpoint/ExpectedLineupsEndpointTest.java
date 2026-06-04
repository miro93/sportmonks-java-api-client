package io.github.miro93.sportmonks.football.endpoint;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class ExpectedLineupsEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private ExpectedLineupsEndpoint lineups(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new ExpectedLineupsEndpoint(executor, codec);
    }

    @Test
    void byTeamHitsTeamsPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/expected-lineups/teams/1")).willReturn(okJson("""
                { "data": [ { "id": 1, "team_id": 1, "player_name": "Virgil van Dijk" } ] }
                """)));

        var response = lineups(wm.getHttpBaseUrl()).byTeam(1L).get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().teamId()).isEqualTo(1L);
        assertThat(response.data().getFirst().playerName()).isEqualTo("Virgil van Dijk");
    }

    @Test
    void byPlayerHitsPlayersPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/expected-lineups/players/172")).willReturn(okJson("""
                { "data": [ { "id": 1, "player_id": 172, "player_name": "Virgil van Dijk" } ] }
                """)));

        var response = lineups(wm.getHttpBaseUrl()).byPlayer(172L).get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().playerId()).isEqualTo(172L);
    }
}
