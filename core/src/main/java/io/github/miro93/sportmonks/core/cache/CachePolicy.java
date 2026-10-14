package io.github.miro93.sportmonks.core.cache;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/// Which entities to cache, for how long, and where. Entities are keyed by the first path
/// segment of the request ({@code "types"}, {@code "countries"}, {@code "leagues"}, ...);
/// an entity without a TTL is never cached. {@link #NONE} (the default) caches nothing.
///
/// @param cache the store
/// @param ttls  TTL per entity key
public record CachePolicy(ResponseCache cache, Map<String, Duration> ttls) {

    /// Cache nothing.
    public static final CachePolicy NONE = new CachePolicy(new ResponseCache() {
        @Override
        public java.util.Optional<byte[]> get(java.net.URI uri) {
            return java.util.Optional.empty();
        }

        @Override
        public void put(java.net.URI uri, byte[] body, Duration ttl) {
        }
    }, Map.of());

    public CachePolicy {
        Objects.requireNonNull(cache, "cache");
        ttls = Map.copyOf(ttls);
        ttls.forEach((k, v) -> {
            if (v.isNegative() || v.isZero()) {
                throw new IllegalArgumentException("TTL for '" + k + "' must be positive, got " + v);
            }
        });
    }

    /// An in-memory policy of at most {@code maxEntries} responses with the given TTLs.
    public static CachePolicy inMemory(int maxEntries, Map<String, Duration> ttls) {
        return new CachePolicy(new InMemoryResponseCache(maxEntries), ttls);
    }

    /// @return the TTL for {@code entity}, or {@code null} when it is not cached
    public Duration ttlFor(String entity) {
        return ttls.get(entity);
    }
}
