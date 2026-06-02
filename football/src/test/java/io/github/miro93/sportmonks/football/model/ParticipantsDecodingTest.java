package io.github.miro93.sportmonks.football.model;

import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.response.ApiResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ParticipantsDecodingTest {

    private final JacksonCodec codec = new JacksonCodec();

    // -------------------------------------------------------------------------
    // 1. Single Team with squad included, each squad member has player included
    // -------------------------------------------------------------------------

    @Test
    void decodesTeamWithSquadAndPlayerIncluded() {
        String json = """
                {
                  "data": {
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
                    "last_played_at": "2024-05-19 15:00:00",
                    "squad": [
                      {
                        "id": 10001,
                        "transfer_id": null,
                        "player_id": 200001,
                        "team_id": 53,
                        "position_id": 27,
                        "detailed_position_id": 153,
                        "jersey_number": 9,
                        "start": "2023-07-01",
                        "end": null,
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
                        }
                      }
                    ]
                  }
                }
                """;

        ApiResponse<Team> response = codec.decode(json, codec.type(Team.class));
        Team team = response.data();

        assertThat(team.id()).isEqualTo(53L);
        assertThat(team.sportId()).isEqualTo(1L);
        assertThat(team.countryId()).isEqualTo(1161L);
        assertThat(team.venueId()).isEqualTo(8909L);
        assertThat(team.gender()).isEqualTo("male");
        assertThat(team.name()).isEqualTo("Celtic");
        assertThat(team.shortCode()).isEqualTo("CEL");
        assertThat(team.imagePath()).isEqualTo("https://cdn.sportmonks.com/images/soccer/teams/53.png");
        assertThat(team.founded()).isEqualTo(1888);
        assertThat(team.type()).isEqualTo("domestic");
        assertThat(team.placeholder()).isFalse();
        assertThat(team.lastPlayedAt()).isEqualTo("2024-05-19 15:00:00");

        assertThat(team.squad()).hasSize(1);
        Squad squadMember = team.squad().getFirst();
        assertThat(squadMember.id()).isEqualTo(10001L);
        assertThat(squadMember.transferId()).isNull();
        assertThat(squadMember.playerId()).isEqualTo(200001L);
        assertThat(squadMember.teamId()).isEqualTo(53L);
        assertThat(squadMember.positionId()).isEqualTo(27L);
        assertThat(squadMember.detailedPositionId()).isEqualTo(153L);
        assertThat(squadMember.jerseyNumber()).isEqualTo(9);
        assertThat(squadMember.start()).isEqualTo("2023-07-01");
        assertThat(squadMember.end()).isNull();

        Player player = squadMember.player();
        assertThat(player).isNotNull();
        assertThat(player.id()).isEqualTo(200001L);
        assertThat(player.commonName()).isEqualTo("Kyogo");
        assertThat(player.name()).isEqualTo("Kyogo Furuhashi");
        assertThat(player.cityId()).isEqualTo("Osaka");
        assertThat(player.height()).isEqualTo(173);
        assertThat(player.weight()).isEqualTo(67);
        assertThat(player.dateOfBirth()).isEqualTo("1995-01-20");
    }

    @Test
    void decodesTeamWithoutIncludes() {
        String json = """
                {
                  "data": {
                    "id": 62,
                    "name": "Rangers",
                    "placeholder": false
                  }
                }
                """;

        Team team = codec.decode(json, codec.type(Team.class)).data();

        assertThat(team.id()).isEqualTo(62L);
        assertThat(team.name()).isEqualTo("Rangers");
        assertThat(team.squad()).isNull();
    }

    // -------------------------------------------------------------------------
    // 2. List of Players — assert scalars; verify cityId decodes as a String
    // -------------------------------------------------------------------------

    @Test
    void decodesPlayerListWithCityIdAsString() {
        String json = """
                {
                  "data": [
                    {
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
                    {
                      "id": 200002,
                      "sport_id": 1,
                      "country_id": 462,
                      "nationality_id": 462,
                      "city_id": null,
                      "position_id": 24,
                      "detailed_position_id": null,
                      "type_id": 220,
                      "common_name": "Tavernier",
                      "firstname": "James",
                      "lastname": "Tavernier",
                      "name": "James Tavernier",
                      "display_name": "Tavernier",
                      "image_path": "https://cdn.sportmonks.com/images/soccer/players/200002.png",
                      "height": 180,
                      "weight": 72,
                      "date_of_birth": "1991-10-31",
                      "gender": "male"
                    }
                  ]
                }
                """;

        ApiResponse<List<Player>> response = codec.decode(json, codec.listType(Player.class));
        List<Player> players = response.data();

        assertThat(players).hasSize(2);

        Player first = players.get(0);
        assertThat(first.id()).isEqualTo(200001L);
        assertThat(first.sportId()).isEqualTo(1L);
        assertThat(first.countryId()).isEqualTo(320L);
        assertThat(first.nationalityId()).isEqualTo(320L);
        // cityId must decode as a String, not a numeric type
        assertThat(first.cityId()).isInstanceOf(String.class);
        assertThat(first.cityId()).isEqualTo("Osaka");
        assertThat(first.positionId()).isEqualTo(27L);
        assertThat(first.detailedPositionId()).isEqualTo(153L);
        assertThat(first.typeId()).isEqualTo(220L);
        assertThat(first.commonName()).isEqualTo("Kyogo");
        assertThat(first.firstname()).isEqualTo("Furuhashi");
        assertThat(first.lastname()).isEqualTo("Kyogo");
        assertThat(first.name()).isEqualTo("Kyogo Furuhashi");
        assertThat(first.displayName()).isEqualTo("Kyogo");
        assertThat(first.height()).isEqualTo(173);
        assertThat(first.weight()).isEqualTo(67);
        assertThat(first.dateOfBirth()).isEqualTo("1995-01-20");
        assertThat(first.gender()).isEqualTo("male");

        Player second = players.get(1);
        assertThat(second.id()).isEqualTo(200002L);
        assertThat(second.cityId()).isNull();
        assertThat(second.detailedPositionId()).isNull();
    }

    // -------------------------------------------------------------------------
    // 3. Single Coach — assert scalars including playerId
    // -------------------------------------------------------------------------

    @Test
    void decodesCoachScalarsIncludingPlayerId() {
        String json = """
                {
                  "data": {
                    "id": 300001,
                    "player_id": 199999,
                    "sport_id": 1,
                    "country_id": 462,
                    "nationality_id": 462,
                    "city_id": "Glasgow",
                    "common_name": "Rodgers",
                    "firstname": "Brendan",
                    "lastname": "Rodgers",
                    "name": "Brendan Rodgers",
                    "display_name": "B. Rodgers",
                    "image_path": "https://cdn.sportmonks.com/images/soccer/coaches/300001.png",
                    "height": 183,
                    "weight": 80,
                    "date_of_birth": "1973-01-26",
                    "gender": "male"
                  }
                }
                """;

        ApiResponse<Coach> response = codec.decode(json, codec.type(Coach.class));
        Coach coach = response.data();

        assertThat(coach.id()).isEqualTo(300001L);
        assertThat(coach.playerId()).isEqualTo(199999L);
        assertThat(coach.sportId()).isEqualTo(1L);
        assertThat(coach.countryId()).isEqualTo(462L);
        assertThat(coach.nationalityId()).isEqualTo(462L);
        assertThat(coach.cityId()).isEqualTo("Glasgow");
        assertThat(coach.commonName()).isEqualTo("Rodgers");
        assertThat(coach.firstname()).isEqualTo("Brendan");
        assertThat(coach.lastname()).isEqualTo("Rodgers");
        assertThat(coach.name()).isEqualTo("Brendan Rodgers");
        assertThat(coach.displayName()).isEqualTo("B. Rodgers");
        assertThat(coach.height()).isEqualTo(183);
        assertThat(coach.weight()).isEqualTo(80);
        assertThat(coach.dateOfBirth()).isEqualTo("1973-01-26");
        assertThat(coach.gender()).isEqualTo("male");
    }

    // -------------------------------------------------------------------------
    // 4. Transfer with player, fromTeam, toTeam included
    //    + assert omitted relations are null
    // -------------------------------------------------------------------------

    @Test
    void decodesTransferWithRelationsIncluded() {
        String json = """
                {
                  "data": {
                    "id": 400001,
                    "sport_id": 1,
                    "player_id": 200001,
                    "type_id": 219,
                    "from_team_id": 62,
                    "to_team_id": 53,
                    "position_id": 27,
                    "detailed_position_id": 153,
                    "date": "2021-07-01",
                    "career_ended": false,
                    "completed": true,
                    "amount": "5000000",
                    "completed_at": "2021-06-28",
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
                    "from_team": {
                      "id": 62,
                      "sport_id": 1,
                      "country_id": 1161,
                      "venue_id": 8910,
                      "gender": "male",
                      "name": "Rangers",
                      "short_code": "RAN",
                      "image_path": "https://cdn.sportmonks.com/images/soccer/teams/62.png",
                      "founded": 1872,
                      "type": "domestic",
                      "placeholder": false,
                      "last_played_at": "2024-05-19 12:00:00"
                    },
                    "to_team": {
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
                }
                """;

        ApiResponse<Transfer> response = codec.decode(json, codec.type(Transfer.class));
        Transfer transfer = response.data();

        assertThat(transfer.id()).isEqualTo(400001L);
        assertThat(transfer.sportId()).isEqualTo(1L);
        assertThat(transfer.playerId()).isEqualTo(200001L);
        assertThat(transfer.typeId()).isEqualTo(219L);
        assertThat(transfer.fromTeamId()).isEqualTo(62L);
        assertThat(transfer.toTeamId()).isEqualTo(53L);
        assertThat(transfer.positionId()).isEqualTo(27L);
        assertThat(transfer.detailedPositionId()).isEqualTo(153L);
        assertThat(transfer.date()).isEqualTo("2021-07-01");
        assertThat(transfer.careerEnded()).isFalse();
        assertThat(transfer.completed()).isTrue();
        assertThat(transfer.amount()).isEqualTo("5000000");
        assertThat(transfer.completedAt()).isEqualTo("2021-06-28");

        // Relation: player
        assertThat(transfer.player()).isNotNull();
        assertThat(transfer.player().id()).isEqualTo(200001L);
        assertThat(transfer.player().commonName()).isEqualTo("Kyogo");

        // Relation: fromTeam
        assertThat(transfer.fromTeam()).isNotNull();
        assertThat(transfer.fromTeam().id()).isEqualTo(62L);
        assertThat(transfer.fromTeam().name()).isEqualTo("Rangers");

        // Relation: toTeam
        assertThat(transfer.toTeam()).isNotNull();
        assertThat(transfer.toTeam().id()).isEqualTo(53L);
        assertThat(transfer.toTeam().name()).isEqualTo("Celtic");
    }

    @Test
    void decodesTransferWithoutRelations() {
        String json = """
                {
                  "data": {
                    "id": 400002,
                    "sport_id": 1,
                    "player_id": 200002,
                    "type_id": 219,
                    "from_team_id": 53,
                    "to_team_id": 62,
                    "date": "2022-01-15",
                    "completed": true
                  }
                }
                """;

        Transfer transfer = codec.decode(json, codec.type(Transfer.class)).data();

        assertThat(transfer.id()).isEqualTo(400002L);
        // Relations omitted → null
        assertThat(transfer.player()).isNull();
        assertThat(transfer.fromTeam()).isNull();
        assertThat(transfer.toTeam()).isNull();
    }
}
