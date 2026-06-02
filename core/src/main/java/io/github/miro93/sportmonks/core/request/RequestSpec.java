package io.github.miro93.sportmonks.core.request;

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

    public static Builder builder(String path) {
        return new Builder(path);
    }

    /// Returns a copy of this spec with the page number set, leaving this instance unchanged.
    public RequestSpec withPage(int page) {
        return new RequestSpec(path, includes, filters, select, sort, page);
    }

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

        public Builder include(String... values) {
            includes.addAll(List.of(values));
            return this;
        }

        public Builder filter(String name, String... values) {
            filters.put(name, List.of(values));
            return this;
        }

        public Builder select(String... fields) {
            select.addAll(List.of(fields));
            return this;
        }

        public Builder sort(String... fields) {
            sort.addAll(List.of(fields));
            return this;
        }

        public Builder page(int page) {
            this.page = page;
            return this;
        }

        public RequestSpec build() {
            return new RequestSpec(path, includes, filters, select, sort, page);
        }
    }
}
