package com.ordermanagement.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for creating an order item.
 * Used in the API layer to transfer order item data from client to server.
 *
 * Design Pattern: Data Transfer Object (DTO) Pattern
 * SOLID Principle: Single Responsibility - Only handles data transfer
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for creating an order item")
public class OrderItemRequest {

    @Schema(description = "Name of the product", example = "Laptop", required = true)
    @NotBlank(message = "Product name is required")
    @Size(min = 1, max = 200, message = "Product name must be between 1 and 200 characters")
    private String productName;

    @Schema(description = "Unique product code", example = "LAPTOP-001", required = true)
    @NotBlank(message = "Product code is required")
    @Size(min = 1, max = 50, message = "Product code must be between 1 and 50 characters")
    private String productCode;

    @Schema(description = "Quantity of the product", example = "2", required = true, minimum = "1", maximum = "10000")
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 10000, message = "Quantity cannot exceed 10000")
    private Integer quantity;

    @Schema(description = "Unit price of the product", example = "1200.00", required = true)
    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.01", message = "Unit price must be greater than 0")
    @DecimalMax(value = "1000000.00", message = "Unit price cannot exceed 1000000")
    private BigDecimal unitPrice;
}
