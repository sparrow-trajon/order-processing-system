package com.ordermanagement.model.valueobject;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Value Object representing a unique order number.
 * Immutable with built-in generation and validation.
 *
 * Design Pattern: Value Object Pattern
 * Use Case: Encapsulate order number generation and validation logic
 */
@Embeddable
@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class OrderNumber implements Serializable {

    private static final Pattern ORDER_NUMBER_PATTERN = Pattern.compile("^ORD-\\d{8}-[A-Z0-9]{8}$");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private String value;

    private OrderNumber(String value) {
        validate(value);
        this.value = value;
    }

    /**
     * Generates a new unique order number
     * Format: ORD-YYYYMMDD-XXXXXXXX
     */
    public static OrderNumber generate() {
        String datePart = LocalDateTime.now().format(DATE_FORMATTER);
        String uniquePart = UUID.randomUUID().toString()
            .replace("-", "")
            .substring(0, 8)
            .toUpperCase();

        return new OrderNumber("ORD-" + datePart + "-" + uniquePart);
    }

    /**
     * Creates OrderNumber from existing value with validation
     */
    public static OrderNumber of(String value) {
        return new OrderNumber(value);
    }

    /**
     * Validates order number format
     */
    private void validate(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Order number cannot be null or empty");
        }

        if (!ORDER_NUMBER_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                "Invalid order number format. Expected: ORD-YYYYMMDD-XXXXXXXX, got: " + value
            );
        }
    }

    /**
     * Gets the date part of the order number
     */
    public String getDatePart() {
        if (value == null || value.length() < 12) {
            return "";
        }
        return value.substring(4, 12); // YYYYMMDD
    }

    /**
     * Gets the unique identifier part
     */
    public String getUniquePart() {
        if (value == null || value.length() < 21) {
            return "";
        }
        return value.substring(13); // Last 8 characters
    }

    @Override
    public String toString() {
        return value;
    }
}
