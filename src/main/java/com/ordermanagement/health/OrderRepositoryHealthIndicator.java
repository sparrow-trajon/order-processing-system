package com.ordermanagement.health;

import com.ordermanagement.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator to verify order repository functionality.
 * Performs a simple count query to ensure database access works.
 *
 * Design Pattern: Strategy Pattern (implements HealthIndicator)
 * Use Case: Application-level health monitoring
 */
@Component("orderRepositoryHealth")
@Slf4j
@RequiredArgsConstructor
public class OrderRepositoryHealthIndicator implements HealthIndicator {

    private final OrderRepository orderRepository;

    @Override
    public Health health() {
        try {
            long orderCount = orderRepository.count();
            return Health.up()
                    .withDetail("repository", "OrderRepository")
                    .withDetail("status", "Accessible")
                    .withDetail("orderCount", orderCount)
                    .build();
        } catch (Exception e) {
            log.error("Order repository health check failed", e);
            return Health.down()
                    .withDetail("repository", "OrderRepository")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
