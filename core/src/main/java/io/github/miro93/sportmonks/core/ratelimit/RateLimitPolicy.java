package io.github.miro93.sportmonks.core.ratelimit;

/// Client-side guard against exhausting the SportMonks quota (3000 requests per hour per
/// entity). When the last response seen for an entity reports fewer than {@code threshold}
/// remaining requests, the next request to that entity either waits until the window resets
/// or fails fast with a {@link io.github.miro93.sportmonks.core.error.RateLimitException}.
/// {@link #NONE} (the default) never intervenes.
///
/// @param threshold the guard triggers when {@code remaining < threshold}
/// @param action    what to do when it triggers
public record RateLimitPolicy(int threshold, Action action) {

    /// What to do when the remaining quota drops below the threshold.
    public enum Action {
        /// Sleep until the window resets, then send the request.
        WAIT,
        /// Throw a {@link io.github.miro93.sportmonks.core.error.RateLimitException} without sending.
        FAIL
    }

    /// Never intervene.
    public static final RateLimitPolicy NONE = new RateLimitPolicy(0, Action.FAIL);

    public RateLimitPolicy {
        if (threshold < 0) {
            throw new IllegalArgumentException("threshold must be >= 0, got " + threshold);
        }
    }

    /// Wait for the reset once fewer than {@code remaining} requests are left.
    public static RateLimitPolicy waitBelow(int remaining) {
        return new RateLimitPolicy(remaining, Action.WAIT);
    }

    /// Fail fast once fewer than {@code remaining} requests are left.
    public static RateLimitPolicy failBelow(int remaining) {
        return new RateLimitPolicy(remaining, Action.FAIL);
    }

    /// Whether this policy guards at all.
    public boolean isActive() {
        return threshold > 0;
    }
}
