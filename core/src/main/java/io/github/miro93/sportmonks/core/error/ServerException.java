package io.github.miro93.sportmonks.core.error;

import java.io.Serial;

public final class ServerException extends SportmonksException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ServerException(String message, int statusCode) {
        super(message, statusCode);
    }

    public ServerException(String message, int statusCode, Throwable cause) {
        super(message, statusCode, cause);
    }
}
