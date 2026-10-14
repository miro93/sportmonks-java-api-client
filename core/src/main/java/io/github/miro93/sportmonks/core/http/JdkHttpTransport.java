package io.github.miro93.sportmonks.core.http;

import io.github.miro93.sportmonks.core.error.TransportException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.zip.GZIPInputStream;

/// {@link HttpTransport} backed by the JDK {@link HttpClient}.
public final class JdkHttpTransport implements HttpTransport {

    /// Default connection-establishment timeout for the built-in {@link HttpClient}.
    /// Override it via the client builder's {@code connectTimeout(Duration)} / {@code requestTimeout(Duration)}.
    public static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(10);

    /// Default request→response deadline applied per request (see {@link HttpRequest#timeout}).
    /// Override it via the client builder's {@code connectTimeout(Duration)} / {@code requestTimeout(Duration)}.
    public static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(30);

    /// Builds the default {@link HttpClient} using {@link #DEFAULT_CONNECT_TIMEOUT}.
    /// See {@link #newDefaultClient(Duration)} for the configuration details.
    public static HttpClient newDefaultClient() {
        return newDefaultClient(DEFAULT_CONNECT_TIMEOUT);
    }

    /// Builds the default {@link HttpClient} with the given connect timeout: NORMAL redirect
    /// following and the JDK-default HTTP version (HTTP/2 with HTTP/1.1 fallback). Connection
    /// pooling is automatic (keep-alive + HTTP/2 multiplexing) and not builder-tunable.
    ///
    /// @param connectTimeout the connection-establishment timeout
    /// @return a new default HTTP client
    public static HttpClient newDefaultClient(Duration connectTimeout) {
        Objects.requireNonNull(connectTimeout, "connectTimeout");
        return HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    /// Largest response body accepted by default (32 MiB); larger ones fail with a
    /// {@link TransportException} instead of being buffered in memory.
    public static final long DEFAULT_MAX_RESPONSE_SIZE = 32L * 1024 * 1024;

    private final HttpClient client;
    private final Duration requestTimeout;
    private final long maxResponseSize;

    public JdkHttpTransport(HttpClient client, Duration requestTimeout) {
        this(client, requestTimeout, DEFAULT_MAX_RESPONSE_SIZE);
    }

    /// @param maxResponseSize the largest body (in bytes) read into memory; must be positive
    public JdkHttpTransport(HttpClient client, Duration requestTimeout, long maxResponseSize) {
        this.client = Objects.requireNonNull(client, "client");
        this.requestTimeout = Objects.requireNonNull(requestTimeout, "requestTimeout");
        if (maxResponseSize <= 0) {
            throw new IllegalArgumentException("maxResponseSize must be positive, got " + maxResponseSize);
        }
        this.maxResponseSize = maxResponseSize;
    }

    @Override
    public RawResponse get(URI uri, Map<String, String> headers) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri).GET().timeout(requestTimeout)
                .header("Accept-Encoding", "gzip");
        headers.forEach(builder::header);
        HttpRequest request = builder.build();
        try {
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            byte[] body;
            boolean gzipped = response.headers().firstValue("Content-Encoding")
                    .map(v -> v.trim().equalsIgnoreCase("gzip")).orElse(false);
            try (InputStream raw = response.body(); InputStream in = gzipped ? new GZIPInputStream(raw) : raw) {
                body = readBounded(in, uri);
            }
            return new RawResponse(response.statusCode(), body, response.headers().map());
        } catch (IOException e) {
            throw new TransportException("HTTP request failed: " + uri, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TransportException("HTTP request interrupted: " + uri, e);
        }
    }

    /// Reads the whole (decompressed) stream, failing once it exceeds {@code maxResponseSize}.
    private byte[] readBounded(InputStream in, URI uri) throws IOException {
        byte[] bytes = in.readNBytes(Math.toIntExact(Math.min(maxResponseSize, Integer.MAX_VALUE - 8)) + 1);
        if (bytes.length > maxResponseSize) {
            throw new TransportException("Response body exceeds " + maxResponseSize + " bytes: " + uri, null);
        }
        return bytes;
    }
}
