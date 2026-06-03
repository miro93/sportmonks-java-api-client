package io.github.miro93.sportmonks.core.error;

import java.io.Serial;

import java.time.Duration;
import java.util.Optional;

public final class RateLimitException extends SportmonksException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Duration retryAfter;

    public RateLimitException(String message, int statusCode, Duration retryAfter) {
        super(message, statusCode);
        this.retryAfter = retryAfter;
    }

    public Optional<Duration> retryAfter() {
        return Optional.ofNullable(retryAfter);
    }
}
