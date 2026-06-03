package io.github.miro93.sportmonks.core.coreapi.endpoint;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.coreapi.model.Type;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class TypesEndpointTest {

    private final JacksonCodec codec = new JacksonCodec();

    private TypesEndpoint types(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        var executor = new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
        return new TypesEndpoint(executor, codec);
    }

    @Test
    void allHitsTypesRoot(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/types")).willReturn(okJson("""
                { "data": [ { "id": 208, "name": "Goals", "code": "goals", "developer_name": "GOALS", "group": "events" } ] }
                """)));

        var response = types(wm.getHttpBaseUrl()).all().get();

        assertThat(response.data()).hasSize(1);
        assertThat(response.data().getFirst().name()).isEqualTo("Goals");
        assertThat(response.data().getFirst().developerName()).isEqualTo("GOALS");
    }

    @Test
    void byIdHitsTheCorrectPath(WireMockRuntimeInfo wm) {
        stubFor(get(urlEqualTo("/types/208")).willReturn(okJson("""
                { "data": { "id": 208, "name": "Goals", "code": "goals", "group": "events" } }
                """)));

        Type type = types(wm.getHttpBaseUrl()).byId(208L).get().data();

        assertThat(type.id()).isEqualTo(208L);
        assertThat(type.code()).isEqualTo("goals");
    }
}
