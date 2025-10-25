package com.ordermanagement.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * OrderStatus entity - replaces OrderStatus enum for dynamic status management.
 * This allows adding new statuses without code changes.
 *
 * Design Pattern: Entity Pattern (replacing Enum)
 * Use Case: Dynamic order status configuration and workflow management
 *
 * Benefits:
 * - Add new statuses via database inserts
 * - Modify status properties without deployment
 * - Configure status transitions dynamically
 * - Track status metadata and business rules
 */
@Entity
@Table(
    name = "order_statuses",
    indexes = {
        @Index(name = "idx_order_status_code", columnList = "code", unique = true),
        @Index(name = "idx_order_status_display_order", columnList = "displayOrder"),
        @Index(name = "idx_order_status_active", columnList = "isActive")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique status code (e.g., PENDING, PROCESSING, CONFIRMED)
     * This is what we'll use in code for lookups
     */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /**
     * Display name for UI (e.g., "Pending Approval", "In Processing")
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Detailed description of this status
     */
    @Column(length = 500)
    private String description;

    /**
     * Color code for UI display (e.g., "#FFA500" for PENDING)
     */
    @Column(length = 7)
    private String colorCode;

    /**
     * Icon name for UI display
     */
    @Column(length = 50)
    private String iconName;

    /**
     * Display order for sorting in UI
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    /**
     * Is this a terminal status? (e.g., COMPLETED, CANCELLED)
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isFinal = false;

    /**
     * Can orders be cancelled from this status?
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isCancellable = true;

    /**
     * Can orders be modified from this status?
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isModifiable = true;

    /**
     * Does this status trigger payment processing?
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean triggersPayment = false;

    /**
     * Does this status trigger inventory reservation?
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean triggersInventoryReservation = false;

    /**
     * Does this status trigger shipping?
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean triggersShipping = false;

    /**
     * Does this status send notification to customer?
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean sendsNotification = true;

    /**
     * Is this status currently active/available?
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Additional metadata as JSON
     */
    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(length = 100)
    private String createdBy;

    @Column(length = 100)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (displayOrder == null) {
            displayOrder = 0;
        }
        if (isFinal == null) {
            isFinal = false;
        }
        if (isCancellable == null) {
            isCancellable = true;
        }
        if (isModifiable == null) {
            isModifiable = true;
        }
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Business method: Check if transition to another status is allowed
     * This will be used in conjunction with OrderStatusTransition entity
     */
    public boolean canTransitionTo(OrderStatusEntity targetStatus) {
        // Basic rules
        if (this.isFinal) {
            return false; // Cannot transition from final status
        }
        if (!targetStatus.isActive) {
            return false; // Cannot transition to inactive status
        }
        // More complex rules will be in OrderStatusTransition table
        return true;
    }

    /**
     * Business method: Check if order can be cancelled from this status
     */
    public boolean allowsCancellation() {
        return this.isCancellable && !this.isFinal;
    }

    /**
     * Business method: Check if order can be modified from this status
     */
    public boolean allowsModification() {
        return this.isModifiable && !this.isFinal;
    }
}
