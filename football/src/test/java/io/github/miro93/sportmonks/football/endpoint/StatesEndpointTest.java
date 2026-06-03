package io.github.miro93.sportmonks.football.endpoint;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.football.model.State;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class StatesEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private StatesEndpoint states(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new StatesEndpoint(executor, codec);
    }

    @Test
    void allHitsStatesRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/states")).willReturn(okJson("""
                { "data": [ { "id": 1, "state": "NS", "name": "Not Started", "short_name": "NS", "developer_name": "NOT_STARTED" } ] }
                """)));

        var response = states(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().developerName()).isEqualTo("NOT_STARTED");
    }

    @Test
    void byIdHitsTheCorrectPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlEqualTo("/states/1")).willReturn(okJson("""
                { "data": { "id": 1, "state": "NS", "name": "Not Started", "short_name": "NS" } }
                """)));

        State state = states(wm.getHttpBaseUrl()).byId(1L).get().data();

        assertThat(state.id()).isEqualTo(1L);
        assertThat(state.shortName()).isEqualTo("NS");
    }
}
