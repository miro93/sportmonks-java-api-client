package io.github.miro93.sportmonks.core.error;

public final class NotFoundException extends SportmonksException {
    public NotFoundException(String message, int statusCode) {
        super(message, statusCode);
    }
}
