package io.github.miro93.sportmonks.football.model;

import io.helidon.json.binding.Json;

import java.util.List;

/// A season's knockout bracket: the knockout stages (each with its fixtures, which are
/// placeholders until the teams are known) and the edges describing how the winner or
/// loser of one fixture advances into the home or away slot of another.
@Json.Entity
public record Bracket(List<BracketStage> stages, List<BracketEdge> edges) {
}
