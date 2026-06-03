package io.github.miro93.sportmonks.football.model;

import io.github.miro93.sportmonks.core.json.JacksonCodec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BookmakerMarketDecodingTest {

    private final JacksonCodec codec = new JacksonCodec();

    @Test
    void decodesBookmaker() {
        String json = """
                { "data": { "id": 34, "legacy_id": 2, "name": "bet365" } }
                """;

        Bookmaker bookmaker = codec.decode(json, codec.type(Bookmaker.class)).data();

        assertThat(bookmaker.id()).isEqualTo(34L);
        assertThat(bookmaker.legacyId()).isEqualTo(2L);
        assertThat(bookmaker.name()).isEqualTo("bet365");
    }

    @Test
    void decodesBookmakerWithOptionalFieldsAbsent() {
        String json = """
                { "data": { "id": 34 } }
                """;

        Bookmaker bookmaker = codec.decode(json, codec.type(Bookmaker.class)).data();

        assertThat(bookmaker.id()).isEqualTo(34L);
        assertThat(bookmaker.legacyId()).isNull();
        assertThat(bookmaker.name()).isNull();
    }

    @Test
    void decodesMarketWithBooleanFlag() {
        String json = """
                {
                  "data": {
                    "id": 1,
                    "legacy_id": 1,
                    "name": "Fulltime Result",
                    "developer_name": "FULLTIME_RESULT",
                    "has_winning_calculations": true
                  }
                }
                """;

        Market market = codec.decode(json, codec.type(Market.class)).data();

        assertThat(market.id()).isEqualTo(1L);
        assertThat(market.legacyId()).isEqualTo(1L);
        assertThat(market.name()).isEqualTo("Fulltime Result");
        assertThat(market.developerName()).isEqualTo("FULLTIME_RESULT");
        assertThat(market.hasWinningCalculations()).isTrue();
    }

    @Test
    void decodesMarketWithOptionalFieldsAbsent() {
        String json = """
                { "data": { "id": 1, "name": "Fulltime Result" } }
                """;

        Market market = codec.decode(json, codec.type(Market.class)).data();

        assertThat(market.id()).isEqualTo(1L);
        assertThat(market.name()).isEqualTo("Fulltime Result");
        assertThat(market.legacyId()).isNull();
        assertThat(market.developerName()).isNull();
        assertThat(market.hasWinningCalculations()).isNull();
    }
}
