package com.ordermanagement.repository;

import com.ordermanagement.model.entity.Order;
import com.ordermanagement.model.entity.Payment;
import com.ordermanagement.model.enums.PaymentMethod;
import com.ordermanagement.model.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Payment entity.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Find payment by transaction ID
     */
    Optional<Payment> findByTransactionId(String transactionId);

    /**
     * Find all payments for an order
     */
    List<Payment> findByOrder(Order order);

    /**
     * Find payments by status
     */
    List<Payment> findByStatus(PaymentStatus status);

    /**
     * Find payments by payment method
     */
    List<Payment> findByPaymentMethod(PaymentMethod paymentMethod);

    /**
     * Find successful payments for an order
     */
    @Query("SELECT p FROM Payment p WHERE p.order = :order AND p.status = 'CAPTURED'")
    List<Payment> findSuccessfulPaymentsByOrder(@Param("order") Order order);

    /**
     * Find failed payments that can be retried
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'FAILED' AND p.attemptCount < 3")
    List<Payment> findRetryablePayments();

    /**
     * Find payments created between dates
     */
    List<Payment> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Count payments by status
     */
    long countByStatus(PaymentStatus status);
}
