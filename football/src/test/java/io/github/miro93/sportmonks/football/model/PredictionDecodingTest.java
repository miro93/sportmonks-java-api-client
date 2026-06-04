package io.github.miro93.sportmonks.football.model;

import io.github.miro93.sportmonks.core.json.JacksonCodec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PredictionDecodingTest {

    private final JacksonCodec codec = new JacksonCodec();

    @Test
    void decodesYesNoProbability() {
        String json = """
                {
                  "data": {
                    "id": 100,
                    "fixture_id": 18533878,
                    "type_id": 231,
                    "predictions": { "yes": 0.67, "no": 0.33 }
                  }
                }
                """;

        Prediction prediction = codec.decode(json, codec.type(Prediction.class)).data();

        assertThat(prediction.id()).isEqualTo(100L);
        assertThat(prediction.fixtureId()).isEqualTo(18533878L);
        assertThat(prediction.typeId()).isEqualTo(231L);
        assertThat(prediction.predictions())
                .containsEntry("yes", 0.67)
                .containsEntry("no", 0.33);
    }

    @Test
    void decodesHomeDrawAwayProbability() {
        String json = """
                {
                  "data": {
                    "id": 101,
                    "fixture_id": 18533878,
                    "type_id": 237,
                    "predictions": { "home": 0.5, "draw": 0.3, "away": 0.2 }
                  }
                }
                """;

        Prediction prediction = codec.decode(json, codec.type(Prediction.class)).data();

        assertThat(prediction.predictions())
                .containsEntry("home", 0.5)
                .containsEntry("draw", 0.3)
                .containsEntry("away", 0.2);
    }

    @Test
    void decodesPredictionWithOptionalFieldsAbsent() {
        String json = """
                { "data": { "id": 100 } }
                """;

        Prediction prediction = codec.decode(json, codec.type(Prediction.class)).data();

        assertThat(prediction.id()).isEqualTo(100L);
        assertThat(prediction.fixtureId()).isNull();
        assertThat(prediction.typeId()).isNull();
        assertThat(prediction.predictions()).isNull();
    }
}
