package com.ordermanagement.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Custom metrics for order management operations.
 * Exposes metrics to Prometheus for monitoring and alerting.
 *
 * Metrics Categories:
 * - Counters: Total counts (orders created, cancelled, etc.)
 * - Timers: Operation durations (order creation time, etc.)
 * - Gauges: Current values (pending orders count, etc.)
 *
 * Design Pattern: Facade Pattern
 * SOLID Principle: Single Responsibility - Only handles metrics collection
 *
 * Prometheus Endpoint: /actuator/prometheus
 */
@Component
public class OrderMetrics {

    private final Counter ordersCreatedCounter;
    private final Counter ordersCancelledCounter;
    private final Counter ordersStatusChangedCounter;
    private final Timer orderCreationTimer;
    private final Timer orderUpdateTimer;
    private final MeterRegistry meterRegistry;

    public OrderMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Counter: Total orders created
        this.ordersCreatedCounter = Counter.builder("orders.created.total")
                .description("Total number of orders created")
                .tag("application", "order-management")
                .register(meterRegistry);

        // Counter: Total orders cancelled
        this.ordersCancelledCounter = Counter.builder("orders.cancelled.total")
                .description("Total number of orders cancelled")
                .tag("application", "order-management")
                .register(meterRegistry);

        // Counter: Total order status changes
        this.ordersStatusChangedCounter = Counter.builder("orders.status.changed.total")
                .description("Total number of order status changes")
                .tag("application", "order-management")
                .register(meterRegistry);

        // Timer: Order creation duration
        this.orderCreationTimer = Timer.builder("orders.creation.duration")
                .description("Time taken to create an order")
                .tag("application", "order-management")
                .register(meterRegistry);

        // Timer: Order update duration
        this.orderUpdateTimer = Timer.builder("orders.update.duration")
                .description("Time taken to update an order")
                .tag("application", "order-management")
                .register(meterRegistry);
    }

    /**
     * Increment orders created counter
     */
    public void incrementOrdersCreated() {
        ordersCreatedCounter.increment();
    }

    /**
     * Increment orders cancelled counter
     */
    public void incrementOrdersCancelled() {
        ordersCancelledCounter.increment();
    }

    /**
     * Increment status changed counter with specific status tags
     */
    public void incrementStatusChanged(String fromStatus, String toStatus) {
        Counter.builder("orders.status.transition.total")
                .description("Order status transitions")
                .tag("from", fromStatus)
                .tag("to", toStatus)
                .register(meterRegistry)
                .increment();
    }

    /**
     * Record order creation time
     */
    public void recordOrderCreationTime(long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        orderCreationTimer.record(duration, TimeUnit.MILLISECONDS);
    }

    /**
     * Record order update time
     */
    public void recordOrderUpdateTime(long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        orderUpdateTimer.record(duration, TimeUnit.MILLISECONDS);
    }

    /**
     * Get meter registry for custom metrics
     */
    public MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }
}
