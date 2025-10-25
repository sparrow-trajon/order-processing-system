package com.ordermanagement.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for business rules.
 * Externalizes business constraints to application.properties for easy modification.
 *
 * Design Pattern: Configuration Pattern
 * SOLID Principle: Open/Closed - Rules can be changed without modifying code
 */
@Configuration
@ConfigurationProperties(prefix = "business.order")
@Getter
@Setter
public class BusinessRulesProperties {

    /**
     * Maximum number of different items allowed in a single order.
     * Default: 100
     */
    private int maxItems = 100;

    /**
     * Maximum quantity allowed for a single order item.
     * Default: 10,000
     */
    private int maxQuantityPerItem = 10000;

    /**
     * Maximum unit price allowed for a single item (in currency units).
     * Default: 1,000,000
     */
    private String maxUnitPrice = "1000000";

    /**
     * Interval in milliseconds for the order status update scheduler.
     * Default: 300,000 (5 minutes)
     */
    private long schedulerIntervalMs = 300000;
}
