package io.github.miro93.sportmonks.core.request;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class UrlBuilderTest {

    private static final String BASE = "https://api.sportmonks.com/v3/football";

    @Test
    void buildsPathWithNoParams() {
        URI uri = UrlBuilder.build(BASE, RequestSpec.builder("leagues").build());
        assertThat(uri.toString()).isEqualTo(BASE + "/leagues");
    }

    @Test
    void joinsIncludesWithSemicolonAndKeepsNestingDot() {
        URI uri = UrlBuilder.build(BASE, RequestSpec.builder("fixtures/1")
                .include("participants", "events.player")
                .build());
        assertThat(uri.toString()).isEqualTo(BASE + "/fixtures/1?include=participants;events.player");
    }

    @Test
    void encodesFiltersSelectSortAndPage() {
        URI uri = UrlBuilder.build(BASE, RequestSpec.builder("fixtures/1")
                .filter("eventTypes", "15", "16")
                .select("name", "starting_at")
                .sort("starting_at")
                .page(3)
                .build());

        String s = uri.toString();
        assertThat(s).contains("filters=eventTypes:15,16");
        assertThat(s).contains("select=name,starting_at");
        assertThat(s).contains("sort=starting_at");
        assertThat(s).contains("page=3");
    }
}
