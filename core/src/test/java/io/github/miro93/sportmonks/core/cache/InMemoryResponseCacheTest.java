package io.github.miro93.sportmonks.core.cache;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryResponseCacheTest {

    private Instant now = Instant.parse("2026-10-14T21:00:00Z");
    private final Clock clock = new Clock() {
        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    };

    @Test
    void expiresEntriesAfterTheirTtl() {
        var cache = new InMemoryResponseCache(10, clock);
        URI uri = URI.create("https://api/types");
        cache.put(uri, new byte[] {1}, Duration.ofMinutes(5));

        assertThat(cache.get(uri)).isPresent();
        now = now.plus(Duration.ofMinutes(5));
        assertThat(cache.get(uri)).isEmpty();
        assertThat(cache.size()).isZero();
    }

    @Test
    void evictsLeastRecentlyUsedBeyondMaxEntries() {
        var cache = new InMemoryResponseCache(2, clock);
        URI a = URI.create("https://api/a"), b = URI.create("https://api/b"), c = URI.create("https://api/c");
        cache.put(a, new byte[] {1}, Duration.ofHours(1));
        cache.put(b, new byte[] {2}, Duration.ofHours(1));
        cache.get(a);                       // a is now more recent than b
        cache.put(c, new byte[] {3}, Duration.ofHours(1));

        assertThat(cache.get(b)).isEmpty();
        assertThat(cache.get(a)).isPresent();
        assertThat(cache.get(c)).isPresent();
    }

    @Test
    void policyValidatesInputs() {
        assertThatThrownBy(() -> new InMemoryResponseCache(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CachePolicy.inMemory(10, Map.of("types", Duration.ZERO))).isInstanceOf(IllegalArgumentException.class);
        assertThat(CachePolicy.inMemory(10, Map.of("types", Duration.ofHours(1))).ttlFor("fixtures")).isNull();
        assertThat(CachePolicy.NONE.ttlFor("types")).isNull();
    }
}
