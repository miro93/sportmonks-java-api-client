package io.github.miro93.sportmonks.core.http;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.error.TransportException;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest
class JdkHttpTransportTest {

    private JdkHttpTransport transport() {
        return new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
    }

    @Test
    void getReturnsStatusBodyAndHeaders(WireMockRuntimeInfo wm) {
        stubFor(get("/ping").willReturn(aResponse()
                .withStatus(200)
                .withHeader("X-Test", "yes")
                .withBody("pong")));

        RawResponse response = transport().get(
                URI.create(wm.getHttpBaseUrl() + "/ping"), Map.of());

        assertThat(response.status()).isEqualTo(200);
        assertThat(response.bodyAsString()).isEqualTo("pong");
        assertThat(response.header("x-test")).contains("yes");
    }

    @Test
    void getSendsRequestHeaders(WireMockRuntimeInfo wm) {
        stubFor(get("/secured").willReturn(ok()));

        transport().get(URI.create(wm.getHttpBaseUrl() + "/secured"),
                Map.of("Authorization", "tok-42"));

        verify(getRequestedFor(urlEqualTo("/secured"))
                .withHeader("Authorization", equalTo("tok-42")));
    }

    @Test
    void getWrapsConnectionFailureAsTransportException() {
        // Nothing listening on this port.
        assertThatThrownBy(() -> transport().get(
                URI.create("http://localhost:1/nope"), Map.of()))
                .isInstanceOf(TransportException.class);
    }

    @Test
    void defaultRequestTimeoutConstantIs30s() {
        assertThat(JdkHttpTransport.DEFAULT_REQUEST_TIMEOUT).isEqualTo(Duration.ofSeconds(30));
    }

    @Test
    void newDefaultClientHasConnectTimeoutAndNeverFollowsRedirects() {
        HttpClient client = JdkHttpTransport.newDefaultClient();

        assertThat(client.connectTimeout()).contains(Duration.ofSeconds(10));
        assertThat(client.followRedirects()).isEqualTo(HttpClient.Redirect.NEVER);
    }

    @Test
    void newDefaultClientWithCustomConnectTimeoutHonoursIt() {
        HttpClient client = JdkHttpTransport.newDefaultClient(Duration.ofSeconds(5));

        assertThat(client.connectTimeout()).contains(Duration.ofSeconds(5));
        assertThat(client.followRedirects()).isEqualTo(HttpClient.Redirect.NEVER);
    }

    @Test
    void noArgNewDefaultClientDelegatesToDefaultConstant() {
        assertThat(JdkHttpTransport.newDefaultClient().connectTimeout())
                .contains(JdkHttpTransport.DEFAULT_CONNECT_TIMEOUT);
    }

    @Test
    void defaultClientDoesNotFollowRedirects(WireMockRuntimeInfo wm) {
        stubFor(get("/moved").willReturn(aResponse().withStatus(302).withHeader("Location", wm.getHttpBaseUrl() + "/elsewhere")));
        stubFor(get("/elsewhere").willReturn(ok("should not be reached")));

        RawResponse response = new JdkHttpTransport(JdkHttpTransport.newDefaultClient(), Duration.ofSeconds(5))
                .get(URI.create(wm.getHttpBaseUrl() + "/moved"), Map.of("Authorization", "tok"));

        assertThat(response.status()).isEqualTo(302);
        verify(0, getRequestedFor(urlEqualTo("/elsewhere")));
    }

    @Test
    void rejectsBodiesLargerThanTheCapAndAcceptsOnesAtTheCap(WireMockRuntimeInfo wm) {
        stubFor(get("/big").willReturn(ok("x".repeat(101))));
        stubFor(get("/fit").willReturn(ok("x".repeat(100))));
        var capped = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5), 100);

        assertThatThrownBy(() -> capped.get(URI.create(wm.getHttpBaseUrl() + "/big"), Map.of()))
                .isInstanceOf(TransportException.class)
                .hasMessageContaining("exceeds 100 bytes");
        assertThat(capped.get(URI.create(wm.getHttpBaseUrl() + "/fit"), Map.of()).bodyAsString()).hasSize(100);
        assertThatThrownBy(() -> new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5), 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
