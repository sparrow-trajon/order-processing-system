package com.ordermanagement.service;

import com.ordermanagement.constants.ApplicationConstants;
import com.ordermanagement.model.entity.ConfigurationParameter;
import com.ordermanagement.model.entity.OrderStatusEntity;
import com.ordermanagement.model.entity.OrderStatusTransition;
import com.ordermanagement.model.enums.ParameterType;
import com.ordermanagement.repository.ConfigurationParameterRepository;
import com.ordermanagement.repository.OrderStatusRepository;
import com.ordermanagement.repository.OrderStatusTransitionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service to initialize database with default data.
 * Seeds OrderStatus entities, transitions, and configuration parameters.
 *
 * Design Pattern: Data Initialization Pattern
 * Use Case: Bootstrap application with required reference data
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DataInitializationService {

    private final OrderStatusRepository statusRepository;
    private final OrderStatusTransitionRepository transitionRepository;
    private final ConfigurationParameterRepository configRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeData() {
        log.info("=== Starting Data Initialization ===");

        initializeOrderStatuses();
        initializeStatusTransitions();
        initializeConfigurationParameters();

        log.info("=== Data Initialization Complete ===");
    }

    /**
     * Initialize default order statuses
     */
    private void initializeOrderStatuses() {
        if (statusRepository.count() > 0) {
            log.info("Order statuses already initialized, skipping...");
            return;
        }

        log.info("Initializing order statuses...");

        // PENDING
        createStatus("PENDING", "Pending", "Order received and awaiting processing",
            "#FFA500", "hourglass", 1, false, true, true, false, false, false);

        // PROCESSING
        createStatus("PROCESSING", "Processing", "Order is being processed",
            "#1E90FF", "cog", 2, false, true, false, false, true, false);

        // CONFIRMED
        createStatus("CONFIRMED", "Confirmed", "Order confirmed and payment verified",
            "#32CD32", "check-circle", 3, false, false, false, true, false, false);

        // PREPARING
        createStatus("PREPARING", "Preparing", "Order is being prepared for shipment",
            "#4169E1", "box", 4, false, false, false, false, false, true);

        // SHIPPED
        createStatus("SHIPPED", "Shipped", "Order has been shipped",
            "#6A5ACD", "truck", 5, false, false, false, false, false, true);

        // DELIVERED
        createStatus("DELIVERED", "Delivered", "Order has been delivered",
            "#228B22", "package", 6, true, false, false, false, false, false);

        // COMPLETED
        createStatus("COMPLETED", "Completed", "Order completed successfully",
            "#008000", "check-double", 7, true, false, false, false, false, false);

        // CANCELLED
        createStatus("CANCELLED", "Cancelled", "Order has been cancelled",
            "#DC143C", "times-circle", 8, true, false, false, false, false, false);

        log.info("Order statuses initialized: {} statuses created", statusRepository.count());
    }

    /**
     * Helper method to create status
     */
    private void createStatus(String code, String name, String description,
                            String colorCode, String iconName, int displayOrder,
                            boolean isFinal, boolean isCancellable, boolean isModifiable,
                            boolean triggersPayment, boolean triggersInventory, boolean triggersShipping) {
        OrderStatusEntity status = OrderStatusEntity.builder()
            .code(code)
            .name(name)
            .description(description)
            .colorCode(colorCode)
            .iconName(iconName)
            .displayOrder(displayOrder)
            .isFinal(isFinal)
            .isCancellable(isCancellable)
            .isModifiable(isModifiable)
            .triggersPayment(triggersPayment)
            .triggersInventoryReservation(triggersInventory)
            .triggersShipping(triggersShipping)
            .sendsNotification(true)
            .isActive(true)
            .createdBy("SYSTEM")
            .build();

        statusRepository.save(status);
        log.debug("Created status: {}", code);
    }

    /**
     * Initialize default status transitions
     */
    private void initializeStatusTransitions() {
        if (transitionRepository.count() > 0) {
            log.info("Status transitions already initialized, skipping...");
            return;
        }

        log.info("Initializing status transitions...");

        // Get all statuses
        OrderStatusEntity pending = statusRepository.findByCode("PENDING").orElseThrow();
        OrderStatusEntity processing = statusRepository.findByCode("PROCESSING").orElseThrow();
        OrderStatusEntity confirmed = statusRepository.findByCode("CONFIRMED").orElseThrow();
        OrderStatusEntity preparing = statusRepository.findByCode("PREPARING").orElseThrow();
        OrderStatusEntity shipped = statusRepository.findByCode("SHIPPED").orElseThrow();
        OrderStatusEntity delivered = statusRepository.findByCode("DELIVERED").orElseThrow();
        OrderStatusEntity completed = statusRepository.findByCode("COMPLETED").orElseThrow();
        OrderStatusEntity cancelled = statusRepository.findByCode("CANCELLED").orElseThrow();

        // Define allowed transitions
        createTransition(pending, processing, "Begin order processing", 1);
        createTransition(pending, cancelled, "Cancel pending order", 2);

        createTransition(processing, confirmed, "Confirm order and payment", 1, true, false);
        createTransition(processing, cancelled, "Cancel during processing", 2);

        createTransition(confirmed, preparing, "Start preparing order", 1, false, true);
        createTransition(confirmed, cancelled, "Cancel confirmed order", 2);

        createTransition(preparing, shipped, "Ship the order", 1, false, false);

        createTransition(shipped, delivered, "Mark as delivered", 1);

        createTransition(delivered, completed, "Complete the order", 1);

        log.info("Status transitions initialized: {} transitions created", transitionRepository.count());
    }

    /**
     * Helper method to create transition
     */
    private void createTransition(OrderStatusEntity from, OrderStatusEntity to, String description,
                                int displayOrder) {
        createTransition(from, to, description, displayOrder, false, false);
    }

    private void createTransition(OrderStatusEntity from, OrderStatusEntity to, String description,
                                int displayOrder, boolean requiresPayment, boolean requiresInventory) {
        OrderStatusTransition transition = OrderStatusTransition.builder()
            .fromStatus(from)
            .toStatus(to)
            .isAllowed(true)
            .requiresApproval(false)
            .requiresPayment(requiresPayment)
            .requiresInventoryCheck(requiresInventory)
            .displayOrder(displayOrder)
            .description(description)
            .requiresReason(to.getCode().equals("CANCELLED"))
            .createdBy("SYSTEM")
            .build();

        transitionRepository.save(transition);
        log.debug("Created transition: {} -> {}", from.getCode(), to.getCode());
    }

    /**
     * Initialize configuration parameters
     */
    private void initializeConfigurationParameters() {
        if (configRepository.count() > 0) {
            log.info("Configuration parameters already initialized, skipping...");
            return;
        }

        log.info("Initializing configuration parameters...");

        // Order configuration
        createConfig("order.max.items", "100", ParameterType.INTEGER, "100",
            "Maximum items per order", "ORDER", "LIMITS", 1);
        createConfig("order.max.quantity.per.item", "10000", ParameterType.INTEGER, "10000",
            "Maximum quantity per item", "ORDER", "LIMITS", 2);
        createConfig("order.auto.cancel.hours", "24", ParameterType.INTEGER, "24",
            "Hours before auto-cancelling pending orders", "ORDER", "TIMEOUTS", 3);

        // Payment configuration
        createConfig("payment.max.attempts", "3", ParameterType.INTEGER, "3",
            "Maximum payment retry attempts", "PAYMENT", "LIMITS", 1);
        createConfig("payment.refund.timeout.hours", "24", ParameterType.INTEGER, "24",
            "Hours to process refund", "PAYMENT", "TIMEOUTS", 2);

        // Shipping configuration
        createConfig("shipping.free.threshold", "100.00", ParameterType.DOUBLE, "100.00",
            "Minimum order amount for free shipping", "SHIPPING", "COSTS", 1);
        createConfig("shipping.standard.cost", "10.00", ParameterType.DOUBLE, "10.00",
            "Standard shipping cost", "SHIPPING", "COSTS", 2);
        createConfig("shipping.express.cost", "25.00", ParameterType.DOUBLE, "25.00",
            "Express shipping cost", "SHIPPING", "COSTS", 3);

        // Discount configuration
        createConfig("discount.vip.percent", "15.0", ParameterType.DOUBLE, "15.0",
            "VIP customer discount percentage", "DISCOUNT", "CUSTOMER_TYPE", 1);
        createConfig("discount.wholesale.percent", "10.0", ParameterType.DOUBLE, "10.0",
            "Wholesale customer discount percentage", "DISCOUNT", "CUSTOMER_TYPE", 2);
        createConfig("order.bulk.discount.threshold", "10", ParameterType.INTEGER, "10",
            "Minimum items for bulk discount", "DISCOUNT", "BULK", 3);
        createConfig("order.bulk.discount.percent", "5.0", ParameterType.DOUBLE, "5.0",
            "Bulk order discount percentage", "DISCOUNT", "BULK", 4);

        // Tax configuration
        createConfig("tax.rate.percent", "10.0", ParameterType.DOUBLE, "10.0",
            "Default tax rate percentage", "TAX", "RATES", 1);

        // Inventory configuration
        createConfig("inventory.reservation.timeout.minutes", "30", ParameterType.INTEGER, "30",
            "Minutes before releasing inventory reservation", "INVENTORY", "TIMEOUTS", 1);
        createConfig("inventory.low.stock.threshold", "10", ParameterType.INTEGER, "10",
            "Threshold for low stock alerts", "INVENTORY", "ALERTS", 2);

        // Feature flags
        createConfig("feature.payment.gateway.enabled", "true", ParameterType.BOOLEAN, "true",
            "Enable payment gateway integration", "FEATURES", "TOGGLES", 1);
        createConfig("feature.email.notifications.enabled", "true", ParameterType.BOOLEAN, "true",
            "Enable email notifications", "FEATURES", "TOGGLES", 2);
        createConfig("feature.inventory.tracking.enabled", "true", ParameterType.BOOLEAN, "true",
            "Enable real-time inventory tracking", "FEATURES", "TOGGLES", 3);

        log.info("Configuration parameters initialized: {} parameters created", configRepository.count());
    }

    /**
     * Helper method to create configuration parameter
     */
    private void createConfig(String key, String value, ParameterType type, String defaultValue,
                            String description, String category, String subCategory, int displayOrder) {
        ConfigurationParameter param = ConfigurationParameter.builder()
            .paramKey(key)
            .paramValue(value)
            .paramType(type)
            .defaultValue(defaultValue)
            .description(description)
            .category(category)
            .subCategory(subCategory)
            .displayOrder(displayOrder)
            .isActive(true)
            .isEditable(true)
            .isSensitive(false)
            .environment("ALL")
            .createdBy("SYSTEM")
            .build();

        configRepository.save(param);
        log.debug("Created config: {}", key);
    }
}
