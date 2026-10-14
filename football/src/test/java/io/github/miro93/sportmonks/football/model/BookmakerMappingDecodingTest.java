package io.github.miro93.sportmonks.football.model;

import io.github.miro93.sportmonks.core.json.HelidonJsonCodec;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BookmakerMappingDecodingTest {

    private final HelidonJsonCodec codec = new HelidonJsonCodec();

    @Test
    void decodesMapping() {
        List<BookmakerMapping> mappings = codec.decode("""
                { "data": [ { "fixture_id": 19427243, "bookmaker_id": 2, "bookmaker_name": "bet365",
                              "bookmaker_event_id": "194085995", "bookmaker_event_url": null } ] }
                """, codec.listType(BookmakerMapping.class)).data();

        BookmakerMapping mapping = mappings.getFirst();
        assertThat(mapping.fixtureId()).isEqualTo(19427243L);
        assertThat(mapping.bookmakerName()).isEqualTo("bet365");
        assertThat(mapping.bookmakerEventId()).isEqualTo("194085995");
        assertThat(mapping.bookmakerEventUrl()).isNull();
    }
}
