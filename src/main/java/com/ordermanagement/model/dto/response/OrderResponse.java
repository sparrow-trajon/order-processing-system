package com.ordermanagement.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for order response.
 * Returns complete order details to the client.
 *
 * Design Pattern: Data Transfer Object (DTO) Pattern
 * SOLID Principle: Single Responsibility - Only handles data transfer for order responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Complete order details in the response")
public class OrderResponse {

    @Schema(description = "Unique identifier of the order", example = "1")
    private Long id;

    @Schema(description = "Unique order number", example = "ORD-1729766400000-5678")
    private String orderNumber;

    @Schema(description = "Customer's full name", example = "John Doe")
    private String customerName;

    @Schema(description = "Customer's email address", example = "john.doe@example.com")
    private String customerEmail;

    @Schema(description = "Total amount of the order", example = "2650.00")
    private BigDecimal totalAmount;

    @Schema(description = "Current status code of the order", example = "PENDING")
    private String status;

    @Schema(description = "List of items in the order")
    private List<OrderItemResponse> items;

    @Schema(description = "Timestamp when the order was created", example = "2025-10-24T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the order was last updated", example = "2025-10-24T10:35:00")
    private LocalDateTime updatedAt;
}
