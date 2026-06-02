package io.github.miro93.sportmonks.core.auth;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApiTokenTest {

    @Test
    void ofExposesTheValue() {
        ApiToken token = ApiToken.of("secret-123");
        assertThat(token.value()).isEqualTo("secret-123");
    }

    @Test
    void ofRejectsBlankToken() {
        assertThatThrownBy(() -> ApiToken.of("  "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void toStringDoesNotLeakTheValue() {
        ApiToken token = ApiToken.of("super-secret");
        assertThat(token.toString()).doesNotContain("super-secret");
    }

    @Test
    void fromLookupReadsNamedVariable() {
        ApiToken token = ApiToken.from("SPORTMONKS_API_TOKEN", name -> "env-token");
        assertThat(token.value()).isEqualTo("env-token");
    }

    @Test
    void fromLookupFailsWhenMissing() {
        assertThatThrownBy(() -> ApiToken.from("SPORTMONKS_API_TOKEN", name -> null))
                .isInstanceOf(IllegalStateException.class);
    }
}
