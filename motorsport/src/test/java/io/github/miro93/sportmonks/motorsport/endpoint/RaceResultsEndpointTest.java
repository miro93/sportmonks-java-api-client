package io.github.miro93.sportmonks.motorsport.endpoint;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.HelidonJsonCodec;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.LocalDate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class RaceResultsEndpointTest {

    private final HelidonJsonCodec codec = new HelidonJsonCodec();

    private RaceResultsEndpoint endpoint(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new RaceResultsEndpoint(executor, codec);
    }

    @Test
    void bySeasonAndDriverHitsResultsPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/results/seasons/25273/drivers/37920802")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        assertThat(endpoint(wm.getHttpBaseUrl()).bySeasonAndDriver(25273L, 37920802L).get().data()).hasSize(2);
    }

    @Test
    void bySeasonAndTeamHitsResultsPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/results/seasons/25273/teams/276191")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        assertThat(endpoint(wm.getHttpBaseUrl()).bySeasonAndTeam(25273L, 276191L).get().data()).hasSize(2);
    }
}
