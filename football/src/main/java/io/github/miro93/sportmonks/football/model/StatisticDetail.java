package io.github.miro93.sportmonks.football.model;

import java.util.Map;

/// A single detail entry inside a {@link Statistic}. {@code id} is always
/// present; every other field may be {@code null}. {@code value} is a free-form
/// object whose keys depend on {@code typeId} (e.g. {@code {total}},
/// {@code {home,away}}, {@code {average,highest,lowest}}), so it is exposed as a
/// raw {@code Map<String, Object>}; numeric values decode as
/// {@code Double}/{@code Integer}.
public record StatisticDetail(
        long id,
        Long typeId,
        Map<String, Object> value) {
}
