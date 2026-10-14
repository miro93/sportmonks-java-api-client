package io.github.miro93.sportmonks.football.model;

import io.github.miro93.sportmonks.core.json.HelidonJsonCodec;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RivalDecodingTest {

    private final HelidonJsonCodec codec = new HelidonJsonCodec();

    @Test
    void decodesRivalWithTeamIncludes() {
        String json = """
                { "data": [ { "id": 1, "sport_id": 1, "team_id": 53, "rival_id": 62,
                              "team": { "id": 53, "name": "Celtic" }, "rival": { "id": 62, "name": "Rangers" } } ] }
                """;

        List<Rival> rivals = codec.decode(json, codec.listType(Rival.class)).data();

        Rival rival = rivals.getFirst();
        assertThat(rival.teamId()).isEqualTo(53L);
        assertThat(rival.rivalId()).isEqualTo(62L);
        assertThat(rival.team().name()).isEqualTo("Celtic");
        assertThat(rival.rival().name()).isEqualTo("Rangers");
    }

    @Test
    void decodesRivalWithoutIncludes() {
        List<Rival> rivals = codec.decode("""
                { "data": [ { "sport_id": 1, "team_id": 53, "rival_id": 62 } ] }
                """, codec.listType(Rival.class)).data();

        assertThat(rivals.getFirst().id()).isNull();
        assertThat(rivals.getFirst().team()).isNull();
    }
}
