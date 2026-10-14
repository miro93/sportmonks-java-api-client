package io.github.miro93.sportmonks.core.cache;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/// Bounded LRU cache of response bodies with per-entry expiry. Entries are evicted when the
/// entry count exceeds {@code maxEntries} (least recently used first) or when read after
/// their TTL. Synchronized: one lock, plenty for a client-side reference-data cache.
///
/// ponytail: a single lock and no background sweeper; expired entries are dropped lazily
/// on read or evicted by size. Plug in Caffeine through {@link ResponseCache} if contention
/// or memory precision ever matters.
public final class InMemoryResponseCache implements ResponseCache {

    private record Entry(byte[] body, Instant expiresAt) {
    }

    private final Map<URI, Entry> entries;
    private final Clock clock;

    /// @param maxEntries the maximum number of cached responses (positive)
    public InMemoryResponseCache(int maxEntries) {
        this(maxEntries, Clock.systemUTC());
    }

    InMemoryResponseCache(int maxEntries, Clock clock) {
        if (maxEntries <= 0) {
            throw new IllegalArgumentException("maxEntries must be positive, got " + maxEntries);
        }
        this.clock = Objects.requireNonNull(clock, "clock");
        this.entries = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<URI, Entry> eldest) {
                return size() > maxEntries;
            }
        };
    }

    @Override
    public synchronized Optional<byte[]> get(URI uri) {
        Entry entry = entries.get(uri);
        if (entry == null) {
            return Optional.empty();
        }
        if (!clock.instant().isBefore(entry.expiresAt())) {
            entries.remove(uri);
            return Optional.empty();
        }
        return Optional.of(entry.body());
    }

    @Override
    public synchronized void put(URI uri, byte[] body, Duration ttl) {
        entries.put(uri, new Entry(body, clock.instant().plus(ttl)));
    }

    /// @return the number of entries currently held (expired ones included until touched)
    public synchronized int size() {
        return entries.size();
    }
}
