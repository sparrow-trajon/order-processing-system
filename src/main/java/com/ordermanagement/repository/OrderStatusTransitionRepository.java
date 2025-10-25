package com.ordermanagement.repository;

import com.ordermanagement.model.entity.OrderStatusEntity;
import com.ordermanagement.model.entity.OrderStatusTransition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for OrderStatusTransition entity.
 * Manages workflow transition rules between statuses.
 */
@Repository
public interface OrderStatusTransitionRepository extends JpaRepository<OrderStatusTransition, Long> {

    /**
     * Find transition by from and to status
     */
    Optional<OrderStatusTransition> findByFromStatusAndToStatus(
        OrderStatusEntity fromStatus,
        OrderStatusEntity toStatus
    );

    /**
     * Find all allowed transitions from a specific status
     */
    @Query("SELECT t FROM OrderStatusTransition t WHERE t.fromStatus = :fromStatus AND t.isAllowed = true ORDER BY t.displayOrder")
    List<OrderStatusTransition> findAllowedTransitionsFrom(@Param("fromStatus") OrderStatusEntity fromStatus);

    /**
     * Find all transitions to a specific status
     */
    List<OrderStatusTransition> findByToStatus(OrderStatusEntity toStatus);

    /**
     * Find all transitions from a specific status
     */
    List<OrderStatusTransition> findByFromStatus(OrderStatusEntity fromStatus);

    /**
     * Find transitions requiring approval
     */
    List<OrderStatusTransition> findByRequiresApprovalTrue();

    /**
     * Find transitions requiring payment
     */
    List<OrderStatusTransition> findByRequiresPaymentTrue();

    /**
     * Check if transition is allowed
     */
    @Query("SELECT COUNT(t) > 0 FROM OrderStatusTransition t WHERE t.fromStatus = :fromStatus AND t.toStatus = :toStatus AND t.isAllowed = true")
    boolean isTransitionAllowed(
        @Param("fromStatus") OrderStatusEntity fromStatus,
        @Param("toStatus") OrderStatusEntity toStatus
    );
}
