package io.github.miro93.sportmonks.football.model;

import io.helidon.json.binding.Json;

/// A {@link Bracket} edge: the {@code parentOutcome} ({@code "winner"} or {@code "loser"})
/// of {@code parentFixtureId} takes the {@code childSlot} ({@code "home"} or {@code "away"})
/// of {@code childFixtureId}.
@Json.Entity
public record BracketEdge(
        long id,
        @Json.Property("season_id") Long seasonId,
        @Json.Property("child_fixture_id") Long childFixtureId,
        @Json.Property("child_slot") String childSlot,
        @Json.Property("parent_fixture_id") Long parentFixtureId,
        @Json.Property("parent_outcome") String parentOutcome) {
}
