package com.ordermanagement.model.enums;

/**
 * Enum representing payment statuses.
 * Note: This can also be converted to database table for more flexibility.
 */
public enum PaymentStatus {
    /**
     * Payment initiated but not yet processed
     */
    PENDING("Pending", false, false),

    /**
     * Payment authorized (amount reserved but not captured)
     */
    AUTHORIZED("Authorized", false, false),

    /**
     * Payment captured/completed successfully
     */
    CAPTURED("Captured", true, true),

    /**
     * Payment processing failed
     */
    FAILED("Failed", true, false),

    /**
     * Payment was refunded
     */
    REFUNDED("Refunded", true, false),

    /**
     * Partial refund issued
     */
    PARTIALLY_REFUNDED("Partially Refunded", false, true),

    /**
     * Payment cancelled before capture
     */
    CANCELLED("Cancelled", true, false),

    /**
     * Payment is being processed
     */
    PROCESSING("Processing", false, false),

    /**
     * Payment disputed/chargeback
     */
    DISPUTED("Disputed", true, false);

    private final String displayName;
    private final boolean isFinal;
    private final boolean isSuccessful;

    PaymentStatus(String displayName, boolean isFinal, boolean isSuccessful) {
        this.displayName = displayName;
        this.isFinal = isFinal;
        this.isSuccessful = isSuccessful;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public boolean canRefund() {
        return this == CAPTURED || this == PARTIALLY_REFUNDED;
    }

    public boolean canCapture() {
        return this == AUTHORIZED;
    }
}
