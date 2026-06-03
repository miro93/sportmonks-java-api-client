package io.github.miro93.sportmonks.core.coreapi.model;

/// A region (sub-national area) from the SportMonks Core API. {@code id} is
/// always present; {@code countryId} and {@code name} may be {@code null}.
public record Region(long id, Long countryId, String name) {
}
