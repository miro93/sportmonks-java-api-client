package io.github.miro93.sportmonks.core.request.internal;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

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
    void encodesSpacesInPathSegmentsAsPercent20() {
        URI uri = UrlBuilder.build(BASE, RequestSpec.builder("leagues/search/Premier League").build());
        assertThat(uri.toString()).isEqualTo(BASE + "/leagues/search/Premier%20League");
    }

    @Test
    void doesNotThrowForSpaceInPathSegment() {
        assertThatCode(() ->
                UrlBuilder.build(BASE, RequestSpec.builder("leagues/search/Premier League").build()))
                .doesNotThrowAnyException();
    }

    @Test
    void encodesReservedCharactersInPathSegments() {
        URI uri = UrlBuilder.build(BASE, RequestSpec.builder("leagues/search/A&B?C").build());
        assertThat(uri.toString()).isEqualTo(BASE + "/leagues/search/A%26B%3FC");
    }

    @Test
    void preservesSlashSeparatorsBetweenSegments() {
        URI uri = UrlBuilder.build(BASE, RequestSpec.builder("fixtures/head-to-head/1/2").build());
        assertThat(uri.toString()).isEqualTo(BASE + "/fixtures/head-to-head/1/2");
    }

    @Test
    void keepsCommasInMultiIdPathSegmentLiteral() {
        URI uri = UrlBuilder.build(BASE, RequestSpec.builder("fixtures/multi/1,2,3").build());
        assertThat(uri.toString()).isEqualTo(BASE + "/fixtures/multi/1,2,3");
    }

    @Test
    void encodesFiltersSelectSortAndPage() {
        URI uri = UrlBuilder.build(BASE, RequestSpec.builder("fixtures/1")
                .filter("eventTypes", "15", "16")
                .select("name", "starting_at")
                .sortBy("starting_at")
                .order(io.github.miro93.sportmonks.core.request.Order.DESC)
                .page(3)
                .perPage(50)
                .locale("de")
                .timezone(java.time.ZoneId.of("Europe/Amsterdam"))
                .build());

        String s = uri.toString();
        assertThat(s).contains("filters=eventTypes:15,16");
        assertThat(s).contains("select=name,starting_at");
        assertThat(s).contains("sortBy=starting_at");
        assertThat(s).contains("order=desc");
        assertThat(s).doesNotContain("sort=");
        assertThat(s).contains("page=3");
        assertThat(s).contains("per_page=50");
        assertThat(s).contains("locale=de");
        assertThat(s).contains("timezone=Europe%2FAmsterdam");
    }

    @Test
    void encodesCursorParam() {
        URI uri = UrlBuilder.build(BASE, RequestSpec.builder("fixtures").cursor("a+b=").build());
        assertThat(uri.toString()).isEqualTo(BASE + "/fixtures?cursor=a%2Bb%3D");
    }
}
