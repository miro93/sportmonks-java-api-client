package io.github.miro93.sportmonks.core.error;

import java.io.Serial;

public final class AuthenticationException extends SportmonksException {

    @Serial
    private static final long serialVersionUID = 1L;

    public AuthenticationException(String message, int statusCode) {
        super(message, statusCode);
    }
}
