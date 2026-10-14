package io.github.miro93.sportmonks.football;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.request.RequestListener;
import io.github.miro93.sportmonks.core.retry.Backoff;
import io.github.miro93.sportmonks.core.retry.RetryPolicy;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class FootballClientListenerTest {

    @Test
    void listenerSeesEveryAttemptIncludingRetries(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/fixtures/1")).inScenario("retry")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse().withStatus(503))
                .willSetStateTo("ok"));
        stubFor(get(urlPathEqualTo("/fixtures/1")).inScenario("retry")
                .whenScenarioStateIs("ok")
                .willReturn(okJson("{ \"data\": { \"id\": 1 } }")));
        List<String> events = new CopyOnWriteArrayList<>();

        var client = FootballClient.builder()
                .apiToken(ApiToken.of("tok"))
                .baseUrl(wm.getHttpBaseUrl())
                .retryPolicy(RetryPolicy.builder().maxAttempts(2).backoff(new Backoff(Duration.ofMillis(1), Duration.ofMillis(1), 1.0)).build())
                .listener(new RequestListener() {
                    @Override
                    public void onRequest(URI uri) {
                        events.add("request " + uri.getPath());
                    }

                    @Override
                    public void onResponse(URI uri, int status, Duration elapsed) {
                        events.add("response " + status);
                    }
                })
                .build();

        assertThat(client.fixtures().byId(1L).get().data().id()).isEqualTo(1L);
        assertThat(events).containsExactly("request /fixtures/1", "response 503", "request /fixtures/1", "response 200");
    }
}
