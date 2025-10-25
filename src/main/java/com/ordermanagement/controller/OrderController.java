package com.ordermanagement.controller;

import com.ordermanagement.model.dto.request.CreateOrderRequest;
import com.ordermanagement.model.dto.request.UpdateOrderStatusRequest;
import com.ordermanagement.model.dto.response.ErrorResponse;
import com.ordermanagement.model.dto.response.OrderResponse;
import com.ordermanagement.model.enums.OrderStatus;
import com.ordermanagement.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for order management operations.
 * Provides RESTful API endpoints for CRUD operations on orders.
 *
 * Design Patterns:
 * - Controller Pattern - Handles HTTP requests and responses
 * - Dependency Injection - OrderService injected via constructor
 * - RESTful API Design - Standard HTTP methods and status codes
 *
 * SOLID Principles:
 * - Single Responsibility: Handles HTTP layer only, delegates business logic to service
 * - Dependency Inversion: Depends on OrderService abstraction
 */
@RestController
@RequestMapping("/api/v1/orders")
@Slf4j
@Validated
@Tag(name = "Order Management", description = "APIs for managing orders in the e-commerce system")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Creates a new order.
     */
    @PostMapping
    @Operation(
            summary = "Create a new order",
            description = "Creates a new order with customer information and order items. " +
                         "The order will be created with PENDING status."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Order created successfully",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {

        log.info("Received request to create order for customer: {}", request.getCustomerName());

        OrderResponse response = orderService.createOrder(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves an order by ID.
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Get order by ID",
            description = "Retrieves detailed information about a specific order including all its items"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Order found",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<OrderResponse> getOrderById(
            @Parameter(description = "Order ID", required = true, example = "1")
            @PathVariable @Min(value = 1, message = "Order ID must be a positive number") Long id) {

        log.info("Received request to get order with ID: {}", id);

        OrderResponse response = orderService.getOrderById(id);

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all orders or filters by status (deprecated - use paginated endpoint).
     */
    @GetMapping
    @Deprecated
    @Operation(
            summary = "Get all orders or filter by status (deprecated)",
            description = "Retrieves all orders in the system. Optionally filter by order status. " +
                         "DEPRECATED: Use /api/v1/orders/paginated instead for better performance."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Orders retrieved successfully",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid status parameter",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<List<OrderResponse>> getAllOrders(
            @Parameter(description = "Filter by order status code (optional)", example = "PENDING")
            @RequestParam(required = false) String status) {

        log.info("Received request to get orders with status filter: {}", status);

        List<OrderResponse> responses;

        if (status != null) {
            responses = orderService.getOrdersByStatus(status);
        } else {
            responses = orderService.getAllOrders();
        }

        return ResponseEntity.ok(responses);
    }

    /**
     * Retrieves all orders with pagination support.
     */
    @GetMapping("/paginated")
    @Operation(
            summary = "Get all orders with pagination",
            description = "Retrieves orders in the system with pagination support. " +
                         "This is the recommended endpoint for production use. " +
                         "Default page size: 20, sorted by createdAt descending."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Orders retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid pagination parameters",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<Page<OrderResponse>> getAllOrdersPaginated(
            @Parameter(description = "Filter by order status code (optional)", example = "PENDING")
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC)
            Pageable pageable) {

        log.info("Received paginated request - page: {}, size: {}, status: {}",
                pageable.getPageNumber(), pageable.getPageSize(), status);

        Page<OrderResponse> responses;

        if (status != null) {
            responses = orderService.getOrdersByStatusPaginated(status, pageable);
        } else {
            responses = orderService.getAllOrdersPaginated(pageable);
        }

        return ResponseEntity.ok(responses);
    }

    /**
     * Updates the status of an order.
     */
    @PutMapping("/{id}/status")
    @Operation(
            summary = "Update order status",
            description = "Updates the status of an existing order. " +
                         "Valid transitions: PENDING→PROCESSING, PROCESSING→SHIPPED, SHIPPED→DELIVERED"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Order status updated successfully",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid status transition",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @Parameter(description = "Order ID", required = true, example = "1")
            @PathVariable @Min(value = 1, message = "Order ID must be a positive number") Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        log.info("Received request to update order {} to status: {}", id, request.getStatus());

        OrderResponse response = orderService.updateOrderStatus(id, request.getStatus());

        return ResponseEntity.ok(response);
    }

    /**
     * Cancels an order.
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Cancel an order",
            description = "Cancels an order. Only orders in PENDING status can be cancelled."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Order cancelled successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Order cannot be cancelled (not in PENDING status)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<Void> cancelOrder(
            @Parameter(description = "Order ID", required = true, example = "1")
            @PathVariable @Min(value = 1, message = "Order ID must be a positive number") Long id) {

        log.info("Received request to cancel order with ID: {}", id);

        orderService.cancelOrder(id);

        return ResponseEntity.noContent().build();
    }
}
