package io.github.miro93.sportmonks.football.model;

import java.util.List;

/// A football team. {@code id} is always present; {@code placeholder} is always
/// present (defaults to {@code false}). Foreign-key {@code Long} fields
/// ({@code sportId}, {@code countryId}, {@code venueId}) and other optional
/// scalars ({@code gender}, {@code name}, {@code shortCode}, {@code imagePath},
/// {@code founded}, {@code type}, {@code lastPlayedAt}) may be {@code null}.
/// The relation field ({@code squad}) is {@code null} unless requested via includes.
public record Team(
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
        List<Squad> squad) {
}
