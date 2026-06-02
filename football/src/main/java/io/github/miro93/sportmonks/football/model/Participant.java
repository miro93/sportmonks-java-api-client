package io.github.miro93.sportmonks.football.model;

/// A team taking part in a fixture. {@code meta} is present when the participant
/// is loaded through a fixture include.
public record Participant(
        long id,
        Long sportId,
        Long countryId,
        Long venueId,
        String gender,
        String name,
        String shortCode,
        String imagePath,
        Integer founded,
        String type,
        boolean placeholder,
        String lastPlayedAt,
        ParticipantMeta meta) {
}
