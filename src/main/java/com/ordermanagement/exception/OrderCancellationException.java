package com.ordermanagement.exception;

/**
 * Exception thrown when an order cannot be cancelled due to business rules.
 * For example, only PENDING orders can be cancelled.
 *
 * Design Pattern: Exception Hierarchy Pattern
 * SOLID Principle: Single Responsibility - Handles order cancellation violation scenarios only
 */
public class OrderCancellationException extends RuntimeException {

    public OrderCancellationException(String message) {
        super(message);
    }

    public OrderCancellationException(String message, Throwable cause) {
        super(message, cause);
    }
}
