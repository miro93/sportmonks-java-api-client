package io.github.miro93.sportmonks.football.model;

import io.helidon.json.binding.Json;

/// The id a bookmaker uses for a SportMonks fixture. Every field may be {@code null}.
@Json.Entity
public record BookmakerMapping(
        @Json.Property("fixture_id") Long fixtureId,
        @Json.Property("bookmaker_id") Long bookmakerId,
        @Json.Property("bookmaker_name") String bookmakerName,
        @Json.Property("bookmaker_event_id") String bookmakerEventId,
        @Json.Property("bookmaker_event_url") String bookmakerEventUrl) {
}
