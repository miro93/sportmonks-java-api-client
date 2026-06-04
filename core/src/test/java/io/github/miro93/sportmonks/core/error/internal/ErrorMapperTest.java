package io.github.miro93.sportmonks.core.error.internal;

import io.github.miro93.sportmonks.core.error.AuthenticationException;
import io.github.miro93.sportmonks.core.error.NotFoundException;
import io.github.miro93.sportmonks.core.error.RateLimitException;
import io.github.miro93.sportmonks.core.error.ServerException;
import io.github.miro93.sportmonks.core.error.SportmonksException;
import io.github.miro93.sportmonks.core.error.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorMapperTest {

    @Test
    void maps401ToAuthentication() {
        SportmonksException ex = ErrorMapper.fromResponse(401, "unauthorized", Optional.empty());
        assertThat(ex).isInstanceOf(AuthenticationException.class);
        assertThat(ex.statusCode()).isEqualTo(401);
    }

    @Test
    void maps404ToNotFound() {
        assertThat(ErrorMapper.fromResponse(404, "missing", Optional.empty()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void maps429ToRateLimitWithRetryAfter() {
        SportmonksException ex = ErrorMapper.fromResponse(429, "slow down",
                Optional.of(Duration.ofSeconds(30)));
        assertThat(ex).isInstanceOf(RateLimitException.class);
        assertThat(((RateLimitException) ex).retryAfter()).contains(Duration.ofSeconds(30));
    }

    @Test
    void maps422ToValidation() {
        assertThat(ErrorMapper.fromResponse(422, "bad", Optional.empty()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void maps503ToServer() {
        assertThat(ErrorMapper.fromResponse(503, "down", Optional.empty()))
                .isInstanceOf(ServerException.class);
    }
}
