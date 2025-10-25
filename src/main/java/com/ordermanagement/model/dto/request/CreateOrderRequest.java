package com.ordermanagement.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for creating a new order.
 * Contains customer information and order items.
 *
 * Design Pattern: Data Transfer Object (DTO) Pattern
 * SOLID Principle: Single Responsibility - Only handles data transfer for order creation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for creating a new order")
public class CreateOrderRequest {

    @Schema(description = "Customer's full name", example = "John Doe", required = true)
    @NotBlank(message = "Customer name is required")
    @Size(min = 2, max = 100, message = "Customer name must be between 2 and 100 characters")
    private String customerName;

    @Schema(description = "Customer's email address", example = "john.doe@example.com", required = true)
    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email format")
    private String customerEmail;

    @Schema(description = "List of items in the order", required = true)
    @NotNull(message = "Order items are required")
    @NotEmpty(message = "Order must have at least one item")
    @Valid
    private List<OrderItemRequest> items;
}
