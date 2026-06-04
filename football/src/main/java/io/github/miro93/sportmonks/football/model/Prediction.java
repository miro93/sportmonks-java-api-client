package io.github.miro93.sportmonks.football.model;

import java.util.Map;

/// A predicted-probability record from the SportMonks predictions feed
/// (probabilities endpoints). {@code id} is always present; every other field
/// may be {@code null}. {@code predictions} is a free-form object whose keys
/// depend on {@code typeId} (e.g. {@code {yes,no}}, {@code {home,draw,away}},
/// correct-score maps), so it is exposed as a raw {@code Map<String, Object>};
/// numeric values decode as {@code Double}/{@code Integer}.
public record Prediction(
        long id,
        Long fixtureId,
        Long typeId,
        Map<String, Object> predictions) {
}
