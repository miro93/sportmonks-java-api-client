package io.github.miro93.sportmonks.core;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.error.NotFoundException;
import io.github.miro93.sportmonks.core.error.ServerException;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.core.response.ApiResponse;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest
class ApiExecutorTest {

    record Team(long id, String name) {
    }

    private final JacksonCodec codec = new JacksonCodec();

    private ApiExecutor executor(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        return new ApiExecutor(transport, codec, ApiToken.of("tok-99"), baseUrl);
    }

    @Test
    void executesAndDecodesSingleResource(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/teams/1")).willReturn(okJson("""
                { "data": { "id": 1, "name": "Ajax" } }
                """)));

        ApiResponse<Team> response = executor(wm.getHttpBaseUrl())
                .execute(RequestSpec.builder("teams/1").build(), codec.type(Team.class));

        assertThat(response.data().name()).isEqualTo("Ajax");
        verify(getRequestedFor(urlPathEqualTo("/teams/1"))
                .withHeader("Authorization", equalTo("tok-99")));
    }

    @Test
    void mapsNotFoundToTypedException(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/teams/999")).willReturn(aResponse().withStatus(404).withBody("missing")));

        assertThatThrownBy(() -> executor(wm.getHttpBaseUrl())
                .execute(RequestSpec.builder("teams/999").build(), codec.type(Team.class)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void wrapsUnparseableSuccessBodyAsServerException(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/teams/1")).willReturn(aResponse().withStatus(200).withBody("{ not json")));

        assertThatThrownBy(() -> executor(wm.getHttpBaseUrl())
                .execute(RequestSpec.builder("teams/1").build(), codec.type(Team.class)))
                .isInstanceOf(ServerException.class);
    }

    @Test
    void executeAsyncReturnsDecodedResponse(WireMockRuntimeInfo wm) throws Exception {
        stubFor(get(urlPathEqualTo("/teams")).willReturn(okJson("""
                { "data": [ { "id": 1, "name": "Ajax" } ] }
                """)));

        var future = executor(wm.getHttpBaseUrl())
                .executeAsync(RequestSpec.builder("teams").build(), codec.listType(Team.class));
        @SuppressWarnings("unchecked")
        ApiResponse<List<Team>> response = (ApiResponse<List<Team>>) (Object) future.get();

        assertThat(response.data()).hasSize(1);
    }
}
