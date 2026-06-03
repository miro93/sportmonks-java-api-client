package io.github.miro93.sportmonks.football.endpoint;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.football.model.Venue;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest
class VenuesEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private VenuesEndpoint venues(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new VenuesEndpoint(executor, codec);
    }

    @Test
    void allHitsVenuesRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/venues")).willReturn(okJson("""
                { "data": [ { "id": 8909, "name": "Celtic Park", "capacity": 60411 } ] }
                """)));

        var response = venues(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().capacity()).isEqualTo(60411);
    }

    @Test
    void byIdHitsTheCorrectPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlEqualTo("/venues/8909")).willReturn(okJson("""
                { "data": { "id": 8909, "name": "Celtic Park", "city_name": "Glasgow", "surface": "grass", "national_team": false } }
                """)));

        Venue venue = venues(wm.getHttpBaseUrl()).byId(8909L).get().data();

        assertThat(venue.id()).isEqualTo(8909L);
        assertThat(venue.cityName()).isEqualTo("Glasgow");
        assertThat(venue.nationalTeam()).isFalse();
    }

    @Test
    void bySeasonHitsSeasonsPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/venues/seasons/19735")).willReturn(okJson("""
                { "data": [ { "id": 8909, "name": "Celtic Park" } ] }
                """)));

        var response = venues(wm.getHttpBaseUrl()).bySeason(19735L).get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().id()).isEqualTo(8909L);
    }

    @Test
    void searchHitsSearchPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/venues/search/Celtic")).willReturn(okJson("""
                { "data": [ { "id": 8909, "name": "Celtic Park" } ] }
                """)));

        var response = venues(wm.getHttpBaseUrl()).search("Celtic").get();

        assertThat(response.data()).hasSize(1);
    }

    @Test
    void searchRejectsNull(WireMockRuntimeInfo wm) {
        assertThatThrownBy(() -> venues(wm.getHttpBaseUrl()).search(null))
                .isInstanceOf(NullPointerException.class);
    }
}
