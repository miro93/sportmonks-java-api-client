package io.github.miro93.sportmonks.football.endpoint;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.football.model.Referee;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest
class RefereesEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private RefereesEndpoint referees(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new RefereesEndpoint(executor, codec);
    }

    @Test
    void allHitsRefereesRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/referees")).willReturn(okJson("""
                { "data": [ { "id": 14, "name": "John Beaton" } ] }
                """)));

        var response = referees(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().name()).isEqualTo("John Beaton");
    }

    @Test
    void byIdHitsTheCorrectPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlEqualTo("/referees/14")).willReturn(okJson("""
                { "data": { "id": 14, "name": "John Beaton", "country_id": 1161 } }
                """)));

        Referee referee = referees(wm.getHttpBaseUrl()).byId(14L).get().data();

        assertThat(referee.id()).isEqualTo(14L);
        assertThat(referee.countryId()).isEqualTo(1161L);
    }

    @Test
    void byCountryHitsCountriesPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/referees/countries/1161")).willReturn(okJson("""
                { "data": [ { "id": 14, "name": "John Beaton" } ] }
                """)));

        var response = referees(wm.getHttpBaseUrl()).byCountry(1161L).get();

        assertThat(response.data()).hasSize(1);
    }

    @Test
    void bySeasonHitsSeasonsPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/referees/seasons/19735")).willReturn(okJson("""
                { "data": [ { "id": 14, "name": "John Beaton" } ] }
                """)));

        var response = referees(wm.getHttpBaseUrl()).bySeason(19735L).get();

        assertThat(response.data()).hasSize(1);
    }

    @Test
    void searchHitsSearchPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/referees/search/Beaton")).willReturn(okJson("""
                { "data": [ { "id": 14, "name": "John Beaton" } ] }
                """)));

        var response = referees(wm.getHttpBaseUrl()).search("Beaton").get();

        assertThat(response.data()).hasSize(1);
    }

    @Test
    void searchRejectsNull(WireMockRuntimeInfo wm) {
        assertThatThrownBy(() -> referees(wm.getHttpBaseUrl()).search(null))
                .isInstanceOf(NullPointerException.class);
    }
}
