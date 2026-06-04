package io.github.miro93.sportmonks.football.model;

import io.github.miro93.sportmonks.core.json.JacksonCodec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExpectedLineupDecodingTest {

    private final JacksonCodec codec = new JacksonCodec();

    @Test
    void decodesExpectedLineupWithAllScalars() {
        String json = """
                {
                  "data": {
                    "id": 1,
                    "sport_id": 1,
                    "fixture_id": 18533878,
                    "player_id": 172,
                    "team_id": 1,
                    "type_id": 11,
                    "player_name": "Virgil van Dijk",
                    "jersey_number": 4,
                    "position_id": 148,
                    "detailed_position_id": 153,
                    "formation_field": "1",
                    "formation_position": "4"
                  }
                }
                """;

        ExpectedLineup lineup = codec.decode(json, codec.type(ExpectedLineup.class)).data();

        assertThat(lineup.id()).isEqualTo(1L);
        assertThat(lineup.sportId()).isEqualTo(1L);
        assertThat(lineup.fixtureId()).isEqualTo(18533878L);
        assertThat(lineup.playerId()).isEqualTo(172L);
        assertThat(lineup.teamId()).isEqualTo(1L);
        assertThat(lineup.typeId()).isEqualTo(11L);
        assertThat(lineup.playerName()).isEqualTo("Virgil van Dijk");
        assertThat(lineup.jerseyNumber()).isEqualTo(4);
        assertThat(lineup.positionId()).isEqualTo(148L);
        assertThat(lineup.detailedPositionId()).isEqualTo(153L);
        assertThat(lineup.formationField()).isEqualTo("1");
        assertThat(lineup.formationPosition()).isEqualTo("4");
    }

    @Test
    void decodesExpectedLineupWithOptionalFieldsAbsent() {
        String json = """
                { "data": { "id": 1 } }
                """;

        ExpectedLineup lineup = codec.decode(json, codec.type(ExpectedLineup.class)).data();

        assertThat(lineup.id()).isEqualTo(1L);
        assertThat(lineup.sportId()).isNull();
        assertThat(lineup.fixtureId()).isNull();
        assertThat(lineup.playerId()).isNull();
        assertThat(lineup.teamId()).isNull();
        assertThat(lineup.typeId()).isNull();
        assertThat(lineup.playerName()).isNull();
        assertThat(lineup.jerseyNumber()).isNull();
        assertThat(lineup.positionId()).isNull();
        assertThat(lineup.detailedPositionId()).isNull();
        assertThat(lineup.formationField()).isNull();
        assertThat(lineup.formationPosition()).isNull();
    }
}
