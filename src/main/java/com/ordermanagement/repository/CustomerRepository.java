package com.ordermanagement.repository;

import com.ordermanagement.model.entity.Customer;
import com.ordermanagement.model.enums.CustomerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Customer entity.
 * Provides data access methods for customer management.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Find customer by unique customer code
     */
    Optional<Customer> findByCustomerCode(String customerCode);

    /**
     * Find customer by email address
     */
    @Query("SELECT c FROM Customer c WHERE c.email.address = :email")
    Optional<Customer> findByEmail(@Param("email") String email);

    /**
     * Find all customers by type
     */
    List<Customer> findByType(CustomerType type);

    /**
     * Find all active customers
     */
    List<Customer> findByIsActiveTrue();

    /**
     * Find customers created after a specific date
     */
    List<Customer> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Check if customer exists by email
     */
    @Query("SELECT COUNT(c) > 0 FROM Customer c WHERE c.email.address = :email")
    boolean existsByEmail(@Param("email") String email);

    /**
     * Check if customer exists by customer code
     */
    boolean existsByCustomerCode(String customerCode);

    /**
     * Find customers with orders count
     */
    @Query("SELECT c FROM Customer c LEFT JOIN c.orders o GROUP BY c HAVING COUNT(o) > :minOrders")
    List<Customer> findCustomersWithMinimumOrders(@Param("minOrders") int minOrders);

    /**
     * Count customers by type
     */
    long countByType(CustomerType type);
}
