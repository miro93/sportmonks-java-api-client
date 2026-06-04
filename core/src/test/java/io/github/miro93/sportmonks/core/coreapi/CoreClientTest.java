package io.github.miro93.sportmonks.core.coreapi;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.error.TransportException;
import io.github.miro93.sportmonks.core.retry.RetryPolicy;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest
class CoreClientTest {

    @Test
    void defaultBaseUrlIsCoreApi() {
        assertThat(CoreClient.DEFAULT_BASE_URL).isEqualTo("https://api.sportmonks.com/v3/core");
    }

    @Test
    void builderRequiresApiToken() {
        assertThatThrownBy(() -> CoreClient.builder().build())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void exposesAllEndpoints() {
        var client = CoreClient.builder().apiToken(ApiToken.of("tok")).build();
        assertThat(client.continents()).isNotNull();
        assertThat(client.countries()).isNotNull();
        assertThat(client.regions()).isNotNull();
        assertThat(client.cities()).isNotNull();
        assertThat(client.types()).isNotNull();
    }

    @Test
    void httpClientRejectsNull() {
        assertThatThrownBy(() -> CoreClient.builder().httpClient(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void injectedHttpClientIsActuallyUsed(WireMockRuntimeInfo wm) {
        // Would succeed via the default client; a dead proxy proves the injected client is used.
        stubFor(get(urlPathEqualTo("/continents")).willReturn(okJson("""
                { "data": [ { "id": 1, "name": "Europe", "code": "EU" } ] }
                """)));
        HttpClient deadProxyClient = HttpClient.newBuilder()
                .proxy(ProxySelector.of(new InetSocketAddress("localhost", 1)))
                .build();

        var client = CoreClient.builder()
                .apiToken(ApiToken.of("tok"))
                .baseUrl(wm.getHttpBaseUrl())
                .httpClient(deadProxyClient)
                .retryPolicy(RetryPolicy.none())
                .build();

        assertThatThrownBy(() -> client.continents().all().get())
                .isInstanceOf(TransportException.class);
    }

    @Test
    void honoursConfiguredBaseUrlAndSendsAuth(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/continents")).willReturn(okJson("""
                { "data": [ { "id": 1, "name": "Europe", "code": "EU" } ] }
                """)));

        var client = CoreClient.builder()
                .apiToken(ApiToken.of("tok"))
                .baseUrl(wm.getHttpBaseUrl())
                .build();

        var response = client.continents().all().get();

        assertThat(response.data()).hasSize(1);
        verify(getRequestedFor(urlPathEqualTo("/continents"))
                .withHeader("Authorization", equalTo("tok")));
    }
}
