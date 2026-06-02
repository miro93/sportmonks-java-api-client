package io.github.miro93.sportmonks.core.retry;

import io.github.miro93.sportmonks.core.error.TransportException;

import java.time.Duration;

/// Abstracts thread sleeping so retry timing is testable.
@FunctionalInterface
public interface Sleeper {

    void sleep(Duration duration);

    Sleeper REAL = duration -> {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TransportException("Interrupted during backoff", e);
        }
    };
}
