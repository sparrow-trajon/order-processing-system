package com.ordermanagement.service.impl;

import com.ordermanagement.event.OrderCancelledEvent;
import com.ordermanagement.event.OrderCreatedEvent;
import com.ordermanagement.event.OrderStatusChangedEvent;
import com.ordermanagement.exception.OrderCancellationException;
import com.ordermanagement.exception.OrderNotFoundException;
import com.ordermanagement.mapper.OrderMapper;
import com.ordermanagement.metrics.OrderMetrics;
import com.ordermanagement.model.dto.request.CreateOrderRequest;
import com.ordermanagement.model.dto.response.OrderResponse;
import com.ordermanagement.model.entity.Order;
import com.ordermanagement.model.entity.OrderStatusEntity;
import com.ordermanagement.model.valueobject.OrderNumber;
import com.ordermanagement.repository.OrderRepository;
import com.ordermanagement.service.OrderService;
import com.ordermanagement.service.OrderStatusService;
import com.ordermanagement.service.OrderPricingService;
import com.ordermanagement.validator.OrderValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of OrderService interface.
 * Provides core business logic for order management.
 *
 * Design Patterns:
 * - Service Layer Pattern - Encapsulates business logic
 * - Facade Pattern - Simplifies complex operations
 * - Dependency Injection - All dependencies injected via constructor
 *
 * SOLID Principles:
 * - Single Responsibility: Manages order business logic only
 * - Open/Closed: Open for extension, closed for modification
 * - Dependency Inversion: Depends on abstractions (interfaces)
 * - Interface Segregation: Implements focused OrderService interface
 */
@Service
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderValidator orderValidator;
    private final ApplicationEventPublisher eventPublisher;
    private final OrderMetrics orderMetrics;
    private final OrderStatusService orderStatusService;
    private final OrderPricingService orderPricingService;

    @Autowired
    public OrderServiceImpl(
            OrderRepository orderRepository,
            OrderMapper orderMapper,
            OrderValidator orderValidator,
            ApplicationEventPublisher eventPublisher,
            OrderMetrics orderMetrics,
            OrderStatusService orderStatusService,
            OrderPricingService orderPricingService) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.orderValidator = orderValidator;
        this.eventPublisher = eventPublisher;
        this.orderMetrics = orderMetrics;
        this.orderStatusService = orderStatusService;
        this.orderPricingService = orderPricingService;
    }

    /**
     * Creates a new order with validation and order number generation.
     */
    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("Creating order for customer: {}", request.getCustomerName());

        try {
            // Validate the request
            orderValidator.validateCreateOrderRequest(request);

            // Map request to entity
            Order order = orderMapper.toEntity(request);

            // Generate unique order number using Value Object
            order.setOrderNumber(OrderNumber.generate());

            // Set default status from database
            order.setStatus(orderStatusService.getDefaultStatus());

            // Calculate pricing (handled by pricing service)
            orderPricingService.calculateOrderPricing(order);

            // Save order
            Order savedOrder = orderRepository.save(order);

            log.info("Order created successfully with order number: {}", savedOrder.getOrderNumber().getValue());

            // Publish event for other components to react (e.g., send email, update inventory)
            eventPublisher.publishEvent(new OrderCreatedEvent(this, savedOrder));

            // Record metrics
            orderMetrics.incrementOrdersCreated();
            orderMetrics.recordOrderCreationTime(startTime);

            return orderMapper.toResponse(savedOrder);
        } catch (Exception e) {
            log.error("Error creating order: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieves an order by ID with items eagerly loaded.
     */
    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        log.debug("Fetching order with ID: {}", id);

        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        return orderMapper.toResponse(order);
    }

    /**
     * Retrieves all orders in the system with items eagerly loaded.
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        log.debug("Fetching all orders");

        List<Order> orders = orderRepository.findAllWithItems();
        return orderMapper.toResponses(orders);
    }

    /**
     * Retrieves all orders in the system with pagination support.
     * Recommended for production use.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrdersPaginated(Pageable pageable) {
        log.debug("Fetching paginated orders - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Order> orders = orderRepository.findAllWithItemsPaginated(pageable);
        return orders.map(orderMapper::toResponse);
    }

    /**
     * Retrieves orders by status with items eagerly loaded for performance.
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByStatus(String statusCode) {
        log.debug("Fetching orders with status: {}", statusCode);

        if (statusCode == null) {
            throw new IllegalArgumentException("Status code cannot be null");
        }

        OrderStatusEntity status = orderStatusService.getStatusByCode(statusCode);
        List<Order> orders = orderRepository.findByStatusWithItems(status);
        return orderMapper.toResponses(orders);
    }

    /**
     * Retrieves orders by status with pagination support.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByStatusPaginated(String statusCode, Pageable pageable) {
        log.debug("Fetching paginated orders with status: {} - page: {}, size: {}",
                statusCode, pageable.getPageNumber(), pageable.getPageSize());

        if (statusCode == null) {
            throw new IllegalArgumentException("Status code cannot be null");
        }

        OrderStatusEntity status = orderStatusService.getStatusByCode(statusCode);
        Page<Order> orders = orderRepository.findByStatusWithItemsPaginated(status, pageable);
        return orders.map(orderMapper::toResponse);
    }

    /**
     * Updates order status with validation using OrderStatusService.
     */
    @Override
    public OrderResponse updateOrderStatus(Long id, String newStatusCode) {
        long startTime = System.currentTimeMillis();
        log.info("Updating order {} to status: {}", id, newStatusCode);

        if (newStatusCode == null) {
            throw new IllegalArgumentException("Status code cannot be null");
        }

        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        // Capture old status for event
        OrderStatusEntity oldStatus = order.getStatus();

        // Use OrderStatusService to handle transition (validates rules, creates history)
        orderStatusService.transitionStatus(order, newStatusCode, "SYSTEM", "Status update via API");

        Order updatedOrder = orderRepository.save(order);

        log.info("Order {} status updated to {}", id, newStatusCode);

        // Publish status change event
        OrderStatusEntity newStatus = order.getStatus();
        eventPublisher.publishEvent(new OrderStatusChangedEvent(this, updatedOrder, oldStatus, newStatus));

        // Record metrics
        orderMetrics.incrementStatusChanged(oldStatus.getCode(), newStatus.getCode());
        orderMetrics.recordOrderUpdateTime(startTime);

        return orderMapper.toResponse(updatedOrder);
    }

    /**
     * Cancels an order if it's in PENDING status.
     */
    @Override
    public void cancelOrder(Long id) {
        log.info("Cancelling order with ID: {}", id);

        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        // Check if order can be cancelled
        if (!order.canBeCancelled()) {
            throw new OrderCancellationException(
                    String.format("Order with ID %d cannot be cancelled. Current status: %s. " +
                                  "Only orders in PENDING status can be cancelled.",
                            id, order.getStatus())
            );
        }

        // Capture order details before deletion
        String orderNumber = order.getOrderNumber().getValue();
        String customerEmail = order.getCustomer() != null && order.getCustomer().getEmail() != null
                ? order.getCustomer().getEmail().getAddress()
                : "unknown";

        // Delete the order (cascade will delete items)
        orderRepository.delete(order);

        log.info("Order {} cancelled successfully", id);

        // Publish cancellation event for cleanup tasks (refund, release inventory, etc.)
        eventPublisher.publishEvent(new OrderCancelledEvent(
                this, id, orderNumber, customerEmail, "Customer requested cancellation"
        ));

        // Record metrics
        orderMetrics.incrementOrdersCancelled();
    }

    /**
     * Processes scheduled status update from PENDING to PROCESSING.
     * Called by scheduler every 5 minutes.
     * Uses batch update for optimal performance.
     */
    @Override
    public void processScheduledStatusUpdate() {
        log.info("Starting scheduled status update: PENDING → PROCESSING");

        try {
            // Get status entities from database
            OrderStatusEntity pendingStatus = orderStatusService.getStatusByCode("PENDING");
            OrderStatusEntity processingStatus = orderStatusService.getStatusByCode("PROCESSING");

            // Use batch update for better performance
            int updatedCount = orderRepository.updateOrderStatusBatch(
                    pendingStatus,
                    processingStatus
            );

            log.info("Scheduled status update completed. {} orders updated from PENDING to PROCESSING", updatedCount);
        } catch (Exception e) {
            log.error("Error during scheduled status update: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Generates a unique order number using UUID.
     * Format: ORD-{UUID-first-13-chars}
     *
     * UUID guarantees uniqueness without database queries,
     * eliminating race conditions in concurrent environments.
     *
     * @return Unique order number
     */
    private String generateOrderNumber() {
        String uuid = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        // Take first 13 characters for reasonable length: ORD-XXXXXXXXXXXX
        return "ORD-" + uuid.substring(0, 13);
    }
}
