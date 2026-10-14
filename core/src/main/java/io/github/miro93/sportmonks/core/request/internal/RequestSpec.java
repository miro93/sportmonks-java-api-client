package io.github.miro93.sportmonks.core.request.internal;

import io.github.miro93.sportmonks.core.request.Order;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/// Immutable description of a SportMonks request: a relative path plus the
/// optional include/filter/select/sortBy/order/page/cursor/perPage/locale/timezone
/// query options. Built via {@link #builder}.
public record RequestSpec(
        String path,
        List<String> includes,
        Map<String, List<String>> filters,
        List<String> select,
        String sortBy,
        Order order,
        Integer page,
        String cursor,
        Integer perPage,
        String locale,
        String timezone) {

    /// Largest {@code per_page} the API accepts.
    public static final int MAX_PER_PAGE = 50;

    public RequestSpec {
        includes = List.copyOf(includes);
        filters = Map.copyOf(filters);
        select = List.copyOf(select);
    }

    /// Starts a new builder for the given relative path.
    ///
    /// @param path the raw (un-encoded) relative request path; callers must not
    ///             percent-encode it — {@link UrlBuilder} encodes each path segment
    /// @return a fresh builder
    public static Builder builder(String path) {
        return new Builder(path);
    }

    /// Returns a copy of this spec with the page number set and no cursor, leaving this instance unchanged.
    public RequestSpec withPage(int page) {
        return new RequestSpec(path, includes, filters, select, sortBy, order, page, null, perPage, locale, timezone);
    }

    /// Returns a copy of this spec with the cursor set and neither page number nor
    /// {@code per_page} (the API rejects {@code per_page} together with {@code cursor}),
    /// leaving this instance unchanged.
    public RequestSpec withCursor(String cursor) {
        return new RequestSpec(path, includes, filters, select, sortBy, order, null, cursor, null, locale, timezone);
    }

    /// Mutable builder that accumulates query options before producing an
    /// immutable {@link RequestSpec}.
    public static final class Builder {
        private final String path;
        private final List<String> includes = new ArrayList<>();
        private final Map<String, List<String>> filters = new LinkedHashMap<>();
        private final List<String> select = new ArrayList<>();
        private String sortBy;
        private Order order;
        private Integer page;
        private String cursor;
        private Integer perPage;
        private String locale;
        private String timezone;

        private Builder(String path) {
            this.path = path;
        }

        /// Adds one or more includes (related resources) to load.
        ///
        /// @param values the include names
        /// @return this builder
        public Builder include(String... values) {
            includes.addAll(List.of(values));
            return this;
        }

        /// Adds a filter on the named attribute, replacing any prior value.
        ///
        /// @param name   the filter name
        /// @param values the filter values
        /// @return this builder
        public Builder filter(String name, String... values) {
            filters.put(name, List.of(values));
            return this;
        }

        /// Restricts the returned fields to the given selection.
        ///
        /// @param fields the field names to select
        /// @return this builder
        public Builder select(String... fields) {
            select.addAll(List.of(fields));
            return this;
        }

        /// Sets the single field to sort by ({@code sortBy}); the API rejects more than one.
        ///
        /// @param field the field name
        /// @return this builder
        public Builder sortBy(String field) {
            this.sortBy = field;
            return this;
        }

        /// Sets the sort direction ({@code order}); applies to {@link #sortBy} or, without
        /// it, to the default ordering by id.
        ///
        /// @param order the direction
        /// @return this builder
        public Builder order(Order order) {
            this.order = order;
            return this;
        }

        /// Legacy sort: a single field, optionally prefixed with {@code -} for descending.
        ///
        /// @param fields exactly one field name
        /// @return this builder
        /// @throws IllegalArgumentException if more than one field is given (the API
        ///                                  only supports a single {@code sortBy})
        /// @deprecated use {@link #sortBy(String)} and {@link #order(Order)}
        @Deprecated(since = "3.1.0")
        public Builder sort(String... fields) {
            if (fields.length == 0) {
                return this;
            }
            if (fields.length > 1) {
                throw new IllegalArgumentException("SportMonks sortBy accepts a single field, got " + fields.length);
            }
            String field = fields[0];
            if (field.startsWith("-")) {
                return sortBy(field.substring(1)).order(Order.DESC);
            }
            return sortBy(field);
        }

        /// Sets the 1-based page number to request.
        ///
        /// @param page the page number
        /// @return this builder
        public Builder page(int page) {
            this.page = page;
            return this;
        }

        /// Sets the pagination cursor (the {@code next_cursor} of a previous response).
        ///
        /// @param cursor the opaque cursor token
        /// @return this builder
        public Builder cursor(String cursor) {
            this.cursor = cursor;
            return this;
        }

        /// Sets the page size ({@code per_page}, 1 to {@value RequestSpec#MAX_PER_PAGE}).
        ///
        /// @param perPage the page size
        /// @return this builder
        /// @throws IllegalArgumentException if out of range
        public Builder perPage(int perPage) {
            if (perPage < 1 || perPage > MAX_PER_PAGE) {
                throw new IllegalArgumentException("per_page must be between 1 and " + MAX_PER_PAGE + ", got " + perPage);
            }
            this.perPage = perPage;
            return this;
        }

        /// Sets the translation locale ({@code locale}, e.g. {@code "de"}).
        ///
        /// @param locale the locale code
        /// @return this builder
        public Builder locale(String locale) {
            this.locale = locale;
            return this;
        }

        /// Sets the timezone in which date-times are returned ({@code timezone}).
        ///
        /// @param zone the zone
        /// @return this builder
        public Builder timezone(ZoneId zone) {
            this.timezone = zone.getId();
            return this;
        }

        /// Produces the immutable spec from the accumulated options.
        ///
        /// @return the built {@link RequestSpec}
        public RequestSpec build() {
            return new RequestSpec(path, includes, filters, select, sortBy, order, page, cursor, perPage, locale, timezone);
        }
    }
}
