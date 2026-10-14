package io.github.miro93.sportmonks.football.model;

import io.github.miro93.sportmonks.core.json.HelidonJsonCodec;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MatchFactDecodingTest {

    private final HelidonJsonCodec codec = new HelidonJsonCodec();

    @Test
    void decodesDocumentedSampleWithFreeFormData() {
        String json = """
                { "data": [ { "id": 113, "sport_id": 1, "fixture_id": 19173461, "type_id": 76088, "participant": "away",
                              "basis": "h2h",
                              "data": { "all": { "count": 6, "percentage": 37.5 }, "home": { "count": 5, "percentage": 83.33 } },
                              "natural_language": "Paradou AC has 6 wins against JS Saoura", "category": "statistics",
                              "scope": "allFixtures",
                              "type": { "id": 76088, "name": "Wins", "developer_name": "WINS" },
                              "fixture": { "id": 19173461, "name": "Paradou AC vs JS Saoura" } },
                            { "id": 28083540, "fixture_id": 19427187, "type_id": 76115, "participant": "both", "basis": "h2h",
                              "data": { "count": 22 }, "natural_language": null, "category": "statistics", "scope": "allFixtures" } ] }
                """;

        List<MatchFact> facts = codec.decode(json, codec.listType(MatchFact.class)).data();

        MatchFact first = facts.getFirst();
        assertThat(first.participant()).isEqualTo("away");
        assertThat(first.basis()).isEqualTo("h2h");
        assertThat(first.naturalLanguage()).startsWith("Paradou AC");
        assertThat(first.data()).containsKey("all");
        @SuppressWarnings("unchecked")
        var all = (java.util.Map<String, Object>) first.data().get("all");
        assertThat(all).containsEntry("count", 6).containsEntry("percentage", 37.5);
        assertThat(first.type().developerName()).isEqualTo("WINS");
        assertThat(first.fixture().name()).isEqualTo("Paradou AC vs JS Saoura");

        MatchFact second = facts.getLast();
        assertThat(second.naturalLanguage()).isNull();
        assertThat(second.data()).containsEntry("count", 22);
        assertThat(second.type()).isNull();
    }
}
