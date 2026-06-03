package io.github.miro93.sportmonks.core.coreapi.model;

import io.github.miro93.sportmonks.core.json.JacksonCodec;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TypeDecodingTest {

    private final JacksonCodec codec = new JacksonCodec();

    @Test
    void decodesTypeWithAllScalars() {
        String json = """
                {
                  "data": {
                    "id": 208,
                    "parent_id": 1,
                    "name": "Goals",
                    "code": "goals",
                    "developer_name": "GOALS",
                    "group": "events",
                    "description": "Number of goals"
                  }
                }
                """;

        Type type = codec.decode(json, codec.type(Type.class)).data();

        assertThat(type.id()).isEqualTo(208L);
        assertThat(type.parentId()).isEqualTo(1L);
        assertThat(type.name()).isEqualTo("Goals");
        assertThat(type.code()).isEqualTo("goals");
        assertThat(type.developerName()).isEqualTo("GOALS");
        assertThat(type.group()).isEqualTo("events");
        assertThat(type.description()).isEqualTo("Number of goals");
    }

    @Test
    void decodesTypeWithOptionalFieldsAbsent() {
        String json = """
                { "data": { "id": 208, "name": "Goals" } }
                """;

        Type type = codec.decode(json, codec.type(Type.class)).data();

        assertThat(type.id()).isEqualTo(208L);
        assertThat(type.name()).isEqualTo("Goals");
        assertThat(type.parentId()).isNull();
        assertThat(type.code()).isNull();
        assertThat(type.developerName()).isNull();
        assertThat(type.group()).isNull();
        assertThat(type.description()).isNull();
    }
}
