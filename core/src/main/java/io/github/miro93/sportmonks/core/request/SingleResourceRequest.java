package io.github.miro93.sportmonks.core.request;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.response.ApiResponse;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/// Fluent builder for a single-resource request. Accumulates query options, then
/// resolves to an {@code ApiResponse<T>} via {@link #get()} / {@link #getAsync()}.
public final class SingleResourceRequest<T> {

    private final ApiExecutor executor;
    private final RequestSpec.Builder spec;
    private final DataType<T> dataType;

    public SingleResourceRequest(ApiExecutor executor, RequestSpec.Builder spec, DataType<T> dataType) {
        this.executor = Objects.requireNonNull(executor, "executor");
        this.spec = Objects.requireNonNull(spec, "spec");
        this.dataType = Objects.requireNonNull(dataType, "dataType");
    }

    public SingleResourceRequest<T> include(String... values) {
        spec.include(values);
        return this;
    }

    public SingleResourceRequest<T> filter(String name, String... values) {
        spec.filter(name, values);
        return this;
    }

    public SingleResourceRequest<T> select(String... fields) {
        spec.select(fields);
        return this;
    }

    public SingleResourceRequest<T> sort(String... fields) {
        spec.sort(fields);
        return this;
    }

    public ApiResponse<T> get() {
        return executor.execute(spec.build(), dataType);
    }

    public CompletableFuture<ApiResponse<T>> getAsync() {
        return executor.executeAsync(spec.build(), dataType);
    }
}
