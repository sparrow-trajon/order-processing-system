package com.ordermanagement.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for error responses.
 * Provides standardized error information to clients.
 *
 * Design Pattern: Data Transfer Object (DTO) Pattern
 * SOLID Principle: Single Responsibility - Only handles error response data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Error response object with detailed error information")
public class ErrorResponse {

    @Schema(description = "Timestamp when the error occurred", example = "2025-10-24T10:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Error type/category", example = "Bad Request")
    private String error;

    @Schema(description = "Detailed error message", example = "Order not found with ID: 123")
    private String message;

    @Schema(description = "API endpoint path where error occurred", example = "/api/v1/orders/123")
    private String path;

    @Schema(description = "Field-level validation errors (if applicable)")
    private List<FieldError> fieldErrors;

    /**
     * Nested class for field-level validation errors.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Field-level validation error details")
    public static class FieldError {

        @Schema(description = "Name of the field that failed validation", example = "customerEmail")
        private String field;

        @Schema(description = "Validation error message", example = "Invalid email format")
        private String message;

        @Schema(description = "Value that was rejected", example = "invalid-email")
        private Object rejectedValue;
    }
}
