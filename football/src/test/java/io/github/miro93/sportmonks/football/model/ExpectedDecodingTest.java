package io.github.miro93.sportmonks.football.model;

import io.github.miro93.sportmonks.core.json.JacksonCodec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExpectedDecodingTest {

    private final JacksonCodec codec = new JacksonCodec();

    @Test
    void decodesExpectedWithDataMap() {
        String json = """
                {
                  "data": {
                    "id": 7001,
                    "fixture_id": 18533878,
                    "type_id": 5304,
                    "participant_id": 1,
                    "location": "home",
                    "data": { "value": 1.85 }
                  }
                }
                """;

        Expected expected = codec.decode(json, codec.type(Expected.class)).data();

        assertThat(expected.id()).isEqualTo(7001L);
        assertThat(expected.fixtureId()).isEqualTo(18533878L);
        assertThat(expected.typeId()).isEqualTo(5304L);
        assertThat(expected.participantId()).isEqualTo(1L);
        assertThat(expected.location()).isEqualTo("home");
        assertThat(expected.data()).containsEntry("value", 1.85);
    }

    @Test
    void decodesExpectedWithOptionalFieldsAbsent() {
        String json = """
                { "data": { "id": 7001 } }
                """;

        Expected expected = codec.decode(json, codec.type(Expected.class)).data();

        assertThat(expected.id()).isEqualTo(7001L);
        assertThat(expected.fixtureId()).isNull();
        assertThat(expected.typeId()).isNull();
        assertThat(expected.participantId()).isNull();
        assertThat(expected.location()).isNull();
        assertThat(expected.data()).isNull();
    }
}
