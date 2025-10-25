package com.ordermanagement.repository;

import com.ordermanagement.model.entity.Product;
import com.ordermanagement.model.enums.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Product entity.
 * Provides data access methods for product catalog management.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Find product by unique product code
     */
    Optional<Product> findByProductCode(String productCode);

    /**
     * Find products by SKU
     */
    Optional<Product> findBySku(String sku);

    /**
     * Find all active products
     */
    List<Product> findByIsActiveTrueAndIsDiscontinuedFalse();

    /**
     * Find products by category
     */
    List<Product> findByCategory(ProductCategory category);

    /**
     * Find products by brand
     */
    List<Product> findByBrand(String brand);

    /**
     * Find products that need reorder (low stock)
     */
    @Query("SELECT p FROM Product p WHERE p.stockQuantity.value <= p.reorderLevel.value AND p.isActive = true")
    List<Product> findProductsNeedingReorder();

    /**
     * Find products with stock less than threshold
     */
    @Query("SELECT p FROM Product p WHERE p.stockQuantity.value < :threshold AND p.isActive = true")
    List<Product> findLowStockProducts(@Param("threshold") int threshold);

    /**
     * Find products in stock
     */
    @Query("SELECT p FROM Product p WHERE p.stockQuantity.value > p.reservedQuantity.value AND p.isActive = true")
    List<Product> findProductsInStock();

    /**
     * Find products by name containing (case insensitive)
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Product> searchByName(@Param("name") String name);

    /**
     * Check if product exists by product code
     */
    boolean existsByProductCode(String productCode);

    /**
     * Count products by category
     */
    long countByCategory(ProductCategory category);

    /**
     * Count active products
     */
    long countByIsActiveTrueAndIsDiscontinuedFalse();
}
