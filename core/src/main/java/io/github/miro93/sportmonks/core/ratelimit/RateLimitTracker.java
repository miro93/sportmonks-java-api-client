package io.github.miro93.sportmonks.core.ratelimit;

import io.github.miro93.sportmonks.core.response.RateLimit;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/// Remembers the last {@code rate_limit} block seen per entity, keyed by the first path
/// segment of the request ({@code "fixtures"}, {@code "teams"}, ...), and forgets it once
/// its {@code resets_in_seconds} window has elapsed. Thread-safe; one instance is shared by
/// every executor of a client.
///
/// ponytail: the key is the path's first segment, not the response's {@code requested_entity},
/// because the guard must run before the request is sent; the two coincide for every
/// documented endpoint group.
public final class RateLimitTracker {

    private record Entry(RateLimit limit, Instant resetAt) {
    }

    private final Map<String, Entry> entries = new ConcurrentHashMap<>();
    private final Clock clock;

    public RateLimitTracker() {
        this(Clock.systemUTC());
    }

    RateLimitTracker(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /// Records the block of a response; {@code null} is ignored.
    public void record(String key, RateLimit limit) {
        if (limit != null) {
            entries.put(key, new Entry(limit, clock.instant().plusSeconds(limit.resetsInSeconds())));
        }
    }

    /// The last block seen for {@code key}, if its window has not reset yet.
    public Optional<RateLimit> latest(String key) {
        Entry entry = entries.get(key);
        if (entry == null) {
            return Optional.empty();
        }
        if (!clock.instant().isBefore(entry.resetAt())) {
            entries.remove(key, entry);
            return Optional.empty();
        }
        return Optional.of(entry.limit());
    }

    /// Time until the window of {@code key} resets; {@link Duration#ZERO} when unknown or reset.
    public Duration untilReset(String key) {
        Entry entry = entries.get(key);
        if (entry == null) {
            return Duration.ZERO;
        }
        Duration left = Duration.between(clock.instant(), entry.resetAt());
        return left.isNegative() ? Duration.ZERO : left;
    }

    /// Every entity whose window has not reset yet, with its last block.
    public Map<String, RateLimit> snapshot() {
        Instant now = clock.instant();
        return entries.entrySet().stream()
                .filter(e -> now.isBefore(e.getValue().resetAt()))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, e -> e.getValue().limit()));
    }
}
