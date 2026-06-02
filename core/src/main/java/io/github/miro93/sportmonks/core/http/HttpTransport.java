package io.github.miro93.sportmonks.core.http;

import java.net.URI;
import java.util.Map;

/// Performs raw HTTP GETs. Implementations throw
/// {@link io.github.miro93.sportmonks.core.error.TransportException} on I/O failure.
public interface HttpTransport {

    RawResponse get(URI uri, Map<String, String> headers);
}
