package com.ordermanagement.repository;

import com.ordermanagement.model.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for OrderItem entity.
 * Provides data access operations for order items.
 *
 * Design Patterns:
 * - Repository Pattern - Abstraction over data access layer
 * - Proxy Pattern - Spring Data JPA creates proxy implementation
 *
 * SOLID Principles:
 * - Interface Segregation: Focused interface for order item data access
 * - Dependency Inversion: Depend on abstraction, not concrete implementation
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Finds all order items belonging to a specific order.
     *
     * @param orderId The order ID
     * @return List of order items for the specified order
     */
    List<OrderItem> findByOrderId(Long orderId);

    /**
     * Deletes all order items belonging to a specific order.
     *
     * @param orderId The order ID
     */
    void deleteByOrderId(Long orderId);

    /**
     * Finds all order items with a specific product code.
     *
     * @param productCode The product code to search for
     * @return List of order items with the specified product code
     */
    List<OrderItem> findByProductCode(String productCode);
}
