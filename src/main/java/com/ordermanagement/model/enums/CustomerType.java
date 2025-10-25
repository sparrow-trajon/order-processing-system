package com.ordermanagement.model.enums;

/**
 * Enum representing different types of customers.
 * Can be used for business rules like discounts, credit limits, etc.
 */
public enum CustomerType {
    /**
     * Regular retail customer (B2C)
     */
    RETAIL("Retail Customer", 0.0),

    /**
     * Wholesale customer (B2B) with potential bulk discounts
     */
    WHOLESALE("Wholesale Customer", 0.10),

    /**
     * VIP customer with premium benefits
     */
    VIP("VIP Customer", 0.15),

    /**
     * Corporate customer with negotiated rates
     */
    CORPORATE("Corporate Customer", 0.20);

    private final String displayName;
    private final Double defaultDiscount; // Percentage discount

    CustomerType(String displayName, Double defaultDiscount) {
        this.displayName = displayName;
        this.defaultDiscount = defaultDiscount;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Double getDefaultDiscount() {
        return defaultDiscount;
    }
}
