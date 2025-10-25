package com.ordermanagement.exception;

/**
 * Exception thrown when an invalid order status transition is attempted.
 *
 * Design Pattern: Exception Hierarchy Pattern
 * SOLID Principle: Single Responsibility - Handles invalid status transition scenarios only
 */
public class InvalidOrderStatusException extends RuntimeException {

    public InvalidOrderStatusException(String message) {
        super(message);
    }

    public InvalidOrderStatusException(String message, Throwable cause) {
        super(message, cause);
    }
}
