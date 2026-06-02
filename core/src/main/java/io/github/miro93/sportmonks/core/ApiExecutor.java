package io.github.miro93.sportmonks.core;

import io.github.miro93.sportmonks.core.auth.ApiToken;
import io.github.miro93.sportmonks.core.error.ErrorMapper;
import io.github.miro93.sportmonks.core.error.ServerException;
import io.github.miro93.sportmonks.core.http.HttpTransport;
import io.github.miro93.sportmonks.core.http.RawResponse;
import io.github.miro93.sportmonks.core.json.CodecException;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.json.JacksonCodec;
import io.github.miro93.sportmonks.core.request.RequestSpec;
import io.github.miro93.sportmonks.core.request.UrlBuilder;
import io.github.miro93.sportmonks.core.response.ApiResponse;

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
    private final JacksonCodec codec;
    private final ApiToken token;
    private final String baseUrl;
    private final Executor asyncExecutor;

    /// Creates an executor with a per-instance virtual-thread executor for async calls.
    ///
    /// @param transport the HTTP transport
    /// @param codec     the JSON codec
    /// @param token     the API token applied to every request
    /// @param baseUrl   the API base URL
    public ApiExecutor(HttpTransport transport, JacksonCodec codec, ApiToken token, String baseUrl) {
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
    public ApiExecutor(HttpTransport transport, JacksonCodec codec, ApiToken token,
                       String baseUrl, Executor asyncExecutor) {
        this.transport = Objects.requireNonNull(transport, "transport");
        this.codec = Objects.requireNonNull(codec, "codec");
        this.token = Objects.requireNonNull(token, "token");
        this.baseUrl = Objects.requireNonNull(baseUrl, "baseUrl");
        this.asyncExecutor = Objects.requireNonNull(asyncExecutor, "asyncExecutor");
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
        RawResponse response = transport.get(
                UrlBuilder.build(baseUrl, spec),
                Map.of("Authorization", token.value(), "Accept", "application/json"));

        if (!response.isSuccessful()) {
            throw ErrorMapper.fromResponse(response.status(), response.body(), retryAfter(response));
        }
        try {
            return codec.decode(response.body(), dataType);
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
