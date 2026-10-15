package io.github.miro93.sportmonks.core.retry.internal;

import io.github.miro93.sportmonks.core.error.TransportException;
import io.github.miro93.sportmonks.core.http.HttpTransport;
import io.github.miro93.sportmonks.core.http.RawResponse;
import io.github.miro93.sportmonks.core.retry.RetryPolicy;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

/// Decorates an {@link HttpTransport} with retry on 429/5xx and on
/// {@link TransportException}, honoring a numeric {@code Retry-After} header when present
/// (an unparsable one falls back to the backoff).
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
                int current = attempt;
                sleeper.sleep(response.retryAfter().orElseGet(() -> policy.backoff().delayFor(current)));
                attempt++;
                continue;
            }
            return response;
        }
    }
}
