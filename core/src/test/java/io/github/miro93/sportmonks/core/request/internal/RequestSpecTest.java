package io.github.miro93.sportmonks.core.request.internal;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RequestSpecTest {

    @Test
    void builderAccumulatesIncludesAndFilters() {
        RequestSpec spec = RequestSpec.builder("fixtures/18535517")
                .include("participants", "events.player")
                .include("scores")
                .filter("eventTypes", "15", "16")
                .select("name", "starting_at")
                .sort("starting_at")
                .page(2)
                .build();

        assertThat(spec.path()).isEqualTo("fixtures/18535517");
        assertThat(spec.includes()).containsExactly("participants", "events.player", "scores");
        assertThat(spec.filters()).containsEntry("eventTypes", java.util.List.of("15", "16"));
        assertThat(spec.select()).containsExactly("name", "starting_at");
        assertThat(spec.sort()).containsExactly("starting_at");
        assertThat(spec.page()).isEqualTo(2);
    }

    @Test
    void builderDefaultsAreEmpty() {
        RequestSpec spec = RequestSpec.builder("leagues").build();

        assertThat(spec.includes()).isEmpty();
        assertThat(spec.filters()).isEmpty();
        assertThat(spec.select()).isEmpty();
        assertThat(spec.sort()).isEmpty();
        assertThat(spec.page()).isNull();
        assertThat(spec.cursor()).isNull();
    }

    @Test
    void builderAcceptsCursor() {
        RequestSpec spec = RequestSpec.builder("fixtures").cursor("WzEsMl0").build();

        assertThat(spec.cursor()).isEqualTo("WzEsMl0");
    }

    @Test
    void withCursorReplacesPageAndKeepsOtherOptions() {
        RequestSpec spec = RequestSpec.builder("fixtures").include("scores").page(3).build();

        RequestSpec next = spec.withCursor("c1");

        assertThat(next.cursor()).isEqualTo("c1");
        assertThat(next.page()).isNull();
        assertThat(next.includes()).containsExactly("scores");
        assertThat(spec.page()).isEqualTo(3);
    }
}
