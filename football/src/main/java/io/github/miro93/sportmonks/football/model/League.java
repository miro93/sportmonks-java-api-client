package io.github.miro93.sportmonks.football.model;

import java.util.List;

/// A football league. Scalar fields ({@code id} through {@code hasJerseys}) are
/// always present, except {@code sportId} and {@code countryId} which may be
/// {@code null} for international competitions. The relation field
/// ({@code seasons}) is {@code null} unless requested via includes.
public record League(
        long id,
        Long sportId,
        Long countryId,
        String name,
        Boolean active,
        String shortCode,
        String imagePath,
        String type,
        String subType,
        String lastPlayedAt,
        Integer category,
        Boolean hasJerseys,
        List<Season> seasons) {
}
