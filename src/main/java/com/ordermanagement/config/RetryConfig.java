package com.ordermanagement.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.classify.Classifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.context.annotation.Bean;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for Spring Retry mechanism.
 * Enables automatic retry on transient failures.
 *
 * Use Cases:
 * - Database connection failures (temporary network issues)
 * - External API calls (rate limiting, timeouts)
 * - Distributed transactions
 *
 * Design Pattern: Aspect-Oriented Programming (AOP)
 * SOLID Principle: Single Responsibility - Configures retry logic only
 */
@Configuration
@EnableRetry
@Slf4j
public class RetryConfig {

    /**
     * Configures RetryTemplate for programmatic retry usage.
     * This can be injected anywhere in the application.
     *
     * @return Configured RetryTemplate
     */
    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // Configure which exceptions should trigger retry
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(SQLException.class, true);
        retryableExceptions.put(org.springframework.dao.DataAccessResourceFailureException.class, true);
        retryableExceptions.put(org.springframework.dao.TransientDataAccessException.class, true);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(3, retryableExceptions);
        retryTemplate.setRetryPolicy(retryPolicy);

        // Configure backoff (wait 1 second between retries)
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(1000L);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        // Add retry listener for logging
        retryTemplate.registerListener(new CustomRetryListener());

        return retryTemplate;
    }

    /**
     * Custom retry listener for logging retry attempts.
     */
    @Slf4j
    static class CustomRetryListener implements RetryListener {

        @Override
        public <T, E extends Throwable> void onError(
                RetryContext context,
                RetryCallback<T, E> callback,
                Throwable throwable) {
            log.warn("Retry attempt {} failed for {}. Error: {}",
                    context.getRetryCount(),
                    context.getAttribute("context.name"),
                    throwable.getMessage());
        }
    }
}
