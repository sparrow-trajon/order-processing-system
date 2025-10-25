package com.ordermanagement.model.enums;

/**
 * Enum representing shipment/delivery statuses.
 */
public enum ShipmentStatus {
    PENDING("Pending", false, false),
    PREPARING("Preparing for Shipment", false, false),
    READY_TO_SHIP("Ready to Ship", false, false),
    SHIPPED("Shipped", false, false),
    IN_TRANSIT("In Transit", false, false),
    OUT_FOR_DELIVERY("Out for Delivery", false, false),
    DELIVERED("Delivered", true, true),
    DELIVERY_ATTEMPTED("Delivery Attempted", false, false),
    DELIVERY_FAILED("Delivery Failed", true, false),
    RETURNED_TO_SENDER("Returned to Sender", true, false),
    CANCELLED("Cancelled", true, false),
    LOST("Lost", true, false),
    DAMAGED("Damaged", false, false);

    private final String displayName;
    private final boolean isFinal;
    private final boolean isSuccessful;

    ShipmentStatus(String displayName, boolean isFinal, boolean isSuccessful) {
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
}
