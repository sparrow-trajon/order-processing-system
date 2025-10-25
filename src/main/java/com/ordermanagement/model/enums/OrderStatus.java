package com.ordermanagement.model.enums;

/**
 * Enum representing the various statuses an order can have.
 * Defines the order lifecycle: PENDING → PROCESSING → SHIPPED → DELIVERED
 *
 * Design Pattern: State Pattern (implicit)
 * SOLID Principle: Single Responsibility - Handles status transitions only
 */
public enum OrderStatus {
    PENDING("Pending"),
    PROCESSING("Processing"),
    SHIPPED("Shipped"),
    DELIVERED("Delivered");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Validates if a transition from current status to new status is allowed.
     * Implements business rule: Status can only move forward in the lifecycle.
     *
     * @param newStatus The target status to transition to
     * @return true if transition is valid, false otherwise
     */
    public boolean canTransitionTo(OrderStatus newStatus) {
        return switch (this) {
            case PENDING -> newStatus == PROCESSING;
            case PROCESSING -> newStatus == SHIPPED;
            case SHIPPED -> newStatus == DELIVERED;
            case DELIVERED -> false; // Final state - no transitions allowed
        };
    }

    /**
     * Returns the next logical status in the order lifecycle.
     *
     * @return The next status, or current status if already at final state
     */
    public OrderStatus getNextStatus() {
        return switch (this) {
            case PENDING -> PROCESSING;
            case PROCESSING -> SHIPPED;
            case SHIPPED -> DELIVERED;
            case DELIVERED -> DELIVERED; // Already at final state
        };
    }
}
