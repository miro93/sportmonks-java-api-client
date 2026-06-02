package io.github.miro93.sportmonks.core.retry;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

/// Exponential backoff with "equal jitter": the delay for attempt N is a random
/// value in [capped/2, capped], where capped = min(base * multiplier^(N-1), max).
public final class Backoff {

    private final Duration base;
    private final Duration max;
    private final double multiplier;

    public Backoff(Duration base, Duration max, double multiplier) {
        this.base = base;
        this.max = max;
        this.multiplier = multiplier;
    }

    public static Backoff defaults() {
        return new Backoff(Duration.ofMillis(500), Duration.ofSeconds(30), 2.0);
    }

    /// @param attempt 1-based attempt number.
    public Duration delayFor(int attempt) {
        double raw = base.toMillis() * Math.pow(multiplier, attempt - 1);
        long capped = (long) Math.min(raw, max.toMillis());
        long half = Math.max(1, capped / 2);
        long jitter = ThreadLocalRandom.current().nextLong(half + 1);
        return Duration.ofMillis(half + jitter);
    }
}
