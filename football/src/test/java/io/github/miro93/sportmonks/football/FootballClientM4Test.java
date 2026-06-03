package io.github.miro93.sportmonks.football;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class FootballClientM4Test {

    private FootballClient client(String baseUrl) {
        return FootballClient.builder()
                .apiToken(ApiToken.of("tok-test"))
                .baseUrl(baseUrl)
                .build();
    }

    // ── non-null accessor checks ──────────────────────────────────────────────

    @Test
    void exposesM4Endpoints(WireMockRuntimeInfo wm) {
        var client = client(wm.getHttpBaseUrl());
        assertThat(client.teams()).isNotNull();
        assertThat(client.players()).isNotNull();
        assertThat(client.coaches()).isNotNull();
        assertThat(client.squads()).isNotNull();
        assertThat(client.transfers()).isNotNull();
    }

    // ── teams ─────────────────────────────────────────────────────────────────

    @Test
    void teamsAllDecodesAndSendsAuth(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/teams")).willReturn(okJson("""
                { "data": [{ "id": 1, "placeholder": false }] }
                """)));

        var teams = client(wm.getHttpBaseUrl()).teams().all().get().data();

        assertThat(teams).hasSize(1);
        assertThat(teams.getFirst().id()).isEqualTo(1L);
        verify(getRequestedFor(urlPathEqualTo("/teams"))
                .withHeader("Authorization", equalTo("tok-test")));
    }

    // ── players ───────────────────────────────────────────────────────────────

    @Test
    void playersByIdDecodesAndSendsAuth(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/players/1")).willReturn(okJson("""
                { "data": { "id": 1, "name": "Lionel Messi" } }
                """)));

        var player = client(wm.getHttpBaseUrl()).players().byId(1L).get().data();

        assertThat(player.name()).isEqualTo("Lionel Messi");
        verify(getRequestedFor(urlPathEqualTo("/players/1"))
                .withHeader("Authorization", equalTo("tok-test")));
    }

    // ── coaches ───────────────────────────────────────────────────────────────

    @Test
    void coachesAllDecodesAndSendsAuth(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/coaches")).willReturn(okJson("""
                { "data": [{ "id": 7, "name": "Pep Guardiola" }] }
                """)));

        var coaches = client(wm.getHttpBaseUrl()).coaches().all().get().data();

        assertThat(coaches).hasSize(1);
        assertThat(coaches.getFirst().name()).isEqualTo("Pep Guardiola");
        verify(getRequestedFor(urlPathEqualTo("/coaches"))
                .withHeader("Authorization", equalTo("tok-test")));
    }

    // ── squads ────────────────────────────────────────────────────────────────

    @Test
    void squadsByTeamDecodesAndSendsAuth(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/squads/teams/1")).willReturn(okJson("""
                { "data": [{ "id": 99, "team_id": 1 }] }
                """)));

        var squad = client(wm.getHttpBaseUrl()).squads().byTeam(1L).get().data();

        assertThat(squad).hasSize(1);
        assertThat(squad.getFirst().teamId()).isEqualTo(1L);
        verify(getRequestedFor(urlPathEqualTo("/squads/teams/1"))
                .withHeader("Authorization", equalTo("tok-test")));
    }

    // ── transfers ─────────────────────────────────────────────────────────────

    @Test
    void transfersLatestDecodesAndSendsAuth(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/transfers/latest")).willReturn(okJson("""
                { "data": [{ "id": 42 }] }
                """)));

        var transfers = client(wm.getHttpBaseUrl()).transfers().latest().get().data();

        assertThat(transfers).hasSize(1);
        assertThat(transfers.getFirst().id()).isEqualTo(42L);
        verify(getRequestedFor(urlPathEqualTo("/transfers/latest"))
                .withHeader("Authorization", equalTo("tok-test")));
    }
}
