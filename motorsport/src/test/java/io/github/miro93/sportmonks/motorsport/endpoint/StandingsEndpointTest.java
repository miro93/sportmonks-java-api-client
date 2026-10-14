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
class StandingsEndpointTest {

    private final HelidonJsonCodec codec = new HelidonJsonCodec();

    private StandingsEndpoint endpoint(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new StandingsEndpoint(executor, codec);
    }

    @Test
    void driversHitsDriversPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/standings/drivers")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        assertThat(endpoint(wm.getHttpBaseUrl()).drivers().get().data()).hasSize(2);
    }

    @Test
    void teamsHitsTeamsPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/standings/teams")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        assertThat(endpoint(wm.getHttpBaseUrl()).teams().get().data()).hasSize(2);
    }

    @Test
    void driversBySeasonHitsSeasonsPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/standings/drivers/seasons/26733")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        assertThat(endpoint(wm.getHttpBaseUrl()).driversBySeason(26733L).get().data()).hasSize(2);
    }

    @Test
    void teamsBySeasonHitsSeasonsPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/standings/teams/seasons/26733")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        assertThat(endpoint(wm.getHttpBaseUrl()).teamsBySeason(26733L).get().data()).hasSize(2);
    }
}
