package io.github.miro93.sportmonks.core.retry;

import io.github.miro93.sportmonks.core.error.TransportException;
import io.github.miro93.sportmonks.core.http.HttpTransport;
import io.github.miro93.sportmonks.core.http.RawResponse;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/// Decorates an {@link HttpTransport} with retry on 429/5xx and on
/// {@link TransportException}, honoring a {@code Retry-After} header when present.
public final class RetryingTransport implements HttpTransport {

    private final HttpTransport delegate;
    private final RetryPolicy policy;
    private final Sleeper sleeper;

    public RetryingTransport(HttpTransport delegate, RetryPolicy policy, Sleeper sleeper) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.policy = Objects.requireNonNull(policy, "policy");
        this.sleeper = Objects.requireNonNull(sleeper, "sleeper");
    }

    @Override
    public RawResponse get(URI uri, Map<String, String> headers) {
        int attempt = 1;
        while (true) {
            RawResponse response;
            try {
                response = delegate.get(uri, headers);
            } catch (TransportException e) {
                if (attempt >= policy.maxAttempts()) {
                    throw e;
                }
                sleeper.sleep(policy.backoff().delayFor(attempt));
                attempt++;
                continue;
            }

            if (policy.isRetryableStatus(response.status()) && attempt < policy.maxAttempts()) {
                sleeper.sleep(delayFor(response, attempt));
                attempt++;
                continue;
            }
            return response;
        }
    }

    private Duration delayFor(RawResponse response, int attempt) {
        return response.header("Retry-After")
                .map(RetryingTransport::parseSeconds)
                .orElseGet(() -> policy.backoff().delayFor(attempt));
    }

    private static Duration parseSeconds(String value) {
        try {
            return Duration.ofSeconds(Long.parseLong(value.trim()));
        } catch (NumberFormatException e) {
            return Duration.ZERO;
        }
    }
}
