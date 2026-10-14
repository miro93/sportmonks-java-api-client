package io.github.miro93.sportmonks.football.model;

    import io.github.miro93.sportmonks.core.json.HelidonJsonCodec;
    import org.junit.jupiter.api.Test;

    import java.util.List;

    import static org.assertj.core.api.Assertions.assertThat;

    class TeamOfTheWeekEntryDecodingTest {

        private final HelidonJsonCodec codec = new HelidonJsonCodec();

    @Test
    void decodesEntryWithIncludes() {
        String json = """
                { "data": [ { "id": 1, "player_id": 455900, "fixture_id": 18946393, "round_id": 318161, "team_id": 18644,
                              "rating": "7.45", "formation_position": 2, "formation": "4-3-3",
                              "player": { "id": 455900, "display_name": "J. Doe" }, "team": { "id": 18644, "name": "Club" },
                              "fixture": { "id": 18946393, "name": "A vs B" }, "round": { "id": 318161, "name": "1" } } ] }
                """;

        TeamOfTheWeekEntry entry = codec.decode(json, codec.listType(TeamOfTheWeekEntry.class)).data().getFirst();

        assertThat(entry.rating()).isEqualTo("7.45");
        assertThat(entry.formationPosition()).isEqualTo(2);
        assertThat(entry.formation()).isEqualTo("4-3-3");
        assertThat(entry.player().displayName()).isEqualTo("J. Doe");
        assertThat(entry.team().name()).isEqualTo("Club");
        assertThat(entry.fixture().name()).isEqualTo("A vs B");
        assertThat(entry.round().id()).isEqualTo(318161L);
    }
}
