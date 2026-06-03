package io.github.miro93.sportmonks.core.error;

import java.io.Serial;

public final class TransportException extends SportmonksException {

    @Serial
    private static final long serialVersionUID = 1L;

    public TransportException(String message, Throwable cause) {
        super(message, -1, cause);
    }
}
