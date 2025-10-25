package com.ordermanagement.event;

import com.ordermanagement.model.entity.Order;
import com.ordermanagement.model.entity.OrderStatusEntity;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when an order's status changes.
 * Enables event-driven architecture for status-based workflows.
 *
 * Design Pattern: Observer Pattern
 * Use Cases:
 * - Send status update email to customer
 * - Trigger shipping when status becomes SHIPPED
 * - Update external systems
 * - Log status changes for audit
 */
@Getter
public class OrderStatusChangedEvent extends ApplicationEvent {

    private final Order order;
    private final OrderStatusEntity oldStatus;
    private final OrderStatusEntity newStatus;
    private final String orderNumber;

    public OrderStatusChangedEvent(Object source, Order order, OrderStatusEntity oldStatus, OrderStatusEntity newStatus) {
        super(source);
        this.order = order;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.orderNumber = order.getOrderNumber() != null ? order.getOrderNumber().getValue() : null;
    }
}
