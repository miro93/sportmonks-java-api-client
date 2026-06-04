package io.github.miro93.sportmonks.football.model;

import io.github.miro93.sportmonks.core.json.JacksonCodec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PremiumOddDecodingTest {

    private final JacksonCodec codec = new JacksonCodec();

    @Test
    void decodesPremiumOddWithAllScalars() {
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
                    "market_description": "Match Winner",
                    "probability": "67.57%",
                    "dp3": "1.480",
                    "fractional": "37/25",
                    "american": "-209",
                    "stopped": false,
                    "total": null,
                    "handicap": null,
                    "participants": null,
                    "latest_bookmaker_update": "2023-01-11 14:40:25"
                  }
                }
                """;

        PremiumOdd odd = codec.decode(json, codec.type(PremiumOdd.class)).data();

        assertThat(odd.id()).isEqualTo(1L);
        assertThat(odd.fixtureId()).isEqualTo(18533878L);
        assertThat(odd.marketId()).isEqualTo(1L);
        assertThat(odd.bookmakerId()).isEqualTo(34L);
        assertThat(odd.label()).isEqualTo("Home");
        assertThat(odd.value()).isEqualTo("1.48");
        assertThat(odd.name()).isEqualTo("Home");
        assertThat(odd.marketDescription()).isEqualTo("Match Winner");
        assertThat(odd.probability()).isEqualTo("67.57%");
        assertThat(odd.dp3()).isEqualTo("1.480");
        assertThat(odd.fractional()).isEqualTo("37/25");
        assertThat(odd.american()).isEqualTo("-209");
        assertThat(odd.stopped()).isFalse();
        assertThat(odd.total()).isNull();
        assertThat(odd.handicap()).isNull();
        assertThat(odd.participants()).isNull();
        assertThat(odd.latestBookmakerUpdate()).isEqualTo("2023-01-11 14:40:25");
    }

    @Test
    void decodesPremiumOddWithOptionalFieldsAbsent() {
        String json = """
                { "data": { "id": 1 } }
                """;

        PremiumOdd odd = codec.decode(json, codec.type(PremiumOdd.class)).data();

        assertThat(odd.id()).isEqualTo(1L);
        assertThat(odd.fixtureId()).isNull();
        assertThat(odd.marketId()).isNull();
        assertThat(odd.bookmakerId()).isNull();
        assertThat(odd.label()).isNull();
        assertThat(odd.value()).isNull();
        assertThat(odd.name()).isNull();
        assertThat(odd.marketDescription()).isNull();
        assertThat(odd.probability()).isNull();
        assertThat(odd.dp3()).isNull();
        assertThat(odd.fractional()).isNull();
        assertThat(odd.american()).isNull();
        assertThat(odd.stopped()).isNull();
        assertThat(odd.total()).isNull();
        assertThat(odd.handicap()).isNull();
        assertThat(odd.participants()).isNull();
        assertThat(odd.latestBookmakerUpdate()).isNull();
    }
}
