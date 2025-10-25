package com.ordermanagement.service;

import com.ordermanagement.model.dto.request.CreateOrderRequest;
import com.ordermanagement.model.dto.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for order operations.
 * Defines the contract for order management business logic.
 *
 * Design Patterns:
 * - Facade Pattern - Provides simplified interface to complex subsystems
 * - Service Layer Pattern - Encapsulates business logic
 *
 * SOLID Principles:
 * - Interface Segregation: Focused interface for order operations
 * - Dependency Inversion: Depend on abstraction, not implementation
 * - Single Responsibility: Defines order business operations only
 */
public interface OrderService {

    /**
     * Creates a new order.
     *
     * @param request The order creation request containing customer and item details
     * @return OrderResponse with created order details
     * @throws IllegalArgumentException if validation fails
     */
    OrderResponse createOrder(CreateOrderRequest request);

    /**
     * Retrieves an order by its ID.
     *
     * @param id The order ID
     * @return OrderResponse with order details
     * @throws com.ordermanagement.exception.OrderNotFoundException if order not found
     */
    OrderResponse getOrderById(Long id);

    /**
     * Retrieves all orders in the system.
     *
     * @return List of all orders
     * @deprecated Use {@link #getAllOrdersPaginated(Pageable)} instead for better performance
     */
    @Deprecated
    List<OrderResponse> getAllOrders();

    /**
     * Retrieves all orders in the system with pagination support.
     * This is the recommended method for production use.
     *
     * @param pageable Pagination and sorting parameters
     * @return Page of orders
     */
    Page<OrderResponse> getAllOrdersPaginated(Pageable pageable);

    /**
     * Retrieves orders filtered by status.
     *
     * @param statusCode The order status code to filter by (e.g., "PENDING", "PROCESSING")
     * @return List of orders with the specified status
     * @deprecated Use {@link #getOrdersByStatusPaginated(String, Pageable)} instead
     */
    @Deprecated
    List<OrderResponse> getOrdersByStatus(String statusCode);

    /**
     * Retrieves orders filtered by status with pagination support.
     *
     * @param statusCode The order status code to filter by (e.g., "PENDING", "PROCESSING")
     * @param pageable Pagination and sorting parameters
     * @return Page of orders with the specified status
     */
    Page<OrderResponse> getOrdersByStatusPaginated(String statusCode, Pageable pageable);

    /**
     * Updates the status of an order.
     *
     * @param id The order ID
     * @param newStatusCode The new status code to set (e.g., "PROCESSING", "CONFIRMED")
     * @return OrderResponse with updated order details
     * @throws com.ordermanagement.exception.OrderNotFoundException if order not found
     * @throws com.ordermanagement.exception.InvalidOrderStatusException if status transition is invalid
     */
    OrderResponse updateOrderStatus(Long id, String newStatusCode);

    /**
     * Cancels an order.
     * Business rule: Only orders in PENDING status can be cancelled.
     *
     * @param id The order ID to cancel
     * @throws com.ordermanagement.exception.OrderNotFoundException if order not found
     * @throws com.ordermanagement.exception.OrderCancellationException if order cannot be cancelled
     */
    void cancelOrder(Long id);

    /**
     * Processes scheduled status update from PENDING to PROCESSING.
     * This method is called by the scheduler every 5 minutes.
     * Updates all orders with PENDING status to PROCESSING status.
     */
    void processScheduledStatusUpdate();
}
