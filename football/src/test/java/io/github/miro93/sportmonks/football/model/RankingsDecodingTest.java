package io.github.miro93.sportmonks.football.model;

import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.response.ApiResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RankingsDecodingTest {

    private final JacksonCodec codec = new JacksonCodec();

    // -------------------------------------------------------------------------
    // 1. Standing with all scalars, participant Team, and details list
    // -------------------------------------------------------------------------

    @Test
    void decodesStandingWithParticipantAndDetails() {
        String json = """
                {
                  "data": {
                    "id": 70001,
                    "participant_id": 53,
                    "sport_id": 1,
                    "league_id": 501,
                    "season_id": 19734,
                    "stage_id": 77447,
                    "group_id": null,
                    "round_id": null,
                    "standing_rule_id": 205,
                    "position": 1,
                    "result": "equal",
                    "points": 87,
                    "participant": {
                      "id": 53,
                      "sport_id": 1,
                      "country_id": 1161,
                      "venue_id": 8909,
                      "gender": "male",
                      "name": "Celtic",
                      "short_code": "CEL",
                      "image_path": "https://cdn.sportmonks.com/images/soccer/teams/53.png",
                      "founded": 1888,
                      "type": "domestic",
                      "placeholder": false,
                      "last_played_at": "2024-05-19 15:00:00"
                    },
                    "details": [
                      {
                        "id": 900001,
                        "standing_type": "normal",
                        "standing_id": 70001,
                        "type_id": 129,
                        "value": 38
                      },
                      {
                        "id": 900002,
                        "standing_type": "normal",
                        "standing_id": 70001,
                        "type_id": 130,
                        "value": 27
                      }
                    ]
                  }
                }
                """;

        ApiResponse<Standing> response = codec.decode(json, codec.type(Standing.class));
        Standing standing = response.data();

        assertThat(standing.id()).isEqualTo(70001L);
        assertThat(standing.participantId()).isEqualTo(53L);
        assertThat(standing.sportId()).isEqualTo(1L);
        assertThat(standing.leagueId()).isEqualTo(501L);
        assertThat(standing.seasonId()).isEqualTo(19734L);
        assertThat(standing.stageId()).isEqualTo(77447L);
        assertThat(standing.groupId()).isNull();
        assertThat(standing.roundId()).isNull();
        assertThat(standing.standingRuleId()).isEqualTo(205L);
        assertThat(standing.position()).isEqualTo(1);
        assertThat(standing.result()).isEqualTo("equal");
        assertThat(standing.points()).isEqualTo(87);

        Team team = standing.participant();
        assertThat(team).isNotNull();
        assertThat(team.id()).isEqualTo(53L);
        assertThat(team.name()).isEqualTo("Celtic");

        List<StandingDetail> details = standing.details();
        assertThat(details).hasSize(2);

        StandingDetail first = details.get(0);
        assertThat(first.id()).isEqualTo(900001L);
        assertThat(first.standingType()).isEqualTo("normal");
        assertThat(first.standingId()).isEqualTo(70001L);
        assertThat(first.typeId()).isEqualTo(129L);
        assertThat(first.value()).isEqualTo(38);

        StandingDetail second = details.get(1);
        assertThat(second.id()).isEqualTo(900002L);
        assertThat(second.standingType()).isEqualTo("normal");
        assertThat(second.standingId()).isEqualTo(70001L);
        assertThat(second.typeId()).isEqualTo(130L);
        assertThat(second.value()).isEqualTo(27);
    }

    // -------------------------------------------------------------------------
    // 2. Standing with optional fields absent (group_id, round_id, result,
    //    participant, details all null)
    // -------------------------------------------------------------------------

    @Test
    void decodesStandingWithNullableFieldsAbsent() {
        String json = """
                {
                  "data": {
                    "id": 70002,
                    "participant_id": 62,
                    "sport_id": 1,
                    "league_id": 501,
                    "season_id": 19734,
                    "stage_id": 77447,
                    "standing_rule_id": 205,
                    "position": 2,
                    "points": 80
                  }
                }
                """;

        Standing standing = codec.decode(json, codec.type(Standing.class)).data();

        assertThat(standing.id()).isEqualTo(70002L);
        assertThat(standing.position()).isEqualTo(2);
        assertThat(standing.points()).isEqualTo(80);
        assertThat(standing.groupId()).isNull();
        assertThat(standing.roundId()).isNull();
        assertThat(standing.result()).isNull();
        assertThat(standing.participant()).isNull();
        assertThat(standing.details()).isNull();
    }

    // -------------------------------------------------------------------------
    // 3. Topscorer with player and participant included
    // -------------------------------------------------------------------------

    @Test
    void decodesTopscorersWithRelationsIncluded() {
        String json = """
                {
                  "data": [
                    {
                      "id": 80001,
                      "season_id": 19734,
                      "stage_id": 77447,
                      "player_id": 200001,
                      "participant_id": 53,
                      "type_id": 208,
                      "position": 1,
                      "total": 27,
                      "player": {
                        "id": 200001,
                        "sport_id": 1,
                        "country_id": 320,
                        "nationality_id": 320,
                        "city_id": "Osaka",
                        "position_id": 27,
                        "detailed_position_id": 153,
                        "type_id": 220,
                        "common_name": "Kyogo",
                        "firstname": "Furuhashi",
                        "lastname": "Kyogo",
                        "name": "Kyogo Furuhashi",
                        "display_name": "Kyogo",
                        "image_path": "https://cdn.sportmonks.com/images/soccer/players/200001.png",
                        "height": 173,
                        "weight": 67,
                        "date_of_birth": "1995-01-20",
                        "gender": "male"
                      },
                      "participant": {
                        "id": 53,
                        "sport_id": 1,
                        "country_id": 1161,
                        "venue_id": 8909,
                        "gender": "male",
                        "name": "Celtic",
                        "short_code": "CEL",
                        "image_path": "https://cdn.sportmonks.com/images/soccer/teams/53.png",
                        "founded": 1888,
                        "type": "domestic",
                        "placeholder": false,
                        "last_played_at": "2024-05-19 15:00:00"
                      }
                    }
                  ]
                }
                """;

        ApiResponse<List<Topscorer>> response = codec.decode(json, codec.listType(Topscorer.class));
        List<Topscorer> scorers = response.data();

        assertThat(scorers).hasSize(1);
        Topscorer scorer = scorers.get(0);

        assertThat(scorer.id()).isEqualTo(80001L);
        assertThat(scorer.seasonId()).isEqualTo(19734L);
        assertThat(scorer.stageId()).isEqualTo(77447L);
        assertThat(scorer.playerId()).isEqualTo(200001L);
        assertThat(scorer.participantId()).isEqualTo(53L);
        assertThat(scorer.typeId()).isEqualTo(208L);
        assertThat(scorer.position()).isEqualTo(1);
        assertThat(scorer.total()).isEqualTo(27);

        Player player = scorer.player();
        assertThat(player).isNotNull();
        assertThat(player.id()).isEqualTo(200001L);
        assertThat(player.commonName()).isEqualTo("Kyogo");
        assertThat(player.name()).isEqualTo("Kyogo Furuhashi");

        Team team = scorer.participant();
        assertThat(team).isNotNull();
        assertThat(team.id()).isEqualTo(53L);
        assertThat(team.name()).isEqualTo("Celtic");
    }

    // -------------------------------------------------------------------------
    // 4. Topscorer with player and participant absent
    // -------------------------------------------------------------------------

    @Test
    void decodesTopscorersWithoutRelations() {
        String json = """
                {
                  "data": {
                    "id": 80002,
                    "season_id": 19734,
                    "stage_id": 77447,
                    "player_id": 200002,
                    "participant_id": 62,
                    "type_id": 208,
                    "position": 2,
                    "total": 19
                  }
                }
                """;

        Topscorer scorer = codec.decode(json, codec.type(Topscorer.class)).data();

        assertThat(scorer.id()).isEqualTo(80002L);
        assertThat(scorer.seasonId()).isEqualTo(19734L);
        assertThat(scorer.stageId()).isEqualTo(77447L);
        assertThat(scorer.playerId()).isEqualTo(200002L);
        assertThat(scorer.participantId()).isEqualTo(62L);
        assertThat(scorer.typeId()).isEqualTo(208L);
        assertThat(scorer.position()).isEqualTo(2);
        assertThat(scorer.total()).isEqualTo(19);
        assertThat(scorer.player()).isNull();
        assertThat(scorer.participant()).isNull();
    }
}
