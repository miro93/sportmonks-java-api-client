package io.github.miro93.sportmonks.core.error;

import java.time.Duration;
import java.util.Optional;

public final class RateLimitException extends SportmonksException {

    private final Duration retryAfter;

    public RateLimitException(String message, int statusCode, Duration retryAfter) {
        super(message, statusCode);
        this.retryAfter = retryAfter;
    }

    public Optional<Duration> retryAfter() {
        return Optional.ofNullable(retryAfter);
    }
}
