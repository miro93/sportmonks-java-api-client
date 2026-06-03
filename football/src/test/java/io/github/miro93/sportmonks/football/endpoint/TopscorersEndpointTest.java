package io.github.miro93.sportmonks.football.endpoint;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.football.model.Topscorer;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class TopscorersEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private TopscorersEndpoint topscorers(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new TopscorersEndpoint(executor, codec);
    }

    @Test
    void bySeasonHitsSeasonPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/topscorers/seasons/19735")).willReturn(okJson("""
                {
                  "data": [
                    {
                      "id": 1001,
                      "season_id": 19735,
                      "player_id": 55001,
                      "position": 1,
                      "total": 22
                    },
                    {
                      "id": 1002,
                      "season_id": 19735,
                      "player_id": 55002,
                      "position": 2,
                      "total": 18
                    }
                  ]
                }
                """)));

        var response = topscorers(wm.getHttpBaseUrl())
                .bySeason(19735L)
                .get();

        assertThat(response.data()).hasSize(2);
        Topscorer first = response.data().getFirst();
        assertThat(first.id()).isEqualTo(1001L);
        assertThat(first.position()).isEqualTo(1);
        assertThat(first.total()).isEqualTo(22);
        assertThat(first.playerId()).isEqualTo(55001L);
    }

    @Test
    void byStageHitsStagePath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/topscorers/stages/77457866")).willReturn(okJson("""
                {
                  "data": [
                    {
                      "id": 2001,
                      "stage_id": 77457866,
                      "player_id": 66001,
                      "position": 1,
                      "total": 14
                    }
                  ]
                }
                """)));

        var response = topscorers(wm.getHttpBaseUrl())
                .byStage(77457866L)
                .get();

        assertThat(response.data()).hasSize(1);
        Topscorer first = response.data().getFirst();
        assertThat(first.id()).isEqualTo(2001L);
        assertThat(first.position()).isEqualTo(1);
        assertThat(first.total()).isEqualTo(14);
        assertThat(first.playerId()).isEqualTo(66001L);
    }

    @Test
    void bySeasonUsesGetAsync(WireMockRuntimeInfo wm) throws Exception {
        stubFor(get(urlPathEqualTo("/topscorers/seasons/19735")).willReturn(okJson("""
                {
                  "data": [
                    {
                      "id": 3001,
                      "season_id": 19735,
                      "player_id": 77001,
                      "position": 3,
                      "total": 11
                    },
                    {
                      "id": 3002,
                      "season_id": 19735,
                      "player_id": 77002,
                      "position": 4,
                      "total": 9
                    }
                  ]
                }
                """)));

        var future = topscorers(wm.getHttpBaseUrl())
                .bySeason(19735L)
                .getAsync();

        var response = future.get();
        assertThat(response.data()).hasSize(2);
        assertThat(response.data().getFirst().id()).isEqualTo(3001L);
        assertThat(response.data().getFirst().total()).isEqualTo(11);
    }
}
