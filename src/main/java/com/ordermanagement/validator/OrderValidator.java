package com.ordermanagement.validator;

import com.ordermanagement.model.dto.request.CreateOrderRequest;

/**
 * Interface for order validation strategies.
 * Defines contract for order validation logic.
 *
 * Design Patterns:
 * - Strategy Pattern - Allows different validation strategies
 * - Interface Segregation Principle - Focused interface for validation
 *
 * SOLID Principles:
 * - Single Responsibility: Only defines validation contract
 * - Open/Closed: Open for extension with new validators, closed for modification
 * - Interface Segregation: Small, focused interface
 */
public interface OrderValidator {

    /**
     * Validates order creation request.
     * Throws IllegalArgumentException if validation fails.
     *
     * @param request The order creation request to validate
     * @throws IllegalArgumentException if validation fails
     */
    void validateCreateOrderRequest(CreateOrderRequest request);

    /**
     * Validates if an order number is unique.
     *
     * @param orderNumber The order number to validate
     * @throws IllegalArgumentException if order number already exists
     */
    void validateUniqueOrderNumber(String orderNumber);
}
