package com.ordermanagement.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration class to enable scheduling support.
 * Allows @Scheduled annotations to work in the application.
 *
 * Design Patterns:
 * - Configuration Pattern - Centralized configuration
 *
 * SOLID Principles:
 * - Single Responsibility: Only handles scheduler configuration
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {
    // This class enables scheduling functionality
    // The @EnableScheduling annotation allows @Scheduled methods to run
}
