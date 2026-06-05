package io.github.miro93.sportmonks.core.retry;

import org.junit.jupiter.api.Test;

import java.util.function.IntPredicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RetryPolicyTest {

    @Test
    void defaultsRetry429AndAll5xx() {
        RetryPolicy policy = RetryPolicy.defaults();

        assertThat(policy.isRetryableStatus(429)).isTrue();
        assertThat(policy.isRetryableStatus(500)).isTrue();
        assertThat(policy.isRetryableStatus(502)).isTrue();
        assertThat(policy.isRetryableStatus(503)).isTrue();
        assertThat(policy.isRetryableStatus(504)).isTrue();
        assertThat(policy.isRetryableStatus(200)).isFalse();
        assertThat(policy.isRetryableStatus(404)).isFalse();
    }

    @Test
    void noneKeepsDefaultRetryablePredicate() {
        // none() only changes attempts to 1; the predicate is unchanged.
        assertThat(RetryPolicy.none().maxAttempts()).isEqualTo(1);
        assertThat(RetryPolicy.none().isRetryableStatus(503)).isTrue();
    }

    @Test
    void builderDefaultsMatchDefaultsFactory() {
        RetryPolicy policy = RetryPolicy.builder().build();

        assertThat(policy.maxAttempts()).isEqualTo(3);
        assertThat(policy.isRetryableStatus(429)).isTrue();
        assertThat(policy.isRetryableStatus(500)).isTrue();
        assertThat(policy.isRetryableStatus(404)).isFalse();
    }

    @Test
    void retryableStatusesOverridesPredicateExactly() {
        RetryPolicy policy = RetryPolicy.builder()
                .retryableStatuses(429, 503)
                .build();

        assertThat(policy.isRetryableStatus(429)).isTrue();
        assertThat(policy.isRetryableStatus(503)).isTrue();
        assertThat(policy.isRetryableStatus(500)).isFalse();
        assertThat(policy.isRetryableStatus(502)).isFalse();
        assertThat(policy.isRetryableStatus(504)).isFalse();
    }

    @Test
    void builderSetsMaxAttemptsAndBackoff() {
        Backoff backoff = new Backoff(java.time.Duration.ofMillis(100),
                java.time.Duration.ofSeconds(5), 3.0);
        RetryPolicy policy = RetryPolicy.builder()
                .maxAttempts(5)
                .backoff(backoff)
                .build();

        assertThat(policy.maxAttempts()).isEqualTo(5);
        assertThat(policy.backoff()).isSameAs(backoff);
    }

    @Test
    void builderRejectsMaxAttemptsBelowOne() {
        assertThatThrownBy(() -> RetryPolicy.builder().maxAttempts(0).build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void threeArgCtorUsesCustomPredicate() {
        IntPredicate onlyTeapot = status -> status == 418;
        RetryPolicy policy = new RetryPolicy(2, Backoff.defaults(), onlyTeapot);

        assertThat(policy.isRetryableStatus(418)).isTrue();
        assertThat(policy.isRetryableStatus(503)).isFalse();
    }
}
