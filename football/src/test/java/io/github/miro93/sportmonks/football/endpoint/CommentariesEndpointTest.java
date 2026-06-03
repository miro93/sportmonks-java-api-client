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
class CommentariesEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private CommentariesEndpoint commentaries(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new CommentariesEndpoint(executor, codec);
    }

    @Test
    void allHitsCommentariesRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/commentaries")).willReturn(okJson("""
                { "data": [ { "id": "abc1", "fixture_id": 18535517, "comment": "Kick-off", "minute": 1, "is_goal": false } ] }
                """)));

        var response = commentaries(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().id()).isEqualTo("abc1");
        assertThat(response.data().getFirst().fixtureId()).isEqualTo(18535517L);
    }

    @Test
    void byFixtureHitsFixturesPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/commentaries/fixtures/18535517")).willReturn(okJson("""
                { "data": [ { "id": "abc1", "fixture_id": 18535517, "comment": "Goal!", "is_goal": true, "minute": 23 } ] }
                """)));

        var response = commentaries(wm.getHttpBaseUrl()).byFixture(18535517L).get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().isGoal()).isTrue();
        assertThat(response.data().getFirst().minute()).isEqualTo(23);
    }
}
