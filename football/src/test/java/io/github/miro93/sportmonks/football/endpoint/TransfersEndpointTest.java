package io.github.miro93.sportmonks.football.endpoint;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.football.model.Transfer;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.LocalDate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest
class TransfersEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private TransfersEndpoint transfers(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new TransfersEndpoint(executor, codec);
    }

    @Test
    void allHitsTransfersRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/transfers")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        var response = transfers(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(2);
        assertThat(response.data().get(0).id()).isEqualTo(1L);
    }

    @Test
    void byIdHitsTheCorrectPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlEqualTo("/transfers/42"))
                .willReturn(okJson("""
                        { "data": { "id": 42, "date": "2024-01-15" } }
                        """)));

        Transfer transfer = transfers(wm.getHttpBaseUrl())
                .byId(42L)
                .get()
                .data();

        assertThat(transfer.id()).isEqualTo(42L);
        assertThat(transfer.date()).isEqualTo("2024-01-15");
    }

    @Test
    void latestHitsLatestPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/transfers/latest")).willReturn(okJson("""
                { "data": [ { "id": 100, "date": "2024-01-15" } ] }
                """)));

        var response = transfers(wm.getHttpBaseUrl()).latest().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().get(0).id()).isEqualTo(100L);
        assertThat(response.data().get(0).date()).isEqualTo("2024-01-15");
    }

    @Test
    void byDateRangeHitsBetweenPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/transfers/between/2024-01-01/2024-01-31")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        var response = transfers(wm.getHttpBaseUrl())
                .byDateRange(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31))
                .get();

        assertThat(response.data()).hasSize(2);
        assertThat(response.data().get(0).id()).isEqualTo(1L);
        assertThat(response.data().get(1).id()).isEqualTo(2L);
    }

    @Test
    void byDateRangeRejectsNullStart(WireMockRuntimeInfo wm) {
        assertThatThrownBy(() -> transfers(wm.getHttpBaseUrl())
                .byDateRange(null, LocalDate.of(2024, 1, 31)))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("start");
    }

    @Test
    void byDateRangeRejectsNullEnd(WireMockRuntimeInfo wm) {
        assertThatThrownBy(() -> transfers(wm.getHttpBaseUrl())
                .byDateRange(LocalDate.of(2024, 1, 1), null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("end");
    }

    @Test
    void byTeamHitsTeamPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/transfers/teams/53")).willReturn(okJson("""
                { "data": [ { "id": 5 }, { "id": 6 } ] }
                """)));

        var response = transfers(wm.getHttpBaseUrl()).byTeam(53L).get();

        assertThat(response.data()).hasSize(2);
        assertThat(response.data().get(0).id()).isEqualTo(5L);
    }

    @Test
    void byPlayerHitsPlayerPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/transfers/players/101")).willReturn(okJson("""
                { "data": [ { "id": 7 }, { "id": 8 } ] }
                """)));

        var response = transfers(wm.getHttpBaseUrl()).byPlayer(101L).get();

        assertThat(response.data()).hasSize(2);
        assertThat(response.data().get(0).id()).isEqualTo(7L);
        assertThat(response.data().get(1).id()).isEqualTo(8L);
    }

    @Test
    void byDateRangeUsesGetAsync(WireMockRuntimeInfo wm) throws Exception {
        stubFor(get(urlPathEqualTo("/transfers/between/2024-06-01/2024-08-31")).willReturn(okJson("""
                { "data": [ { "id": 99, "date": "2024-07-01" } ] }
                """)));

        var future = transfers(wm.getHttpBaseUrl())
                .byDateRange(LocalDate.of(2024, 6, 1), LocalDate.of(2024, 8, 31))
                .getAsync();

        var response = future.get();
        assertThat(response.data()).hasSize(1);
        assertThat(response.data().get(0).id()).isEqualTo(99L);
        assertThat(response.data().get(0).date()).isEqualTo("2024-07-01");
    }

    @Test
    void byIdDecodesNestedRelations(WireMockRuntimeInfo wm) {
        stubFor(get(urlEqualTo("/transfers/42?include=player;fromTeam;toTeam"))
                .willReturn(okJson("""
                        {
                          "data": {
                            "id": 42,
                            "from_team_id": 10,
                            "to_team_id": 20,
                            "player": { "id": 101, "display_name": "Jane Smith" },
                            "from_team": { "id": 10, "name": "Club A", "placeholder": false },
                            "to_team": { "id": 20, "name": "Club B", "placeholder": false }
                          }
                        }
                        """)));

        Transfer transfer = transfers(wm.getHttpBaseUrl())
                .byId(42L)
                .include("player", "fromTeam", "toTeam")
                .get()
                .data();

        assertThat(transfer.fromTeamId()).isEqualTo(10L);
        assertThat(transfer.toTeamId()).isEqualTo(20L);
        assertThat(transfer.player()).isNotNull();
        assertThat(transfer.player().displayName()).isEqualTo("Jane Smith");
        assertThat(transfer.fromTeam()).isNotNull();
        assertThat(transfer.fromTeam().name()).isEqualTo("Club A");
        assertThat(transfer.toTeam()).isNotNull();
        assertThat(transfer.toTeam().name()).isEqualTo("Club B");
    }
}
