package io.github.miro93.sportmonks.football.model;

import io.helidon.json.binding.Json;

/// A TV station from the SportMonks football API. {@code id} is always present;
/// {@code name}, {@code url} and {@code imagePath} may be {@code null}.
@Json.Entity
public record TvStation(
        long id,
        String name,
        String url,
        @Json.Property("image_path") String imagePath) {
}
