package io.github.miro93.sportmonks.core.coreapi.model;

import io.helidon.json.binding.Json;

import java.util.List;

/// The types available for one entity, as returned per key by the
/// {@code types/entities} endpoint.
@Json.Entity
public record EntityTypes(
        @Json.Property("updated_at") String updatedAt,
        List<Type> types) {
}
