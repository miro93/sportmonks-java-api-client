package io.github.miro93.sportmonks.football;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class FootballClientM5Test {

    private FootballClient client(String baseUrl) {
        return FootballClient.builder()
                .apiToken(ApiToken.of("tok"))
                .baseUrl(baseUrl)
                .build();
    }

    // ── non-null accessor checks ──────────────────────────────────────────────

    @Test
    void exposesM5Endpoints(WireMockRuntimeInfo wm) {
        var client = client(wm.getHttpBaseUrl());
        assertThat(client.standings()).isNotNull();
        assertThat(client.topscorers()).isNotNull();
    }

    // ── standings ─────────────────────────────────────────────────────────────

    @Test
    void standingsBySeasonDecodesAndSendsAuth(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/standings/seasons/19735")).willReturn(okJson("""
                { "data": [{ "id": 1, "season_id": 19735, "position": 1 }] }
                """)));

        var standings = client(wm.getHttpBaseUrl()).standings().bySeason(19735L).get().data();

        assertThat(standings).hasSize(1);
        assertThat(standings.getFirst().id()).isEqualTo(1L);
        assertThat(standings.getFirst().seasonId()).isEqualTo(19735L);
        verify(getRequestedFor(urlPathEqualTo("/standings/seasons/19735"))
                .withHeader("Authorization", equalTo("tok")));
    }

    // ── topscorers ────────────────────────────────────────────────────────────

    @Test
    void topscorersBySeasonDecodesAndSendsAuth(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/topscorers/seasons/19735")).willReturn(okJson("""
                { "data": [{ "id": 7, "season_id": 19735, "position": 1, "total": 30 }] }
                """)));

        var topscorers = client(wm.getHttpBaseUrl()).topscorers().bySeason(19735L).get().data();

        assertThat(topscorers).hasSize(1);
        assertThat(topscorers.getFirst().id()).isEqualTo(7L);
        assertThat(topscorers.getFirst().total()).isEqualTo(30);
        verify(getRequestedFor(urlPathEqualTo("/topscorers/seasons/19735"))
                .withHeader("Authorization", equalTo("tok")));
    }
}
