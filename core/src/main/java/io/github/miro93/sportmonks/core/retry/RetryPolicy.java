package io.github.miro93.sportmonks.core.retry;

import java.util.Objects;
import java.util.Set;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/// How many attempts to make and which statuses are retryable. Any 5xx and 429
/// are retried by default. Build a custom policy via {@link #builder()}.
public final class RetryPolicy {

    private static final IntPredicate DEFAULT_RETRYABLE = status -> status == 429 || status >= 500;

    private final int maxAttempts;
    private final Backoff backoff;
    private final IntPredicate retryableStatus;

    public RetryPolicy(int maxAttempts, Backoff backoff) {
        this(maxAttempts, backoff, DEFAULT_RETRYABLE);
    }

    public RetryPolicy(int maxAttempts, Backoff backoff, IntPredicate retryableStatus) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }
        this.maxAttempts = maxAttempts;
        this.backoff = Objects.requireNonNull(backoff, "backoff");
        this.retryableStatus = Objects.requireNonNull(retryableStatus, "retryableStatus");
    }

    public static RetryPolicy defaults() {
        return new RetryPolicy(3, Backoff.defaults());
    }

    public static RetryPolicy none() {
        return new RetryPolicy(1, Backoff.defaults());
    }

    /// @return a new builder (defaults: 3 attempts, default backoff, retry 429 + all 5xx)
    public static Builder builder() {
        return new Builder();
    }

    public boolean isRetryableStatus(int status) {
        return retryableStatus.test(status);
    }

    public int maxAttempts() {
        return maxAttempts;
    }

    public Backoff backoff() {
        return backoff;
    }

    /// Fluent builder for {@link RetryPolicy}.
    public static final class Builder {
        private int maxAttempts = 3;
        private Backoff backoff = Backoff.defaults();
        private IntPredicate retryableStatus = DEFAULT_RETRYABLE;

        private Builder() {
        }

        /// @param maxAttempts total attempts (must be >= 1, validated at {@link #build()})
        /// @return this builder
        public Builder maxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
            return this;
        }

        /// @param backoff the backoff strategy (non-null)
        /// @return this builder
        public Builder backoff(Backoff backoff) {
            this.backoff = Objects.requireNonNull(backoff, "backoff");
            return this;
        }

        /// Replaces the retryable-status rule with exact membership of the given codes.
        /// Overrides the default (429 + all 5xx) entirely.
        ///
        /// @param statuses the HTTP status codes to treat as retryable
        /// @return this builder
        public Builder retryableStatuses(int... statuses) {
            Set<Integer> set = IntStream.of(statuses).boxed().collect(Collectors.toUnmodifiableSet());
            this.retryableStatus = set::contains;
            return this;
        }

        /// Builds the configured {@link RetryPolicy}.
        ///
        /// @return a new {@code RetryPolicy}
        /// @throws IllegalArgumentException if {@code maxAttempts} is less than 1
        public RetryPolicy build() {
            return new RetryPolicy(maxAttempts, backoff, retryableStatus);
        }
    }
}
