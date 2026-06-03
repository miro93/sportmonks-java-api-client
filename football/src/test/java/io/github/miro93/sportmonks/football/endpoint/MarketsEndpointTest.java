package io.github.miro93.sportmonks.football.endpoint;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.football.model.Market;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest
class MarketsEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private MarketsEndpoint markets(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new MarketsEndpoint(executor, codec);
    }

    @Test
    void allHitsMarketsRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/markets")).willReturn(okJson("""
                { "data": [ { "id": 1, "legacy_id": 1, "name": "Fulltime Result", "developer_name": "FULLTIME_RESULT", "has_winning_calculations": true } ] }
                """)));

        var response = markets(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().developerName()).isEqualTo("FULLTIME_RESULT");
        assertThat(response.data().getFirst().hasWinningCalculations()).isTrue();
    }

    @Test
    void byIdHitsTheCorrectPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlEqualTo("/markets/1")).willReturn(okJson("""
                { "data": { "id": 1, "name": "Fulltime Result" } }
                """)));

        Market market = markets(wm.getHttpBaseUrl()).byId(1L).get().data();

        assertThat(market.id()).isEqualTo(1L);
        assertThat(market.name()).isEqualTo("Fulltime Result");
    }

    @Test
    void searchHitsSearchPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/markets/search/Result")).willReturn(okJson("""
                { "data": [ { "id": 1, "name": "Fulltime Result" } ] }
                """)));

        var response = markets(wm.getHttpBaseUrl()).search("Result").get();

        assertThat(response.data()).hasSize(1);
    }

    @Test
    void searchRejectsNull(WireMockRuntimeInfo wm) {
        assertThatThrownBy(() -> markets(wm.getHttpBaseUrl()).search(null))
                .isInstanceOf(NullPointerException.class);
    }
}
