package io.github.miro93.sportmonks.football.model;

/// A historical premium odd record from the SportMonks Premium Odds Feed
/// (entity {@code PremiumOddHistory}): one snapshot of a premium odd's value
/// over time. {@code id} is always present; every other field may be
/// {@code null}. {@code oddId} references the parent premium odd. The
/// numeric-looking fields — {@code value}, {@code probability}, {@code dp3},
/// {@code fractional} and {@code american} — are {@code String} because the API
/// returns them as strings.
public record HistoricalOdd(
        long id,
        Long oddId,
        String value,
        String probability,
        String dp3,
        String fractional,
        String american,
        String bookmakerUpdate) {
}
