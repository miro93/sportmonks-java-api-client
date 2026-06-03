package io.github.miro93.sportmonks.football.endpoint;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.football.model.Player;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest
class PlayersEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private PlayersEndpoint players(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new PlayersEndpoint(executor, codec);
    }

    @Test
    void allHitsPlayersRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/players")).willReturn(okJson("""
                { "data": [ { "id": 1, "name": "Lionel Messi" } ] }
                """)));

        var response = players(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().get(0).name()).isEqualTo("Lionel Messi");
    }

    @Test
    void byIdHitsTheCorrectPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlEqualTo("/players/14689"))
                .willReturn(okJson("""
                        { "data": { "id": 14689, "name": "Lionel Messi" } }
                        """)));

        Player player = players(wm.getHttpBaseUrl())
                .byId(14689L)
                .get()
                .data();

        assertThat(player.id()).isEqualTo(14689L);
        assertThat(player.name()).isEqualTo("Lionel Messi");
    }

    @Test
    void byIdWithIncludesHitsCorrectPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlEqualTo("/players/14689?include=statistics"))
                .willReturn(okJson("""
                        { "data": { "id": 14689, "name": "Lionel Messi" } }
                        """)));

        Player player = players(wm.getHttpBaseUrl())
                .byId(14689L)
                .include("statistics")
                .get()
                .data();

        assertThat(player.id()).isEqualTo(14689L);
    }

    @Test
    void byCountryHitsCountriesPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/players/countries/32")).willReturn(okJson("""
                { "data": [ { "id": 14689, "name": "Lionel Messi" } ] }
                """)));

        var response = players(wm.getHttpBaseUrl())
                .byCountry(32L)
                .get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().get(0).id()).isEqualTo(14689L);
    }

    @Test
    void searchHitsSearchPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/players/search/Messi")).willReturn(okJson("""
                { "data": [ { "id": 14689, "name": "Lionel Messi" } ] }
                """)));

        var response = players(wm.getHttpBaseUrl())
                .search("Messi")
                .get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().get(0).name()).isEqualTo("Lionel Messi");
    }

    @Test
    void searchRejectsNull(WireMockRuntimeInfo wm) {
        assertThatThrownBy(() -> players(wm.getHttpBaseUrl()).search(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void latestHitsLatestPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/players/latest")).willReturn(okJson("""
                { "data": [ { "id": 99991, "name": "New Player" }, { "id": 99992, "name": "Another Player" } ] }
                """)));

        var response = players(wm.getHttpBaseUrl())
                .latest()
                .get();

        assertThat(response.data()).hasSize(2);
    }

    @Test
    void byCountryUsesGetAsync(WireMockRuntimeInfo wm) throws Exception {
        stubFor(get(urlPathEqualTo("/players/countries/47")).willReturn(okJson("""
                { "data": [ { "id": 14689, "name": "Lionel Messi" }, { "id": 14690, "name": "Sergio Aguero" } ] }
                """)));

        var future = players(wm.getHttpBaseUrl())
                .byCountry(47L)
                .getAsync();

        var response = future.get();
        assertThat(response.data()).hasSize(2);
        assertThat(response.data().get(0).id()).isEqualTo(14689L);
    }
}
