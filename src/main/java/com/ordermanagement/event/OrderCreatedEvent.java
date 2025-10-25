package com.ordermanagement.event;

import com.ordermanagement.model.entity.Order;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a new order is created.
 * Allows loose coupling - other components can react to order creation
 * without the OrderService knowing about them.
 *
 * Design Pattern: Observer Pattern (via Spring Events)
 * SOLID Principle: Open/Closed - Can add new listeners without modifying OrderService
 *
 * Example listeners:
 * - Send confirmation email
 * - Update inventory
 * - Trigger analytics
 * - Notify warehouse system
 */
@Getter
public class OrderCreatedEvent extends ApplicationEvent {

    private final Order order;
    private final String orderNumber;
    private final String customerEmail;

    public OrderCreatedEvent(Object source, Order order) {
        super(source);
        this.order = order;
        this.orderNumber = order.getOrderNumber() != null ? order.getOrderNumber().getValue() : null;
        this.customerEmail = order.getCustomer() != null && order.getCustomer().getEmail() != null
                ? order.getCustomer().getEmail().getAddress() : null;
    }
}
