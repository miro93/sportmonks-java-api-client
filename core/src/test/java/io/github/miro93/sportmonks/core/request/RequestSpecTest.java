package io.github.miro93.sportmonks.core.request;

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
    }
}
