package io.github.miro93.sportmonks.core.http;

import io.github.miro93.sportmonks.core.error.TransportException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/// {@link HttpTransport} backed by the JDK {@link HttpClient}.
public final class JdkHttpTransport implements HttpTransport {

    /// Default connection-establishment timeout for the built-in {@link HttpClient}.
    /// Hard-coded for now; a later ticket may externalise this to a property.
    public static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(10);

    /// Default request→response deadline applied per request (see {@link HttpRequest#timeout}).
    /// Hard-coded for now; a later ticket may externalise this to a property.
    public static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(30);

    /// Builds the default {@link HttpClient}: an explicit connect timeout and NORMAL redirect
    /// following. The HTTP version is left at the JDK default (HTTP/2 with HTTP/1.1 fallback);
    /// connection pooling is automatic (keep-alive + HTTP/2 multiplexing) and not builder-tunable.
    public static HttpClient newDefaultClient() {
        return HttpClient.newBuilder()
                .connectTimeout(DEFAULT_CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    private final HttpClient client;
    private final Duration requestTimeout;

    public JdkHttpTransport(HttpClient client, Duration requestTimeout) {
        this.client = Objects.requireNonNull(client, "client");
        this.requestTimeout = Objects.requireNonNull(requestTimeout, "requestTimeout");
    }

    @Override
    public RawResponse get(URI uri, Map<String, String> headers) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri).GET().timeout(requestTimeout);
        headers.forEach(builder::header);
        HttpRequest request = builder.build();
        try {
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            return new RawResponse(response.statusCode(), response.body(), response.headers().map());
        } catch (IOException e) {
            throw new TransportException("HTTP request failed: " + uri, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TransportException("HTTP request interrupted: " + uri, e);
        }
    }
}
