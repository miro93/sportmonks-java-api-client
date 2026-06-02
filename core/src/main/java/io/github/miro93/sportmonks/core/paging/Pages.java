package io.github.miro93.sportmonks.core.paging;

import io.github.miro93.sportmonks.core.response.ApiResponse;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.IntFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/// Lazily walks a paginated collection endpoint, following
/// {@link io.github.miro93.sportmonks.core.response.Pagination#hasMore()}.
public final class Pages {

    private Pages() {
    }

    /// @param fetchPage 1-based page number -> the response for that page.
    public static <T> Stream<T> stream(IntFunction<ApiResponse<List<T>>> fetchPage) {
        Iterator<T> iterator = new PageIterator<>(fetchPage);
        Spliterator<T> spliterator = Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED);
        return StreamSupport.stream(spliterator, false);
    }

    private static final class PageIterator<T> implements Iterator<T> {
        private final IntFunction<ApiResponse<List<T>>> fetchPage;
        private int nextPage = 1;
        private boolean exhausted = false;
        private Iterator<T> current = Collections.emptyIterator();

        private PageIterator(IntFunction<ApiResponse<List<T>>> fetchPage) {
            this.fetchPage = fetchPage;
        }

        @Override
        public boolean hasNext() {
            while (!current.hasNext() && !exhausted) {
                ApiResponse<List<T>> response = fetchPage.apply(nextPage);
                List<T> data = response.data() == null ? List.of() : response.data();
                current = data.iterator();
                boolean hasMore = response.pagination() != null && response.pagination().hasMore();
                if (hasMore) {
                    nextPage++;
                } else {
                    exhausted = true;
                }
            }
            return current.hasNext();
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return current.next();
        }
    }
}
