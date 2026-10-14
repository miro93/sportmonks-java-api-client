package io.github.miro93.sportmonks.core.paging;

import io.github.miro93.sportmonks.core.response.ApiResponse;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.BiFunction;
import java.util.function.IntFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/// Lazily walks a paginated collection endpoint, following
/// {@link io.github.miro93.sportmonks.core.response.Pagination#hasMore()}.
public final class Pages {

    private Pages() {
    }

    /// Offset-only walk: the fetcher receives the 1-based page number and never sees cursors.
    /// Offset pagination is capped by SportMonks at 20 000 rows; prefer {@link #stream(BiFunction)}.
    ///
    /// @param fetchPage 1-based page number -> the response for that page.
    public static <T> Stream<T> stream(IntFunction<ApiResponse<List<T>>> fetchPage) {
        return stream((page, cursor) -> fetchPage.apply(page));
    }

    /// Cursor-aware walk. The fetcher receives the 1-based page number and, from the second
    /// page on, the {@code next_cursor} of the previous response ({@code null} when the server
    /// did not return one, in which case the fetcher should fall back to the page number).
    ///
    /// @param fetch (page number, cursor or {@code null}) -> the response for that page.
    public static <T> Stream<T> stream(BiFunction<Integer, String, ApiResponse<List<T>>> fetch) {
        Iterator<T> iterator = new PageIterator<>(fetch);
        Spliterator<T> spliterator = Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED);
        return StreamSupport.stream(spliterator, false);
    }

    /// Cursor-aware walk with a one-page lookahead: as soon as a page arrives and reports
    /// {@code has_more}, the next page is requested through {@code fetchAsync} while the
    /// current one is consumed, so page boundaries no longer pay a full round trip. At most
    /// one page is in flight; closing the stream early cancels a pending lookahead.
    /// Failures of the lookahead surface on the pull that needs it, unwrapped from
    /// {@link CompletionException}.
    ///
    /// @param fetchAsync (page number, cursor or {@code null}) -> a future of that page
    public static <T> Stream<T> streamPrefetching(
            BiFunction<Integer, String, CompletableFuture<ApiResponse<List<T>>>> fetchAsync) {
        PrefetchingIterator<T> iterator = new PrefetchingIterator<>(fetchAsync);
        Spliterator<T> spliterator = Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED);
        return StreamSupport.stream(spliterator, false).onClose(iterator::cancelPending);
    }

    private static final class PageIterator<T> implements Iterator<T> {
        private final BiFunction<Integer, String, ApiResponse<List<T>>> fetch;
        private int nextPage = 1;
        private String nextCursor;
        private boolean exhausted = false;
        private Iterator<T> current = Collections.emptyIterator();

        private PageIterator(BiFunction<Integer, String, ApiResponse<List<T>>> fetch) {
            this.fetch = fetch;
        }

        @Override
        public boolean hasNext() {
            while (!current.hasNext() && !exhausted) {
                ApiResponse<List<T>> response = fetch.apply(nextPage, nextCursor);
                List<T> data = response.data() == null ? List.of() : response.data();
                current = data.iterator();
                boolean hasMore = response.pagination() != null && response.pagination().hasMore();
                if (hasMore) {
                    nextPage++;
                    nextCursor = response.pagination().nextCursor();
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

    private static final class PrefetchingIterator<T> implements Iterator<T> {
        private final BiFunction<Integer, String, CompletableFuture<ApiResponse<List<T>>>> fetchAsync;
        private CompletableFuture<ApiResponse<List<T>>> pending;
        private int nextPage = 1;
        private boolean exhausted = false;
        private Iterator<T> current = Collections.emptyIterator();

        private PrefetchingIterator(BiFunction<Integer, String, CompletableFuture<ApiResponse<List<T>>>> fetchAsync) {
            this.fetchAsync = fetchAsync;
            this.pending = fetchAsync.apply(1, null);
        }

        @Override
        public boolean hasNext() {
            while (!current.hasNext() && !exhausted) {
                ApiResponse<List<T>> response = await(pending);
                pending = null;
                List<T> data = response.data() == null ? List.of() : response.data();
                current = data.iterator();
                boolean hasMore = response.pagination() != null && response.pagination().hasMore();
                if (hasMore) {
                    nextPage++;
                    pending = fetchAsync.apply(nextPage, response.pagination().nextCursor());
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

        void cancelPending() {
            if (pending != null) {
                pending.cancel(true);
            }
        }

        private static <R> R await(CompletableFuture<R> future) {
            try {
                return future.join();
            } catch (CompletionException e) {
                if (e.getCause() instanceof RuntimeException cause) {
                    throw cause;
                }
                throw e;
            }
        }
    }
}
