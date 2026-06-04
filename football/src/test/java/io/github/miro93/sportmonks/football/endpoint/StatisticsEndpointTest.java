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
class StatisticsEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private StatisticsEndpoint statistics(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new StatisticsEndpoint(executor, codec);
    }

    @Test
    void seasonByTeamHitsTeamsPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/statistics/seasons/teams/1")).willReturn(okJson("""
                { "data": [ { "id": 5001, "team_id": 1 } ] }
                """)));

        var response = statistics(wm.getHttpBaseUrl()).seasonByTeam(1L).get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().teamId()).isEqualTo(1L);
    }

    @Test
    void seasonByPlayerHitsPlayersPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/statistics/seasons/players/172")).willReturn(okJson("""
                { "data": [ { "id": 5002, "player_id": 172 } ] }
                """)));

        var response = statistics(wm.getHttpBaseUrl()).seasonByPlayer(172L).get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().playerId()).isEqualTo(172L);
    }

    @Test
    void seasonByCoachHitsCoachesPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/statistics/seasons/coaches/455")).willReturn(okJson("""
                { "data": [ { "id": 5003, "coach_id": 455 } ] }
                """)));

        var response = statistics(wm.getHttpBaseUrl()).seasonByCoach(455L).get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().coachId()).isEqualTo(455L);
    }

    @Test
    void seasonByRefereeHitsRefereesPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/statistics/seasons/referees/12")).willReturn(okJson("""
                { "data": [ { "id": 5004, "referee_id": 12 } ] }
                """)));

        var response = statistics(wm.getHttpBaseUrl()).seasonByReferee(12L).get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().refereeId()).isEqualTo(12L);
    }

    @Test
    void byStageHitsStagesPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/statistics/stages/77")).willReturn(okJson("""
                { "data": [ { "id": 5005, "stage_id": 77 } ] }
                """)));

        var response = statistics(wm.getHttpBaseUrl()).byStage(77L).get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().stageId()).isEqualTo(77L);
    }

    @Test
    void byRoundHitsRoundsPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/statistics/rounds/33")).willReturn(okJson("""
                { "data": [ { "id": 5006, "round_id": 33 } ] }
                """)));

        var response = statistics(wm.getHttpBaseUrl()).byRound(33L).get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().roundId()).isEqualTo(33L);
    }
}
