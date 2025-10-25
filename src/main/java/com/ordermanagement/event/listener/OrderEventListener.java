package com.ordermanagement.event.listener;

import com.ordermanagement.event.OrderCancelledEvent;
import com.ordermanagement.event.OrderCreatedEvent;
import com.ordermanagement.event.OrderStatusChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Example event listener that reacts to order events.
 * Demonstrates event-driven architecture and loose coupling.
 *
 * Design Pattern: Observer Pattern
 * Benefits:
 * - OrderService doesn't need to know about email/analytics/inventory
 * - Easy to add new listeners without modifying existing code
 * - Can process events asynchronously
 *
 * In production, you might have separate listeners for:
 * - EmailNotificationListener
 * - InventoryUpdateListener
 * - AnalyticsListener
 * - PaymentListener
 * - ShippingListener
 */
@Component
@Slf4j
public class OrderEventListener {

    /**
     * Handles order creation events.
     * In production: Send confirmation email, reserve inventory, track analytics
     */
    @EventListener
    @Async
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("=== ORDER CREATED EVENT ===");
        log.info("Order Number: {}", event.getOrderNumber());
        log.info("Customer Email: {}", event.getCustomerEmail());
        log.info("Total Amount: {}", event.getOrder().getTotalAmount());
        log.info("Items Count: {}", event.getOrder().getItemCount());

        // TODO: In production, implement:
        // - emailService.sendOrderConfirmation(event.getOrder());
        // - inventoryService.reserveItems(event.getOrder().getOrderItems());
        // - analyticsService.trackOrderCreation(event.getOrder());

        log.info("Order creation event processed");
    }

    /**
     * Handles order status change events.
     * In production: Send status update email, trigger workflows
     */
    @EventListener
    @Async
    public void handleOrderStatusChanged(OrderStatusChangedEvent event) {
        log.info("=== ORDER STATUS CHANGED EVENT ===");
        log.info("Order Number: {}", event.getOrderNumber());
        log.info("Status Change: {} → {}", event.getOldStatus(), event.getNewStatus());

        // Different actions based on new status code
        String statusCode = event.getNewStatus() != null ? event.getNewStatus().getCode() : "";
        switch (statusCode) {
            case "PROCESSING":
                log.info("Order is being processed. Notify warehouse.");
                // warehouseService.notifyNewOrder(event.getOrder());
                break;
            case "SHIPPED":
                log.info("Order shipped. Send tracking info to customer.");
                // emailService.sendShippingNotification(event.getOrder());
                break;
            case "DELIVERED":
                log.info("Order delivered. Request customer feedback.");
                // feedbackService.requestReview(event.getOrder());
                break;
            default:
                log.debug("No specific action for status: {}", event.getNewStatus());
        }

        log.info("Order status change event processed");
    }

    /**
     * Handles order cancellation events.
     * In production: Refund payment, release inventory, send cancellation email
     */
    @EventListener
    @Async
    public void handleOrderCancelled(OrderCancelledEvent event) {
        log.info("=== ORDER CANCELLED EVENT ===");
        log.info("Order Number: {}", event.getOrderNumber());
        log.info("Customer Email: {}", event.getCustomerEmail());
        log.info("Cancellation Reason: {}", event.getReason());

        // TODO: In production, implement:
        // - paymentService.refund(event.getOrderId());
        // - inventoryService.releaseReservedItems(event.getOrderId());
        // - emailService.sendCancellationConfirmation(event.getCustomerEmail());
        // - analyticsService.trackCancellation(event.getOrderId(), event.getReason());

        log.info("Order cancellation event processed");
    }
}
