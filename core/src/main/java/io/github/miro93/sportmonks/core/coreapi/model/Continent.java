package io.github.miro93.sportmonks.core.coreapi.model;

import io.helidon.json.binding.Json;

/// A geographic continent from the SportMonks Core API. {@code id} is always
/// present; {@code name} and {@code code} may be {@code null}.
@Json.Entity
public record Continent(long id, String name, String code) {
}
