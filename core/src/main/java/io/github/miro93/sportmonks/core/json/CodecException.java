package io.github.miro93.sportmonks.core.json;

import java.io.Serial;

/// Thrown when a response body cannot be parsed into the expected shape.
/// Internal to the codec layer; {@code ApiExecutor} converts it into a
/// {@code ServerException} carrying the real HTTP status.
public final class CodecException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public CodecException(String message, Throwable cause) {
        super(message, cause);
    }
}
