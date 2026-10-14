package io.github.miro93.sportmonks.football.model;

import io.helidon.json.binding.Json;

/// A single in-match event (goal, card, substitution, ...). {@code id} is always
/// present; every other field may be {@code null}. {@code rescinded} is only set on
/// card events and tells whether the referee rescinded the card.
@Json.Entity
public record Event(
        long id,
        @Json.Property("fixture_id") Long fixtureId,
        @Json.Property("period_id") Long periodId,
        @Json.Property("participant_id") Long participantId,
        @Json.Property("type_id") Long typeId,
        @Json.Property("sub_type_id") Long subTypeId,
        @Json.Property("player_id") Long playerId,
        @Json.Property("related_player_id") Long relatedPlayerId,
        @Json.Property("player_name") String playerName,
        @Json.Property("related_player_name") String relatedPlayerName,
        String result,
        String info,
        String addition,
        Integer minute,
        @Json.Property("extra_minute") Integer extraMinute,
        Boolean injured,
        @Json.Property("on_bench") Boolean onBench,
        @Json.Property("coach_id") Long coachId,
        @Json.Property("sort_order") Integer sortOrder,
        Boolean rescinded) {
}
