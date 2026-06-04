package io.github.miro93.sportmonks.football.model;

/// A single premium betting odd from the SportMonks Premium Odds Feed. Mirrors
/// {@link Odd} but, per the API, premium odds have no {@code winning} or
/// {@code original_label} fields. {@code id} is always present; every other
/// field may be {@code null}. The numeric-looking fields — {@code value},
/// {@code probability}, {@code dp3}, {@code fractional}, {@code american},
/// {@code total} and {@code handicap} — are {@code String} because the API
/// returns them as strings (e.g. {@code "1.48"}, {@code "67.57%"}).
public record PremiumOdd(
        long id,
        Long fixtureId,
        Long marketId,
        Long bookmakerId,
        String label,
        String value,
        String name,
        String marketDescription,
        String probability,
        String dp3,
        String fractional,
        String american,
        Boolean stopped,
        String total,
        String handicap,
        String participants,
        String latestBookmakerUpdate) {
}
