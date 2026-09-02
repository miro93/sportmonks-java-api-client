package io.github.miro93.sportmonks.football.model;

import io.helidon.json.binding.Json;

/// The inner score payload: goals and which side ("home"/"away") they belong to.
@Json.Entity
public record ScoreDetail(Integer goals, String participant) {
}
