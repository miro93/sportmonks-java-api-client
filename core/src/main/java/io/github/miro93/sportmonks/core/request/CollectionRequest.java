package io.github.miro93.sportmonks.core.request;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.paging.Pages;
import io.github.miro93.sportmonks.core.response.ApiResponse;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/// Fluent builder for a collection request. Resolves to an {@code ApiResponse<List<T>>}
/// via {@link #get()} / {@link #getAsync()}, or a lazily-paginated {@link #stream()}.
///
/// Instances are not thread-safe; use one instance per logical request.
///
/// @implNote {@link #stream()} is sequential; calling {@code parallel()} on it is
/// unsupported because the underlying page iterator is stateful.
public final class CollectionRequest<T> {

    private final ApiExecutor executor;
    private final RequestSpec.Builder spec;
    private final DataType<List<T>> listType;

    /// Creates a request bound to an executor, a mutable spec builder and the
    /// list response type token.
    ///
    /// @param executor the executor that runs the request
    /// @param spec     the spec builder accumulating query options
    /// @param listType the token describing the {@code List<T>} payload type
    public CollectionRequest(ApiExecutor executor, RequestSpec.Builder spec, DataType<List<T>> listType) {
        this.executor = Objects.requireNonNull(executor, "executor");
        this.spec = Objects.requireNonNull(spec, "spec");
        this.listType = Objects.requireNonNull(listType, "listType");
    }

    /// Adds one or more includes (related resources) to load.
    ///
    /// @param values the include names
    /// @return this request
    public CollectionRequest<T> include(String... values) {
        spec.include(values);
        return this;
    }

    /// Adds a filter on the named attribute.
    ///
    /// @param name   the filter name
    /// @param values the filter values
    /// @return this request
    public CollectionRequest<T> filter(String name, String... values) {
        spec.filter(name, values);
        return this;
    }

    /// Restricts the returned fields to the given selection.
    ///
    /// @param fields the field names to select
    /// @return this request
    public CollectionRequest<T> select(String... fields) {
        spec.select(fields);
        return this;
    }

    /// Sets the sort order on the given fields.
    ///
    /// @param fields the field names to sort by
    /// @return this request
    public CollectionRequest<T> sort(String... fields) {
        spec.sort(fields);
        return this;
    }

    /// Requests a specific page. Applies to {@link #get()} / {@link #getAsync()} only;
    /// {@link #stream()} always walks from the first page.
    ///
    /// @param page the 1-based page number
    /// @return this request
    public CollectionRequest<T> page(int page) {
        spec.page(page);
        return this;
    }

    /// Executes the request synchronously, returning a single page.
    ///
    /// @return the decoded page of results
    public ApiResponse<List<T>> get() {
        return executor.execute(spec.build(), listType);
    }

    /// Executes the request asynchronously on the executor's async pool.
    ///
    /// @return a future completing with the decoded page of results
    public CompletableFuture<ApiResponse<List<T>>> getAsync() {
        return executor.executeAsync(spec.build(), listType);
    }

    /// Lazily walks every page, following {@code pagination.has_more}.
    public Stream<T> stream() {
        RequestSpec base = spec.build();
        return Pages.stream(page -> {
            ApiResponse<List<T>> response = executor.execute(base.withPage(page), listType);
            return response;
        });
    }
}
