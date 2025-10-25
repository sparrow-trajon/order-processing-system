package com.ordermanagement.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator for application readiness.
 * Kubernetes uses this to determine if the application is ready to accept traffic.
 *
 * Design Pattern: State Pattern
 * Use Case: Kubernetes readiness probe
 *
 * Behavior:
 * - Returns DOWN until ApplicationReadyEvent is received
 * - Returns UP after application is fully initialized
 * - Prevents traffic routing to uninitialized pods
 */
@Component("applicationReadiness")
public class ApplicationReadinessHealthIndicator implements HealthIndicator {

    private volatile boolean ready = false;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        this.ready = true;
    }

    @Override
    public Health health() {
        if (ready) {
            return Health.up()
                    .withDetail("status", "Application is ready to accept traffic")
                    .build();
        } else {
            return Health.down()
                    .withDetail("status", "Application is starting up")
                    .build();
        }
    }
}
