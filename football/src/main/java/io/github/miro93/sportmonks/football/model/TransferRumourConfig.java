package io.github.miro93.sportmonks.football.model;

import io.github.miro93.sportmonks.core.coreapi.model.Type;
import io.helidon.json.binding.Json;

import java.util.List;

/// Reference values for transfer rumours: the fee types ({@code transferFeeId}), transfer
/// types ({@code typeId}) and rumour statuses ({@code statusId}). Each list may be {@code null}.
@Json.Entity
public record TransferRumourConfig(
        List<Type> fee,
        List<Type> transfers,
        List<Type> statuses) {
}
