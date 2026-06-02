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
class LivescoresEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private LivescoresEndpoint livescores(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new LivescoresEndpoint(executor, codec);
    }

    @Test
    void inplayHitsInplayPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlEqualTo("/livescores/inplay?include=scores;state"))
                .willReturn(okJson("""
                        { "data": [ { "id": 1, "name": "A vs B" } ] }
                        """)));

        var response = livescores(wm.getHttpBaseUrl())
                .inplay()
                .include("scores", "state")
                .get();

        assertThat(response.data()).hasSize(1);
    }

    @Test
    void allHitsLivescoresRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/livescores")).willReturn(okJson("""
                { "data": [ { "id": 1, "name": "A vs B" } ] }
                """)));

        assertThat(livescores(wm.getHttpBaseUrl()).all().get().data()).hasSize(1);
    }

    @Test
    void latestHitsLatestPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/livescores/latest")).willReturn(okJson("""
                { "data": [ { "id": 1, "name": "A vs B" } ] }
                """)));

        assertThat(livescores(wm.getHttpBaseUrl()).latest().get().data()).hasSize(1);
    }
}
