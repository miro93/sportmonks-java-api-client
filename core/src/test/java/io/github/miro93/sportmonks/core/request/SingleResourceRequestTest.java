package io.github.miro93.sportmonks.core.request;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.response.ApiResponse;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class SingleResourceRequestTest {

    record Team(long id, String name) {
    }

    private final JacksonCodec codec = new JacksonCodec();

    private ApiExecutor executor(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        return new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
    }

    @Test
    void fluentIncludesReachTheUrlAndResultDecodes(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/teams/1"))
                .withQueryParam("include", equalTo("country"))
                .willReturn(okJson("""
                        { "data": { "id": 1, "name": "Ajax" } }
                        """)));

        ApiResponse<Team> response = new SingleResourceRequest<Team>(
                executor(wm.getHttpBaseUrl()), RequestSpec.builder("teams/1"), codec.type(Team.class))
                .include("country")
                .get();

        assertThat(response.data().name()).isEqualTo("Ajax");
    }

    @Test
    void getAsyncReturnsDecodedResource(WireMockRuntimeInfo wm) throws Exception {
        stubFor(get(urlPathEqualTo("/teams/2")).willReturn(okJson("""
                { "data": { "id": 2, "name": "PSV" } }
                """)));

        var future = new SingleResourceRequest<Team>(
                executor(wm.getHttpBaseUrl()), RequestSpec.builder("teams/2"), codec.type(Team.class))
                .getAsync();

        assertThat(future.get().data().name()).isEqualTo("PSV");
    }
}
