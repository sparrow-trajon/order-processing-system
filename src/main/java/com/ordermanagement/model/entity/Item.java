package com.ordermanagement.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ordermanagement.model.valueobject.Money;
import com.ordermanagement.model.valueobject.Quantity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Item entity representing a line item in any context (order, cart, wishlist, etc.).
 * Renamed from OrderItem for loose coupling - not tied to Order concept.
 *
 * Design Patterns:
 * - Builder Pattern (via Lombok @Builder) - Easy object construction
 * - Entity Pattern - JPA domain model
 * - Value Object Pattern - Uses Money and Quantity value objects
 *
 * SOLID Principles:
 * - Single Responsibility: Represents item data only
 * - Open/Closed: Can be extended for different contexts (cart, wishlist, etc.)
 * - Dependency Inversion: Depends on Product abstraction
 *
 * Benefits of renaming to "Item":
 * - Can be reused in shopping cart, wishlist, quotes, invoices
 * - Not semantically bound to "Order" concept
 * - More flexible domain modeling
 */
@Entity
@Table(
    name = "items",
    indexes = {
        @Index(name = "idx_item_order", columnList = "order_id"),
        @Index(name = "idx_item_product", columnList = "product_id"),
        @Index(name = "idx_item_created", columnList = "createdAt")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The order this item belongs to
     * Note: In future, this could be made polymorphic to support
     * Cart, Wishlist, etc. using @ManyToOne with a common parent
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Order order;

    /**
     * Reference to the actual Product entity (optional)
     * This enables inventory tracking, pricing updates, etc.
     * Can be null if the product doesn't exist in the catalog yet
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = true)
    private Product product;

    /**
     * Snapshot of product name at time of purchase
     * (In case product name changes later)
     */
    @Column(nullable = false, length = 200)
    private String productNameSnapshot;

    /**
     * Snapshot of product code at time of purchase
     */
    @Column(nullable = false, length = 50)
    private String productCodeSnapshot;

    /**
     * Quantity ordered (using Value Object)
     */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "quantity", nullable = false))
    })
    private Quantity quantity;

    /**
     * Unit price at time of purchase (using Value Object)
     * Snapshot pricing - may differ from current product price
     */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "unit_price_amount", nullable = false, precision = 10, scale = 2)),
        @AttributeOverride(name = "currency", column = @Column(name = "unit_price_currency", nullable = false, length = 3))
    })
    private Money unitPrice;

    /**
     * Total price for this line item (quantity * unitPrice)
     */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "total_price_amount", nullable = false, precision = 10, scale = 2)),
        @AttributeOverride(name = "currency", column = @Column(name = "total_price_currency", nullable = false, length = 3))
    })
    private Money totalPrice;

    /**
     * Discount applied to this item (if any)
     */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "discount_amount", precision = 10, scale = 2)),
        @AttributeOverride(name = "currency", column = @Column(name = "discount_currency", length = 3))
    })
    private Money discount;

    /**
     * Tax amount for this item
     */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "tax_amount", precision = 10, scale = 2)),
        @AttributeOverride(name = "currency", column = @Column(name = "tax_currency", length = 3))
    })
    private Money tax;

    /**
     * Final amount after discount and tax
     */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "final_amount", precision = 10, scale = 2)),
        @AttributeOverride(name = "currency", column = @Column(name = "final_currency", length = 3))
    })
    private Money finalAmount;

    /**
     * Special notes/instructions for this item
     */
    @Column(length = 500)
    private String notes;

    /**
     * Gift wrap requested?
     */
    @Column
    @Builder.Default
    private Boolean isGiftWrapped = false;

    /**
     * Gift message (if gift wrapped)
     */
    @Column(length = 500)
    private String giftMessage;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (product != null) {
            this.productNameSnapshot = product.getName();
            this.productCodeSnapshot = product.getProductCode();

            // If unit price not set, use product's current price
            if (unitPrice == null) {
                this.unitPrice = product.getCurrentPrice();
            }
        }

        calculatePricing();

        if (isGiftWrapped == null) {
            isGiftWrapped = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculatePricing();
    }

    /**
     * Business method: Calculate all pricing (total, discount, tax, final)
     */
    public void calculatePricing() {
        if (quantity == null || unitPrice == null) {
            return;
        }

        // Calculate total price (quantity * unitPrice)
        this.totalPrice = unitPrice.multiply(quantity.getValue());

        // Apply discount if any
        Money amountAfterDiscount = totalPrice;
        if (discount != null && discount.isPositive()) {
            amountAfterDiscount = totalPrice.subtract(discount);
        }

        // Calculate tax if not already set
        if (tax == null || tax.isZero()) {
            // Default tax calculation (can be overridden by OrderPricingService)
            double taxRate = 0.10; // 10% - should come from configuration
            this.tax = amountAfterDiscount.multiply(java.math.BigDecimal.valueOf(taxRate));
        }

        // Calculate final amount
        this.finalAmount = amountAfterDiscount.add(tax);
    }

    /**
     * Business method: Update quantity and recalculate pricing
     */
    public void updateQuantity(Quantity newQuantity) {
        if (newQuantity == null || !newQuantity.isPositive()) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.quantity = newQuantity;
        calculatePricing();
    }

    /**
     * Business method: Apply discount
     */
    public void applyDiscount(Money discountAmount) {
        if (discountAmount.isGreaterThan(this.totalPrice)) {
            throw new IllegalArgumentException("Discount cannot exceed total price");
        }
        this.discount = discountAmount;
        calculatePricing();
    }

    /**
     * Business method: Apply percentage discount
     */
    public void applyPercentageDiscount(double percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100");
        }
        Money discountAmount = totalPrice.multiply(java.math.BigDecimal.valueOf(percentage / 100.0));
        this.discount = discountAmount;
        calculatePricing();
    }

    /**
     * Business method: Set custom tax
     */
    public void setCustomTax(Money taxAmount) {
        this.tax = taxAmount;
        calculatePricing();
    }

    /**
     * Business method: Get subtotal (after discount, before tax)
     */
    public Money getSubtotal() {
        Money subtotal = totalPrice;
        if (discount != null && discount.isPositive()) {
            subtotal = subtotal.subtract(discount);
        }
        return subtotal;
    }

    /**
     * Equals and hashCode based only on ID for JPA entity best practices.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item)) return false;
        Item item = (Item) o;
        return id != null && id.equals(item.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return String.format("Item{id=%d, product=%s, quantity=%s, finalAmount=%s}",
            id, productCodeSnapshot, quantity, finalAmount);
    }
}
