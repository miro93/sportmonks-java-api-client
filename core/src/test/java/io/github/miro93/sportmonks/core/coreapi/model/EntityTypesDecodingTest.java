package io.github.miro93.sportmonks.core.coreapi.model;

import io.github.miro93.sportmonks.core.json.HelidonJsonCodec;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EntityTypesDecodingTest {

    private final HelidonJsonCodec codec = new HelidonJsonCodec();

    @Test
    void decodesEntityKeyedTypesMap() {
        String json = """
                { "data": {
                    "CoachStatisticDetail": {
                      "updated_at": "2023-06-30T12:00:31.000000Z",
                      "types": [
                        { "id": 59, "name": "Substitutions", "code": "substitutions", "developer_name": "SUBSTITUTIONS",
                          "model_type": "statistic", "stat_group": "overall" },
                        { "id": 83, "name": "Redcards", "code": "redcards", "developer_name": "REDCARDS",
                          "model_type": "statistic", "stat_group": "overall" }
                      ]
                    },
                    "Event": { "updated_at": "2023-06-30T12:00:31.000000Z", "types": [] }
                } }
                """;

        Map<String, EntityTypes> byEntity = codec.decode(json, codec.mapType(EntityTypes.class)).data();

        assertThat(byEntity).containsOnlyKeys("CoachStatisticDetail", "Event");
        EntityTypes coach = byEntity.get("CoachStatisticDetail");
        assertThat(coach.updatedAt()).isEqualTo("2023-06-30T12:00:31.000000Z");
        assertThat(coach.types()).extracting(Type::developerName).containsExactly("SUBSTITUTIONS", "REDCARDS");
        assertThat(byEntity.get("Event").types()).isEmpty();
    }
}
