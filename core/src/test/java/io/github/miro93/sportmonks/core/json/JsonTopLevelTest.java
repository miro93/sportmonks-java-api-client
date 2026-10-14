package io.github.miro93.sportmonks.core.json;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonTopLevelTest {

    private static Map<String, String> slices(String json) {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        Map<String, JsonTopLevel.Range> ranges = JsonTopLevel.scan(bytes);
        return ranges.entrySet().stream().collect(java.util.stream.Collectors.toMap(Map.Entry::getKey,
                e -> new String(bytes, e.getValue().start(), e.getValue().end() - e.getValue().start(), StandardCharsets.UTF_8)));
    }

    @Test
    void slicesEveryTopLevelMemberValue() {
        Map<String, String> s = slices("""
                { "data": [ { "id": 1, "name": "A, \\"quoted\\" } ]" }, { "nested": { "x": [1, 2, {"y": null}] } } ],
                  "pagination": {"count": 2, "has_more": false}, "timezone": "UTC", "n": -12.5e3, "t": true, "z": null }
                """);

        assertThat(s.get("data")).startsWith("[ { \"id\": 1").endsWith("} ]");
        assertThat(s.get("pagination")).isEqualTo("{\"count\": 2, \"has_more\": false}");
        assertThat(s.get("timezone")).isEqualTo("\"UTC\"");
        assertThat(s.get("n")).isEqualTo("-12.5e3");
        assertThat(s.get("t")).isEqualTo("true");
        assertThat(s.get("z")).isEqualTo("null");
    }

    @Test
    void handlesEscapedBackslashBeforeQuoteAndUnicode() {
        Map<String, String> s = slices("{ \"data\": { \"path\": \"C:\\\\\\\\\", \"name\": \"Zürich — 東京\" }, \"timezone\": \"Europe/Zürich\" }");

        assertThat(s.get("data")).contains("Zürich — 東京");
        assertThat(s.get("timezone")).isEqualTo("\"Europe/Zürich\"");
    }

    @Test
    void emptyObjectAndWhitespace() {
        assertThat(slices("  {  }  ")).isEmpty();
        assertThat(slices("{\n\t\"data\" :\n[ ]\n}")).containsEntry("data", "[ ]");
    }

    @Test
    void rejectsNonObjectsAndTruncatedDocuments() {
        assertThatThrownBy(() -> slices("[1, 2]")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> slices("{ \"data\": [1, 2")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> slices("{ \"data\": \"open")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> slices("{ data: 1 }")).isInstanceOf(IllegalArgumentException.class);
    }
}
