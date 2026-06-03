package io.github.miro93.sportmonks.football.model;

import io.github.miro93.sportmonks.core.json.JacksonCodec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StateTvCommentaryDecodingTest {

    private final JacksonCodec codec = new JacksonCodec();

    @Test
    void decodesState() {
        String json = """
                { "data": { "id": 5, "state": "FT", "name": "Full Time", "short_name": "FT", "developer_name": "FT" } }
                """;

        State state = codec.decode(json, codec.type(State.class)).data();

        assertThat(state.id()).isEqualTo(5L);
        assertThat(state.state()).isEqualTo("FT");
        assertThat(state.name()).isEqualTo("Full Time");
        assertThat(state.shortName()).isEqualTo("FT");
        assertThat(state.developerName()).isEqualTo("FT");
    }

    @Test
    void decodesTvStationWithNullableUrlAndImage() {
        String json = """
                { "data": { "id": 5, "name": "Sky Sports", "url": null, "image_path": null } }
                """;

        TvStation station = codec.decode(json, codec.type(TvStation.class)).data();

        assertThat(station.id()).isEqualTo(5L);
        assertThat(station.name()).isEqualTo("Sky Sports");
        assertThat(station.url()).isNull();
        assertThat(station.imagePath()).isNull();
    }

    @Test
    void decodesCommentaryWithStringIdAndBooleans() {
        String json = """
                {
                  "data": {
                    "id": "c-9981",
                    "fixture_id": 18535517,
                    "comment": "GOAL! What a strike.",
                    "minute": 23,
                    "extra_minute": 2,
                    "is_goal": true,
                    "is_important": true,
                    "order": 45
                  }
                }
                """;

        Commentary commentary = codec.decode(json, codec.type(Commentary.class)).data();

        assertThat(commentary.id()).isEqualTo("c-9981");
        assertThat(commentary.fixtureId()).isEqualTo(18535517L);
        assertThat(commentary.comment()).isEqualTo("GOAL! What a strike.");
        assertThat(commentary.minute()).isEqualTo(23);
        assertThat(commentary.extraMinute()).isEqualTo(2);
        assertThat(commentary.isGoal()).isTrue();
        assertThat(commentary.isImportant()).isTrue();
        assertThat(commentary.order()).isEqualTo(45);
    }

    @Test
    void decodesCommentaryWithOptionalFieldsAbsent() {
        String json = """
                { "data": { "id": "c-1" } }
                """;

        Commentary commentary = codec.decode(json, codec.type(Commentary.class)).data();

        assertThat(commentary.id()).isEqualTo("c-1");
        assertThat(commentary.fixtureId()).isNull();
        assertThat(commentary.comment()).isNull();
        assertThat(commentary.minute()).isNull();
        assertThat(commentary.extraMinute()).isNull();
        assertThat(commentary.isGoal()).isNull();
        assertThat(commentary.isImportant()).isNull();
        assertThat(commentary.order()).isNull();
    }
}
