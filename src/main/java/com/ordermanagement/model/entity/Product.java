package com.ordermanagement.model.entity;

import com.ordermanagement.model.enums.ProductCategory;
import com.ordermanagement.model.valueobject.Money;
import com.ordermanagement.model.valueobject.Quantity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Product entity representing a product in the catalog.
 * Includes inventory tracking and pricing information.
 *
 * Design Pattern: Aggregate Root
 * Use Case: Product catalog management with inventory
 */
@Entity
@Table(
    name = "products",
    indexes = {
        @Index(name = "idx_product_code", columnList = "productCode", unique = true),
        @Index(name = "idx_product_category", columnList = "category"),
        @Index(name = "idx_product_active", columnList = "isActive"),
        @Index(name = "idx_product_created", columnList = "createdAt")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String productCode;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 1000)
    private String description;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "price_amount", nullable = false)),
        @AttributeOverride(name = "currency", column = @Column(name = "price_currency", nullable = false))
    })
    private Money currentPrice;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "cost_amount")),
        @AttributeOverride(name = "currency", column = @Column(name = "cost_currency"))
    })
    private Money costPrice;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "stock_quantity", nullable = false))
    })
    @Builder.Default
    private Quantity stockQuantity = Quantity.zero();

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "reserved_quantity", nullable = false))
    })
    @Builder.Default
    private Quantity reservedQuantity = Quantity.zero();

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "reorder_level"))
    })
    private Quantity reorderLevel;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private ProductCategory category;

    @Column(length = 100)
    private String brand;

    @Column(length = 50)
    private String sku;

    @Column
    private Double weight; // in kg

    @Column
    @Builder.Default
    private Boolean isActive = true;

    @Column
    @Builder.Default
    private Boolean isDiscontinued = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version; // For optimistic locking on inventory updates

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (stockQuantity == null) {
            stockQuantity = Quantity.zero();
        }
        if (reservedQuantity == null) {
            reservedQuantity = Quantity.zero();
        }
        if (isActive == null) {
            isActive = true;
        }
        if (isDiscontinued == null) {
            isDiscontinued = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Business method: Check if product is in stock
     */
    public boolean isInStock() {
        return getAvailableQuantity().isPositive();
    }

    /**
     * Business method: Get available quantity (stock - reserved)
     */
    public Quantity getAvailableQuantity() {
        return stockQuantity.subtract(reservedQuantity);
    }

    /**
     * Business method: Reserve inventory
     */
    public void reserveInventory(Quantity quantity) {
        if (getAvailableQuantity().isLessThan(quantity)) {
            throw new IllegalStateException(
                String.format("Insufficient stock for product %s. Available: %s, Requested: %s",
                    productCode, getAvailableQuantity(), quantity)
            );
        }
        this.reservedQuantity = this.reservedQuantity.add(quantity);
    }

    /**
     * Business method: Release reserved inventory
     */
    public void releaseReservedInventory(Quantity quantity) {
        if (this.reservedQuantity.isLessThan(quantity)) {
            throw new IllegalStateException(
                String.format("Cannot release more than reserved. Reserved: %s, Requested: %s",
                    reservedQuantity, quantity)
            );
        }
        this.reservedQuantity = this.reservedQuantity.subtract(quantity);
    }

    /**
     * Business method: Deduct inventory (when order is confirmed)
     */
    public void deductInventory(Quantity quantity) {
        if (this.stockQuantity.isLessThan(quantity)) {
            throw new IllegalStateException(
                String.format("Insufficient total stock for product %s. Stock: %s, Requested: %s",
                    productCode, stockQuantity, quantity)
            );
        }
        this.stockQuantity = this.stockQuantity.subtract(quantity);
        if (this.reservedQuantity.isGreaterThan(Quantity.zero())) {
            // Release from reserved if there's any
            Quantity toRelease = this.reservedQuantity.isLessThan(quantity)
                ? this.reservedQuantity
                : quantity;
            this.reservedQuantity = this.reservedQuantity.subtract(toRelease);
        }
    }

    /**
     * Business method: Add inventory (restocking)
     */
    public void addInventory(Quantity quantity) {
        this.stockQuantity = this.stockQuantity.add(quantity);
    }

    /**
     * Business method: Check if reorder needed
     */
    public boolean needsReorder() {
        if (reorderLevel == null) {
            return false;
        }
        return getAvailableQuantity().isLessThan(reorderLevel);
    }

    /**
     * Business method: Discontinue product
     */
    public void discontinue() {
        this.isDiscontinued = true;
        this.isActive = false;
    }

    /**
     * Business method: Reactivate product
     */
    public void activate() {
        if (isDiscontinued) {
            throw new IllegalStateException("Cannot activate discontinued product");
        }
        this.isActive = true;
    }

    /**
     * Business method: Deactivate product
     */
    public void deactivate() {
        this.isActive = false;
    }
}
