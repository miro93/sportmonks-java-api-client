package io.github.miro93.sportmonks.core.paging;

import io.github.miro93.sportmonks.core.response.ApiResponse;
import io.github.miro93.sportmonks.core.response.Pagination;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class PagesTest {

    private static ApiResponse<List<Integer>> page(List<Integer> data, int current, boolean hasMore) {
        return new ApiResponse<>(data,
                new Pagination(data.size(), 2, current, hasMore ? "next" : null, hasMore),
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
}
