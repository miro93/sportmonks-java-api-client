package io.github.miro93.sportmonks.core.request.internal;

import io.github.miro93.sportmonks.core.request.Order;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RequestSpecTest {

    @Test
    void builderAccumulatesIncludesAndFilters() {
        RequestSpec spec = RequestSpec.builder("fixtures/18535517")
                .include("participants", "events.player")
                .include("scores")
                .filter("eventTypes", "15", "16")
                .select("name", "starting_at")
                .sortBy("starting_at")
                .order(Order.DESC)
                .perPage(50)
                .locale("de")
                .timezone(ZoneId.of("Europe/Amsterdam"))
                .page(2)
                .build();

        assertThat(spec.path()).isEqualTo("fixtures/18535517");
        assertThat(spec.includes()).containsExactly("participants", "events.player", "scores");
        assertThat(spec.filters()).containsEntry("eventTypes", java.util.List.of("15", "16"));
        assertThat(spec.select()).containsExactly("name", "starting_at");
        assertThat(spec.sortBy()).isEqualTo("starting_at");
        assertThat(spec.order()).isEqualTo(Order.DESC);
        assertThat(spec.perPage()).isEqualTo(50);
        assertThat(spec.locale()).isEqualTo("de");
        assertThat(spec.timezone()).isEqualTo("Europe/Amsterdam");
        assertThat(spec.page()).isEqualTo(2);
    }

    @Test
    void builderDefaultsAreEmpty() {
        RequestSpec spec = RequestSpec.builder("leagues").build();

        assertThat(spec.includes()).isEmpty();
        assertThat(spec.filters()).isEmpty();
        assertThat(spec.select()).isEmpty();
        assertThat(spec.sortBy()).isNull();
        assertThat(spec.order()).isNull();
        assertThat(spec.perPage()).isNull();
        assertThat(spec.locale()).isNull();
        assertThat(spec.timezone()).isNull();
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

    @Test
    @SuppressWarnings("deprecation")
    void legacySortMapsToSortByAndOrder() {
        assertThat(RequestSpec.builder("fixtures").sort("starting_at").build())
                .extracting(RequestSpec::sortBy, RequestSpec::order).containsExactly("starting_at", null);
        assertThat(RequestSpec.builder("fixtures").sort("-starting_at").build())
                .extracting(RequestSpec::sortBy, RequestSpec::order).containsExactly("starting_at", Order.DESC);
        assertThat(RequestSpec.builder("fixtures").sort().build().sortBy()).isNull();
        assertThatThrownBy(() -> RequestSpec.builder("fixtures").sort("a", "b"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void perPageIsRangeChecked() {
        assertThatThrownBy(() -> RequestSpec.builder("fixtures").perPage(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> RequestSpec.builder("fixtures").perPage(51)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void withCursorDropsPerPageButWithPageKeepsIt() {
        RequestSpec spec = RequestSpec.builder("fixtures").perPage(50).build();

        assertThat(spec.withPage(1).perPage()).isEqualTo(50);
        assertThat(spec.withCursor("c1").perPage()).isNull();
    }
}
