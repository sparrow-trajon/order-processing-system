package com.ordermanagement.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * OrderStatusTransition entity defines allowed transitions between order statuses.
 * This enables dynamic workflow configuration without code changes.
 *
 * Design Pattern: State Machine Configuration
 * Use Case: Define and control order status workflow transitions
 *
 * Benefits:
 * - Configure allowed status transitions via database
 * - Add business rules and validations for transitions
 * - Track transition requirements (approval, payment, etc.)
 * - Support multiple workflow variants per customer type
 */
@Entity
@Table(
    name = "order_status_transitions",
    indexes = {
        @Index(name = "idx_transition_from_to", columnList = "fromStatusId, toStatusId", unique = true),
        @Index(name = "idx_transition_from", columnList = "fromStatusId"),
        @Index(name = "idx_transition_to", columnList = "toStatusId"),
        @Index(name = "idx_transition_active", columnList = "isAllowed")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusTransition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Source status for this transition
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fromStatusId", nullable = false)
    private OrderStatusEntity fromStatus;

    /**
     * Target status for this transition
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "toStatusId", nullable = false)
    private OrderStatusEntity toStatus;

    /**
     * Is this transition allowed/enabled?
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isAllowed = true;

    /**
     * Does this transition require manual approval?
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean requiresApproval = false;

    /**
     * Does this transition require payment confirmation?
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean requiresPayment = false;

    /**
     * Does this transition require inventory availability check?
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean requiresInventoryCheck = false;

    /**
     * Role/permission required to execute this transition
     * e.g., "ROLE_ADMIN", "ROLE_MANAGER"
     */
    @Column(length = 100)
    private String requiredRole;

    /**
     * Notification template to use for this transition
     */
    @Column(length = 100)
    private String notificationTemplate;

    /**
     * Display order for UI (when showing available transitions)
     */
    @Column
    @Builder.Default
    private Integer displayOrder = 0;

    /**
     * Description of this transition (shown to users)
     */
    @Column(length = 500)
    private String description;

    /**
     * Reason code category for this transition
     * e.g., "CANCELLATION", "RETURN", "ESCALATION"
     */
    @Column(length = 50)
    private String reasonCategory;

    /**
     * Is reason required when performing this transition?
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean requiresReason = false;

    /**
     * Custom validation rules as JSON
     * e.g., {"minOrderAmount": 100, "maxDaysSinceCreation": 30}
     */
    @Column(columnDefinition = "TEXT")
    private String validationRules;

    /**
     * Actions to trigger automatically on this transition (as JSON array)
     * e.g., ["SEND_EMAIL", "RESERVE_INVENTORY", "INITIATE_PAYMENT"]
     */
    @Column(columnDefinition = "TEXT")
    private String autoActions;

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
        if (isAllowed == null) {
            isAllowed = true;
        }
        if (requiresApproval == null) {
            requiresApproval = false;
        }
        if (requiresPayment == null) {
            requiresPayment = false;
        }
        if (requiresInventoryCheck == null) {
            requiresInventoryCheck = false;
        }
        if (requiresReason == null) {
            requiresReason = false;
        }
        if (displayOrder == null) {
            displayOrder = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Business method: Check if this transition can be executed
     */
    public boolean canExecute() {
        return isAllowed
            && fromStatus.getIsActive()
            && toStatus.getIsActive()
            && !fromStatus.getIsFinal();
    }

    /**
     * Business method: Check if user has required role
     */
    public boolean hasRequiredRole(String userRole) {
        if (requiredRole == null || requiredRole.trim().isEmpty()) {
            return true; // No role required
        }
        return requiredRole.equals(userRole);
    }

    /**
     * Business method: Get transition name for logging/display
     */
    public String getTransitionName() {
        return String.format("%s → %s", fromStatus.getCode(), toStatus.getCode());
    }
}
