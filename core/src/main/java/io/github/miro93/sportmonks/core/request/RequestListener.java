package io.github.miro93.sportmonks.core.request;

import java.net.URI;
import java.time.Duration;

/// Observes every HTTP attempt the client makes (each retry is a separate event), for
/// logging or metrics. All methods have no-op defaults; implementations must be
/// thread-safe and must not throw.
public interface RequestListener {

    /// A listener that ignores every event.
    RequestListener NOOP = new RequestListener() {
    };

    /// Called before an attempt is sent.
    ///
    /// @param uri the full request URI (without the API token, which travels in a header)
    default void onRequest(URI uri) {
    }

    /// Called when an attempt got an HTTP response, whatever its status.
    ///
    /// @param uri     the request URI
    /// @param status  the HTTP status code
    /// @param elapsed time between send and response
    default void onResponse(URI uri, int status, Duration elapsed) {
    }

    /// Called when an attempt failed before any response (connect error, timeout, ...).
    ///
    /// @param uri     the request URI
    /// @param error   the failure
    /// @param elapsed time between send and failure
    default void onError(URI uri, Throwable error, Duration elapsed) {
    }
}
