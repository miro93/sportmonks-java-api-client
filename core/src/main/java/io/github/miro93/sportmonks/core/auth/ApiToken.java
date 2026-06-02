package io.github.miro93.sportmonks.core.auth;

import java.util.function.UnaryOperator;

/// Holds a SportMonks API token. Sent on requests as the {@code Authorization} header value.
public final class ApiToken {

    private final String value;

    private ApiToken(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("API token must not be blank");
        }
        this.value = value;
    }

    public static ApiToken of(String value) {
        return new ApiToken(value);
    }

    /// Reads {@code SPORTMONKS_API_TOKEN} from the process environment.
    public static ApiToken fromEnv() {
        return from("SPORTMONKS_API_TOKEN", System::getenv);
    }

    /// Package-visible seam for testing without touching the real environment.
    static ApiToken from(String variableName, UnaryOperator<String> lookup) {
        String resolved = lookup.apply(variableName);
        if (resolved == null || resolved.isBlank()) {
            throw new IllegalStateException("Environment variable " + variableName + " is not set");
        }
        return of(resolved);
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return "ApiToken{****}";
    }
}
