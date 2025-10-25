package com.ordermanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main application class for Order Management System.
 * Entry point for the Spring Boot application.
 *
 * Annotations:
 * - @SpringBootApplication: Enables auto-configuration, component scanning, and configuration
 * - @EnableJpaAuditing: Enables JPA auditing for @CreatedDate and @LastModifiedDate
 */
@SpringBootApplication
@EnableJpaAuditing
public class OrderManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderManagementApplication.class, args);
    }
}
