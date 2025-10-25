package com.ordermanagement.validator.impl;

import com.ordermanagement.config.BusinessRulesProperties;
import com.ordermanagement.model.dto.request.CreateOrderRequest;
import com.ordermanagement.model.dto.request.OrderItemRequest;
import com.ordermanagement.repository.OrderRepository;
import com.ordermanagement.validator.OrderValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of OrderValidator interface.
 * Provides business-level validation for orders.
 *
 * Design Patterns:
 * - Strategy Pattern - Implements validation strategy
 * - Template Method Pattern - Defines validation flow
 *
 * SOLID Principles:
 * - Single Responsibility: Only handles order validation
 * - Dependency Inversion: Depends on OrderRepository abstraction
 */
@Component
@Slf4j
public class OrderValidatorImpl implements OrderValidator {

    private final OrderRepository orderRepository;
    private final BusinessRulesProperties businessRules;

    @Autowired
    public OrderValidatorImpl(
            OrderRepository orderRepository,
            BusinessRulesProperties businessRules) {
        this.orderRepository = orderRepository;
        this.businessRules = businessRules;
    }

    /**
     * Validates order creation request.
     * Performs business-level validation beyond basic field validation.
     */
    @Override
    public void validateCreateOrderRequest(CreateOrderRequest request) {
        log.debug("Validating order creation request for customer: {}", request.getCustomerName());

        // Validate customer information
        validateCustomerInfo(request);

        // Validate order items
        validateOrderItems(request);

        log.debug("Order creation request validation successful");
    }

    /**
     * Validates if an order number is unique.
     */
    @Override
    public void validateUniqueOrderNumber(String orderNumber) {
        if (orderNumber == null || orderNumber.isBlank()) {
            throw new IllegalArgumentException("Order number cannot be null or empty");
        }

        if (orderRepository.existsByOrderNumber(orderNumber)) {
            throw new IllegalArgumentException(
                    String.format("Order with number %s already exists", orderNumber)
            );
        }
    }

    /**
     * Validates customer information.
     *
     * @param request The order creation request
     */
    private void validateCustomerInfo(CreateOrderRequest request) {
        if (request.getCustomerName() == null || request.getCustomerName().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name is required");
        }

        if (request.getCustomerEmail() == null || request.getCustomerEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer email is required");
        }

        // Email format validation (basic check, @Email annotation handles detailed validation)
        if (!request.getCustomerEmail().contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    /**
     * Validates order items.
     *
     * @param request The order creation request
     */
    private void validateOrderItems(CreateOrderRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        // Check for duplicate product codes
        Set<String> productCodes = new HashSet<>();
        for (OrderItemRequest item : request.getItems()) {
            if (item.getProductCode() != null) {
                String productCode = item.getProductCode().trim().toUpperCase();
                if (!productCodes.add(productCode)) {
                    throw new IllegalArgumentException(
                            String.format("Duplicate product code found: %s. " +
                                        "Each product can only appear once in an order. " +
                                        "To order multiple quantities, increase the quantity field.",
                                    item.getProductCode())
                    );
                }
            }
        }

        // Validate each item
        for (int i = 0; i < request.getItems().size(); i++) {
            OrderItemRequest item = request.getItems().get(i);
            validateOrderItem(item, i);
        }

        // Business rule: Maximum number of different items in an order
        if (request.getItems().size() > businessRules.getMaxItems()) {
            throw new IllegalArgumentException(
                    String.format("Order cannot contain more than %d different items",
                            businessRules.getMaxItems())
            );
        }
    }

    /**
     * Validates a single order item.
     *
     * @param item The order item to validate
     * @param index The index of the item in the list (for error reporting)
     */
    private void validateOrderItem(OrderItemRequest item, int index) {
        String itemPrefix = String.format("Item at index %d: ", index);

        if (item.getProductName() == null || item.getProductName().trim().isEmpty()) {
            throw new IllegalArgumentException(itemPrefix + "Product name is required");
        }

        if (item.getProductCode() == null || item.getProductCode().trim().isEmpty()) {
            throw new IllegalArgumentException(itemPrefix + "Product code is required");
        }

        if (item.getQuantity() == null || item.getQuantity() < 1) {
            throw new IllegalArgumentException(itemPrefix + "Quantity must be at least 1");
        }

        if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(itemPrefix + "Unit price must be greater than 0");
        }

        // Business rule: Maximum quantity per item
        if (item.getQuantity() > businessRules.getMaxQuantityPerItem()) {
            throw new IllegalArgumentException(
                    itemPrefix + "Quantity cannot exceed " + businessRules.getMaxQuantityPerItem()
            );
        }

        // Business rule: Maximum unit price
        BigDecimal maxPrice = new BigDecimal(businessRules.getMaxUnitPrice());
        if (item.getUnitPrice().compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException(
                    itemPrefix + "Unit price cannot exceed " + businessRules.getMaxUnitPrice()
            );
        }
    }
}
