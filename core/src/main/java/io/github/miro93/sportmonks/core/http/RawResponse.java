package io.github.miro93.sportmonks.core.http;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/// Raw, undecoded HTTP response. The body is kept as raw bytes so the JSON codec parses
/// straight from them, with no intermediate `String` for big payloads (e.g. livescores).
/// Header lookup is case-insensitive.
public record RawResponse(int status, byte[] body, Map<String, List<String>> headers) {

    public Optional<String> header(String name) {
        return headers.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(name))
                .flatMap(entry -> entry.getValue().stream())
                .findFirst();
    }

    /// The {@code Retry-After} header as a delay; empty when absent or not a whole number of
    /// seconds (the HTTP-date form is not supported).
    public Optional<Duration> retryAfter() {
        return header("Retry-After").flatMap(value -> {
            try {
                return Optional.of(Duration.ofSeconds(Long.parseLong(value.trim())));
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        });
    }

    public boolean isSuccessful() {
        return status >= 200 && status < 300;
    }

    /// The body decoded as a UTF-8 string. Used for human-readable error messages; the
    /// success path decodes directly from the raw bytes.
    public String bodyAsString() {
        return new String(body, StandardCharsets.UTF_8);
    }
}
