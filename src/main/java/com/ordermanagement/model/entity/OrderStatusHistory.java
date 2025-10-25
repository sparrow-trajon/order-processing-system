package com.ordermanagement.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * OrderStatusHistory entity tracks all status changes for an order.
 * Provides complete audit trail for compliance and troubleshooting.
 *
 * Design Pattern: Audit Log Pattern
 * Use Case: Track order status lifecycle and changes
 *
 * Benefits:
 * - Complete audit trail of all status changes
 * - Track who changed status and when
 * - Store reason for status changes
 * - Support compliance and dispute resolution
 * - Enable analytics on order processing times
 */
@Entity
@Table(
    name = "order_status_history",
    indexes = {
        @Index(name = "idx_history_order", columnList = "orderId"),
        @Index(name = "idx_history_from_status", columnList = "fromStatusId"),
        @Index(name = "idx_history_to_status", columnList = "toStatusId"),
        @Index(name = "idx_history_changed_at", columnList = "changedAt"),
        @Index(name = "idx_history_changed_by", columnList = "changedBy")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The order this history entry belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId", nullable = false)
    private Order order;

    /**
     * Previous status (null for initial status)
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "fromStatusId")
    private OrderStatusEntity fromStatus;

    /**
     * New status after change
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "toStatusId", nullable = false)
    private OrderStatusEntity toStatus;

    /**
     * User or system that performed the change
     * e.g., "admin@example.com", "SYSTEM", "scheduler-job"
     */
    @Column(nullable = false, length = 100)
    private String changedBy;

    /**
     * Timestamp when status was changed
     */
    @Column(nullable = false)
    private LocalDateTime changedAt;

    /**
     * Reason for status change
     */
    @Column(length = 1000)
    private String reason;

    /**
     * Category of the change
     * e.g., "AUTOMATIC", "MANUAL", "SCHEDULED", "CUSTOMER_REQUEST"
     */
    @Column(length = 50)
    private String changeCategory;

    /**
     * IP address of the user who made the change
     */
    @Column(length = 50)
    private String ipAddress;

    /**
     * User agent (browser/app) that made the change
     */
    @Column(length = 500)
    private String userAgent;

    /**
     * Duration in this status (calculated when next change happens)
     * Stored in seconds
     */
    @Column
    private Long durationInStatus;

    /**
     * Was this change automatic or manual?
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isAutomatic = false;

    /**
     * Was notification sent for this change?
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean notificationSent = false;

    /**
     * Notification template used (if any)
     */
    @Column(length = 100)
    private String notificationTemplate;

    /**
     * Additional context/metadata as JSON
     * e.g., payment details, shipping info, etc.
     */
    @Column(columnDefinition = "TEXT")
    private String metadata;

    /**
     * Correlation ID for tracking across systems
     */
    @Column(length = 100)
    private String correlationId;

    /**
     * Trace ID from distributed tracing
     */
    @Column(length = 100)
    private String traceId;

    @PrePersist
    protected void onCreate() {
        if (changedAt == null) {
            changedAt = LocalDateTime.now();
        }
        if (isAutomatic == null) {
            isAutomatic = false;
        }
        if (notificationSent == null) {
            notificationSent = false;
        }
    }

    /**
     * Static factory method for creating history entry
     */
    public static OrderStatusHistory create(
            Order order,
            OrderStatusEntity fromStatus,
            OrderStatusEntity toStatus,
            String changedBy,
            String reason
    ) {
        return OrderStatusHistory.builder()
                .order(order)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .changedBy(changedBy)
                .changedAt(LocalDateTime.now())
                .reason(reason)
                .build();
    }

    /**
     * Business method: Get transition name for display
     */
    public String getTransitionName() {
        String from = fromStatus != null ? fromStatus.getCode() : "NONE";
        String to = toStatus != null ? toStatus.getCode() : "UNKNOWN";
        return String.format("%s → %s", from, to);
    }

    /**
     * Business method: Calculate duration since this change
     */
    public long getSecondsSinceChange() {
        return java.time.Duration.between(changedAt, LocalDateTime.now()).getSeconds();
    }

    /**
     * Business method: Set duration in status
     */
    public void calculateDuration(LocalDateTime nextChangeTime) {
        this.durationInStatus = java.time.Duration.between(changedAt, nextChangeTime).getSeconds();
    }
}
