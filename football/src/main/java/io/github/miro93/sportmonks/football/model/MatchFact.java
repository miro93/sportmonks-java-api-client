package io.github.miro93.sportmonks.football.model;

import io.github.miro93.sportmonks.core.coreapi.model.Type;
import io.helidon.json.JsonValue;
import io.helidon.json.binding.Json;

import java.util.Map;

/// A pre-computed match fact for a fixture. {@code id} is always present; every other
/// field may be {@code null}. {@code data} is a free-form object whose shape depends on
/// {@code typeId} (e.g. {@code {all: {count, percentage}, home: ..., away: ...}}) and is
/// decoded via {@link FreeFormJson} like {@link Prediction#predictions()}. The relation
/// fields ({@code type}, {@code fixture}) are {@code null} unless requested via includes.
@Json.Entity
public record MatchFact(
        long id,
        @Json.Property("sport_id") Long sportId,
        @Json.Property("fixture_id") Long fixtureId,
        @Json.Property("type_id") Long typeId,
        String participant,
        String basis,
        String category,
        String scope,
        @Json.Property("natural_language") String naturalLanguage,
        @Json.Property("data") JsonValue rawData,
        Type type,
        Fixture fixture) {

    /// The decoded {@code data} payload; {@code null} if the field was absent.
    public Map<String, Object> data() {
        return FreeFormJson.toMap(rawData);
    }
}
