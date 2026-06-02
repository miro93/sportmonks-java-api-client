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
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return new RawResponse(response.statusCode(), response.body(), response.headers().map());
        } catch (IOException e) {
            throw new TransportException("HTTP request failed: " + uri, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TransportException("HTTP request interrupted: " + uri, e);
        }
    }
}
