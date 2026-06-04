package io.github.miro93.sportmonks.core.request;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.request.internal.RequestSpec;
import io.github.miro93.sportmonks.core.response.ApiResponse;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/// Fluent builder for a single-resource request. Accumulates query options, then
/// resolves to an {@code ApiResponse<T>} via {@link #get()} / {@link #getAsync()}.
public final class SingleResourceRequest<T> {

    private final ApiExecutor executor;
    private final RequestSpec.Builder spec;
    private final DataType<T> dataType;

    /// Creates a request bound to an executor, a mutable spec builder and the
    /// response type token.
    ///
    /// @param executor the executor that runs the request
    /// @param spec     the spec builder accumulating query options
    /// @param dataType the token describing the {@code data} payload type
    public SingleResourceRequest(ApiExecutor executor, RequestSpec.Builder spec, DataType<T> dataType) {
        this.executor = Objects.requireNonNull(executor, "executor");
        this.spec = Objects.requireNonNull(spec, "spec");
        this.dataType = Objects.requireNonNull(dataType, "dataType");
    }

    /// Adds one or more includes (related resources) to load.
    ///
    /// @param values the include names
    /// @return this request
    public SingleResourceRequest<T> include(String... values) {
        spec.include(values);
        return this;
    }

    /// Adds a filter on the named attribute.
    ///
    /// @param name   the filter name
    /// @param values the filter values
    /// @return this request
    public SingleResourceRequest<T> filter(String name, String... values) {
        spec.filter(name, values);
        return this;
    }

    /// Restricts the returned fields to the given selection.
    ///
    /// @param fields the field names to select
    /// @return this request
    public SingleResourceRequest<T> select(String... fields) {
        spec.select(fields);
        return this;
    }

    /// Sets the sort order on the given fields.
    ///
    /// @param fields the field names to sort by
    /// @return this request
    public SingleResourceRequest<T> sort(String... fields) {
        spec.sort(fields);
        return this;
    }

    /// Executes the request synchronously.
    ///
    /// @return the decoded response
    public ApiResponse<T> get() {
        return executor.execute(spec.build(), dataType);
    }

    /// Executes the request asynchronously on the executor's async pool.
    ///
    /// @return a future completing with the decoded response
    public CompletableFuture<ApiResponse<T>> getAsync() {
        return executor.executeAsync(spec.build(), dataType);
    }
}
