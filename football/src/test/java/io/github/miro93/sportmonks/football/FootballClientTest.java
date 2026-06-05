package io.github.miro93.sportmonks.football;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.error.NotFoundException;
import io.github.miro93.sportmonks.core.error.TransportException;
import io.github.miro93.sportmonks.core.retry.RetryPolicy;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest
class FootballClientTest {

    private FootballClient client(String baseUrl) {
        return FootballClient.builder()
                .apiToken(ApiToken.of("tok-77"))
                .baseUrl(baseUrl)
                .build();
    }

    @Test
    void exposesFixturesAndLivescoresEndpoints(WireMockRuntimeInfo wm) {
        var client = client(wm.getHttpBaseUrl());
        assertThat(client.fixtures()).isNotNull();
        assertThat(client.livescores()).isNotNull();
    }

    @Test
    void endToEndFixtureCallSendsAuthAndDecodes(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/fixtures/1")).willReturn(okJson("""
                { "data": { "id": 1, "name": "A vs B" } }
                """)));

        var fixture = client(wm.getHttpBaseUrl()).fixtures().byId(1L).get().data();

        assertThat(fixture.name()).isEqualTo("A vs B");
        verify(getRequestedFor(urlPathEqualTo("/fixtures/1"))
                .withHeader("Authorization", equalTo("tok-77")));
    }

    @Test
    void builderRequiresApiToken() {
        assertThatThrownBy(() -> FootballClient.builder().build())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void defaultBaseUrlIsSportmonksFootballV3() {
        assertThat(FootballClient.DEFAULT_BASE_URL)
                .isEqualTo("https://api.sportmonks.com/v3/football");
    }

    @Test
    void notFoundPropagatesAsTypedException(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/fixtures/999"))
                .willReturn(aResponse().withStatus(404).withBody("missing")));

        assertThatThrownBy(() -> client(wm.getHttpBaseUrl()).fixtures().byId(999L).get())
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void httpClientRejectsNull() {
        assertThatThrownBy(() -> FootballClient.builder().httpClient(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void connectTimeoutRejectsNull() {
        assertThatThrownBy(() -> FootballClient.builder().connectTimeout(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void connectTimeoutAndHttpClientAreMutuallyExclusive() {
        HttpClient anyClient = HttpClient.newHttpClient();
        assertThatThrownBy(() -> FootballClient.builder()
                .apiToken(ApiToken.of("tok-77"))
                .connectTimeout(Duration.ofSeconds(5))
                .httpClient(anyClient)
                .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mutually exclusive");
    }

    @Test
    void connectTimeoutOnDefaultClientStillReachesServer(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/fixtures/1")).willReturn(okJson("""
                { "data": { "id": 1, "name": "A vs B" } }
                """)));

        var client = FootballClient.builder()
                .apiToken(ApiToken.of("tok-77"))
                .baseUrl(wm.getHttpBaseUrl())
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        assertThat(client.fixtures().byId(1L).get().data().name()).isEqualTo("A vs B");
    }

    @Test
    void injectedHttpClientIsActuallyUsed(WireMockRuntimeInfo wm) {
        // Deliberate guard, not dead code: if httpClient() injection ever regressed, the default
        // client would reach this stub and return 200, making the test fail clearly with "nothing
        // thrown" rather than an unrelated 404/NotFoundException.
        stubFor(get(urlPathEqualTo("/fixtures/1")).willReturn(okJson("""
                { "data": { "id": 1, "name": "A vs B" } }
                """)));
        // Port 1 is never bound in this JVM; connecting through it yields an immediate
        // ECONNREFUSED that JdkHttpTransport wraps as TransportException — fast and deterministic.
        HttpClient deadProxyClient = HttpClient.newBuilder()
                .proxy(ProxySelector.of(new InetSocketAddress("localhost", 1)))
                .build();

        var client = FootballClient.builder()
                .apiToken(ApiToken.of("tok-77"))
                .baseUrl(wm.getHttpBaseUrl())
                .httpClient(deadProxyClient)
                .retryPolicy(RetryPolicy.none())
                .build();

        assertThatThrownBy(() -> client.fixtures().byId(1L).get())
                .isInstanceOf(TransportException.class);
    }
}
