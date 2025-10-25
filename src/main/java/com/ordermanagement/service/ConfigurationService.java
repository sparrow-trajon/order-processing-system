package com.ordermanagement.service;

import com.ordermanagement.model.entity.ConfigurationParameter;
import com.ordermanagement.model.enums.ParameterType;
import com.ordermanagement.repository.ConfigurationParameterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing dynamic configuration parameters.
 * Enables runtime configuration changes without code deployment.
 *
 * Design Pattern: Service Pattern, Cache-Aside Pattern
 * Use Case: Dynamic business rules and application settings
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConfigurationService {

    private final ConfigurationParameterRepository configRepository;

    /**
     * Get parameter value as String
     */
    @Cacheable(value = "configuration", key = "#key")
    public String getString(String key) {
        return configRepository.findByParamKey(key)
            .map(ConfigurationParameter::getValueAsString)
            .orElse(null);
    }

    /**
     * Get parameter value as String with default
     */
    @Cacheable(value = "configuration", key = "#key")
    public String getString(String key, String defaultValue) {
        return configRepository.findByParamKey(key)
            .map(ConfigurationParameter::getValueAsString)
            .orElse(defaultValue);
    }

    /**
     * Get parameter value as Integer
     */
    @Cacheable(value = "configuration", key = "#key")
    public Integer getInteger(String key) {
        return configRepository.findByParamKey(key)
            .map(ConfigurationParameter::getValueAsInteger)
            .orElse(null);
    }

    /**
     * Get parameter value as Integer with default
     */
    @Cacheable(value = "configuration", key = "#key")
    public Integer getInteger(String key, Integer defaultValue) {
        return configRepository.findByParamKey(key)
            .map(ConfigurationParameter::getValueAsInteger)
            .orElse(defaultValue);
    }

    /**
     * Get parameter value as Long
     */
    @Cacheable(value = "configuration", key = "#key")
    public Long getLong(String key) {
        return configRepository.findByParamKey(key)
            .map(ConfigurationParameter::getValueAsLong)
            .orElse(null);
    }

    /**
     * Get parameter value as Long with default
     */
    @Cacheable(value = "configuration", key = "#key")
    public Long getLong(String key, Long defaultValue) {
        return configRepository.findByParamKey(key)
            .map(ConfigurationParameter::getValueAsLong)
            .orElse(defaultValue);
    }

    /**
     * Get parameter value as Double
     */
    @Cacheable(value = "configuration", key = "#key")
    public Double getDouble(String key) {
        return configRepository.findByParamKey(key)
            .map(ConfigurationParameter::getValueAsDouble)
            .orElse(null);
    }

    /**
     * Get parameter value as Double with default
     */
    @Cacheable(value = "configuration", key = "#key")
    public Double getDouble(String key, Double defaultValue) {
        return configRepository.findByParamKey(key)
            .map(ConfigurationParameter::getValueAsDouble)
            .orElse(defaultValue);
    }

    /**
     * Get parameter value as Boolean
     */
    @Cacheable(value = "configuration", key = "#key")
    public Boolean getBoolean(String key) {
        return configRepository.findByParamKey(key)
            .map(ConfigurationParameter::getValueAsBoolean)
            .orElse(null);
    }

    /**
     * Get parameter value as Boolean with default
     */
    @Cacheable(value = "configuration", key = "#key")
    public Boolean getBoolean(String key, Boolean defaultValue) {
        return configRepository.findByParamKey(key)
            .map(ConfigurationParameter::getValueAsBoolean)
            .orElse(defaultValue);
    }

    /**
     * Get all parameters by category
     */
    @Cacheable(value = "configuration", key = "'category:' + #category")
    public List<ConfigurationParameter> getByCategory(String category) {
        return configRepository.findByCategoryOrderByDisplayOrder(category);
    }

    /**
     * Get all active parameters
     */
    @Cacheable(value = "configuration", key = "'all-active'")
    public List<ConfigurationParameter> getAllActive() {
        return configRepository.findByIsActiveTrueOrderByDisplayOrder();
    }

    /**
     * Update parameter value
     */
    @Transactional
    public void updateParameter(String key, String newValue, String updatedBy) {
        ConfigurationParameter param = configRepository.findByParamKey(key)
            .orElseThrow(() -> new IllegalArgumentException("Parameter not found: " + key));

        param.updateValue(newValue, updatedBy);
        configRepository.save(param);

        log.info("Configuration parameter updated: key={}, newValue={}, updatedBy={}",
            key, newValue, updatedBy);
    }

    /**
     * Create new parameter
     */
    @Transactional
    public ConfigurationParameter createParameter(ConfigurationParameter parameter) {
        if (configRepository.existsByParamKey(parameter.getParamKey())) {
            throw new IllegalArgumentException("Parameter already exists: " + parameter.getParamKey());
        }

        ConfigurationParameter saved = configRepository.save(parameter);
        log.info("Configuration parameter created: key={}", parameter.getParamKey());
        return saved;
    }

    /**
     * Get parameter entity
     */
    public Optional<ConfigurationParameter> getParameter(String key) {
        return configRepository.findByParamKey(key);
    }
}
