package io.github.miro93.sportmonks.football.model;

import io.github.miro93.sportmonks.core.json.JacksonCodec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PredictabilityDecodingTest {

    private final JacksonCodec codec = new JacksonCodec();

    @Test
    void decodesPredictabilityWithDataMap() {
        String json = """
                {
                  "data": {
                    "id": 300,
                    "league_id": 8,
                    "type_id": 1683,
                    "data": { "fulltime_result": 0.75, "both_teams_to_score": 0.5 }
                  }
                }
                """;

        Predictability predictability = codec.decode(json, codec.type(Predictability.class)).data();

        assertThat(predictability.id()).isEqualTo(300L);
        assertThat(predictability.leagueId()).isEqualTo(8L);
        assertThat(predictability.typeId()).isEqualTo(1683L);
        assertThat(predictability.data())
                .containsEntry("fulltime_result", 0.75)
                .containsEntry("both_teams_to_score", 0.5);
    }

    @Test
    void decodesPredictabilityWithOptionalFieldsAbsent() {
        String json = """
                { "data": { "id": 300 } }
                """;

        Predictability predictability = codec.decode(json, codec.type(Predictability.class)).data();

        assertThat(predictability.id()).isEqualTo(300L);
        assertThat(predictability.leagueId()).isNull();
        assertThat(predictability.typeId()).isNull();
        assertThat(predictability.data()).isNull();
    }
}
