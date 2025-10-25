package com.ordermanagement.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration for asynchronous event processing.
 * Enables @Async annotation for non-blocking event handling.
 *
 * Benefits:
 * - Event listeners don't block the main transaction
 * - Better performance for high-volume operations
 * - Improved scalability
 *
 * Design Pattern: Configuration Pattern
 * SOLID Principle: Single Responsibility - Configures async processing only
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    /**
     * Configures thread pool for async event processing.
     *
     * @return Executor for async tasks
     */
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);  // Minimum threads
        executor.setMaxPoolSize(10);  // Maximum threads
        executor.setQueueCapacity(100);  // Queue size before rejecting
        executor.setThreadNamePrefix("event-");
        executor.initialize();
        return executor;
    }
}
