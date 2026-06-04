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
class PremiumMarketsEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private PremiumMarketsEndpoint markets(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new PremiumMarketsEndpoint(executor, codec);
    }

    @Test
    void allHitsPremiumMarketsPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/markets/premium")).willReturn(okJson("""
                { "data": [ { "id": 1, "legacy_id": 9, "name": "Fulltime Result", "developer_name": "FULLTIME_RESULT" } ] }
                """)));

        var response = markets(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().name()).isEqualTo("Fulltime Result");
        assertThat(response.data().getFirst().developerName()).isEqualTo("FULLTIME_RESULT");
    }
}
