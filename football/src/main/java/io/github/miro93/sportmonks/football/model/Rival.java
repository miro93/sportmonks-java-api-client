package io.github.miro93.sportmonks.football.model;

import io.helidon.json.binding.Json;

/// A rivalry between two teams. Every scalar may be {@code null}; the relation
/// fields ({@code team}, {@code rival}) are {@code null} unless requested via includes.
@Json.Entity
public record Rival(
        Long id,
        @Json.Property("sport_id") Long sportId,
        @Json.Property("team_id") Long teamId,
        @Json.Property("rival_id") Long rivalId,
        Team team,
        Team rival) {
}
