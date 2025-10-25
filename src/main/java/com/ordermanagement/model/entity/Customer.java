package com.ordermanagement.model.entity;

import com.ordermanagement.model.enums.CustomerType;
import com.ordermanagement.model.valueobject.Address;
import com.ordermanagement.model.valueobject.Email;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Customer entity representing a customer in the system.
 * Separated from Order for better normalization and reusability.
 *
 * Design Pattern: Aggregate Root
 * Use Case: Customer management with order history
 */
@Entity
@Table(
    name = "customers",
    indexes = {
        @Index(name = "idx_customer_code", columnList = "customerCode", unique = true),
        @Index(name = "idx_customer_email", columnList = "email_address"),
        @Index(name = "idx_customer_type", columnList = "type"),
        @Index(name = "idx_customer_created", columnList = "createdAt")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String customerCode;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "address", column = @Column(name = "email_address", nullable = false, unique = true))
    })
    private Email email;

    @Column(length = 20)
    private String phone;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "street", column = @Column(name = "address_street")),
        @AttributeOverride(name = "city", column = @Column(name = "address_city")),
        @AttributeOverride(name = "state", column = @Column(name = "address_state")),
        @AttributeOverride(name = "zipCode", column = @Column(name = "address_zip_code")),
        @AttributeOverride(name = "country", column = @Column(name = "address_country"))
    })
    private Address address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CustomerType type = CustomerType.RETAIL;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (customerCode == null) {
            customerCode = generateCustomerCode();
        }
        if (type == null) {
            type = CustomerType.RETAIL;
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
     * Generates a unique customer code
     * Format: CUST-YYYYMMDD-XXXX
     */
    private String generateCustomerCode() {
        return "CUST-" + System.currentTimeMillis();
    }

    /**
     * Business method: Add order to customer's order history
     */
    public void addOrder(Order order) {
        orders.add(order);
        order.setCustomer(this);
    }

    /**
     * Business method: Deactivate customer
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Business method: Activate customer
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * Business method: Get total number of orders
     */
    public int getOrderCount() {
        return orders != null ? orders.size() : 0;
    }
}
