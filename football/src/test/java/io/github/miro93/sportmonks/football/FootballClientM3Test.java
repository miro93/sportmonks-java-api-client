package io.github.miro93.sportmonks.football;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class FootballClientM3Test {

    private FootballClient client(String baseUrl) {
        return FootballClient.builder()
                .apiToken(ApiToken.of("tok-test"))
                .baseUrl(baseUrl)
                .build();
    }

    // ── non-null accessor checks ──────────────────────────────────────────────

    @Test
    void exposesM3Endpoints(WireMockRuntimeInfo wm) {
        var client = client(wm.getHttpBaseUrl());
        assertThat(client.leagues()).isNotNull();
        assertThat(client.seasons()).isNotNull();
        assertThat(client.stages()).isNotNull();
        assertThat(client.rounds()).isNotNull();
        assertThat(client.schedules()).isNotNull();
    }

    // ── leagues ───────────────────────────────────────────────────────────────

    @Test
    void leaguesAllDecodesAndSendsAuth(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/leagues")).willReturn(okJson("""
                { "data": [{ "id": 10, "name": "Premier League", "active": true }] }
                """)));

        var leagues = client(wm.getHttpBaseUrl()).leagues().all().get().data();

        assertThat(leagues).hasSize(1);
        assertThat(leagues.getFirst().name()).isEqualTo("Premier League");
        verify(getRequestedFor(urlPathEqualTo("/leagues"))
                .withHeader("Authorization", equalTo("tok-test")));
    }

    // ── seasons ───────────────────────────────────────────────────────────────

    @Test
    void seasonsByIdDecodesAndSendsAuth(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/seasons/1")).willReturn(okJson("""
                { "data": { "id": 1, "name": "2023/2024" } }
                """)));

        var season = client(wm.getHttpBaseUrl()).seasons().byId(1L).get().data();

        assertThat(season.name()).isEqualTo("2023/2024");
        verify(getRequestedFor(urlPathEqualTo("/seasons/1"))
                .withHeader("Authorization", equalTo("tok-test")));
    }

    // ── stages ────────────────────────────────────────────────────────────────

    @Test
    void stagesBySeasonDecodesAndSendsAuth(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/stages/seasons/1")).willReturn(okJson("""
                { "data": [{ "id": 5, "name": "Regular Season" }] }
                """)));

        var stages = client(wm.getHttpBaseUrl()).stages().bySeason(1L).get().data();

        assertThat(stages).hasSize(1);
        assertThat(stages.getFirst().name()).isEqualTo("Regular Season");
        verify(getRequestedFor(urlPathEqualTo("/stages/seasons/1"))
                .withHeader("Authorization", equalTo("tok-test")));
    }

    // ── rounds ────────────────────────────────────────────────────────────────

    @Test
    void roundsAllDecodesAndSendsAuth(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/rounds")).willReturn(okJson("""
                { "data": [{ "id": 42, "name": "Round 1" }] }
                """)));

        var rounds = client(wm.getHttpBaseUrl()).rounds().all().get().data();

        assertThat(rounds).hasSize(1);
        assertThat(rounds.getFirst().name()).isEqualTo("Round 1");
        verify(getRequestedFor(urlPathEqualTo("/rounds"))
                .withHeader("Authorization", equalTo("tok-test")));
    }

    // ── schedules ─────────────────────────────────────────────────────────────

    @Test
    void schedulesBySeasonDecodesAndSendsAuth(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/schedules/seasons/1")).willReturn(okJson("""
                { "data": [{ "id": 7, "name": "Group Stage" }] }
                """)));

        var stages = client(wm.getHttpBaseUrl()).schedules().bySeason(1L).get().data();

        assertThat(stages).hasSize(1);
        assertThat(stages.getFirst().name()).isEqualTo("Group Stage");
        verify(getRequestedFor(urlPathEqualTo("/schedules/seasons/1"))
                .withHeader("Authorization", equalTo("tok-test")));
    }
}
