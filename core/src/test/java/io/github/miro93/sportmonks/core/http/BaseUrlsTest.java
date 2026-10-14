package io.github.miro93.sportmonks.core.http;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BaseUrlsTest {

    @Test
    void acceptsHttpsAnywhereAndHttpOnLoopback() {
        assertThat(BaseUrls.requireSecure("https://api.sportmonks.com/v3/football")).isEqualTo("https://api.sportmonks.com/v3/football");
        assertThat(BaseUrls.requireSecure("http://localhost:8080")).isEqualTo("http://localhost:8080");
        assertThat(BaseUrls.requireSecure("http://127.0.0.1:8080/football")).isNotNull();
        assertThat(BaseUrls.requireSecure("HTTP://LocalHost")).isNotNull();
    }

    @Test
    void rejectsPlainHttpElsewhereAndGarbage() {
        assertThatThrownBy(() -> BaseUrls.requireSecure("http://api.sportmonks.com/v3/football"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("https");
        assertThatThrownBy(() -> BaseUrls.requireSecure("http://10.0.0.5/v3")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> BaseUrls.requireSecure("ftp://localhost")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> BaseUrls.requireSecure("api.sportmonks.com")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> BaseUrls.requireSecure("http://[bad")).isInstanceOf(IllegalArgumentException.class);
    }
}
