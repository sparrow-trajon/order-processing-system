package com.ordermanagement.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating order status.
 *
 * Design Pattern: Data Transfer Object (DTO) Pattern
 * SOLID Principle: Single Responsibility - Only handles status update data transfer
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for updating order status")
public class UpdateOrderStatusRequest {

    @Schema(description = "New status code for the order", example = "PROCESSING", required = true)
    @NotBlank(message = "Status code is required")
    private String status;
}
