package io.github.miro93.sportmonks.core.retry;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class BackoffTest {

    private final Backoff backoff = Backoff.defaults(); // base 500ms, x2, max 30s

    @RepeatedTest(20)
    void firstAttemptDelayIsBetweenHalfAndFullBase() {
        Duration delay = backoff.delayFor(1);
        assertThat(delay).isBetween(Duration.ofMillis(250), Duration.ofMillis(500));
    }

    @Test
    void delayIsCappedAtMax() {
        Duration delay = backoff.delayFor(100);
        assertThat(delay).isBetween(Duration.ofMillis(15_000), Duration.ofMillis(30_000));
    }
}
