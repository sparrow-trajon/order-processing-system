package com.ordermanagement.repository;

import com.ordermanagement.model.entity.Item;
import com.ordermanagement.model.entity.Order;
import com.ordermanagement.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Item entity.
 * Renamed from OrderItemRepository for loose coupling.
 */
@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    /**
     * Find all items for an order
     */
    List<Item> findByOrder(Order order);

    /**
     * Find items by product
     */
    List<Item> findByProduct(Product product);

    /**
     * Find items with gift wrap
     */
    List<Item> findByIsGiftWrappedTrue();

    /**
     * Count items in an order
     */
    long countByOrder(Order order);

    /**
     * Find total quantity ordered for a product
     */
    @Query("SELECT SUM(i.quantity.value) FROM Item i WHERE i.product = :product")
    Long getTotalQuantityOrderedForProduct(@Param("product") Product product);

    /**
     * Find all items for orders within a date range
     */
    @Query("SELECT i FROM Item i WHERE i.order.createdAt BETWEEN :startDate AND :endDate")
    List<Item> findItemsInDateRange(
        @Param("startDate") java.time.LocalDateTime startDate,
        @Param("endDate") java.time.LocalDateTime endDate
    );
}
