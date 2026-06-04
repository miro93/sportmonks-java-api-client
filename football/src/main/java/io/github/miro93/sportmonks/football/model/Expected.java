package io.github.miro93.sportmonks.football.model;

import java.util.Map;

/// An expected-goals (xG) record from the SportMonks expected endpoints
/// ({@code /expected/fixtures} for team-level, {@code /expected/lineups} for
/// player-level). {@code id} is always present; every other field may be
/// {@code null}. {@code location} is {@code "home"} or {@code "away"}.
/// {@code data} is a free-form object (typically {@code {value}}) whose shape
/// depends on {@code typeId}, exposed as a raw {@code Map<String, Object>};
/// numeric values decode as {@code Double}/{@code Integer}.
public record Expected(
        long id,
        Long fixtureId,
        Long typeId,
        Long participantId,
        String location,
        Map<String, Object> data) {
}
