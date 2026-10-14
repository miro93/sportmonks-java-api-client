package io.github.miro93.sportmonks.football.endpoint;

    import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
    import com.github.tomakehurst.wiremock.junit5.WireMockTest;
    import io.github.miro93.sportmonks.core.ApiExecutor;
    import io.github.miro93.sportmonks.core.auth.ApiToken;
    import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
    import io.github.miro93.sportmonks.core.json.HelidonJsonCodec;
    import org.junit.jupiter.api.Test;

    import java.net.http.HttpClient;
    import java.time.Duration;
    import java.time.LocalDate;

    import static com.github.tomakehurst.wiremock.client.WireMock.*;
    import static org.assertj.core.api.Assertions.assertThat;

    @WireMockTest
    class NewsEndpointTest {

        private final HelidonJsonCodec codec = new HelidonJsonCodec();

        private NewsEndpoint endpoint(String baseUrl) {
            var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
            var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
            return new NewsEndpoint(executor, codec);
        }

    @Test
    void preMatchHitsPreMatchPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/news/pre-match")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        var response = endpoint(wm.getHttpBaseUrl()).preMatch().get();

        assertThat(response.data()).hasSize(2);
    }

    @Test
    void preMatchBySeasonHitsSeasonsPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/news/pre-match/seasons/19686")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        var response = endpoint(wm.getHttpBaseUrl()).preMatchBySeason(19686L).get();

        assertThat(response.data()).hasSize(2);
    }

    @Test
    void preMatchUpcomingHitsUpcomingPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/news/pre-match/upcoming")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        var response = endpoint(wm.getHttpBaseUrl()).preMatchUpcoming().get();

        assertThat(response.data()).hasSize(2);
    }

    @Test
    void postMatchHitsPostMatchPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/news/post-match")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        var response = endpoint(wm.getHttpBaseUrl()).postMatch().get();

        assertThat(response.data()).hasSize(2);
    }

    @Test
    void postMatchBySeasonHitsSeasonsPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/news/post-match/seasons/19686")).willReturn(okJson("""
                { "data": [ { "id": 1 }, { "id": 2 } ] }
                """)));

        var response = endpoint(wm.getHttpBaseUrl()).postMatchBySeason(19686L).get();

        assertThat(response.data()).hasSize(2);
    }
}
