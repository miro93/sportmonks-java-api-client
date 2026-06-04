package io.github.miro93.sportmonks.football.model;

import io.github.miro93.sportmonks.core.json.JacksonCodec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValueBetDecodingTest {

    private final JacksonCodec codec = new JacksonCodec();

    @Test
    void decodesValueBetWithTypedPredictions() {
        String json = """
                {
                  "data": {
                    "id": 200,
                    "fixture_id": 18533878,
                    "type_id": 1685,
                    "predictions": {
                      "bet": "Home",
                      "bookmaker": "bet365",
                      "odd": "2.10",
                      "is_value": true,
                      "stake": "1.0",
                      "fair_odd": "1.95"
                    }
                  }
                }
                """;

        ValueBet valueBet = codec.decode(json, codec.type(ValueBet.class)).data();

        assertThat(valueBet.id()).isEqualTo(200L);
        assertThat(valueBet.fixtureId()).isEqualTo(18533878L);
        assertThat(valueBet.typeId()).isEqualTo(1685L);
        assertThat(valueBet.predictions()).isNotNull();
        assertThat(valueBet.predictions().bet()).isEqualTo("Home");
        assertThat(valueBet.predictions().bookmaker()).isEqualTo("bet365");
        assertThat(valueBet.predictions().odd()).isEqualTo("2.10");
        assertThat(valueBet.predictions().isValue()).isTrue();
        assertThat(valueBet.predictions().stake()).isEqualTo("1.0");
        assertThat(valueBet.predictions().fairOdd()).isEqualTo("1.95");
    }

    @Test
    void decodesValueBetWithOptionalFieldsAbsent() {
        String json = """
                { "data": { "id": 200 } }
                """;

        ValueBet valueBet = codec.decode(json, codec.type(ValueBet.class)).data();

        assertThat(valueBet.id()).isEqualTo(200L);
        assertThat(valueBet.fixtureId()).isNull();
        assertThat(valueBet.typeId()).isNull();
        assertThat(valueBet.predictions()).isNull();
    }

    @Test
    void decodesValueBetPredictionWithAbsentInnerFields() {
        String json = """
                { "data": { "id": 200, "predictions": { "bet": "Away" } } }
                """;

        ValueBet valueBet = codec.decode(json, codec.type(ValueBet.class)).data();

        assertThat(valueBet.predictions().bet()).isEqualTo("Away");
        assertThat(valueBet.predictions().bookmaker()).isNull();
        assertThat(valueBet.predictions().odd()).isNull();
        assertThat(valueBet.predictions().isValue()).isNull();
        assertThat(valueBet.predictions().stake()).isNull();
        assertThat(valueBet.predictions().fairOdd()).isNull();
    }
}
