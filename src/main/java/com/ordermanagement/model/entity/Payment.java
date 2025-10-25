package com.ordermanagement.model.entity;

import com.ordermanagement.model.enums.PaymentMethod;
import com.ordermanagement.model.enums.PaymentStatus;
import com.ordermanagement.model.valueobject.Money;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Payment entity representing a payment transaction for an order.
 * Tracks payment lifecycle from initiation to completion/refund.
 *
 * Design Pattern: Aggregate
 * Use Case: Payment processing and tracking
 */
@Entity
@Table(
    name = "payments",
    indexes = {
        @Index(name = "idx_payment_order", columnList = "orderId"),
        @Index(name = "idx_payment_transaction", columnList = "transactionId", unique = true),
        @Index(name = "idx_payment_status", columnList = "status"),
        @Index(name = "idx_payment_method", columnList = "paymentMethod"),
        @Index(name = "idx_payment_date", columnList = "createdAt")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The order this payment is for
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId", nullable = false)
    private Order order;

    /**
     * Payment amount
     */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "amount", nullable = false)),
        @AttributeOverride(name = "currency", column = @Column(name = "currency", nullable = false))
    })
    private Money amount;

    /**
     * Payment method used
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PaymentMethod paymentMethod;

    /**
     * Current payment status
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    /**
     * Unique transaction ID from payment gateway
     */
    @Column(unique = true, length = 200)
    private String transactionId;

    /**
     * Payment gateway reference ID
     */
    @Column(length = 200)
    private String gatewayReferenceId;

    /**
     * Payment gateway name (e.g., "Stripe", "PayPal", "Razorpay")
     */
    @Column(length = 100)
    private String paymentGateway;

    /**
     * Authorization code from payment processor
     */
    @Column(length = 100)
    private String authorizationCode;

    /**
     * Last 4 digits of card (for card payments)
     */
    @Column(length = 4)
    private String cardLast4;

    /**
     * Card brand (VISA, Mastercard, etc.)
     */
    @Column(length = 50)
    private String cardBrand;

    /**
     * When payment was initiated
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * When payment was last updated
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * When payment was authorized
     */
    @Column
    private LocalDateTime authorizedAt;

    /**
     * When payment was captured/completed
     */
    @Column
    private LocalDateTime capturedAt;

    /**
     * When payment failed
     */
    @Column
    private LocalDateTime failedAt;

    /**
     * When payment was refunded
     */
    @Column
    private LocalDateTime refundedAt;

    /**
     * Refund amount (if different from original amount)
     */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "refund_amount")),
        @AttributeOverride(name = "currency", column = @Column(name = "refund_currency"))
    })
    private Money refundAmount;

    /**
     * Refund reason
     */
    @Column(length = 500)
    private String refundReason;

    /**
     * Payment failure reason
     */
    @Column(length = 500)
    private String failureReason;

    /**
     * Payment failure code
     */
    @Column(length = 50)
    private String failureCode;

    /**
     * Number of payment attempts
     */
    @Column
    @Builder.Default
    private Integer attemptCount = 0;

    /**
     * Payment notes/remarks
     */
    @Column(length = 1000)
    private String notes;

    /**
     * Raw response from payment gateway (as JSON)
     */
    @Column(columnDefinition = "TEXT")
    private String gatewayResponse;

    /**
     * IP address from which payment was made
     */
    @Column(length = 50)
    private String ipAddress;

    /**
     * Is this payment verified?
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    @Version
    private Long version; // For optimistic locking

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = PaymentStatus.PENDING;
        }
        if (attemptCount == null) {
            attemptCount = 0;
        }
        if (isVerified == null) {
            isVerified = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Business method: Authorize payment
     */
    public void authorize(String transactionId, String authCode) {
        this.status = PaymentStatus.AUTHORIZED;
        this.transactionId = transactionId;
        this.authorizationCode = authCode;
        this.authorizedAt = LocalDateTime.now();
        this.isVerified = true;
    }

    /**
     * Business method: Capture/complete payment
     */
    public void capture() {
        if (this.status != PaymentStatus.AUTHORIZED && this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Can only capture authorized or pending payments");
        }
        this.status = PaymentStatus.CAPTURED;
        this.capturedAt = LocalDateTime.now();
        this.isVerified = true;
    }

    /**
     * Business method: Mark payment as failed
     */
    public void fail(String reason, String code) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
        this.failureCode = code;
        this.failedAt = LocalDateTime.now();
    }

    /**
     * Business method: Refund payment
     */
    public void refund(Money refundAmount, String reason) {
        if (!this.status.canRefund()) {
            throw new IllegalStateException("Can only refund captured payments");
        }

        if (refundAmount.isGreaterThan(this.amount)) {
            throw new IllegalArgumentException("Refund amount cannot exceed payment amount");
        }

        this.refundAmount = refundAmount;
        this.refundReason = reason;
        this.refundedAt = LocalDateTime.now();

        if (refundAmount.equals(this.amount)) {
            this.status = PaymentStatus.REFUNDED;
        } else {
            this.status = PaymentStatus.PARTIALLY_REFUNDED;
        }
    }

    /**
     * Business method: Cancel payment
     */
    public void cancel() {
        if (this.status.isFinal()) {
            throw new IllegalStateException("Cannot cancel completed payment");
        }
        this.status = PaymentStatus.CANCELLED;
    }

    /**
     * Business method: Increment attempt count
     */
    public void incrementAttempt() {
        this.attemptCount++;
    }

    /**
     * Business method: Check if payment is successful
     */
    public boolean isSuccessful() {
        return this.status.isSuccessful();
    }

    /**
     * Business method: Check if payment can be retried
     */
    public boolean canRetry() {
        return this.status == PaymentStatus.FAILED && this.attemptCount < 3;
    }
}
