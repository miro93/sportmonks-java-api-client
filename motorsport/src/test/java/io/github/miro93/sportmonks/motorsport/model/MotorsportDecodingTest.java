package io.github.miro93.sportmonks.motorsport.model;

import io.github.miro93.sportmonks.core.json.HelidonJsonCodec;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/// Decodes the documented sample of every Motorsport entity.
class MotorsportDecodingTest {

    private final HelidonJsonCodec codec = new HelidonJsonCodec();

    private <T> T one(String json, Class<T> type) {
        return codec.decode("{ \"data\": " + json + " }", codec.type(type)).data();
    }

    @Test
    void decodesDriver() {
        Driver d = one("""
                { "id": 37920800, "sport_id": 2, "country_id": 38, "nationality_id": 38, "city_id": 36172, "position_id": 9730,
                  "detailed_position_id": 9730, "type_id": null, "common_name": "M. Verstappen", "firstname": "Max",
                  "lastname": "Verstappen", "name": "Max Verstappen", "display_name": "Max Verstappen",
                  "image_path": "https://cdn.sportmonks.com/images/motorsport/players/0/37920800.png", "height": 181,
                  "weight": 72, "date_of_birth": "1997-09-30", "gender": "male", "teams": [ { "id": 276191, "name": "Red Bull" } ] }
                """, Driver.class);

        assertThat(d.displayName()).isEqualTo("Max Verstappen");
        assertThat(d.typeId()).isNull();
        assertThat(d.teams().getFirst().name()).isEqualTo("Red Bull");
    }

    @Test
    void decodesFixtureWithIncludes() {
        Fixture f = one("""
                { "id": 19398653, "sport_id": 2, "league_id": 3468, "season_id": 25273, "stage_id": 77475992, "group_id": null,
                  "aggregate_id": null, "round_id": null, "state_id": 5, "venue_id": 343575, "name": "Practice 1",
                  "starting_at": "2025-03-14 01:30:00", "result_info": null, "leg": "1/1", "details": null, "length": null,
                  "placeholder": false, "has_odds": false, "has_premium_odds": false, "starting_at_timestamp": 1741915800,
                  "state": { "id": 5, "state": "FT", "name": "Finished", "short_name": "FT", "developer_name": "FT" },
                  "venue": { "id": 343575, "name": "Albert Park" }, "participants": [ { "id": 276189, "name": "Ferrari" } ],
                  "laps": [ { "id": 2408, "fixture_id": 19398653, "lap_number": 4, "driver_number": 1, "participant_id": 37920800, "is_latest": false } ],
                  "pitstops": [ { "id": 77, "lap_number": 12, "driver_number": 6 } ],
                  "stints": [ { "id": 141, "stint_number": 1, "driver_number": 5 } ] }
                """, Fixture.class);

        assertThat(f.name()).isEqualTo("Practice 1");
        assertThat(f.leg()).isEqualTo("1/1");
        assertThat(f.startingAtTimestamp()).isEqualTo(1741915800L);
        assertThat(f.state().developerName()).isEqualTo("FT");
        assertThat(f.venue().name()).isEqualTo("Albert Park");
        assertThat(f.participants()).extracting(Team::name).containsExactly("Ferrari");
        assertThat(f.laps().getFirst().lapNumber()).isEqualTo(4);
        assertThat(f.pitstops().getFirst().lapNumber()).isEqualTo(12);
        assertThat(f.stints().getFirst().stintNumber()).isEqualTo(1);
        assertThat(f.league()).isNull();
    }

    @Test
    void decodesRaceResultStageWithLineupDetails() {
        List<Stage> stages = codec.decode("""
                { "data": [ { "id": 77476128, "sport_id": 2, "league_id": 3468, "season_id": 25273, "type_id": 9724,
                    "name": "Formula 1 Etihad Airways Abu Dhabi Grand Prix", "sort_order": 24, "finished": true, "is_current": false,
                    "starting_at": "2025-12-05", "ending_at": "2025-12-07", "games_in_current_week": false, "tie_breaker_rule_id": 573,
                    "fixtures": [ { "id": 19408487, "name": "Race", "starting_at": "2025-12-07 13:00:00", "placeholder": false,
                        "lineups": [ { "id": 14671697693, "sport_id": 2, "fixture_id": 19408487, "player_id": 37920802, "team_id": 276191,
                            "position_id": 9730, "type_id": 11, "grid_position": 2, "driver_name": "Lando Norris", "driver_number": 4,
                            "details": [ { "id": 1816685645, "fixture_id": 19408487, "player_id": 37920802, "team_id": 276191,
                                           "lineup_id": 14671697693, "type_id": 9711, "data": { "value": 3, "points": 15.5 } } ] } ] } ] } ] }
                """, codec.listType(Stage.class)).data();

        Stage stage = stages.getFirst();
        assertThat(stage.sortOrder()).isEqualTo(24);
        assertThat(stage.finished()).isTrue();
        Lineup lineup = stage.fixtures().getFirst().lineups().getFirst();
        assertThat(lineup.driverName()).isEqualTo("Lando Norris");
        assertThat(lineup.gridPosition()).isEqualTo(2);
        LineupDetail detail = lineup.details().getFirst();
        assertThat(detail.typeId()).isEqualTo(9711L);
        assertThat(detail.data()).containsEntry("value", 3).containsEntry("points", 15.5);
    }

    @Test
    void decodesLeagueSeasonStandingStateTeamVenue() {
        League league = one("""
                { "id": 3468, "sport_id": 2, "country_id": 462, "name": "Formula 1", "active": 1, "type": "championship",
                  "sub_type": null, "last_played_at": "2025-12-07 13:00:00", "category": 1, "has_jerseys": false,
                  "currentSeason": { "id": 26733, "name": "2026", "is_current": true, "finished": false } }
                """, League.class);
        assertThat(league.name()).isEqualTo("Formula 1");
        assertThat(league.currentSeason().isCurrent()).isTrue();

        Season season = one("""
                { "id": 25273, "sport_id": 2, "league_id": 3468, "tie_breaker_rule_id": 573, "name": "2025", "finished": true,
                  "pending": false, "is_current": false, "starting_at": "2025-03-14", "ending_at": "2025-12-07",
                  "standings_recalculated_at": "2025-12-07 15:10:00", "games_in_current_week": false }
                """, Season.class);
        assertThat(season.startingAt()).isEqualTo("2025-03-14");

        Standing standing = one("""
                { "id": 253768, "participant_id": 37920802, "sport_id": 2, "league_id": 3468, "season_id": 25273,
                  "stage_id": 77476104, "group_id": null, "round_id": null, "standing_rule_id": null, "position": 2,
                  "result": null, "points": 226 }
                """, Standing.class);
        assertThat(standing.position()).isEqualTo(2);
        assertThat(standing.points()).isEqualTo(226);

        State state = one("{ \"id\": 1, \"state\": \"NS\", \"name\": \"Not Started\", \"short_name\": \"NS\", \"developer_name\": \"NS\" }", State.class);
        assertThat(state.developerName()).isEqualTo("NS");

        Team team = one("""
                { "id": 276189, "sport_id": 2, "country_id": 251, "venue_id": null, "gender": "neutral", "name": "Ferrari",
                  "short_code": "FER", "image_path": "https://cdn/276189.png", "founded": null, "type": "hybrid",
                  "placeholder": false, "last_played_at": "2025-05-17 14:46:00",
                  "drivers": [ { "id": 1, "display_name": "C. Leclerc" } ] }
                """, Team.class);
        assertThat(team.shortCode()).isEqualTo("FER");
        assertThat(team.drivers().getFirst().displayName()).isEqualTo("C. Leclerc");

        Venue venue = one("""
                { "id": 343582, "country_id": 75285, "city_id": 58164, "name": "Circuit de Monaco", "address": null,
                  "zipcode": null, "latitude": "43.7347", "longitude": "7.4206", "capacity": 37000,
                  "image_path": "https://cdn/343582.png", "city_name": null, "surface": "asphalt", "national_team": false }
                """, Venue.class);
        assertThat(venue.capacity()).isEqualTo(37000);
        assertThat(venue.surface()).isEqualTo("asphalt");
    }
}
