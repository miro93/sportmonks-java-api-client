package io.github.miro93.sportmonks.core.request;

import com.fasterxml.jackson.databind.JavaType;
import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.paging.Pages;
import io.github.miro93.sportmonks.core.response.ApiResponse;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/// Fluent builder for a collection request. Resolves to an {@code ApiResponse<List<T>>}
/// via {@link #get()} / {@link #getAsync()}, or a lazily-paginated {@link #stream()}.
///
/// @implNote {@link #stream()} is sequential; calling {@code parallel()} on it is
/// unsupported because the underlying page iterator is stateful.
public final class CollectionRequest<T> {

    private final ApiExecutor executor;
    private final RequestSpec.Builder spec;
    private final JavaType listType;

    public CollectionRequest(ApiExecutor executor, RequestSpec.Builder spec, JavaType listType) {
        this.executor = Objects.requireNonNull(executor, "executor");
        this.spec = Objects.requireNonNull(spec, "spec");
        this.listType = Objects.requireNonNull(listType, "listType");
    }

    public CollectionRequest<T> include(String... values) {
        spec.include(values);
        return this;
    }

    public CollectionRequest<T> filter(String name, String... values) {
        spec.filter(name, values);
        return this;
    }

    public CollectionRequest<T> select(String... fields) {
        spec.select(fields);
        return this;
    }

    public CollectionRequest<T> sort(String... fields) {
        spec.sort(fields);
        return this;
    }

    public CollectionRequest<T> page(int page) {
        spec.page(page);
        return this;
    }

    public ApiResponse<List<T>> get() {
        return executor.execute(spec.build(), listType);
    }

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
