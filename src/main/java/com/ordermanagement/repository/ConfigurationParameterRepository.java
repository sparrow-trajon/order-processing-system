package com.ordermanagement.repository;

import com.ordermanagement.model.entity.ConfigurationParameter;
import com.ordermanagement.model.enums.ParameterType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ConfigurationParameter entity.
 * Enables dynamic application configuration.
 */
@Repository
public interface ConfigurationParameterRepository extends JpaRepository<ConfigurationParameter, Long> {

    /**
     * Find parameter by unique key
     */
    Optional<ConfigurationParameter> findByParamKey(String paramKey);

    /**
     * Find all active parameters
     */
    List<ConfigurationParameter> findByIsActiveTrueOrderByDisplayOrder();

    /**
     * Find parameters by category
     */
    List<ConfigurationParameter> findByCategoryOrderByDisplayOrder(String category);

    /**
     * Find parameters by category and subcategory
     */
    List<ConfigurationParameter> findByCategoryAndSubCategoryOrderByDisplayOrder(
        String category,
        String subCategory
    );

    /**
     * Find editable parameters
     */
    List<ConfigurationParameter> findByIsEditableTrueOrderByDisplayOrder();

    /**
     * Find parameters by type
     */
    List<ConfigurationParameter> findByParamType(ParameterType paramType);

    /**
     * Find parameters by environment
     */
    List<ConfigurationParameter> findByEnvironment(String environment);

    /**
     * Find parameters by tenant (for multi-tenancy)
     */
    List<ConfigurationParameter> findByTenantId(String tenantId);

    /**
     * Search parameters by key pattern
     */
    @Query("SELECT c FROM ConfigurationParameter c WHERE c.paramKey LIKE %:pattern%")
    List<ConfigurationParameter> searchByKeyPattern(@Param("pattern") String pattern);

    /**
     * Check if parameter exists by key
     */
    boolean existsByParamKey(String paramKey);

    /**
     * Count parameters by category
     */
    long countByCategory(String category);
}
