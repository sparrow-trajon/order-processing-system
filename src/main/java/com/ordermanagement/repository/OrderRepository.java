package com.ordermanagement.repository;

import com.ordermanagement.model.entity.Order;
import com.ordermanagement.model.entity.OrderStatusEntity;
import com.ordermanagement.model.valueobject.OrderNumber;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Order entity.
 * Provides data access operations for orders.
 *
 * Design Patterns:
 * - Repository Pattern - Abstraction over data access layer
 * - Proxy Pattern - Spring Data JPA creates proxy implementation
 *
 * SOLID Principles:
 * - Interface Segregation: Focused interface for order data access
 * - Dependency Inversion: Depend on abstraction, not concrete implementation
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Finds an order by its unique order number value.
     * Uses custom query to access the embedded value object's value field.
     *
     * @param orderNumberValue The order number string to search for
     * @return Optional containing the order if found, empty otherwise
     */
    @Query("SELECT o FROM Order o WHERE o.orderNumber.value = :orderNumberValue")
    Optional<Order> findByOrderNumber(@Param("orderNumberValue") String orderNumberValue);

    /**
     * Finds all orders with a specific status.
     *
     * @param status The order status entity to filter by
     * @return List of orders with the specified status
     */
    List<Order> findByStatus(OrderStatusEntity status);

    /**
     * Finds all orders for a specific customer email.
     * Uses custom query to access customer's embedded email value object.
     *
     * @param emailAddress The customer email address to search for
     * @return List of orders for the customer
     */
    @Query("SELECT o FROM Order o WHERE o.customer.email.address = :emailAddress")
    List<Order> findByCustomerEmail(@Param("emailAddress") String emailAddress);

    /**
     * Checks if an order with the given order number exists.
     * Uses custom query to access the embedded value object's value field.
     *
     * @param orderNumberValue The order number string to check
     * @return true if order exists, false otherwise
     */
    @Query("SELECT COUNT(o) > 0 FROM Order o WHERE o.orderNumber.value = :orderNumberValue")
    boolean existsByOrderNumber(@Param("orderNumberValue") String orderNumberValue);

    /**
     * Finds orders by customer full name containing the given text (case-insensitive).
     * Uses custom query to access customer's fullName field.
     *
     * @param namePattern The customer name search term
     * @return List of matching orders
     */
    @Query("SELECT o FROM Order o WHERE LOWER(o.customer.fullName) LIKE LOWER(CONCAT('%', :namePattern, '%'))")
    List<Order> findByCustomerNameContainingIgnoreCase(@Param("namePattern") String namePattern);

    /**
     * Custom query to find orders by status with eager loading of items.
     * Optimizes performance by reducing N+1 query problem.
     *
     * @param status The order status entity to filter by
     * @return List of orders with items eagerly loaded
     */
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items WHERE o.status = :status")
    List<Order> findByStatusWithItems(@Param("status") OrderStatusEntity status);

    /**
     * Custom query to find an order by ID with items eagerly loaded.
     *
     * @param id The order ID
     * @return Optional containing the order with items if found
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);

    /**
     * Custom query to find all orders with items eagerly loaded.
     * Optimizes performance by reducing N+1 query problem.
     *
     * @return List of all orders with items eagerly loaded
     */
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items")
    List<Order> findAllWithItems();

    /**
     * Batch update order status for all orders with a specific current status.
     * More efficient than updating orders one by one in a loop.
     *
     * @param currentStatus The current status entity to filter by
     * @param newStatus The new status entity to set
     * @return Number of orders updated
     */
    @Modifying
    @Query("UPDATE Order o SET o.status = :newStatus, o.updatedAt = CURRENT_TIMESTAMP WHERE o.status = :currentStatus")
    int updateOrderStatusBatch(@Param("currentStatus") OrderStatusEntity currentStatus,
                                @Param("newStatus") OrderStatusEntity newStatus);

    /**
     * Finds all orders with pagination support.
     * Uses count query for optimal performance.
     *
     * @param pageable Pagination and sorting parameters
     * @return Page of orders with items eagerly loaded
     */
    @Query(value = "SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items",
           countQuery = "SELECT COUNT(DISTINCT o) FROM Order o")
    Page<Order> findAllWithItemsPaginated(Pageable pageable);

    /**
     * Finds orders by status with pagination support.
     * Uses count query for optimal performance.
     *
     * @param status The order status entity to filter by
     * @param pageable Pagination and sorting parameters
     * @return Page of orders with the specified status
     */
    @Query(value = "SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items WHERE o.status = :status",
           countQuery = "SELECT COUNT(DISTINCT o) FROM Order o WHERE o.status = :status")
    Page<Order> findByStatusWithItemsPaginated(@Param("status") OrderStatusEntity status, Pageable pageable);
}
