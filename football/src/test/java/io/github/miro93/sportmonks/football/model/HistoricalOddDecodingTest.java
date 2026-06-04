package io.github.miro93.sportmonks.football.model;

import io.github.miro93.sportmonks.core.json.JacksonCodec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HistoricalOddDecodingTest {

    private final JacksonCodec codec = new JacksonCodec();

    @Test
    void decodesHistoricalOddWithAllScalars() {
        String json = """
                {
                  "data": {
                    "id": 100,
                    "odd_id": 1,
                    "value": "1.48",
                    "probability": "67.57%",
                    "dp3": "1.480",
                    "fractional": "37/25",
                    "american": "-209",
                    "bookmaker_update": "2023-01-11 14:40:25"
                  }
                }
                """;

        HistoricalOdd odd = codec.decode(json, codec.type(HistoricalOdd.class)).data();

        assertThat(odd.id()).isEqualTo(100L);
        assertThat(odd.oddId()).isEqualTo(1L);
        assertThat(odd.value()).isEqualTo("1.48");
        assertThat(odd.probability()).isEqualTo("67.57%");
        assertThat(odd.dp3()).isEqualTo("1.480");
        assertThat(odd.fractional()).isEqualTo("37/25");
        assertThat(odd.american()).isEqualTo("-209");
        assertThat(odd.bookmakerUpdate()).isEqualTo("2023-01-11 14:40:25");
    }

    @Test
    void decodesHistoricalOddWithOptionalFieldsAbsent() {
        String json = """
                { "data": { "id": 100 } }
                """;

        HistoricalOdd odd = codec.decode(json, codec.type(HistoricalOdd.class)).data();

        assertThat(odd.id()).isEqualTo(100L);
        assertThat(odd.oddId()).isNull();
        assertThat(odd.value()).isNull();
        assertThat(odd.probability()).isNull();
        assertThat(odd.dp3()).isNull();
        assertThat(odd.fractional()).isNull();
        assertThat(odd.american()).isNull();
        assertThat(odd.bookmakerUpdate()).isNull();
    }
}
