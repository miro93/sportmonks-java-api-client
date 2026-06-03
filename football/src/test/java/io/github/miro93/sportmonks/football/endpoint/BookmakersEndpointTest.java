package io.github.miro93.sportmonks.football.endpoint;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.football.model.Bookmaker;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest
class BookmakersEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private BookmakersEndpoint bookmakers(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new BookmakersEndpoint(executor, codec);
    }

    @Test
    void allHitsBookmakersRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/bookmakers")).willReturn(okJson("""
                { "data": [ { "id": 34, "legacy_id": 2, "name": "bet365" } ] }
                """)));

        var response = bookmakers(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().name()).isEqualTo("bet365");
    }

    @Test
    void byIdHitsTheCorrectPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlEqualTo("/bookmakers/34")).willReturn(okJson("""
                { "data": { "id": 34, "legacy_id": 2, "name": "bet365" } }
                """)));

        Bookmaker bookmaker = bookmakers(wm.getHttpBaseUrl()).byId(34L).get().data();

        assertThat(bookmaker.id()).isEqualTo(34L);
        assertThat(bookmaker.legacyId()).isEqualTo(2L);
    }

    @Test
    void searchHitsSearchPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/bookmakers/search/bet")).willReturn(okJson("""
                { "data": [ { "id": 34, "name": "bet365" } ] }
                """)));

        var response = bookmakers(wm.getHttpBaseUrl()).search("bet").get();

        assertThat(response.data()).hasSize(1);
    }

    @Test
    void byFixtureHitsFixturesPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/bookmakers/fixtures/18535517")).willReturn(okJson("""
                { "data": [ { "id": 34, "name": "bet365" } ] }
                """)));

        var response = bookmakers(wm.getHttpBaseUrl()).byFixture(18535517L).get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().id()).isEqualTo(34L);
    }

    @Test
    void searchRejectsNull(WireMockRuntimeInfo wm) {
        assertThatThrownBy(() -> bookmakers(wm.getHttpBaseUrl()).search(null))
                .isInstanceOf(NullPointerException.class);
    }
}
