package io.github.miro93.sportmonks.football.model;

import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.response.ApiResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CompetitionDecodingTest {

    private final JacksonCodec codec = new JacksonCodec();

    // -------------------------------------------------------------------------
    // 1. Single League with seasons included
    // -------------------------------------------------------------------------

    @Test
    void decodesLeagueWithSeasonsIncluded() {
        String json = """
                {
                  "data": {
                    "id": 501,
                    "sport_id": 1,
                    "country_id": 462,
                    "name": "Scottish Premiership",
                    "active": true,
                    "short_code": "SPL",
                    "image_path": "https://cdn.sportmonks.com/images/soccer/leagues/501.png",
                    "type": "league",
                    "sub_type": "domestic",
                    "last_played_at": "2024-05-19 15:00:00",
                    "category": 1,
                    "has_jerseys": false,
                    "seasons": [
                      {
                        "id": 19735,
                        "sport_id": 1,
                        "league_id": 501,
                        "tie_breaker_rule_id": 169,
                        "name": "2023/2024",
                        "finished": true,
                        "pending": false,
                        "is_current": false,
                        "starting_at": "2023-07-29",
                        "ending_at": "2024-05-19",
                        "standings_recalculated_at": "2024-05-20 04:00:15",
                        "games_in_current_week": false,
                        "standing_method": "points"
                      }
                    ]
                  }
                }
                """;

        ApiResponse<League> response = codec.decode(json, codec.type(League.class));
        League league = response.data();

        assertThat(league.id()).isEqualTo(501L);
        assertThat(league.sportId()).isEqualTo(1L);
        assertThat(league.countryId()).isEqualTo(462L);
        assertThat(league.name()).isEqualTo("Scottish Premiership");
        assertThat(league.active()).isTrue();
        assertThat(league.shortCode()).isEqualTo("SPL");
        assertThat(league.imagePath()).isEqualTo("https://cdn.sportmonks.com/images/soccer/leagues/501.png");
        assertThat(league.type()).isEqualTo("league");
        assertThat(league.subType()).isEqualTo("domestic");
        assertThat(league.lastPlayedAt()).isEqualTo("2024-05-19 15:00:00");
        assertThat(league.category()).isEqualTo(1);
        assertThat(league.hasJerseys()).isFalse();

        assertThat(league.seasons()).hasSize(1);
        Season season = league.seasons().getFirst();
        assertThat(season.id()).isEqualTo(19735L);
        assertThat(season.name()).isEqualTo("2023/2024");
        assertThat(season.finished()).isTrue();
        assertThat(season.isCurrent()).isFalse();
        assertThat(season.tieBreakerRuleId()).isEqualTo(169L);
        assertThat(season.standingMethod()).isEqualTo("points");
    }

    @Test
    void decodesLeagueWithoutIncludes() {
        String json = """
                {
                  "data": {
                    "id": 8,
                    "name": "Premier League",
                    "active": true,
                    "type": "league",
                    "sub_type": "domestic"
                  }
                }
                """;

        League league = codec.decode(json, codec.type(League.class)).data();

        assertThat(league.id()).isEqualTo(8L);
        assertThat(league.name()).isEqualTo("Premier League");
        assertThat(league.seasons()).isNull();
    }

    // -------------------------------------------------------------------------
    // 2. List of Seasons — no includes
    // -------------------------------------------------------------------------

    @Test
    void decodesSeasonListWithoutIncludes() {
        String json = """
                {
                  "data": [
                    {
                      "id": 19735,
                      "sport_id": 1,
                      "league_id": 501,
                      "tie_breaker_rule_id": 169,
                      "name": "2023/2024",
                      "finished": true,
                      "pending": false,
                      "is_current": false,
                      "starting_at": "2023-07-29",
                      "ending_at": "2024-05-19",
                      "standings_recalculated_at": "2024-05-20 04:00:15",
                      "games_in_current_week": false,
                      "standing_method": "points"
                    },
                    {
                      "id": 23614,
                      "sport_id": 1,
                      "league_id": 501,
                      "tie_breaker_rule_id": 169,
                      "name": "2024/2025",
                      "finished": false,
                      "pending": false,
                      "is_current": true,
                      "starting_at": "2024-07-27",
                      "ending_at": "2025-05-25",
                      "standings_recalculated_at": null,
                      "games_in_current_week": true,
                      "standing_method": "points"
                    }
                  ]
                }
                """;

        ApiResponse<List<Season>> response = codec.decode(json, codec.listType(Season.class));
        List<Season> seasons = response.data();

        assertThat(seasons).hasSize(2);

        Season first = seasons.get(0);
        assertThat(first.id()).isEqualTo(19735L);
        assertThat(first.leagueId()).isEqualTo(501L);
        assertThat(first.name()).isEqualTo("2023/2024");
        assertThat(first.finished()).isTrue();
        assertThat(first.isCurrent()).isFalse();
        assertThat(first.gamesInCurrentWeek()).isFalse();
        // Relations not included → null
        assertThat(first.league()).isNull();
        assertThat(first.stages()).isNull();

        Season second = seasons.get(1);
        assertThat(second.id()).isEqualTo(23614L);
        assertThat(second.isCurrent()).isTrue();
        assertThat(second.gamesInCurrentWeek()).isTrue();
        assertThat(second.league()).isNull();
        assertThat(second.stages()).isNull();
    }

    // -------------------------------------------------------------------------
    // 3. Schedule-shaped payload: Stage → rounds → fixtures (3-level nesting)
    // -------------------------------------------------------------------------

    @Test
    void decodesStageListWithRoundsAndFixtures() {
        String json = """
                {
                  "data": [
                    {
                      "id": 77457866,
                      "sport_id": 1,
                      "league_id": 501,
                      "season_id": 19735,
                      "type_id": 223,
                      "name": "Regular Season",
                      "sort_order": 1,
                      "finished": true,
                      "pending": false,
                      "is_current": false,
                      "starting_at": "2023-07-29",
                      "ending_at": "2024-05-19",
                      "games_in_current_week": false,
                      "rounds": [
                        {
                          "id": 274719,
                          "sport_id": 1,
                          "league_id": 501,
                          "season_id": 19735,
                          "stage_id": 77457866,
                          "group_id": null,
                          "name": "1",
                          "finished": true,
                          "pending": false,
                          "is_current": false,
                          "starting_at": "2023-07-29",
                          "ending_at": "2023-08-06",
                          "games_in_current_week": false,
                          "fixtures": [
                            {
                              "id": 18535517,
                              "sport_id": 1,
                              "league_id": 501,
                              "season_id": 19735,
                              "stage_id": 77457866,
                              "round_id": 274719,
                              "state_id": 5,
                              "name": "Celtic vs Rangers",
                              "starting_at": "2024-09-01 11:00:00",
                              "length": 90,
                              "placeholder": false,
                              "has_odds": true
                            }
                          ]
                        }
                      ]
                    }
                  ]
                }
                """;

        ApiResponse<List<Stage>> response = codec.decode(json, codec.listType(Stage.class));
        List<Stage> stages = response.data();

        assertThat(stages).hasSize(1);
        Stage stage = stages.getFirst();
        assertThat(stage.id()).isEqualTo(77457866L);
        assertThat(stage.name()).isEqualTo("Regular Season");
        assertThat(stage.seasonId()).isEqualTo(19735L);
        assertThat(stage.finished()).isTrue();
        assertThat(stage.isCurrent()).isFalse();
        assertThat(stage.sortOrder()).isEqualTo(1);

        assertThat(stage.rounds()).hasSize(1);
        Round round = stage.rounds().getFirst();
        assertThat(round.id()).isEqualTo(274719L);
        assertThat(round.name()).isEqualTo("1");
        assertThat(round.stageId()).isEqualTo(77457866L);
        assertThat(round.groupId()).isNull();
        assertThat(round.finished()).isTrue();
        assertThat(round.isCurrent()).isFalse();

        assertThat(round.fixtures()).hasSize(1);
        Fixture fixture = round.fixtures().getFirst();
        assertThat(fixture.id()).isEqualTo(18535517L);
        assertThat(fixture.name()).isEqualTo("Celtic vs Rangers");
        assertThat(fixture.leagueId()).isEqualTo(501L);
        assertThat(fixture.roundId()).isEqualTo(274719L);
        assertThat(fixture.placeholder()).isFalse();
        assertThat(fixture.hasOdds()).isTrue();
        // No nested includes on the fixture itself
        assertThat(fixture.participants()).isNull();
        assertThat(fixture.state()).isNull();
    }

    // -------------------------------------------------------------------------
    // 4. Stage and Round without includes — relations are null
    // -------------------------------------------------------------------------

    @Test
    void decodesStageListWithoutIncludes() {
        String json = """
                {
                  "data": [
                    {
                      "id": 77457866,
                      "sport_id": 1,
                      "league_id": 501,
                      "season_id": 19735,
                      "type_id": 223,
                      "name": "Regular Season",
                      "sort_order": 1,
                      "finished": true,
                      "pending": false,
                      "is_current": false,
                      "starting_at": "2023-07-29",
                      "ending_at": "2024-05-19",
                      "games_in_current_week": false
                    }
                  ]
                }
                """;

        List<Stage> stages = codec.decode(json, codec.listType(Stage.class)).data();

        assertThat(stages).hasSize(1);
        Stage stage = stages.getFirst();
        assertThat(stage.id()).isEqualTo(77457866L);
        // Relation not included → null
        assertThat(stage.rounds()).isNull();
    }

    @Test
    void decodesRoundWithoutIncludes() {
        String json = """
                {
                  "data": {
                    "id": 274719,
                    "sport_id": 1,
                    "league_id": 501,
                    "season_id": 19735,
                    "stage_id": 77457866,
                    "group_id": null,
                    "name": "1",
                    "finished": true,
                    "pending": false,
                    "is_current": false,
                    "starting_at": "2023-07-29",
                    "ending_at": "2023-08-06",
                    "games_in_current_week": false
                  }
                }
                """;

        Round round = codec.decode(json, codec.type(Round.class)).data();

        assertThat(round.id()).isEqualTo(274719L);
        // Relation not included → null
        assertThat(round.fixtures()).isNull();
    }
}
