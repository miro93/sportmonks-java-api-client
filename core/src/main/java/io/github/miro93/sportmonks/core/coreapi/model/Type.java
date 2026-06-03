package io.github.miro93.sportmonks.core.coreapi.model;

/// A type definition from the SportMonks Core API — the lookup behind the
/// {@code type_id} foreign keys used across the platform (events, statistics,
/// standings details, …). {@code id} is always present; {@code parentId},
/// {@code name}, {@code code}, {@code developerName}, {@code group} and
/// {@code description} may be {@code null}.
public record Type(
        long id,
        Long parentId,
        String name,
        String code,
        String developerName,
        String group,
        String description) {
}
