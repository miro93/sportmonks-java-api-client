package io.github.miro93.sportmonks.football.model;

import io.github.miro93.sportmonks.core.coreapi.model.Type;
import io.helidon.json.binding.Json;

/// A transfer rumour. {@code id} is always present; every other scalar may be
/// {@code null}. {@code probability} is a label such as {@code "HIGH"}; {@code amount}
/// is the rumoured fee in {@code currency}. The relation fields ({@code player},
/// {@code fromTeam}, {@code toTeam}, {@code type}, {@code status}) are {@code null}
/// unless requested via includes.
@Json.Entity
public record TransferRumour(
        long id,
        @Json.Property("sport_id") Long sportId,
        @Json.Property("player_id") Long playerId,
        @Json.Property("position_id") Long positionId,
        @Json.Property("detailed_position_id") Long detailedPositionId,
        @Json.Property("from_team_id") Long fromTeamId,
        @Json.Property("to_team_id") Long toTeamId,
        @Json.Property("from_league_id") Long fromLeagueId,
        @Json.Property("to_league_id") Long toLeagueId,
        @Json.Property("transfer_fee_id") Long transferFeeId,
        @Json.Property("status_id") Long statusId,
        @Json.Property("type_id") Long typeId,
        String probability,
        @Json.Property("source_name") String sourceName,
        @Json.Property("source_url") String sourceUrl,
        Long amount,
        String currency,
        String date,
        Player player,
        @Json.Property("fromTeam") Team fromTeam,
        @Json.Property("toTeam") Team toTeam,
        Type type,
        Type status) {
}
