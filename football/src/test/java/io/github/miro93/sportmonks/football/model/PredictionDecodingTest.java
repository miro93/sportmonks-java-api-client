package io.github.miro93.sportmonks.football.model;

import io.github.miro93.sportmonks.core.json.HelidonJsonCodec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PredictionDecodingTest {

    private final HelidonJsonCodec codec = new HelidonJsonCodec();

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
    void decodesPredictionsWithWholeNumberValues() {
        // Regression coverage for the Helidon 4.5.4 map-value bug FreeFormJson works around
        // (see its javadoc): a bare (no decimal point) integer literal used to throw or come
        // back as Double instead of Integer.
        String json = """
                { "data": { "id": 102, "predictions": { "home": 5, "away": 7 } } }
                """;

        Prediction prediction = codec.decode(json, codec.type(Prediction.class)).data();

        assertThat(prediction.predictions())
                .containsEntry("home", 5)
                .containsEntry("away", 7);
    }

    @Test
    void decodesPredictionsWithMixedIntAndDecimalValues() {
        String json = """
                { "data": { "id": 103, "predictions": { "home": 5, "away": 0.5 } } }
                """;

        Prediction prediction = codec.decode(json, codec.type(Prediction.class)).data();

        assertThat(prediction.predictions())
                .containsEntry("home", 5)
                .containsEntry("away", 0.5);
    }

    @Test
    void decodesPredictionsWithSingleBareIntValue() {
        String json = """
                { "data": { "id": 104, "predictions": { "home": 5 } } }
                """;

        Prediction prediction = codec.decode(json, codec.type(Prediction.class)).data();

        assertThat(prediction.predictions()).containsEntry("home", 5);
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
