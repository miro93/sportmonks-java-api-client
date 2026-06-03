package io.github.miro93.sportmonks.football.model;

/// A textual match commentary line from the SportMonks football API. Unlike
/// every other resource in this client, {@code id} is a {@code String} (the API
/// types the commentary id as a string). Aside from {@code id}, every field may
/// be {@code null}: {@code fixtureId}, {@code comment}, {@code minute},
/// {@code extraMinute}, {@code isGoal}, {@code isImportant} and {@code order}.
public record Commentary(
        String id,
        Long fixtureId,
        String comment,
        Integer minute,
        Integer extraMinute,
        Boolean isGoal,
        Boolean isImportant,
        Integer order) {
}
