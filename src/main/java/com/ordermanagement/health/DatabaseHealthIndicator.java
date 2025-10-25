package com.ordermanagement.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Custom health indicator to check database connectivity.
 * Used by Kubernetes readiness/liveness probes.
 *
 * Design Pattern: Strategy Pattern (implements HealthIndicator)
 * Use Case: Kubernetes health checks
 */
@Component("database")
@Slf4j
@RequiredArgsConstructor
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(1)) {
                return Health.up()
                        .withDetail("database", "H2")
                        .withDetail("status", "Connected")
                        .withDetail("validationQuery", "Connection.isValid()")
                        .build();
            } else {
                return Health.down()
                        .withDetail("database", "H2")
                        .withDetail("status", "Connection invalid")
                        .build();
            }
        } catch (SQLException e) {
            log.error("Database health check failed", e);
            return Health.down()
                    .withDetail("database", "H2")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
