package io.github.miro93.sportmonks.core.error;

/// Base type for every SportMonks client error. {@code statusCode} is the HTTP
/// status that produced the error, or {@code -1} for transport/I-O failures.
public sealed abstract class SportmonksException extends RuntimeException
        permits AuthenticationException, NotFoundException, RateLimitException,
        ValidationException, ServerException, TransportException {

    private final int statusCode;

    protected SportmonksException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    protected SportmonksException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int statusCode() {
        return statusCode;
    }
}
