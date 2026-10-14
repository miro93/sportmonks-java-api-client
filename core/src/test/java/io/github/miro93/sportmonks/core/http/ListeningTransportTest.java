package io.github.miro93.sportmonks.core.http;

import io.github.miro93.sportmonks.core.error.TransportException;
import io.github.miro93.sportmonks.core.request.RequestListener;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ListeningTransportTest {

    private final List<String> events = new ArrayList<>();

    private final RequestListener listener = new RequestListener() {
        @Override
        public void onRequest(URI uri) {
            events.add("request " + uri);
        }

        @Override
        public void onResponse(URI uri, int status, Duration elapsed) {
            events.add("response " + status + " " + (elapsed.isNegative() ? "neg" : "ok"));
        }

        @Override
        public void onError(URI uri, Throwable error, Duration elapsed) {
            events.add("error " + error.getClass().getSimpleName());
        }
    };

    @Test
    void reportsRequestAndResponse() {
        HttpTransport transport = new ListeningTransport(
                (uri, headers) -> new RawResponse(503, new byte[0], Map.of()), listener);

        RawResponse response = transport.get(URI.create("https://api/x"), Map.of());

        assertThat(response.status()).isEqualTo(503);
        assertThat(events).containsExactly("request https://api/x", "response 503 ok");
    }

    @Test
    void reportsErrorAndRethrows() {
        HttpTransport transport = new ListeningTransport((uri, headers) -> {
            throw new TransportException("boom", null);
        }, listener);

        assertThatThrownBy(() -> transport.get(URI.create("https://api/x"), Map.of()))
                .isInstanceOf(TransportException.class);
        assertThat(events).containsExactly("request https://api/x", "error TransportException");
    }
}
