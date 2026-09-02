package io.github.miro93.sportmonks.football.model;

import io.github.miro93.sportmonks.core.json.HelidonJsonCodec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PredictabilityDecodingTest {

    private final HelidonJsonCodec codec = new HelidonJsonCodec();

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
    void decodesPredictabilityDataWithWholeNumberValues() {
        // Regression coverage for the Helidon 4.5.4 map-value bug FreeFormJson works around
        // (see its javadoc): a bare (no decimal point) integer literal used to throw or come
        // back as Double instead of Integer.
        String json = """
                { "data": { "id": 301, "data": { "fulltime_result": 1, "both_teams_to_score": 0 } } }
                """;

        Predictability predictability = codec.decode(json, codec.type(Predictability.class)).data();

        assertThat(predictability.data())
                .containsEntry("fulltime_result", 1)
                .containsEntry("both_teams_to_score", 0);
    }

    @Test
    void decodesPredictabilityDataWithMixedIntAndDecimalValues() {
        String json = """
                { "data": { "id": 302, "data": { "fulltime_result": 1, "both_teams_to_score": 0.5 } } }
                """;

        Predictability predictability = codec.decode(json, codec.type(Predictability.class)).data();

        assertThat(predictability.data())
                .containsEntry("fulltime_result", 1)
                .containsEntry("both_teams_to_score", 0.5);
    }

    @Test
    void decodesPredictabilityDataWithSingleBareIntValue() {
        String json = """
                { "data": { "id": 303, "data": { "fulltime_result": 1 } } }
                """;

        Predictability predictability = codec.decode(json, codec.type(Predictability.class)).data();

        assertThat(predictability.data()).containsEntry("fulltime_result", 1);
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
