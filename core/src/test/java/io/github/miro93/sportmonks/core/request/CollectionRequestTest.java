package io.github.miro93.sportmonks.core.request;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.http.JdkHttpTransport;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.internal.RequestSpec;
import io.github.miro93.sportmonks.core.response.ApiResponse;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class CollectionRequestTest {

    record Team(long id, String name) {
    }

    private final JacksonCodec codec = new JacksonCodec();

    private ApiExecutor executor(String baseUrl) {
        var transport = new JdkHttpTransport(HttpClient.newHttpClient(), Duration.ofSeconds(5));
        return new ApiExecutor(transport, codec, ApiToken.of("tok"), baseUrl);
    }

    private CollectionRequest<Team> teams(String baseUrl) {
        return new CollectionRequest<>(
                executor(baseUrl), RequestSpec.builder("teams"), codec.listType(Team.class));
    }

    @Test
    void getReturnsDecodedList(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/teams")).willReturn(okJson("""
                { "data": [ { "id": 1, "name": "Ajax" }, { "id": 2, "name": "PSV" } ] }
                """)));

        ApiResponse<List<Team>> response = teams(wm.getHttpBaseUrl()).get();

        assertThat(response.data()).extracting(Team::name).containsExactly("Ajax", "PSV");
    }

    @Test
    void streamFollowsPaginationAcrossPages(WireMockRuntimeInfo wm) {
        stubFor(get(urlPathEqualTo("/teams")).withQueryParam("page", equalTo("1")).willReturn(okJson("""
                { "data": [ { "id": 1, "name": "Ajax" } ],
                  "pagination": { "count": 1, "per_page": 1, "current_page": 1, "next_page": "n", "has_more": true } }
                """)));
        stubFor(get(urlPathEqualTo("/teams")).withQueryParam("page", equalTo("2")).willReturn(okJson("""
                { "data": [ { "id": 2, "name": "PSV" } ],
                  "pagination": { "count": 1, "per_page": 1, "current_page": 2, "next_page": null, "has_more": false } }
                """)));

        List<String> names = teams(wm.getHttpBaseUrl()).stream().map(Team::name).toList();

        assertThat(names).containsExactly("Ajax", "PSV");
    }

    @Test
    void streamDoesNotLeakPageIntoLaterGetOnSameRequest(WireMockRuntimeInfo wm) {
        // Two pages for the stream
        stubFor(get(urlPathEqualTo("/teams")).withQueryParam("page", equalTo("1")).willReturn(okJson("""
                { "data": [ { "id": 1, "name": "Ajax" } ],
                  "pagination": { "count": 1, "per_page": 1, "current_page": 1, "next_page": "n", "has_more": true } }
                """)));
        stubFor(get(urlPathEqualTo("/teams")).withQueryParam("page", equalTo("2")).willReturn(okJson("""
                { "data": [ { "id": 2, "name": "PSV" } ],
                  "pagination": { "count": 1, "per_page": 1, "current_page": 2, "next_page": null, "has_more": false } }
                """)));
        // A plain /teams call with NO page param
        stubFor(get(urlEqualTo("/teams")).willReturn(okJson("""
                { "data": [ { "id": 9, "name": "Feyenoord" } ] }
                """)));

        CollectionRequest<Team> request = teams(wm.getHttpBaseUrl());
        request.stream().toList();          // consumes pages 1 and 2, must not mutate the request
        request.get();                       // should hit /teams with NO page param

        verify(1, getRequestedFor(urlEqualTo("/teams")));          // get() hit /teams with NO page param exactly once
        verify(1, getRequestedFor(urlEqualTo("/teams?page=2"))); // stream() page=2 happened exactly once (not leaked by get())
    }
}
