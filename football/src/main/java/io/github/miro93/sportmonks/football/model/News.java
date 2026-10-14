package io.github.miro93.sportmonks.football.model;

import io.helidon.json.binding.Json;

/// A pre-match or post-match news article. {@code id} is always present; every other
/// scalar may be {@code null}. {@code type} is {@code "prematch"} or {@code "postmatch"}.
/// The relation fields ({@code fixture}, {@code league}) are {@code null} unless
/// requested via includes.
@Json.Entity
public record News(
        long id,
        @Json.Property("fixture_id") Long fixtureId,
        @Json.Property("league_id") Long leagueId,
        String title,
        String type,
        Fixture fixture,
        League league) {
}
