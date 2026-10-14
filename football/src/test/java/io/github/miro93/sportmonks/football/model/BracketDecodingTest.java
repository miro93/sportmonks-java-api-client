package io.github.miro93.sportmonks.football.model;

import io.github.miro93.sportmonks.core.json.HelidonJsonCodec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BracketDecodingTest {

    private final HelidonJsonCodec codec = new HelidonJsonCodec();

    @Test
    void decodesStagesFixturesAndEdges() {
        String json = """
                { "data": {
                    "stages": [ { "stage_id": 77479086, "stage_name": "16th Finals",
                                  "fixtures": [ { "id": 19606960, "sport_id": 1, "league_id": 732, "season_id": 26618,
                                                  "stage_id": 77479086, "state_id": 1, "venue_id": 343444,
                                                  "name": "2nd position Group A vs 2nd position Group B",
                                                  "starting_at": "2026-06-28 19:00:00", "result_info": null, "leg": "1/1",
                                                  "details": "Match 73", "length": 90, "placeholder": true,
                                                  "starting_at_timestamp": 1782673200 } ] } ],
                    "edges": [ { "id": 9, "season_id": 26618, "child_fixture_id": 19606975, "child_slot": "home",
                                 "parent_fixture_id": 19606974, "parent_outcome": "winner" } ] } }
                """;

        Bracket bracket = codec.decode(json, codec.type(Bracket.class)).data();

        assertThat(bracket.stages()).hasSize(1);
        BracketStage stage = bracket.stages().getFirst();
        assertThat(stage.stageId()).isEqualTo(77479086L);
        assertThat(stage.stageName()).isEqualTo("16th Finals");
        Fixture fixture = stage.fixtures().getFirst();
        assertThat(fixture.id()).isEqualTo(19606960L);
        assertThat(fixture.placeholder()).isTrue();
        assertThat(fixture.details()).isEqualTo("Match 73");

        assertThat(bracket.edges()).hasSize(1);
        BracketEdge edge = bracket.edges().getFirst();
        assertThat(edge.id()).isEqualTo(9L);
        assertThat(edge.childFixtureId()).isEqualTo(19606975L);
        assertThat(edge.childSlot()).isEqualTo("home");
        assertThat(edge.parentFixtureId()).isEqualTo(19606974L);
        assertThat(edge.parentOutcome()).isEqualTo("winner");
    }
}
