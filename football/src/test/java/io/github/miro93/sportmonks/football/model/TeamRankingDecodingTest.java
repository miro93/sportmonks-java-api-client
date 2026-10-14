package io.github.miro93.sportmonks.football.model;

    import io.github.miro93.sportmonks.core.json.HelidonJsonCodec;
    import org.junit.jupiter.api.Test;

    import java.util.List;

    import static org.assertj.core.api.Assertions.assertThat;

    class TeamRankingDecodingTest {

        private final HelidonJsonCodec codec = new HelidonJsonCodec();

    @Test
    void decodesRankingWithTeamInclude() {
        String json = """
                { "data": [ { "id": 17694711, "team_id": 2963, "date": "2005-07-02", "current_rank": 2, "scaled_score": 94.11,
                              "team": { "id": 2963, "name": "Real Madrid" } },
                            { "id": 17694710, "team_id": 14754, "date": "2005-07-02", "current_rank": 1, "scaled_score": 100 } ] }
                """;

        List<TeamRanking> rankings = codec.decode(json, codec.listType(TeamRanking.class)).data();

        assertThat(rankings.getFirst().currentRank()).isEqualTo(2);
        assertThat(rankings.getFirst().scaledScore()).isEqualTo(94.11);
        assertThat(rankings.getFirst().team().name()).isEqualTo("Real Madrid");
        assertThat(rankings.getLast().scaledScore()).isEqualTo(100.0);
        assertThat(rankings.getLast().team()).isNull();
    }
}
