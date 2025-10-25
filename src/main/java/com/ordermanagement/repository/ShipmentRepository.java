package com.ordermanagement.repository;

import com.ordermanagement.model.entity.Order;
import com.ordermanagement.model.entity.Shipment;
import com.ordermanagement.model.enums.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Shipment entity.
 */
@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    /**
     * Find shipment by tracking number
     */
    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    /**
     * Find shipment by order (one-to-one)
     */
    Optional<Shipment> findByOrder(Order order);

    /**
     * Find shipments by status
     */
    List<Shipment> findByStatus(ShipmentStatus status);

    /**
     * Find shipments by carrier
     */
    List<Shipment> findByCarrier(String carrier);

    /**
     * Find shipments delivered between dates
     */
    List<Shipment> findByDeliveredAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Find delayed deliveries (actual > estimated)
     */
    @Query("SELECT s FROM Shipment s WHERE s.actualDeliveryDate > s.estimatedDeliveryDate")
    List<Shipment> findDelayedDeliveries();

    /**
     * Find shipments requiring delivery attempt
     */
    List<Shipment> findByStatusAndDeliveryAttemptsLessThan(ShipmentStatus status, Integer maxAttempts);

    /**
     * Count shipments by status
     */
    long countByStatus(ShipmentStatus status);
}
