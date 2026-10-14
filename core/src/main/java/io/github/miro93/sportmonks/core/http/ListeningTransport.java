package io.github.miro93.sportmonks.core.http;

import io.github.miro93.sportmonks.core.request.RequestListener;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/// Decorates an {@link HttpTransport} to report every attempt to a {@link RequestListener}.
/// Sits below the retrying transport so each retry is observed separately.
public final class ListeningTransport implements HttpTransport {

    private final HttpTransport delegate;
    private final RequestListener listener;

    public ListeningTransport(HttpTransport delegate, RequestListener listener) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.listener = Objects.requireNonNull(listener, "listener");
    }

    @Override
    public RawResponse get(URI uri, Map<String, String> headers) {
        listener.onRequest(uri);
        long start = System.nanoTime();
        try {
            RawResponse response = delegate.get(uri, headers);
            listener.onResponse(uri, response.status(), Duration.ofNanos(System.nanoTime() - start));
            return response;
        } catch (RuntimeException e) {
            listener.onError(uri, e, Duration.ofNanos(System.nanoTime() - start));
            throw e;
        }
    }
}
