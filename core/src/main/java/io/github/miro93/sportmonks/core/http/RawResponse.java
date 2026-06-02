package io.github.miro93.sportmonks.core.http;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/// Raw, undecoded HTTP response. Header lookup is case-insensitive.
public record RawResponse(int status, String body, Map<String, List<String>> headers) {

    public Optional<String> header(String name) {
        return headers.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(name))
                .flatMap(entry -> entry.getValue().stream())
                .findFirst();
    }

    public boolean isSuccessful() {
        return status >= 200 && status < 300;
    }
}
