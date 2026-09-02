package io.github.miro93.sportmonks.core.http;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/// Raw, undecoded HTTP response. The body is kept as raw bytes so the JSON codec's envelope
/// parse can read straight from the byte stream, avoiding a large intermediate `String`
/// allocation for big payloads (e.g. livescores). The envelope's `data` subtree is still
/// re-serialized to a `String` once per decode before being parsed into its target type — see
/// {@code HelidonJsonCodec}'s class javadoc. Header lookup is case-insensitive.
public record RawResponse(int status, byte[] body, Map<String, List<String>> headers) {

    public Optional<String> header(String name) {
        return headers.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(name))
                .flatMap(entry -> entry.getValue().stream())
                .findFirst();
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
