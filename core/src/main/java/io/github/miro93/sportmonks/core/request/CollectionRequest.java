package io.github.miro93.sportmonks.core.request;

import io.github.miro93.sportmonks.core.ApiExecutor;
import io.github.miro93.sportmonks.core.json.DataType;
import io.github.miro93.sportmonks.core.paging.Pages;
import io.github.miro93.sportmonks.core.request.internal.RequestSpec;
import io.github.miro93.sportmonks.core.response.ApiResponse;

import java.util.List;
import java.time.ZoneId;
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

    /// Sets the single field to sort by ({@code sortBy}); the API rejects more than one.
    ///
    /// @param field the field name (e.g. {@code "starting_at"})
    /// @return this request
    public CollectionRequest<T> sortBy(String field) {
        spec.sortBy(field);
        return this;
    }

    /// Sets the sort direction ({@code order}); without {@link #sortBy} it orders by id.
    ///
    /// @param order {@link Order#ASC} or {@link Order#DESC}
    /// @return this request
    public CollectionRequest<T> order(Order order) {
        spec.order(order);
        return this;
    }

    /// Legacy sort: a single field, optionally prefixed with {@code -} for descending.
    ///
    /// @param fields exactly one field name
    /// @return this request
    /// @throws IllegalArgumentException if more than one field is given
    /// @deprecated use {@link #sortBy(String)} and {@link #order(Order)}; the API never
    ///             honoured the {@code sort} parameter this method used to send
    @Deprecated(since = "3.1.0")
    public CollectionRequest<T> sort(String... fields) {
        spec.sort(fields);
        return this;
    }

    /// Sets the translation locale ({@code locale}, e.g. {@code "de"}).
    ///
    /// @param locale the locale code
    /// @return this request
    public CollectionRequest<T> locale(String locale) {
        spec.locale(locale);
        return this;
    }

    /// Sets the timezone in which date-times are returned ({@code timezone}).
    ///
    /// @param zone the zone
    /// @return this request
    public CollectionRequest<T> timezone(ZoneId zone) {
        spec.timezone(zone);
        return this;
    }

    /// Requests a specific page (offset pagination, capped by SportMonks at 20 000 rows).
    /// Applies to {@link #get()} / {@link #getAsync()} only; {@link #stream()} always walks
    /// from the first page.
    ///
    /// @param page the 1-based page number
    /// @return this request
    public CollectionRequest<T> page(int page) {
        spec.page(page);
        return this;
    }

    /// Sets the page size ({@code per_page}, 1 to 50; the API default is 25). Applies to
    /// {@link #get()} / {@link #getAsync()} and to the first request of {@link #stream()}
    /// (the API rejects {@code per_page} together with {@code cursor}).
    ///
    /// @param perPage the page size
    /// @return this request
    /// @throws IllegalArgumentException if out of range
    public CollectionRequest<T> perPage(int perPage) {
        spec.perPage(perPage);
        return this;
    }

    /// Requests the page identified by a cursor (the {@code next_cursor} of a previous response).
    /// Applies to {@link #get()} / {@link #getAsync()} only; {@link #stream()} always walks
    /// from the first page.
    ///
    /// @param cursor the opaque cursor token
    /// @return this request
    public CollectionRequest<T> cursor(String cursor) {
        spec.cursor(cursor);
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

    /// Lazily walks every page, following {@code pagination.has_more}. After the first page
    /// the walk follows {@code pagination.next_cursor} when the server returns one, and falls
    /// back to incrementing {@code page} otherwise.
    public Stream<T> stream() {
        RequestSpec base = spec.build();
        return Pages.stream((page, cursor) -> executor.execute(
                cursor != null ? base.withCursor(cursor) : base.withPage(page), listType));
    }
}
