package com.ordermanagement.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when an order is cancelled.
 * Allows other components to react to cancellation.
 *
 * Design Pattern: Observer Pattern
 * Use Cases:
 * - Release reserved inventory
 * - Refund payment
 * - Send cancellation confirmation email
 * - Update analytics
 */
@Getter
public class OrderCancelledEvent extends ApplicationEvent {

    private final Long orderId;
    private final String orderNumber;
    private final String customerEmail;
    private final String reason;

    public OrderCancelledEvent(Object source, Long orderId, String orderNumber,
                               String customerEmail, String reason) {
        super(source);
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.customerEmail = customerEmail;
        this.reason = reason;
    }
}
