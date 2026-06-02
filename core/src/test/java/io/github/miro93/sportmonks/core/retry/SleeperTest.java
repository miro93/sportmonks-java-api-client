package io.github.miro93.sportmonks.core.retry;

import io.github.miro93.sportmonks.core.error.TransportException;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SleeperTest {

    @Test
    void realThrowsTransportExceptionAndPreservesInterruptOnInterruptedThread() {
        Thread.currentThread().interrupt(); // sleep() throws InterruptedException immediately
        try {
            assertThatThrownBy(() -> Sleeper.REAL.sleep(Duration.ofSeconds(10)))
                    .isInstanceOf(TransportException.class);
            assertThat(Thread.currentThread().isInterrupted()).isTrue();
        } finally {
            Thread.interrupted(); // clear the flag so we don't leak it to other tests
        }
    }
}
