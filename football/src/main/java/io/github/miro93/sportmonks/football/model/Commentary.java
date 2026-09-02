package io.github.miro93.sportmonks.football.model;

import io.helidon.json.binding.Json;

/// A textual match commentary line from the SportMonks football API. Unlike
/// every other resource in this client, {@code id} is a {@code String} (the API
/// types the commentary id as a string). Aside from {@code id}, every field may
/// be {@code null}: {@code fixtureId}, {@code comment}, {@code minute},
/// {@code extraMinute}, {@code isGoal}, {@code isImportant} and {@code order}.
@Json.Entity
public record Commentary(
        String id,
        @Json.Property("fixture_id") Long fixtureId,
        String comment,
        Integer minute,
        @Json.Property("extra_minute") Integer extraMinute,
        @Json.Property("is_goal") Boolean isGoal,
        @Json.Property("is_important") Boolean isImportant,
        Integer order) {
}
