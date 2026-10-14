package io.github.miro93.sportmonks.core;

import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.error.internal.ErrorMapper;
import io.github.miro93.sportmonks.core.error.RateLimitException;
import io.github.miro93.sportmonks.core.error.ServerException;
import io.github.miro93.sportmonks.core.http.HttpTransport;
import io.github.miro93.sportmonks.core.http.RawResponse;
import io.github.miro93.sportmonks.core.json.CodecException;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.HelidonJsonCodec;
import io.github.miro93.sportmonks.core.ratelimit.RateLimitPolicy;
import io.github.miro93.sportmonks.core.ratelimit.RateLimitTracker;
import io.github.miro93.sportmonks.core.request.internal.RequestSpec;
import io.github.miro93.sportmonks.core.request.internal.UrlBuilder;
import io.github.miro93.sportmonks.core.response.ApiResponse;
import io.github.miro93.sportmonks.core.retry.internal.Sleeper;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/// Ties transport + auth + codec + error mapping together. Sport modules call this.
/// Async variants run the blocking pipeline on a virtual-thread executor (Java 25).
///
/// A client normally creates a single, long-lived {@code ApiExecutor}. The
/// four-argument constructor provisions a per-instance
/// {@link Executors#newVirtualThreadPerTaskExecutor() virtual-thread executor}
/// (which starts no threads until work is submitted); deployments that prefer to
/// share or manage their own executor can supply one via the five-argument
/// constructor instead.
public final class ApiExecutor {

    private final HttpTransport transport;
    private final HelidonJsonCodec codec;
    private final String baseUrl;
    private final Executor asyncExecutor;
    private final Map<String, String> headers;
    private final RateLimitTracker rateLimits;
    private final RateLimitPolicy rateLimitPolicy;
    private final Sleeper sleeper;

    /// Creates an executor with a per-instance virtual-thread executor for async calls.
    ///
    /// @param transport the HTTP transport
    /// @param codec     the JSON codec
    /// @param token     the API token applied to every request
    /// @param baseUrl   the API base URL
    public ApiExecutor(HttpTransport transport, HelidonJsonCodec codec, ApiToken token, String baseUrl) {
        this(transport, codec, token, baseUrl,
                Executors.newVirtualThreadPerTaskExecutor());
    }

    /// Creates an executor using a caller-supplied executor for async calls.
    ///
    /// @param transport     the HTTP transport
    /// @param codec         the JSON codec
    /// @param token         the API token applied to every request
    /// @param baseUrl       the API base URL
    /// @param asyncExecutor the executor backing {@link #executeAsync}
    public ApiExecutor(HttpTransport transport, HelidonJsonCodec codec, ApiToken token,
                       String baseUrl, Executor asyncExecutor) {
        this(transport, codec, token, baseUrl, asyncExecutor, new RateLimitTracker(), RateLimitPolicy.NONE, Sleeper.REAL);
    }

    /// Creates an executor sharing a {@link RateLimitTracker} with sibling executors and
    /// guarded by a {@link RateLimitPolicy}.
    ///
    /// @param transport     the HTTP transport
    /// @param codec         the JSON codec
    /// @param token         the API token
    /// @param baseUrl       the base URL requests are relative to
    /// @param rateLimits    the tracker fed by every response
    /// @param rateLimitPolicy the client-side quota guard
    public ApiExecutor(HttpTransport transport, HelidonJsonCodec codec, ApiToken token, String baseUrl,
                       RateLimitTracker rateLimits, RateLimitPolicy rateLimitPolicy) {
        this(transport, codec, token, baseUrl, Executors.newVirtualThreadPerTaskExecutor(),
                rateLimits, rateLimitPolicy, Sleeper.REAL);
    }

    ApiExecutor(HttpTransport transport, HelidonJsonCodec codec, ApiToken token, String baseUrl,
                Executor asyncExecutor, RateLimitTracker rateLimits, RateLimitPolicy rateLimitPolicy, Sleeper sleeper) {
        this.transport = Objects.requireNonNull(transport, "transport");
        this.codec = Objects.requireNonNull(codec, "codec");
        Objects.requireNonNull(token, "token");
        this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl");
        this.asyncExecutor = Objects.requireNonNull(asyncExecutor, "asyncExecutor");
        this.headers = Map.of("Authorization", token.value(), "Accept", "application/json");
        this.rateLimits = Objects.requireNonNull(rateLimits, "rateLimits");
        this.rateLimitPolicy = Objects.requireNonNull(rateLimitPolicy, "rateLimitPolicy");
        this.sleeper = Objects.requireNonNull(sleeper, "sleeper");
    }

    /// The tracker fed by this executor's responses.
    public RateLimitTracker rateLimits() {
        return rateLimits;
    }

    /// Runs the request synchronously: builds the URL, applies auth, and decodes
    /// the response into a typed {@link ApiResponse}.
    ///
    /// @param spec     the request specification
    /// @param dataType the token describing the {@code data} payload type
    /// @param <T>      the payload type
    /// @return the decoded response
    /// @throws io.github.miro93.sportmonks.core.error.SportmonksException on HTTP or decoding failure
    public <T> ApiResponse<T> execute(RequestSpec spec, DataType<T> dataType) {
        String entity = entityKey(spec.path());
        guard(entity);
        RawResponse response = transport.get(UrlBuilder.build(baseUrl, spec), headers);

        if (!response.isSuccessful()) {
            throw ErrorMapper.fromResponse(response.status(), response.bodyAsString(), retryAfter(response));
        }
        try {
            ApiResponse<T> decoded = codec.decode(response.body(), dataType);
            rateLimits.record(entity, decoded.rateLimit());
            return decoded;
        } catch (CodecException e) {
            throw new ServerException(
                    "Failed to parse successful response (status " + response.status() + ")",
                    response.status(), e);
        }
    }

    /// Runs {@link #execute} on the async executor.
    ///
    /// @param spec     the request specification
    /// @param dataType the token describing the {@code data} payload type
    /// @param <T>      the payload type
    /// @return a future completing with the decoded response (or the failure)
    public <T> CompletableFuture<ApiResponse<T>> executeAsync(RequestSpec spec, DataType<T> dataType) {
        return CompletableFuture.supplyAsync(
                () -> this.<T>execute(spec, dataType), asyncExecutor);
    }

    private void guard(String entity) {
        if (!rateLimitPolicy.isActive()) {
            return;
        }
        rateLimits.latest(entity)
                .filter(limit -> limit.remaining() < rateLimitPolicy.threshold())
                .ifPresent(limit -> {
                    Duration wait = rateLimits.untilReset(entity);
                    if (rateLimitPolicy.action() == RateLimitPolicy.Action.FAIL) {
                        throw new RateLimitException("Rate-limit guard: " + limit.remaining() + " request(s) left for '"
                                + entity + "', window resets in " + wait.toSeconds() + "s", 429, wait);
                    }
                    sleeper.sleep(wait);
                });
    }

    /// First path segment, which matches the entity the API meters ({@code fixtures}, ...).
    static String entityKey(String path) {
        int slash = path.indexOf('/');
        return slash < 0 ? path : path.substring(0, slash);
    }

    private static Optional<Duration> retryAfter(RawResponse response) {
        return response.header("Retry-After").map(value -> {
            try {
                return Duration.ofSeconds(Long.parseLong(value.trim()));
            } catch (NumberFormatException e) {
                return null;
            }
        });
    }
}
