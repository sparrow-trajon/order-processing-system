package com.ordermanagement.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for order item response.
 * Returns order item details to the client.
 *
 * Design Pattern: Data Transfer Object (DTO) Pattern
 * SOLID Principle: Single Responsibility - Only handles data transfer for responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Order item details in the response")
public class OrderItemResponse {

    @Schema(description = "Unique identifier of the order item", example = "1")
    private Long id;

    @Schema(description = "Name of the product", example = "Laptop")
    private String productName;

    @Schema(description = "Unique product code", example = "LAPTOP-001")
    private String productCode;

    @Schema(description = "Quantity of the product", example = "2")
    private Integer quantity;

    @Schema(description = "Unit price of the product", example = "1200.00")
    private BigDecimal unitPrice;

    @Schema(description = "Total price for this item (quantity × unit price)", example = "2400.00")
    private BigDecimal totalPrice;
}
