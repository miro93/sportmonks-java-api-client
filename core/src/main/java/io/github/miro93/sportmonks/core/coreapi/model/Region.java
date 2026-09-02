package io.github.miro93.sportmonks.core.coreapi.model;

import io.helidon.json.binding.Json;

/// A region (sub-national area) from the SportMonks Core API. {@code id} is
/// always present; {@code countryId} and {@code name} may be {@code null}.
@Json.Entity
public record Region(long id, @Json.Property("country_id") Long countryId, String name) {
}
