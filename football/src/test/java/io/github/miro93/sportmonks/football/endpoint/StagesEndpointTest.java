package io.github.miro93.sportmonks.football.endpoint;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.football.model.Stage;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest
class StagesEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private StagesEndpoint stages(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new StagesEndpoint(executor, codec);
    }

    @Test
    void allHitsStagesRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/stages")).willReturn(okJson("""
                { "data": [ { "id": 1, "name": "Group Stage" } ] }
                """)));

        var response = stages(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().get(0).name()).isEqualTo("Group Stage");
    }

    @Test
    void byIdHitsTheCorrectPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlEqualTo("/stages/77453735"))
                .willReturn(okJson("""
                        { "data": { "id": 77453735, "name": "Regular Season" } }
                        """)));

        Stage stage = stages(wm.getHttpBaseUrl())
                .byId(77453735L)
                .get()
                .data();

        assertThat(stage.id()).isEqualTo(77453735L);
        assertThat(stage.name()).isEqualTo("Regular Season");
    }

    @Test
    void byIdWithIncludesHitsCorrectPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlEqualTo("/stages/77453735?include=rounds"))
                .willReturn(okJson("""
                        { "data": { "id": 77453735, "name": "Regular Season" } }
                        """)));

        Stage stage = stages(wm.getHttpBaseUrl())
                .byId(77453735L)
                .include("rounds")
                .get()
                .data();

        assertThat(stage.id()).isEqualTo(77453735L);
    }

    @Test
    void bySeasonHitsSeasonsPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/stages/seasons/19686")).willReturn(okJson("""
                { "data": [ { "id": 1, "name": "Group Stage" } ] }
                """)));

        var response = stages(wm.getHttpBaseUrl())
                .bySeason(19686L)
                .get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().get(0).id()).isEqualTo(1L);
    }

    @Test
    void searchHitsSearchPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/stages/search/Regular")).willReturn(okJson("""
                { "data": [ { "id": 77453735, "name": "Regular Season" } ] }
                """)));

        var response = stages(wm.getHttpBaseUrl())
                .search("Regular")
                .get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().get(0).name()).isEqualTo("Regular Season");
    }

    @Test
    void searchRejectsNull(WireMockRuntimeInfo wm) {
        assertThatThrownBy(() -> stages(wm.getHttpBaseUrl()).search(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void bySeasonUsesGetAsync(WireMockRuntimeInfo wm) throws Exception {
        stubFor(get(urlPathEqualTo("/stages/seasons/19687")).willReturn(okJson("""
                { "data": [ { "id": 10, "name": "Group Stage" }, { "id": 11, "name": "Knockout" } ] }
                """)));

        var future = stages(wm.getHttpBaseUrl())
                .bySeason(19687L)
                .getAsync();

        var response = future.get();
        assertThat(response.data()).hasSize(2);
        assertThat(response.data().get(0).id()).isEqualTo(10L);
    }
}
