package io.github.miro93.sportmonks.football.model;

import io.helidon.json.binding.Json;

/// A daily team ranking entry (beta). {@code id} is always present; every other scalar
/// may be {@code null}. {@code scaledScore} is normalised to 0-100. The relation field
/// ({@code team}) is {@code null} unless requested via includes.
@Json.Entity
public record TeamRanking(
        long id,
        @Json.Property("team_id") Long teamId,
        String date,
        @Json.Property("current_rank") Integer currentRank,
        @Json.Property("scaled_score") Double scaledScore,
        Team team) {
}
