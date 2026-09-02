package io.github.miro93.sportmonks.core.json;

import io.helidon.json.JsonValue;
import io.helidon.json.binding.Json;
import io.github.miro93.sportmonks.core.response.ApiResponse;
import io.github.miro93.sportmonks.core.response.Pagination;
import io.github.miro93.sportmonks.core.response.RateLimit;

/// The SportMonks response envelope, decoded generically: `data` is held as a raw
/// {@link JsonValue} tree and decoded into its actual type separately by
/// {@link HelidonJsonCodec}, since the payload type is only known to the caller
/// (see {@link ApiResponse} javadoc for why the public envelope record cannot be
/// a `@Json.Entity` itself).
@Json.Entity
record Envelope(JsonValue data,
                Pagination pagination,
                @Json.Property("rate_limit") RateLimit rateLimit,
                String timezone) {
}
