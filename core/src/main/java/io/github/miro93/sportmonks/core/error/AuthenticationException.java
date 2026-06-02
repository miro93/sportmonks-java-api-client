package io.github.miro93.sportmonks.core.error;

public final class AuthenticationException extends SportmonksException {
    public AuthenticationException(String message, int statusCode) {
        super(message, statusCode);
    }
}
