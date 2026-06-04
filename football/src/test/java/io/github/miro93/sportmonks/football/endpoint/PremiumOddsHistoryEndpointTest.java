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
class PremiumOddsHistoryEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private PremiumOddsHistoryEndpoint history(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new PremiumOddsHistoryEndpoint(executor, codec);
    }

    @Test
    void allHitsHistoryRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/odds/premium/history")).willReturn(okJson("""
                { "data": [ { "id": 100, "odd_id": 1, "value": "1.48" } ] }
                """)));

        var response = history(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().oddId()).isEqualTo(1L);
    }

    @Test
    void updatedBetweenHitsTimeRangePath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/odds/premium/history/updated/between/1767225600/1767225900")).willReturn(okJson("""
                { "data": [ { "id": 100, "value": "1.48" } ] }
                """)));

        var response = history(wm.getHttpBaseUrl()).updatedBetween(1767225600L, 1767225900L).get();

        assertThat(response.data()).hasSize(1);
    }
}
