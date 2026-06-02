package io.github.miro93.sportmonks.core.retry;

import io.github.miro93.sportmonks.core.error.TransportException;
import io.github.miro93.sportmonks.core.http.HttpTransport;
import io.github.miro93.sportmonks.core.http.RawResponse;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RetryingTransportTest {

    private final List<Duration> slept = new java.util.ArrayList<>();
    private final Sleeper recordingSleeper = slept::add;
    private final URI uri = URI.create("https://api.test/x");

    @Test
    void retriesOn503ThenReturnsSuccess() {
        Queue<RawResponse> responses = new ArrayDeque<>(List.of(
                new RawResponse(503, "down", Map.of()),
                new RawResponse(200, "ok", Map.of())));
        AtomicInteger calls = new AtomicInteger();
        HttpTransport delegate = (u, h) -> {
            calls.incrementAndGet();
            return responses.poll();
        };

        RetryingTransport transport = new RetryingTransport(delegate, RetryPolicy.defaults(), recordingSleeper);
        RawResponse response = transport.get(uri, Map.of());

        assertThat(response.status()).isEqualTo(200);
        assertThat(calls).hasValue(2);
        assertThat(slept).hasSize(1);
    }

    @Test
    void doesNotRetryOn404() {
        AtomicInteger calls = new AtomicInteger();
        HttpTransport delegate = (u, h) -> {
            calls.incrementAndGet();
            return new RawResponse(404, "nope", Map.of());
        };

        RawResponse response = new RetryingTransport(delegate, RetryPolicy.defaults(), recordingSleeper)
                .get(uri, Map.of());

        assertThat(response.status()).isEqualTo(404);
        assertThat(calls).hasValue(1);
        assertThat(slept).isEmpty();
    }

    @Test
    void honorsRetryAfterHeaderOn429() {
        Queue<RawResponse> responses = new ArrayDeque<>(List.of(
                new RawResponse(429, "slow", Map.of("Retry-After", List.of("7"))),
                new RawResponse(200, "ok", Map.of())));
        HttpTransport delegate = (u, h) -> responses.poll();

        new RetryingTransport(delegate, RetryPolicy.defaults(), recordingSleeper).get(uri, Map.of());

        assertThat(slept).containsExactly(Duration.ofSeconds(7));
    }

    @Test
    void exhaustsAttemptsAndReturnsLastFailure() {
        HttpTransport delegate = (u, h) -> new RawResponse(500, "boom", Map.of());

        RawResponse response = new RetryingTransport(delegate, RetryPolicy.defaults(), recordingSleeper)
                .get(uri, Map.of());

        assertThat(response.status()).isEqualTo(500);
        assertThat(slept).hasSize(2); // 3 attempts -> 2 sleeps
    }

    @Test
    void retriesTransportExceptionThenRethrowsAfterMaxAttempts() {
        HttpTransport delegate = (u, h) -> {
            throw new TransportException("conn refused", new RuntimeException());
        };

        assertThatThrownBy(() -> new RetryingTransport(delegate, RetryPolicy.defaults(), recordingSleeper)
                .get(uri, Map.of()))
                .isInstanceOf(TransportException.class);
        assertThat(slept).hasSize(2);
    }
}
