package com.ordermanagement.service;

import com.ordermanagement.exception.InvalidOrderStatusException;
import com.ordermanagement.model.entity.Order;
import com.ordermanagement.model.entity.OrderStatusEntity;
import com.ordermanagement.model.entity.OrderStatusHistory;
import com.ordermanagement.model.entity.OrderStatusTransition;
import com.ordermanagement.repository.OrderStatusRepository;
import com.ordermanagement.repository.OrderStatusTransitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing dynamic order statuses.
 * THIS IS THE KEY SERVICE for your primary request!
 * Enables complete status workflow management via database.
 *
 * Design Pattern: Service Pattern, State Machine Pattern
 * Use Case: Dynamic order status management and workflow transitions
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderStatusService {

    private final OrderStatusRepository statusRepository;
    private final OrderStatusTransitionRepository transitionRepository;

    /**
     * Get status by code (primary lookup method)
     */
    @Cacheable(value = "orderStatuses", key = "#code")
    public OrderStatusEntity getStatusByCode(String code) {
        return statusRepository.findByCode(code)
            .orElseThrow(() -> new InvalidOrderStatusException("Status not found: " + code));
    }

    /**
     * Get all active statuses
     */
    @Cacheable(value = "orderStatuses", key = "'all-active'")
    public List<OrderStatusEntity> getAllActiveStatuses() {
        return statusRepository.findByIsActiveTrueOrderByDisplayOrder();
    }

    /**
     * Get all statuses
     */
    @Cacheable(value = "orderStatuses", key = "'all'")
    public List<OrderStatusEntity> getAllStatuses() {
        return statusRepository.findAllByOrderByDisplayOrder();
    }

    /**
     * Get allowed transitions from a status
     */
    @Cacheable(value = "statusTransitions", key = "#fromStatus.code")
    public List<OrderStatusTransition> getAllowedTransitions(OrderStatusEntity fromStatus) {
        return transitionRepository.findAllowedTransitionsFrom(fromStatus);
    }

    /**
     * Check if transition is allowed
     */
    public boolean isTransitionAllowed(OrderStatusEntity fromStatus, OrderStatusEntity toStatus) {
        return transitionRepository.isTransitionAllowed(fromStatus, toStatus);
    }

    /**
     * Validate and execute status transition
     */
    @Transactional
    public OrderStatusHistory transitionStatus(
        Order order,
        String toStatusCode,
        String changedBy,
        String reason
    ) {
        OrderStatusEntity currentStatus = order.getStatus();
        OrderStatusEntity newStatus = getStatusByCode(toStatusCode);

        // Check if transition is allowed
        if (!isTransitionAllowed(currentStatus, newStatus)) {
            throw new InvalidOrderStatusException(
                String.format("Transition from %s to %s is not allowed",
                    currentStatus.getCode(), newStatus.getCode())
            );
        }

        // Get transition rules
        OrderStatusTransition transition = transitionRepository
            .findByFromStatusAndToStatus(currentStatus, newStatus)
            .orElseThrow(() -> new InvalidOrderStatusException("Transition not configured"));

        // Check if approval required
        if (transition.getRequiresApproval()) {
            log.warn("Transition requires approval: {} -> {}", currentStatus.getCode(), newStatus.getCode());
            // In real implementation, would check user permissions
        }

        // Check if payment required
        if (transition.getRequiresPayment()) {
            if (!order.isFullyPaid()) {
                throw new IllegalStateException("Payment required before status transition");
            }
        }

        // Check if inventory check required
        if (transition.getRequiresInventoryCheck()) {
            // In real implementation, would check inventory availability
            log.info("Inventory check required for transition");
        }

        // Execute transition
        order.updateStatus(newStatus, changedBy, reason);

        log.info("Order status transitioned: orderId={}, {} -> {}, by={}",
            order.getId(), currentStatus.getCode(), newStatus.getCode(), changedBy);

        // Return the history entry
        return order.getStatusHistory().get(order.getStatusHistory().size() - 1);
    }

    /**
     * Create new status (admin function)
     */
    @Transactional
    public OrderStatusEntity createStatus(OrderStatusEntity status) {
        if (statusRepository.existsByCode(status.getCode())) {
            throw new IllegalArgumentException("Status code already exists: " + status.getCode());
        }

        OrderStatusEntity saved = statusRepository.save(status);
        log.info("New order status created: code={}, name={}", status.getCode(), status.getName());
        return saved;
    }

    /**
     * Update status configuration (admin function)
     */
    @Transactional
    public OrderStatusEntity updateStatus(Long id, OrderStatusEntity updatedStatus) {
        OrderStatusEntity existing = statusRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Status not found: " + id));

        existing.setName(updatedStatus.getName());
        existing.setDescription(updatedStatus.getDescription());
        existing.setColorCode(updatedStatus.getColorCode());
        existing.setIconName(updatedStatus.getIconName());
        existing.setDisplayOrder(updatedStatus.getDisplayOrder());
        existing.setIsCancellable(updatedStatus.getIsCancellable());
        existing.setIsModifiable(updatedStatus.getIsModifiable());
        existing.setTriggersPayment(updatedStatus.getTriggersPayment());
        existing.setTriggersInventoryReservation(updatedStatus.getTriggersInventoryReservation());
        existing.setTriggersShipping(updatedStatus.getTriggersShipping());
        existing.setSendsNotification(updatedStatus.getSendsNotification());
        existing.setIsActive(updatedStatus.getIsActive());

        OrderStatusEntity saved = statusRepository.save(existing);
        log.info("Order status updated: code={}", existing.getCode());
        return saved;
    }

    /**
     * Create status transition rule (admin function)
     */
    @Transactional
    public OrderStatusTransition createTransition(OrderStatusTransition transition) {
        OrderStatusTransition saved = transitionRepository.save(transition);
        log.info("Status transition created: {} -> {}",
            transition.getFromStatus().getCode(),
            transition.getToStatus().getCode());
        return saved;
    }

    /**
     * Get default/initial status (typically PENDING)
     */
    public OrderStatusEntity getDefaultStatus() {
        return getStatusByCode("PENDING");
    }

    /**
     * Get cancelled status
     */
    public OrderStatusEntity getCancelledStatus() {
        return getStatusByCode("CANCELLED");
    }

    /**
     * Get completed status
     */
    public OrderStatusEntity getCompletedStatus() {
        return getStatusByCode("COMPLETED");
    }
}
