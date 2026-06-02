package io.github.miro93.sportmonks.core.retry;

/// How many attempts to make and which statuses are retryable. Any 5xx and 429
/// are retried by default.
public final class RetryPolicy {

    private final int maxAttempts;
    private final Backoff backoff;

    public RetryPolicy(int maxAttempts, Backoff backoff) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }
        this.maxAttempts = maxAttempts;
        this.backoff = backoff;
    }

    public static RetryPolicy defaults() {
        return new RetryPolicy(3, Backoff.defaults());
    }

    public static RetryPolicy none() {
        return new RetryPolicy(1, Backoff.defaults());
    }

    public boolean isRetryableStatus(int status) {
        return status == 429 || status >= 500;
    }

    public int maxAttempts() {
        return maxAttempts;
    }

    public Backoff backoff() {
        return backoff;
    }
}
