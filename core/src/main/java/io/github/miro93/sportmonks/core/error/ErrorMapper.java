package io.github.miro93.sportmonks.core.error;

import java.time.Duration;
import java.util.Optional;

/// Maps a non-2xx HTTP status + body into the matching {@link SportmonksException}.
public final class ErrorMapper {

    private ErrorMapper() {
    }

    public static SportmonksException fromResponse(int status, String body, Optional<Duration> retryAfter) {
        String message = "SportMonks API error " + status + ": " + snippet(body);
        return switch (status) {
            case 401, 403 -> new AuthenticationException(message, status);
            case 404 -> new NotFoundException(message, status);
            case 429 -> new RateLimitException(message, status, retryAfter.orElse(null));
            default -> {
                if (status >= 500) {
                    yield new ServerException(message, status);
                }
                yield new ValidationException(message, status);
            }
        };
    }

    private static String snippet(String body) {
        if (body == null) {
            return "";
        }
        return body.length() <= 500 ? body : body.substring(0, 500) + "…";
    }
}
