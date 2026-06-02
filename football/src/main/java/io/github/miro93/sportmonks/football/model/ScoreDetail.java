package io.github.miro93.sportmonks.football.model;

/// The inner score payload: goals and which side ("home"/"away") they belong to.
public record ScoreDetail(Integer goals, String participant) {
}
