package io.github.miro93.sportmonks.football.model;

import io.github.miro93.sportmonks.core.json.JacksonCodec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OddDecodingTest {

    private final JacksonCodec codec = new JacksonCodec();

    @Test
    void decodesOddWithAllScalars() {
        String json = """
                {
                  "data": {
                    "id": 1,
                    "fixture_id": 18533878,
                    "market_id": 1,
                    "bookmaker_id": 34,
                    "label": "Home",
                    "value": "1.48",
                    "name": "Home",
                    "sort_order": 0,
                    "market_description": "Match Winner",
                    "probability": "67.57%",
                    "dp3": "1.480",
                    "fractional": "37/25",
                    "american": "-209",
                    "winning": false,
                    "stopped": false,
                    "total": null,
                    "handicap": null,
                    "participants": null,
                    "created_at": "2023-01-11T14:40:25.000000Z",
                    "updated_at": "2023-01-11T14:47:50.000000Z",
                    "original_label": null,
                    "latest_bookmaker_update": "2023-01-11 14:40:25"
                  }
                }
                """;

        Odd odd = codec.decode(json, codec.type(Odd.class)).data();

        assertThat(odd.id()).isEqualTo(1L);
        assertThat(odd.fixtureId()).isEqualTo(18533878L);
        assertThat(odd.marketId()).isEqualTo(1L);
        assertThat(odd.bookmakerId()).isEqualTo(34L);
        assertThat(odd.label()).isEqualTo("Home");
        assertThat(odd.value()).isEqualTo("1.48");
        assertThat(odd.name()).isEqualTo("Home");
        assertThat(odd.sortOrder()).isEqualTo(0);
        assertThat(odd.marketDescription()).isEqualTo("Match Winner");
        assertThat(odd.probability()).isEqualTo("67.57%");
        assertThat(odd.dp3()).isEqualTo("1.480");
        assertThat(odd.fractional()).isEqualTo("37/25");
        assertThat(odd.american()).isEqualTo("-209");
        assertThat(odd.winning()).isFalse();
        assertThat(odd.stopped()).isFalse();
        assertThat(odd.total()).isNull();
        assertThat(odd.handicap()).isNull();
        assertThat(odd.participants()).isNull();
        assertThat(odd.createdAt()).isEqualTo("2023-01-11T14:40:25.000000Z");
        assertThat(odd.updatedAt()).isEqualTo("2023-01-11T14:47:50.000000Z");
        assertThat(odd.originalLabel()).isNull();
        assertThat(odd.latestBookmakerUpdate()).isEqualTo("2023-01-11 14:40:25");
    }

    @Test
    void decodesOddWithOptionalFieldsAbsent() {
        String json = """
                { "data": { "id": 1 } }
                """;

        Odd odd = codec.decode(json, codec.type(Odd.class)).data();

        assertThat(odd.id()).isEqualTo(1L);
        assertThat(odd.fixtureId()).isNull();
        assertThat(odd.marketId()).isNull();
        assertThat(odd.bookmakerId()).isNull();
        assertThat(odd.label()).isNull();
        assertThat(odd.value()).isNull();
        assertThat(odd.name()).isNull();
        assertThat(odd.sortOrder()).isNull();
        assertThat(odd.marketDescription()).isNull();
        assertThat(odd.probability()).isNull();
        assertThat(odd.dp3()).isNull();
        assertThat(odd.fractional()).isNull();
        assertThat(odd.american()).isNull();
        assertThat(odd.winning()).isNull();
        assertThat(odd.stopped()).isNull();
        assertThat(odd.total()).isNull();
        assertThat(odd.handicap()).isNull();
        assertThat(odd.participants()).isNull();
        assertThat(odd.createdAt()).isNull();
        assertThat(odd.updatedAt()).isNull();
        assertThat(odd.originalLabel()).isNull();
        assertThat(odd.latestBookmakerUpdate()).isNull();
    }
}
