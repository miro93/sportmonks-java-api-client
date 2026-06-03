package io.github.miro93.sportmonks.football.model;

/// A single betting odd from the SportMonks football API (shared by the
/// pre-match and in-play feeds). {@code id} is always present; every other field
/// may be {@code null}. The numeric-looking fields — {@code value},
/// {@code probability}, {@code dp3}, {@code fractional}, {@code american},
/// {@code total} and {@code handicap} — are {@code String} because the API
/// returns them as strings (e.g. {@code "1.48"}, {@code "67.57%"}).
public record Odd(
        long id,
        Long fixtureId,
        Long marketId,
        Long bookmakerId,
        String label,
        String value,
        String name,
        Integer sortOrder,
        String marketDescription,
        String probability,
        String dp3,
        String fractional,
        String american,
        Boolean winning,
        Boolean stopped,
        String total,
        String handicap,
        String participants,
        String createdAt,
        String updatedAt,
        String originalLabel,
        String latestBookmakerUpdate) {
}
