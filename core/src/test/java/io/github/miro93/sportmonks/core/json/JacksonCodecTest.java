package io.github.miro93.sportmonks.core.json;

import com.fasterxml.jackson.databind.JavaType;
import io.github.miro93.sportmonks.core.response.ApiResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JacksonCodecTest {

    record Team(long id, String name) {
    }

    private final JacksonCodec codec = new JacksonCodec();

    @Test
    void decodesSingleResourceWithEnvelope() {
        String json = """
                {
                  "data": { "id": 1, "name": "Ajax" },
                  "rate_limit": { "resets_in_seconds": 3600, "remaining": 2999, "requested_entity": "Team" },
                  "timezone": "UTC"
                }
                """;

        ApiResponse<Team> response = codec.decode(json, codec.type(Team.class));

        assertThat(response.data().name()).isEqualTo("Ajax");
        assertThat(response.rateLimit().remaining()).isEqualTo(2999);
        assertThat(response.rateLimit().resetsInSeconds()).isEqualTo(3600);
        assertThat(response.timezone()).isEqualTo("UTC");
    }

    @Test
    void decodesCollectionWithPagination() {
        String json = """
                {
                  "data": [ { "id": 1, "name": "Ajax" }, { "id": 2, "name": "PSV" } ],
                  "pagination": { "count": 2, "per_page": 25, "current_page": 1,
                                  "next_page": "https://api/next", "has_more": true }
                }
                """;

        ApiResponse<List<Team>> response = codec.decode(json, codec.listType(Team.class));

        assertThat(response.data()).hasSize(2);
        assertThat(response.pagination().hasMore()).isTrue();
        assertThat(response.pagination().currentPage()).isEqualTo(1);
    }

    @Test
    void ignoresUnknownFields() {
        String json = """
                { "data": { "id": 1, "name": "Ajax", "founded": 1900 }, "extra": "ignored" }
                """;

        ApiResponse<Team> response = codec.decode(json, codec.type(Team.class));

        assertThat(response.data().id()).isEqualTo(1);
    }

    @Test
    void throwsCodecExceptionOnMalformedJson() {
        assertThatThrownBy(() -> codec.decode("{ not json", codec.type(Team.class)))
                .isInstanceOf(CodecException.class);
    }
}
