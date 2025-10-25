package com.ordermanagement.service.scheduler;

import com.ordermanagement.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

/**
 * Scheduler component for automated order status updates.
 * Runs periodic tasks to update order statuses based on business rules.
 *
 * Design Patterns:
 * - Scheduler Pattern - Executes tasks at fixed intervals
 * - Separation of Concerns - Scheduling logic separate from business logic
 *
 * SOLID Principles:
 * - Single Responsibility: Only handles scheduled task execution
 * - Dependency Inversion: Depends on OrderService abstraction
 */
@Component
@Slf4j
public class OrderStatusScheduler {

    private final OrderService orderService;

    @Autowired
    public OrderStatusScheduler(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Scheduled task to update orders from PENDING to PROCESSING status.
     * Interval is configurable via application.properties.
     *
     * Business Rule: All orders in PENDING status should automatically
     * transition to PROCESSING status at regular intervals to simulate order processing.
     *
     * Default: 5 minutes (300,000 milliseconds)
     *
     * Retry Strategy:
     * - Retries up to 3 times on transient database failures
     * - Exponential backoff: 1s, 2s, 4s
     * - Handles SQLException and DataAccessException
     */
    @Scheduled(fixedRateString = "${business.order.scheduler-interval-ms}")
    @Retryable(
            retryFor = {SQLException.class, org.springframework.dao.DataAccessException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 5000)
    )
    public void updatePendingOrdersToProcessing() {
        log.info("Executing scheduled task: updatePendingOrdersToProcessing");

        try {
            orderService.processScheduledStatusUpdate();
            log.info("Scheduled task completed successfully");
        } catch (Exception e) {
            log.error("Error during scheduled status update: {}", e.getMessage(), e);
        }
    }

    /*
     * Alternative scheduler using cron expression for more flexible scheduling.
     * Uncomment to use cron-based scheduling instead of fixed rate.
     *
     * Example cron: 0 star-slash-5 star star star star = Every 5 minutes at the start of the minute
     */
    // @Scheduled(cron = "0 */5 * * * *")
    // public void updatePendingOrdersToProcessingCron() {
    //     log.info("Executing cron scheduled task: updatePendingOrdersToProcessing");
    //
    //     try {
    //         orderService.processScheduledStatusUpdate();
    //         log.info("Cron scheduled task completed successfully");
    //     } catch (Exception e) {
    //         log.error("Error during cron scheduled status update: {}", e.getMessage(), e);
    //     }
    // }
}
