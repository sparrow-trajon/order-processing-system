package com.ordermanagement.model.entity;

import com.ordermanagement.exception.InvalidOrderStatusException;
import com.ordermanagement.model.valueobject.Address;
import com.ordermanagement.model.valueobject.Money;
import com.ordermanagement.model.valueobject.OrderNumber;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order entity representing an order in the e-commerce system.
 * COMPLETELY REFACTORED with:
 * - Customer entity relationship (no more embedded data)
 * - OrderStatusEntity (dynamic database-driven status)
 * - Item entities (loosely coupled)
 * - Value Objects (Money, OrderNumber, Address)
 * - Payment and Shipment relationships
 * - Complete audit trail with OrderStatusHistory
 *
 * Design Patterns:
 * - Aggregate Root Pattern - Order is the root of the Order aggregate
 * - Builder Pattern (via Lombok @Builder)
 * - Value Object Pattern - Uses Money, OrderNumber, Address
 * - Entity Pattern - JPA domain model
 *
 * SOLID Principles:
 * - Single Responsibility: Manages order lifecycle
 * - Open/Closed: Extensible without modification
 * - Dependency Inversion: Depends on abstractions
 */
@Entity
@Table(
    name = "orders",
    indexes = {
        @Index(name = "idx_order_number", columnList = "orderNumber_value", unique = true),
        @Index(name = "idx_order_customer", columnList = "customer_id"),
        @Index(name = "idx_order_status", columnList = "status_id"),
        @Index(name = "idx_order_created", columnList = "createdAt"),
        @Index(name = "idx_order_total", columnList = "totalAmount_amount")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique order number (Value Object)
     */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "orderNumber_value", unique = true, nullable = false, length = 50))
    })
    private OrderNumber orderNumber;

    /**
     * Customer who placed this order (proper relationship)
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /**
     * Current order status (database-driven entity - YOUR PRIMARY REQUEST!)
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "status_id", nullable = false)
    private OrderStatusEntity status;

    /**
     * Items in this order (renamed from OrderItem to Item - loosely coupled)
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Item> items = new ArrayList<>();

    /**
     * Total amount before discounts and taxes (Value Object)
     */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "totalAmount_amount", nullable = false, precision = 10, scale = 2)),
        @AttributeOverride(name = "currency", column = @Column(name = "totalAmount_currency", nullable = false, length = 3))
    })
    private Money totalAmount;

    /**
     * Subtotal after discounts but before tax
     */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "subtotal_amount", precision = 10, scale = 2)),
        @AttributeOverride(name = "currency", column = @Column(name = "subtotal_currency", length = 3))
    })
    private Money subtotal;

    /**
     * Total discount applied
     */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "discount_amount", precision = 10, scale = 2)),
        @AttributeOverride(name = "currency", column = @Column(name = "discount_currency", length = 3))
    })
    private Money discountAmount;

    /**
     * Total tax amount
     */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "tax_amount", precision = 10, scale = 2)),
        @AttributeOverride(name = "currency", column = @Column(name = "tax_currency", length = 3))
    })
    private Money taxAmount;

    /**
     * Shipping cost
     */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "shipping_amount", precision = 10, scale = 2)),
        @AttributeOverride(name = "currency", column = @Column(name = "shipping_currency", length = 3))
    })
    private Money shippingAmount;

    /**
     * Final amount (subtotal + tax + shipping)
     */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "finalAmount_amount", precision = 10, scale = 2)),
        @AttributeOverride(name = "currency", column = @Column(name = "finalAmount_currency", length = 3))
    })
    private Money finalAmount;

    /**
     * Shipping address (Value Object)
     */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "street", column = @Column(name = "shipping_street")),
        @AttributeOverride(name = "city", column = @Column(name = "shipping_city")),
        @AttributeOverride(name = "state", column = @Column(name = "shipping_state")),
        @AttributeOverride(name = "zipCode", column = @Column(name = "shipping_zip")),
        @AttributeOverride(name = "country", column = @Column(name = "shipping_country"))
    })
    private Address shippingAddress;

    /**
     * Billing address (Value Object)
     */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "street", column = @Column(name = "billing_street")),
        @AttributeOverride(name = "city", column = @Column(name = "billing_city")),
        @AttributeOverride(name = "state", column = @Column(name = "billing_state")),
        @AttributeOverride(name = "zipCode", column = @Column(name = "billing_zip")),
        @AttributeOverride(name = "country", column = @Column(name = "billing_country"))
    })
    private Address billingAddress;

    /**
     * Payments for this order
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    /**
     * Shipment for this order (one-to-one)
     */
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Shipment shipment;

    /**
     * Status history for complete audit trail
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("changedAt DESC")
    @Builder.Default
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();

    /**
     * Special instructions/notes
     */
    @Column(length = 1000)
    private String notes;

    /**
     * Customer phone number (denormalized for convenience)
     */
    @Column(length = 20)
    private String customerPhone;

    /**
     * Customer email (denormalized for convenience)
     */
    @Column(length = 100)
    private String customerEmail;

    /**
     * Priority order?
     */
    @Column
    @Builder.Default
    private Boolean isPriority = false;

    /**
     * Gift order?
     */
    @Column
    @Builder.Default
    private Boolean isGift = false;

    /**
     * Cancellation reason (if cancelled)
     */
    @Column(length = 500)
    private String cancellationReason;

    /**
     * Who cancelled the order
     */
    @Column(length = 100)
    private String cancelledBy;

    /**
     * When was order cancelled
     */
    @Column
    private LocalDateTime cancelledAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version; // For optimistic locking

    /**
     * Business method: Add item to order
     */
    public void addItem(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }
        items.add(item);
        item.setOrder(this);
        calculateTotals();
    }

    /**
     * Business method: Remove item from order
     */
    public void removeItem(Item item) {
        if (item == null) {
            return;
        }
        items.remove(item);
        item.setOrder(null);
        calculateTotals();
    }

    /**
     * Business method: Calculate all totals (total, subtotal, tax, shipping, final)
     */
    public void calculateTotals() {
        if (items == null || items.isEmpty()) {
            this.totalAmount = Money.zero();
            this.subtotal = Money.zero();
            this.finalAmount = Money.zero();
            return;
        }

        // Sum all item totals (before discount and tax)
        Money total = items.stream()
            .map(Item::getTotalPrice)
            .reduce(Money.zero(), Money::add);
        this.totalAmount = total;

        // Calculate discount (if any)
        Money discount = discountAmount != null ? discountAmount : Money.zero();

        // Subtotal = total - discount
        this.subtotal = total.subtract(discount);

        // Calculate or sum item taxes
        if (taxAmount == null || taxAmount.isZero()) {
            this.taxAmount = items.stream()
                .map(Item::getTax)
                .reduce(Money.zero(), Money::add);
        }

        // Shipping cost (if not set)
        if (shippingAmount == null) {
            shippingAmount = Money.zero();
        }

        // Final amount = subtotal + tax + shipping
        this.finalAmount = subtotal.add(taxAmount).add(shippingAmount);
    }

    /**
     * Business method: Check if order can be cancelled
     */
    public boolean canBeCancelled() {
        return status != null && status.allowsCancellation();
    }

    /**
     * Business method: Check if order can be modified
     */
    public boolean canBeModified() {
        return status != null && status.allowsModification();
    }

    /**
     * Business method: Update status with transition validation
     */
    public void updateStatus(OrderStatusEntity newStatus, String changedBy, String reason) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        if (status != null && !status.canTransitionTo(newStatus)) {
            throw new InvalidOrderStatusException(
                String.format("Cannot transition from %s to %s", status.getCode(), newStatus.getCode())
            );
        }

        OrderStatusEntity oldStatus = this.status;
        this.status = newStatus;

        // Create status history entry
        OrderStatusHistory historyEntry = OrderStatusHistory.builder()
            .order(this)
            .fromStatus(oldStatus)
            .toStatus(newStatus)
            .changedBy(changedBy != null ? changedBy : "SYSTEM")
            .changedAt(LocalDateTime.now())
            .reason(reason)
            .build();

        statusHistory.add(historyEntry);
    }

    /**
     * Business method: Cancel order
     */
    public void cancel(String reason, String cancelledBy) {
        if (!canBeCancelled()) {
            throw new IllegalStateException("Order cannot be cancelled in current status: " + status.getCode());
        }

        this.cancellationReason = reason;
        this.cancelledBy = cancelledBy;
        this.cancelledAt = LocalDateTime.now();
    }

    /**
     * Business method: Add payment
     */
    public void addPayment(Payment payment) {
        if (payment == null) {
            throw new IllegalArgumentException("Payment cannot be null");
        }
        payments.add(payment);
        payment.setOrder(this);
    }

    /**
     * Business method: Get total paid amount
     */
    public Money getTotalPaid() {
        return payments.stream()
            .filter(Payment::isSuccessful)
            .map(Payment::getAmount)
            .reduce(Money.zero(), Money::add);
    }

    /**
     * Business method: Check if order is fully paid
     */
    public boolean isFullyPaid() {
        Money totalPaid = getTotalPaid();
        return totalPaid.isGreaterThan(finalAmount) || totalPaid.equals(finalAmount);
    }

    /**
     * Business method: Get item count
     */
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    /**
     * Business method: Get total quantity
     */
    public int getTotalQuantity() {
        return items != null ? items.stream()
            .mapToInt(item -> item.getQuantity().getValue())
            .sum() : 0;
    }

    @PrePersist
    protected void onCreate() {
        if (orderNumber == null) {
            orderNumber = OrderNumber.generate();
        }
        if (isPriority == null) {
            isPriority = false;
        }
        if (isGift == null) {
            isGift = false;
        }
        if (customer != null && customerEmail == null) {
            customerEmail = customer.getEmail() != null ? customer.getEmail().getAddress() : null;
            customerPhone = customer.getPhone();
        }

        calculateTotals();
    }

    @PreUpdate
    protected void onUpdate() {
        calculateTotals();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order)) return false;
        Order order = (Order) o;
        return id != null && id.equals(order.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format("Order{id=%d, orderNumber=%s, customer=%s, status=%s, finalAmount=%s}",
            id, orderNumber, customer != null ? customer.getEmail() : "null",
            status != null ? status.getCode() : "null", finalAmount);
    }
}
