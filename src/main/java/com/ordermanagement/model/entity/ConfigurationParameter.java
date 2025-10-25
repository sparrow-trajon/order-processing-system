package com.ordermanagement.model.entity;

import com.ordermanagement.model.enums.ParameterType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ConfigurationParameter entity for storing dynamic application configuration.
 * Allows runtime configuration changes without code deployment.
 *
 * Design Pattern: Configuration Management Pattern
 * Use Case: Dynamic business rules and application settings
 *
 * Benefits:
 * - Change business rules without code deployment
 * - A/B testing and feature toggles
 * - Customer-specific configurations
 * - Environment-specific settings
 */
@Entity
@Table(
    name = "configuration_parameters",
    indexes = {
        @Index(name = "idx_config_key", columnList = "paramKey", unique = true),
        @Index(name = "idx_config_category", columnList = "category"),
        @Index(name = "idx_config_active", columnList = "isActive")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfigurationParameter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique key for this parameter
     * e.g., "order.max.items", "payment.refund.timeout.hours"
     */
    @Column(nullable = false, unique = true, length = 200)
    private String paramKey;

    /**
     * Parameter value (stored as string, converted based on paramType)
     */
    @Column(nullable = false, length = 1000)
    private String paramValue;

    /**
     * Data type of the parameter
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ParameterType paramType;

    /**
     * Default value (used if paramValue is null or invalid)
     */
    @Column(length = 1000)
    private String defaultValue;

    /**
     * Human-readable description
     */
    @Column(length = 1000)
    private String description;

    /**
     * Category for grouping parameters
     * e.g., "ORDER", "PAYMENT", "SHIPPING", "BUSINESS_RULES"
     */
    @Column(length = 100)
    private String category;

    /**
     * Subcategory for fine-grained grouping
     */
    @Column(length = 100)
    private String subCategory;

    /**
     * Is this parameter currently active/enabled?
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Is this parameter editable via UI?
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isEditable = true;

    /**
     * Is this parameter sensitive (e.g., API keys)?
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isSensitive = false;

    /**
     * Minimum value (for numeric types)
     */
    @Column(length = 100)
    private String minValue;

    /**
     * Maximum value (for numeric types)
     */
    @Column(length = 100)
    private String maxValue;

    /**
     * Validation regex pattern
     */
    @Column(length = 500)
    private String validationPattern;

    /**
     * Allowed values (comma-separated for enum-like parameters)
     */
    @Column(length = 1000)
    private String allowedValues;

    /**
     * Display order in UI
     */
    @Column
    @Builder.Default
    private Integer displayOrder = 0;

    /**
     * Tags for searching/filtering (comma-separated)
     */
    @Column(length = 500)
    private String tags;

    /**
     * Environment this parameter applies to
     * e.g., "ALL", "DEV", "STAGING", "PROD"
     */
    @Column(length = 50)
    @Builder.Default
    private String environment = "ALL";

    /**
     * Customer/tenant ID (for multi-tenant configurations)
     */
    @Column(length = 100)
    private String tenantId;

    /**
     * When this parameter was created
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Last update timestamp
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Who created this parameter
     */
    @Column(length = 100)
    private String createdBy;

    /**
     * Who last updated this parameter
     */
    @Column(length = 100)
    private String updatedBy;

    /**
     * Additional metadata as JSON
     */
    @Column(columnDefinition = "TEXT")
    private String metadata;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
        if (isEditable == null) {
            isEditable = true;
        }
        if (isSensitive == null) {
            isSensitive = false;
        }
        if (displayOrder == null) {
            displayOrder = 0;
        }
        if (environment == null) {
            environment = "ALL";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Business method: Get value as Integer
     */
    public Integer getValueAsInteger() {
        try {
            return Integer.parseInt(paramValue);
        } catch (NumberFormatException e) {
            return defaultValue != null ? Integer.parseInt(defaultValue) : null;
        }
    }

    /**
     * Business method: Get value as Long
     */
    public Long getValueAsLong() {
        try {
            return Long.parseLong(paramValue);
        } catch (NumberFormatException e) {
            return defaultValue != null ? Long.parseLong(defaultValue) : null;
        }
    }

    /**
     * Business method: Get value as Double
     */
    public Double getValueAsDouble() {
        try {
            return Double.parseDouble(paramValue);
        } catch (NumberFormatException e) {
            return defaultValue != null ? Double.parseDouble(defaultValue) : null;
        }
    }

    /**
     * Business method: Get value as Boolean
     */
    public Boolean getValueAsBoolean() {
        return Boolean.parseBoolean(paramValue);
    }

    /**
     * Business method: Get value as String (raw)
     */
    public String getValueAsString() {
        return paramValue != null ? paramValue : defaultValue;
    }

    /**
     * Business method: Get value as array (split by comma)
     */
    public String[] getValueAsArray() {
        String value = getValueAsString();
        return value != null ? value.split(",") : new String[0];
    }

    /**
     * Business method: Validate value
     */
    public boolean isValid(String newValue) {
        // Type validation
        try {
            switch (paramType) {
                case INTEGER:
                    Integer.parseInt(newValue);
                    break;
                case LONG:
                    Long.parseLong(newValue);
                    break;
                case DOUBLE:
                    Double.parseDouble(newValue);
                    break;
                case BOOLEAN:
                    // Any value is valid for boolean
                    break;
                case STRING:
                case JSON:
                    // Any string is valid
                    break;
            }
        } catch (NumberFormatException e) {
            return false;
        }

        // Pattern validation
        if (validationPattern != null && !newValue.matches(validationPattern)) {
            return false;
        }

        // Allowed values validation
        if (allowedValues != null) {
            String[] allowed = allowedValues.split(",");
            boolean found = false;
            for (String val : allowed) {
                if (val.trim().equals(newValue.trim())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }

        return true;
    }

    /**
     * Business method: Update value with validation
     */
    public void updateValue(String newValue, String updatedBy) {
        if (!isEditable) {
            throw new IllegalStateException("This parameter is not editable");
        }
        if (!isValid(newValue)) {
            throw new IllegalArgumentException("Invalid value for parameter: " + paramKey);
        }
        this.paramValue = newValue;
        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
    }
}
