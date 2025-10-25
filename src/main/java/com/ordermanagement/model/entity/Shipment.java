package com.ordermanagement.model.entity;

import com.ordermanagement.model.enums.ShipmentStatus;
import com.ordermanagement.model.valueobject.Address;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Shipment entity representing delivery/shipping information for an order.
 * Tracks shipment lifecycle from preparation to delivery.
 *
 * Design Pattern: Aggregate
 * Use Case: Shipment tracking and delivery management
 */
@Entity
@Table(
    name = "shipments",
    indexes = {
        @Index(name = "idx_shipment_order", columnList = "orderId", unique = true),
        @Index(name = "idx_shipment_tracking", columnList = "trackingNumber", unique = true),
        @Index(name = "idx_shipment_status", columnList = "status"),
        @Index(name = "idx_shipment_carrier", columnList = "carrier"),
        @Index(name = "idx_shipment_created", columnList = "createdAt")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The order this shipment is for (one-to-one relationship)
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId", nullable = false, unique = true)
    private Order order;

    /**
     * Tracking number from carrier
     */
    @Column(unique = true, length = 100)
    private String trackingNumber;

    /**
     * Carrier/shipping company name
     * e.g., "FedEx", "UPS", "USPS", "DHL", "Blue Dart"
     */
    @Column(nullable = false, length = 100)
    private String carrier;

    /**
     * Carrier service level
     * e.g., "Standard", "Express", "Overnight", "Same Day"
     */
    @Column(length = 50)
    private String serviceLevel;

    /**
     * Current shipment status
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private ShipmentStatus status = ShipmentStatus.PENDING;

    /**
     * Shipping address
     */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "street", column = @Column(name = "shipping_street", nullable = false)),
        @AttributeOverride(name = "city", column = @Column(name = "shipping_city", nullable = false)),
        @AttributeOverride(name = "state", column = @Column(name = "shipping_state")),
        @AttributeOverride(name = "zipCode", column = @Column(name = "shipping_zip_code")),
        @AttributeOverride(name = "country", column = @Column(name = "shipping_country", nullable = false))
    })
    private Address shippingAddress;

    /**
     * Estimated delivery date
     */
    @Column
    private LocalDateTime estimatedDeliveryDate;

    /**
     * Actual delivery date
     */
    @Column
    private LocalDateTime actualDeliveryDate;

    /**
     * When shipment was created/booked
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Last update timestamp
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * When package was picked up by carrier
     */
    @Column
    private LocalDateTime pickedUpAt;

    /**
     * When package was shipped
     */
    @Column
    private LocalDateTime shippedAt;

    /**
     * When package was delivered
     */
    @Column
    private LocalDateTime deliveredAt;

    /**
     * Name of person who received delivery
     */
    @Column(length = 100)
    private String deliveredTo;

    /**
     * Signature/proof of delivery
     */
    @Column(length = 500)
    private String deliveryProof;

    /**
     * Number of delivery attempts
     */
    @Column
    @Builder.Default
    private Integer deliveryAttempts = 0;

    /**
     * Last delivery attempt date
     */
    @Column
    private LocalDateTime lastAttemptDate;

    /**
     * Weight in kg
     */
    @Column
    private Double weight;

    /**
     * Package dimensions (LxWxH in cm)
     */
    @Column(length = 50)
    private String dimensions;

    /**
     * Number of packages/boxes
     */
    @Column
    @Builder.Default
    private Integer packageCount = 1;

    /**
     * Shipping notes/instructions
     */
    @Column(length = 1000)
    private String notes;

    /**
     * Special handling instructions
     */
    @Column(length = 500)
    private String handlingInstructions;

    /**
     * Is signature required for delivery?
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean requiresSignature = false;

    /**
     * Is this an express/priority shipment?
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isExpress = false;

    /**
     * Is insurance included?
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isInsured = false;

    /**
     * Insurance amount
     */
    @Column
    private Double insuranceAmount;

    /**
     * Current location/last checkpoint
     */
    @Column(length = 200)
    private String currentLocation;

    /**
     * Last status update from carrier
     */
    @Column(length = 500)
    private String lastStatusUpdate;

    /**
     * Tracking events as JSON (full tracking history)
     */
    @Column(columnDefinition = "TEXT")
    private String trackingEvents;

    /**
     * Exception/issue description (if any)
     */
    @Column(length = 500)
    private String exceptionDescription;

    /**
     * Return tracking number (if shipment was returned)
     */
    @Column(length = 100)
    private String returnTrackingNumber;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = ShipmentStatus.PENDING;
        }
        if (deliveryAttempts == null) {
            deliveryAttempts = 0;
        }
        if (packageCount == null) {
            packageCount = 1;
        }
        if (requiresSignature == null) {
            requiresSignature = false;
        }
        if (isExpress == null) {
            isExpress = false;
        }
        if (isInsured == null) {
            isInsured = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Business method: Mark as shipped
     */
    public void ship(String trackingNumber) {
        if (this.status != ShipmentStatus.READY_TO_SHIP && this.status != ShipmentStatus.PREPARING) {
            throw new IllegalStateException("Can only ship from READY_TO_SHIP or PREPARING status");
        }
        this.trackingNumber = trackingNumber;
        this.status = ShipmentStatus.SHIPPED;
        this.shippedAt = LocalDateTime.now();
    }

    /**
     * Business method: Update status to in transit
     */
    public void markInTransit(String location) {
        this.status = ShipmentStatus.IN_TRANSIT;
        this.currentLocation = location;
    }

    /**
     * Business method: Mark as out for delivery
     */
    public void markOutForDelivery() {
        this.status = ShipmentStatus.OUT_FOR_DELIVERY;
    }

    /**
     * Business method: Mark as delivered
     */
    public void deliver(String deliveredTo, String proof) {
        this.status = ShipmentStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
        this.actualDeliveryDate = LocalDateTime.now();
        this.deliveredTo = deliveredTo;
        this.deliveryProof = proof;
    }

    /**
     * Business method: Record delivery attempt
     */
    public void recordDeliveryAttempt() {
        this.deliveryAttempts++;
        this.lastAttemptDate = LocalDateTime.now();
        this.status = ShipmentStatus.DELIVERY_ATTEMPTED;
    }

    /**
     * Business method: Mark as delivery failed
     */
    public void markDeliveryFailed(String reason) {
        this.status = ShipmentStatus.DELIVERY_FAILED;
        this.exceptionDescription = reason;
    }

    /**
     * Business method: Cancel shipment
     */
    public void cancel(String reason) {
        if (this.status.isFinal()) {
            throw new IllegalStateException("Cannot cancel completed shipment");
        }
        this.status = ShipmentStatus.CANCELLED;
        this.exceptionDescription = reason;
    }

    /**
     * Business method: Check if shipment is delivered
     */
    public boolean isDelivered() {
        return this.status == ShipmentStatus.DELIVERED;
    }

    /**
     * Business method: Check if shipment can be tracked
     */
    public boolean canTrack() {
        return this.trackingNumber != null && !this.trackingNumber.trim().isEmpty();
    }

    /**
     * Business method: Calculate delivery delay (if any)
     */
    public Long getDeliveryDelayInHours() {
        if (actualDeliveryDate == null || estimatedDeliveryDate == null) {
            return null;
        }
        return java.time.Duration.between(estimatedDeliveryDate, actualDeliveryDate).toHours();
    }
}
