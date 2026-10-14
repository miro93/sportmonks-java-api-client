package io.github.miro93.sportmonks.football.model;

    import io.github.miro93.sportmonks.core.json.HelidonJsonCodec;
    import org.junit.jupiter.api.Test;

    import java.util.List;

    import static org.assertj.core.api.Assertions.assertThat;

    class NewsDecodingTest {

        private final HelidonJsonCodec codec = new HelidonJsonCodec();

    @Test
    void decodesArticleWithIncludes() {
        String json = """
                { "data": [ { "id": 2796, "fixture_id": 18841626, "league_id": 8,
                              "title": "Aston Villa took command from the start against Everton", "type": "postmatch",
                              "fixture": { "id": 18841626, "name": "Aston Villa vs Everton" },
                              "league": { "id": 8, "name": "Premier League" } } ] }
                """;

        News news = codec.decode(json, codec.listType(News.class)).data().getFirst();

        assertThat(news.id()).isEqualTo(2796L);
        assertThat(news.type()).isEqualTo("postmatch");
        assertThat(news.title()).startsWith("Aston Villa");
        assertThat(news.fixture().name()).isEqualTo("Aston Villa vs Everton");
        assertThat(news.league().name()).isEqualTo("Premier League");
    }
}
