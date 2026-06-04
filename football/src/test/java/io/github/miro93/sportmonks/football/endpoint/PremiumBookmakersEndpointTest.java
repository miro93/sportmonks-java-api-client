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
class PremiumBookmakersEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private PremiumBookmakersEndpoint bookmakers(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new PremiumBookmakersEndpoint(executor, codec);
    }

    @Test
    void allHitsPremiumBookmakersPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/bookmakers/premium")).willReturn(okJson("""
                { "data": [ { "id": 34, "legacy_id": 2, "name": "bet365" } ] }
                """)));

        var response = bookmakers(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().id()).isEqualTo(34L);
        assertThat(response.data().getFirst().name()).isEqualTo("bet365");
    }
}
