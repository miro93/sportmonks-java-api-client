package io.github.miro93.sportmonks.core.error;

public final class ServerException extends SportmonksException {
    public ServerException(String message, int statusCode) {
        super(message, statusCode);
    }

    public ServerException(String message, int statusCode, Throwable cause) {
        super(message, statusCode, cause);
    }
}
