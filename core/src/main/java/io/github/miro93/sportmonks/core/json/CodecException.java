package io.github.miro93.sportmonks.core.json;

/// Thrown when a response body cannot be parsed into the expected shape.
/// Internal to the codec layer; {@code ApiExecutor} converts it into a
/// {@code ServerException} carrying the real HTTP status.
public final class CodecException extends RuntimeException {
    public CodecException(String message, Throwable cause) {
        super(message, cause);
    }
}
