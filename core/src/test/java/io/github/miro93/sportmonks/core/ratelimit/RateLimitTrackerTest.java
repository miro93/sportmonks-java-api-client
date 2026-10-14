package io.github.miro93.sportmonks.core.ratelimit;

import io.github.miro93.sportmonks.core.response.RateLimit;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitTrackerTest {

    private Instant now = Instant.parse("2026-10-14T20:00:00Z");
    private final Clock clock = new Clock() {
        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    };
    private final RateLimitTracker tracker = new RateLimitTracker(clock);

    @Test
    void remembersLatestUntilTheWindowResets() {
        tracker.record("fixtures", new RateLimit(5, 60, "fixture"));

        assertThat(tracker.latest("fixtures")).map(RateLimit::remaining).contains(5);
        assertThat(tracker.untilReset("fixtures")).isEqualTo(Duration.ofSeconds(60));
        assertThat(tracker.snapshot()).containsOnlyKeys("fixtures");

        now = now.plusSeconds(60);

        assertThat(tracker.latest("fixtures")).isEmpty();
        assertThat(tracker.untilReset("fixtures")).isZero();
        assertThat(tracker.snapshot()).isEmpty();
    }

    @Test
    void ignoresNullAndUnknownKeys() {
        tracker.record("teams", null);

        assertThat(tracker.latest("teams")).isEmpty();
        assertThat(tracker.untilReset("nope")).isZero();
    }
}
