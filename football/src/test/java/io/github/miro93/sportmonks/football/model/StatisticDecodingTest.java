package io.github.miro93.sportmonks.football.model;

import io.github.miro93.sportmonks.core.json.JacksonCodec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StatisticDecodingTest {

    private final JacksonCodec codec = new JacksonCodec();

    @Test
    void decodesStatisticWithDetails() {
        String json = """
                {
                  "data": {
                    "id": 5001,
                    "team_id": 1,
                    "season_id": 19735,
                    "has_values": true,
                    "position_id": 1,
                    "jersey_number": 4,
                    "details": [
                      { "id": 9001, "type_id": 52, "value": { "total": 12 } },
                      { "id": 9002, "type_id": 80, "value": { "home": 5, "away": 7 } }
                    ]
                  }
                }
                """;

        Statistic statistic = codec.decode(json, codec.type(Statistic.class)).data();

        assertThat(statistic.id()).isEqualTo(5001L);
        assertThat(statistic.teamId()).isEqualTo(1L);
        assertThat(statistic.seasonId()).isEqualTo(19735L);
        assertThat(statistic.hasValues()).isTrue();
        assertThat(statistic.positionId()).isEqualTo(1L);
        assertThat(statistic.jerseyNumber()).isEqualTo(4);
        assertThat(statistic.details()).hasSize(2);
        assertThat(statistic.details().getFirst().id()).isEqualTo(9001L);
        assertThat(statistic.details().getFirst().typeId()).isEqualTo(52L);
        assertThat(statistic.details().getFirst().value()).containsEntry("total", 12);
        assertThat(statistic.details().get(1).value())
                .containsEntry("home", 5)
                .containsEntry("away", 7);
    }

    @Test
    void decodesStatisticWithOptionalFieldsAbsent() {
        String json = """
                { "data": { "id": 5001 } }
                """;

        Statistic statistic = codec.decode(json, codec.type(Statistic.class)).data();

        assertThat(statistic.id()).isEqualTo(5001L);
        assertThat(statistic.playerId()).isNull();
        assertThat(statistic.coachId()).isNull();
        assertThat(statistic.teamId()).isNull();
        assertThat(statistic.refereeId()).isNull();
        assertThat(statistic.seasonId()).isNull();
        assertThat(statistic.stageId()).isNull();
        assertThat(statistic.roundId()).isNull();
        assertThat(statistic.hasValues()).isNull();
        assertThat(statistic.positionId()).isNull();
        assertThat(statistic.jerseyNumber()).isNull();
        assertThat(statistic.details()).isNull();
    }
}
