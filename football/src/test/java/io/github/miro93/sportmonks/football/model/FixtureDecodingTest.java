package io.github.miro93.sportmonks.football.model;

import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.response.ApiResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FixtureDecodingTest {

    private final JacksonCodec codec = new JacksonCodec();

    @Test
    void decodesFixtureWithNestedIncludes() {
        String json = """
                {
                  "data": {
                    "id": 18535517,
                    "sport_id": 1,
                    "league_id": 501,
                    "season_id": 19735,
                    "stage_id": 77457866,
                    "round_id": 274719,
                    "state_id": 5,
                    "venue_id": 8909,
                    "name": "Celtic vs Rangers",
                    "starting_at": "2024-09-01 11:00:00",
                    "result_info": "Celtic won after full-time.",
                    "leg": "1/1",
                    "length": 90,
                    "placeholder": false,
                    "has_odds": true,
                    "starting_at_timestamp": 1725188400,
                    "participants": [
                      { "id": 53, "name": "Celtic", "short_code": "CEL", "meta": { "location": "home", "winner": true, "position": 1 } },
                      { "id": 62, "name": "Rangers", "short_code": "RAN", "meta": { "location": "away", "winner": false, "position": 2 } }
                    ],
                    "scores": [
                      { "id": 1, "fixture_id": 18535517, "type_id": 1525, "participant_id": 53,
                        "score": { "goals": 3, "participant": "home" }, "description": "CURRENT" }
                    ],
                    "state": { "id": 5, "state": "FT", "name": "Full-Time", "short_name": "FT", "developer_name": "FT" },
                    "events": [
                      { "id": 99, "fixture_id": 18535517, "type_id": 14, "participant_id": 53,
                        "player_id": 1001, "player_name": "Kyogo", "minute": 23, "result": "1-0" }
                    ]
                  }
                }
                """;

        ApiResponse<Fixture> response = codec.decode(json, codec.type(Fixture.class));
        Fixture fixture = response.data();

        assertThat(fixture.id()).isEqualTo(18535517L);
        assertThat(fixture.name()).isEqualTo("Celtic vs Rangers");
        assertThat(fixture.leagueId()).isEqualTo(501L);
        assertThat(fixture.startingAtTimestamp()).isEqualTo(1725188400L);
        assertThat(fixture.placeholder()).isFalse();
        assertThat(fixture.hasOdds()).isTrue();

        assertThat(fixture.participants()).hasSize(2);
        Participant home = fixture.participants().getFirst();
        assertThat(home.name()).isEqualTo("Celtic");
        assertThat(home.meta().location()).isEqualTo("home");
        assertThat(home.meta().winner()).isTrue();

        assertThat(fixture.scores()).hasSize(1);
        assertThat(fixture.scores().getFirst().score().goals()).isEqualTo(3);
        assertThat(fixture.scores().getFirst().score().participant()).isEqualTo("home");

        assertThat(fixture.state().developerName()).isEqualTo("FT");

        assertThat(fixture.events()).hasSize(1);
        assertThat(fixture.events().getFirst().playerName()).isEqualTo("Kyogo");
        assertThat(fixture.events().getFirst().minute()).isEqualTo(23);
    }

    @Test
    void decodesFixtureWithoutIncludes() {
        String json = """
                { "data": { "id": 1, "name": "A vs B", "placeholder": false, "has_odds": false } }
                """;

        Fixture fixture = codec.decode(json, codec.type(Fixture.class)).data();

        assertThat(fixture.id()).isEqualTo(1L);
        assertThat(fixture.participants()).isNull();
        assertThat(fixture.scores()).isNull();
        assertThat(fixture.state()).isNull();
        assertThat(fixture.events()).isNull();
    }
}
