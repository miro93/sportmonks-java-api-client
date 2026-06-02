package io.github.miro93.sportmonks.core.error;

public final class TransportException extends SportmonksException {
    public TransportException(String message, Throwable cause) {
        super(message, -1, cause);
    }
}
