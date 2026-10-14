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
class BookmakerMappingsEndpointTest {

    private final HelidonJsonCodec codec = new HelidonJsonCodec();

    private BookmakerMappingsEndpoint endpoint(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new BookmakerMappingsEndpoint(executor, codec);
    }

    @Test
    void byFixtureHitsMappingPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/bookmakers/fixtures/19427243/mapping")).willReturn(okJson("""
                { "data": [ { "fixture_id": 19427243, "bookmaker_id": 2, "bookmaker_name": "bet365",
                              "bookmaker_event_id": "194085995", "bookmaker_event_url": null } ] }
                """)));

        var response = endpoint(wm.getHttpBaseUrl()).byFixture(19427243L).get();

        var mapping = response.data().getFirst();
        assertThat(mapping.bookmakerId()).isEqualTo(2L);
        assertThat(mapping.bookmakerEventId()).isEqualTo("194085995");
        assertThat(mapping.bookmakerEventUrl()).isNull();
    }
}
