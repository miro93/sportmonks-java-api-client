package io.github.miro93.sportmonks.core.paging;

import io.github.miro93.sportmonks.core.response.ApiResponse;
import io.github.miro93.sportmonks.core.response.Pagination;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PagesTest {

    private static ApiResponse<List<Integer>> page(List<Integer> data, int current, boolean hasMore) {
        return new ApiResponse<>(data,
                new Pagination(data.size(), 2, current, hasMore ? "next" : null, null, hasMore),
                null, null);
    }

    @Test
    void streamsAcrossAllPagesInOrder() {
        Stream<Integer> stream = Pages.stream(pageNumber -> switch (pageNumber) {
            case 1 -> page(List.of(1, 2), 1, true);
            case 2 -> page(List.of(3, 4), 2, true);
            case 3 -> page(List.of(5), 3, false);
            default -> throw new IllegalStateException("unexpected page " + pageNumber);
        });

        assertThat(stream).containsExactly(1, 2, 3, 4, 5);
    }

    @Test
    void fetchesLazilyOnlyAsConsumed() {
        AtomicInteger fetched = new AtomicInteger();
        Stream<Integer> stream = Pages.stream(pageNumber -> {
            fetched.incrementAndGet();
            return page(List.of(pageNumber * 10), pageNumber, true); // infinite source
        });

        List<Integer> firstThree = stream.limit(3).toList();

        assertThat(firstThree).containsExactly(10, 20, 30);
        assertThat(fetched.get()).isEqualTo(3); // only 3 pages pulled
    }

    @Test
    void handlesSinglePageWithNoMore() {
        Stream<Integer> stream = Pages.stream(pageNumber -> page(List.of(7, 8), 1, false));
        assertThat(stream).containsExactly(7, 8);
    }

    @Test
    void passesNextCursorToTheFetcher() {
        List<String> seen = new java.util.ArrayList<>();
        Stream<Integer> stream = Pages.stream((pageNumber, cursor) -> {
            seen.add(pageNumber + ":" + cursor);
            String nextCursor = pageNumber < 3 ? "c" + pageNumber : null;
            return new ApiResponse<>(List.of(pageNumber),
                    new Pagination(1, 1, pageNumber, null, nextCursor, nextCursor != null), null, null);
        });

        assertThat(stream).containsExactly(1, 2, 3);
        assertThat(seen).containsExactly("1:null", "2:c1", "3:c2");
    }

    @Test
    void prefetchingStreamKeepsOrderAndFollowsCursors() {
        List<String> seen = new java.util.ArrayList<>();
        Stream<Integer> stream = Pages.streamPrefetching((pageNumber, cursor) -> {
            seen.add(pageNumber + ":" + cursor);
            String nextCursor = pageNumber < 3 ? "c" + pageNumber : null;
            return CompletableFuture.completedFuture(new ApiResponse<>(List.of(pageNumber * 10, pageNumber * 10 + 1),
                    new Pagination(2, 2, pageNumber, null, nextCursor, nextCursor != null), null, null));
        });

        assertThat(stream).containsExactly(10, 11, 20, 21, 30, 31);
        assertThat(seen).containsExactly("1:null", "2:c1", "3:c2");
    }

    @Test
    void prefetchesAtMostOnePageAhead() {
        AtomicInteger started = new AtomicInteger();
        Stream<Integer> stream = Pages.streamPrefetching((pageNumber, cursor) -> {
            started.incrementAndGet();
            return CompletableFuture.completedFuture(page(List.of(pageNumber), pageNumber, true)); // infinite
        });

        var it = stream.iterator();
        assertThat(started.get()).isEqualTo(1);          // first page requested eagerly
        assertThat(it.next()).isEqualTo(1);
        assertThat(started.get()).isEqualTo(2);          // page 2 in flight while page 1 is consumed
        assertThat(it.next()).isEqualTo(2);
        assertThat(started.get()).isEqualTo(3);          // never more than one ahead
    }

    @Test
    void closingTheStreamCancelsThePendingLookahead() throws Exception {
        CountDownLatch release = new CountDownLatch(1);
        List<CompletableFuture<ApiResponse<List<Integer>>>> futures = new java.util.ArrayList<>();
        Stream<Integer> stream = Pages.streamPrefetching((pageNumber, cursor) -> {
            CompletableFuture<ApiResponse<List<Integer>>> f = pageNumber == 1
                    ? CompletableFuture.completedFuture(page(List.of(1), 1, true))
                    : CompletableFuture.supplyAsync(() -> {
                        try {
                            release.await();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        return page(List.of(2), 2, false);
                    });
            futures.add(f);
            return f;
        });

        try (stream) {
            assertThat(stream.limit(1).toList()).containsExactly(1);
        }

        assertThat(futures).hasSize(2);
        assertThat(futures.get(1).isCancelled()).isTrue();
        release.countDown();
    }

    @Test
    void lookaheadFailureSurfacesUnwrappedOnThePullThatNeedsIt() {
        Stream<Integer> stream = Pages.streamPrefetching((pageNumber, cursor) -> pageNumber == 1
                ? CompletableFuture.completedFuture(page(List.of(1), 1, true))
                : CompletableFuture.failedFuture(new IllegalStateException("page 2 exploded")));

        var it = stream.iterator();
        assertThat(it.next()).isEqualTo(1);
        assertThatThrownBy(it::hasNext).isInstanceOf(IllegalStateException.class).hasMessage("page 2 exploded");
    }
}
