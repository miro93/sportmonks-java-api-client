package io.github.miro93.sportmonks.core.request.internal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/// Immutable description of a SportMonks request: a relative path plus the
/// optional include/filter/select/sort/page query options. Built via {@link #builder}.
public record RequestSpec(
        String path,
        List<String> includes,
        Map<String, List<String>> filters,
        List<String> select,
        List<String> sort,
        Integer page) {

    public RequestSpec {
        includes = List.copyOf(includes);
        filters = Map.copyOf(filters);
        select = List.copyOf(select);
        sort = List.copyOf(sort);
    }

    /// Starts a new builder for the given relative path.
    ///
    /// @param path the raw (un-encoded) relative request path; callers must not
    ///             percent-encode it — {@link UrlBuilder} encodes each path segment
    /// @return a fresh builder
    public static Builder builder(String path) {
        return new Builder(path);
    }

    /// Returns a copy of this spec with the page number set, leaving this instance unchanged.
    public RequestSpec withPage(int page) {
        return new RequestSpec(path, includes, filters, select, sort, page);
    }

    /// Mutable builder that accumulates query options before producing an
    /// immutable {@link RequestSpec}.
    public static final class Builder {
        private final String path;
        private final List<String> includes = new ArrayList<>();
        private final Map<String, List<String>> filters = new LinkedHashMap<>();
        private final List<String> select = new ArrayList<>();
        private final List<String> sort = new ArrayList<>();
        private Integer page;

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

        /// Sets the sort order on the given fields.
        ///
        /// @param fields the field names to sort by
        /// @return this builder
        public Builder sort(String... fields) {
            sort.addAll(List.of(fields));
            return this;
        }

        /// Sets the 1-based page number to request.
        ///
        /// @param page the page number
        /// @return this builder
        public Builder page(int page) {
            this.page = page;
            return this;
        }

        /// Produces the immutable spec from the accumulated options.
        ///
        /// @return the built {@link RequestSpec}
        public RequestSpec build() {
            return new RequestSpec(path, includes, filters, select, sort, page);
        }
    }
}
