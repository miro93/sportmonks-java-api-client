package io.github.miro93.sportmonks.core.error;

public final class ValidationException extends SportmonksException {
    public ValidationException(String message, int statusCode) {
        super(message, statusCode);
    }
}
