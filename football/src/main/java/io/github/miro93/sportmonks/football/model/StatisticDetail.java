package io.github.miro93.sportmonks.football.model;

import io.helidon.json.binding.Json;
import java.util.Map;

/// A single detail entry inside a {@link Statistic}. {@code id} is always
/// present; every other field may be {@code null}. {@code value} is a free-form
/// object whose keys depend on {@code typeId} (e.g. {@code {total}},
/// {@code {home,away}}, {@code {average,highest,lowest}}), so it is exposed as a
/// raw {@code Map<String, Integer>}.
///
/// ponytail: typed as {@code Integer}, not {@code Object}, to dodge a Helidon
/// 4.5.4 bug — its {@code Object}/{@code Double}-typed map-value converter
/// misparses a bare (no decimal point) JSON integer literal when it isn't the
/// last entry in the object (confirmed: single-digit-first {@code {"home":5,
/// "away":7}} throws, single-digit-last does not; explicit {@code Integer}/
/// {@code Long} targets are unaffected). All values seen for this field are
/// whole-number counts, so this holds for now; widen back to {@code Object} if
/// SportMonks ever sends a decimal here, once the upstream bug is fixed.
@Json.Entity
public record StatisticDetail(
        long id,
        @Json.Property("type_id") Long typeId,
        Map<String, Integer> value) {
}
