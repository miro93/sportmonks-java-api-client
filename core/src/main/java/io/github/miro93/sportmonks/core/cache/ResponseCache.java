package io.github.miro93.sportmonks.core.cache;

import java.net.URI;
import java.time.Duration;
import java.util.Optional;

/// Stores successful response bodies by request URI for a bounded time. Implementations
/// must be thread-safe. {@link InMemoryResponseCache} is the built-in one; back it with
/// Caffeine, Redis, ... by implementing these two methods.
public interface ResponseCache {

    /// @return the cached body for {@code uri} if present and not expired
    Optional<byte[]> get(URI uri);

    /// Stores {@code body} for {@code uri} until {@code ttl} elapses.
    void put(URI uri, byte[] body, Duration ttl);
}
