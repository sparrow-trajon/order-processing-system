package com.ordermanagement.config;

import org.springframework.context.annotation.Configuration;

/**
 * Resilience4j configuration for circuit breaker pattern.
 * Provides fault tolerance and resilience for external service calls.
 *
 * Circuit Breaker States:
 * - CLOSED: Normal operation, requests pass through
 * - OPEN: Too many failures, requests fail immediately
 * - HALF_OPEN: Testing if service recovered
 *
 * Use Cases:
 * - External API calls (payment gateways, shipping APIs)
 * - Database operations under heavy load
 * - Microservice communication
 *
 * Design Pattern: Circuit Breaker Pattern
 * SOLID Principle: Single Responsibility - Resilience configuration only
 *
 * Configuration is done via application.properties:
 * - resilience4j.circuitbreaker.configs.default.*
 */
@Configuration
public class ResilienceConfig {

    /*
     * Circuit breaker is configured via application.properties.
     * See resilience4j.circuitbreaker.configs.default.* properties.
     *
     * ANNOTATION USAGE:
     *
     * @CircuitBreaker(name = "orderService", fallbackMethod = "fallbackCreateOrder")
     * public OrderResponse createOrder(CreateOrderRequest request) {
     *     // Implementation
     * }
     *
     * private OrderResponse fallbackCreateOrder(CreateOrderRequest request, Exception e) {
     *     log.error("Circuit breaker fallback triggered: {}", e.getMessage());
     *     // Return cached data or default response
     *     return new OrderResponse();
     * }
     *
     * PROGRAMMATIC USAGE:
     *
     * @Autowired
     * private CircuitBreakerRegistry circuitBreakerRegistry;
     *
     * public void someMethod() {
     *     CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("orderService");
     *     circuitBreaker.executeSupplier(() -> externalCall());
     * }
     */
}
