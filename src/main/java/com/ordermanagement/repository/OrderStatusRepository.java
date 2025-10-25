package com.ordermanagement.repository;

import com.ordermanagement.model.entity.OrderStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for OrderStatusEntity.
 * THE KEY REPOSITORY for dynamic order status management!
 * Enables runtime status configuration without code changes.
 */
@Repository
public interface OrderStatusRepository extends JpaRepository<OrderStatusEntity, Long> {

    /**
     * Find status by unique code (e.g., "PENDING", "PROCESSING")
     * This is the PRIMARY lookup method for status entities
     */
    Optional<OrderStatusEntity> findByCode(String code);

    /**
     * Find all active statuses, ordered by display order
     */
    List<OrderStatusEntity> findByIsActiveTrueOrderByDisplayOrder();

    /**
     * Find all statuses ordered by display order
     */
    List<OrderStatusEntity> findAllByOrderByDisplayOrder();

    /**
     * Find final statuses (terminal states)
     */
    List<OrderStatusEntity> findByIsFinalTrue();

    /**
     * Find statuses that allow cancellation
     */
    List<OrderStatusEntity> findByIsCancellableTrue();

    /**
     * Find statuses that allow modification
     */
    List<OrderStatusEntity> findByIsModifiableTrue();

    /**
     * Find statuses that trigger payment
     */
    List<OrderStatusEntity> findByTriggersPaymentTrue();

    /**
     * Find statuses that trigger inventory reservation
     */
    List<OrderStatusEntity> findByTriggersInventoryReservationTrue();

    /**
     * Find statuses that trigger shipping
     */
    List<OrderStatusEntity> findByTriggersShippingTrue();

    /**
     * Check if status exists by code
     */
    boolean existsByCode(String code);

    /**
     * Count active statuses
     */
    long countByIsActiveTrue();

    /**
     * Find status by name (case insensitive)
     */
    @Query("SELECT s FROM OrderStatusEntity s WHERE LOWER(s.name) = LOWER(:name)")
    Optional<OrderStatusEntity> findByNameIgnoreCase(@Param("name") String name);
}
