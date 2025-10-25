package com.ordermanagement.exception;

/**
 * Exception thrown when an order is not found in the system.
 *
 * Design Pattern: Exception Hierarchy Pattern
 * SOLID Principle: Single Responsibility - Handles order not found scenarios only
 */
public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(String message) {
        super(message);
    }

    public OrderNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public OrderNotFoundException(Long orderId) {
        super(String.format("Order not found with ID: %d", orderId));
    }
}
