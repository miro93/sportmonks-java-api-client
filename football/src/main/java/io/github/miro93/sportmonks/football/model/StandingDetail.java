package io.github.miro93.sportmonks.football.model;

/// A single detail entry within a standing (e.g. goals scored, matches played).
/// {@code id} is always present. All other fields ({@code standingType},
/// {@code standingId}, {@code typeId}, {@code value}) may be {@code null}.
public record StandingDetail(
        long id,
        String standingType,
        Long standingId,
        Long typeId,
        Integer value) {
}
