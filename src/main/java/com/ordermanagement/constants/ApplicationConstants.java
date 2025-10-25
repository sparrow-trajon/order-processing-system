package com.ordermanagement.constants;

import java.math.BigDecimal;

/**
 * Application-wide constants to eliminate magic numbers.
 * Centralized location for all hardcoded values.
 *
 * Design Pattern: Constants Pattern
 * Use Case: Eliminate magic numbers, improve maintainability
 *
 * Note: For truly dynamic configuration, use ConfigurationParameter entity.
 * These constants are for compile-time values that rarely change.
 */
public final class ApplicationConstants {

    private ApplicationConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Order-related constants
     */
    public static final class Order {
        public static final int MAX_ITEMS_PER_ORDER = 100;
        public static final int MAX_QUANTITY_PER_ITEM = 10000;
        public static final BigDecimal MAX_UNIT_PRICE = new BigDecimal("1000000.00");
        public static final BigDecimal MIN_UNIT_PRICE = new BigDecimal("0.01");
        public static final long PENDING_TO_PROCESSING_MINUTES = 10;
        public static final long AUTO_CANCEL_PENDING_ORDERS_HOURS = 24;
        public static final String ORDER_NUMBER_PREFIX = "ORD-";
        public static final int ORDER_NUMBER_LENGTH = 21; // ORD-YYYYMMDD-XXXXXXXX

        private Order() {}
    }

    /**
     * Payment-related constants
     */
    public static final class Payment {
        public static final int REFUND_TIMEOUT_HOURS = 24;
        public static final int MAX_PAYMENT_ATTEMPTS = 3;
        public static final int PAYMENT_AUTHORIZATION_VALIDITY_DAYS = 7;
        public static final BigDecimal MIN_PAYMENT_AMOUNT = new BigDecimal("0.01");
        public static final int PAYMENT_GATEWAY_TIMEOUT_SECONDS = 30;
        public static final String[] SUPPORTED_CURRENCIES = {"USD", "EUR", "GBP", "INR"};

        private Payment() {}
    }

    /**
     * Shipment-related constants
     */
    public static final class Shipment {
        public static final int MAX_DELIVERY_ATTEMPTS = 3;
        public static final int STANDARD_DELIVERY_DAYS = 7;
        public static final int EXPRESS_DELIVERY_DAYS = 2;
        public static final int OVERNIGHT_DELIVERY_HOURS = 24;
        public static final double MAX_PACKAGE_WEIGHT_KG = 50.0;
        public static final int TRACKING_NUMBER_LENGTH = 20;

        private Shipment() {}
    }

    /**
     * Customer-related constants
     */
    public static final class Customer {
        public static final int MIN_PASSWORD_LENGTH = 8;
        public static final int MAX_PASSWORD_LENGTH = 128;
        public static final int EMAIL_MAX_LENGTH = 255;
        public static final int PHONE_MAX_LENGTH = 20;
        public static final String CUSTOMER_CODE_PREFIX = "CUST-";
        public static final int MAX_ORDERS_PER_CUSTOMER_PER_DAY = 50;

        private Customer() {}
    }

    /**
     * Product/Inventory-related constants
     */
    public static final class Product {
        public static final int MIN_STOCK_ALERT_QUANTITY = 10;
        public static final int MAX_PRODUCT_NAME_LENGTH = 200;
        public static final int MAX_DESCRIPTION_LENGTH = 1000;
        public static final String PRODUCT_CODE_PREFIX = "PRD-";
        public static final double MAX_PRODUCT_WEIGHT_KG = 100.0;
        public static final int INVENTORY_RESERVATION_TIMEOUT_MINUTES = 30;

        private Product() {}
    }

    /**
     * Status-related constants
     */
    public static final class Status {
        // Default status codes (can be overridden by database)
        public static final String STATUS_PENDING = "PENDING";
        public static final String STATUS_PROCESSING = "PROCESSING";
        public static final String STATUS_CONFIRMED = "CONFIRMED";
        public static final String STATUS_SHIPPED = "SHIPPED";
        public static final String STATUS_DELIVERED = "DELIVERED";
        public static final String STATUS_CANCELLED = "CANCELLED";
        public static final String STATUS_COMPLETED = "COMPLETED";

        // Status transition timeouts
        public static final long PENDING_TIMEOUT_MINUTES = 30;
        public static final long PROCESSING_TIMEOUT_HOURS = 24;
        public static final long CONFIRMED_TIMEOUT_HOURS = 48;

        private Status() {}
    }

    /**
     * Validation-related constants
     */
    public static final class Validation {
        public static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        public static final String PHONE_REGEX = "^[+]?[0-9]{10,15}$";
        public static final String ORDER_NUMBER_REGEX = "^ORD-\\d{8}-[A-Z0-9]{8}$";
        public static final String ZIPCODE_REGEX = "^[0-9]{5,10}$";

        private Validation() {}
    }

    /**
     * Pagination and limits
     */
    public static final class Pagination {
        public static final int DEFAULT_PAGE_SIZE = 20;
        public static final int MAX_PAGE_SIZE = 100;
        public static final int MIN_PAGE_SIZE = 1;
        public static final int DEFAULT_PAGE_NUMBER = 0;

        private Pagination() {}
    }

    /**
     * Cache-related constants
     */
    public static final class Cache {
        public static final String PRODUCT_CACHE = "products";
        public static final String CUSTOMER_CACHE = "customers";
        public static final String ORDER_CACHE = "orders";
        public static final String CONFIG_CACHE = "configuration";
        public static final int DEFAULT_CACHE_TTL_MINUTES = 60;
        public static final int PRODUCT_CACHE_TTL_MINUTES = 120;
        public static final int CONFIG_CACHE_TTL_MINUTES = 30;

        private Cache() {}
    }

    /**
     * Security-related constants
     */
    public static final class Security {
        public static final int JWT_TOKEN_VALIDITY_HOURS = 24;
        public static final int REFRESH_TOKEN_VALIDITY_DAYS = 30;
        public static final int MAX_LOGIN_ATTEMPTS = 5;
        public static final int ACCOUNT_LOCKOUT_MINUTES = 30;
        public static final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/**",
            "/actuator/health",
            "/swagger-ui/**",
            "/api-docs/**",
            "/h2-console/**"
        };

        private Security() {}
    }

    /**
     * Notification-related constants
     */
    public static final class Notification {
        public static final int EMAIL_SUBJECT_MAX_LENGTH = 200;
        public static final int SMS_MESSAGE_MAX_LENGTH = 160;
        public static final int MAX_RETRY_ATTEMPTS = 3;
        public static final int RETRY_DELAY_SECONDS = 60;
        public static final String ORDER_CREATED_TEMPLATE = "order_created";
        public static final String ORDER_CONFIRMED_TEMPLATE = "order_confirmed";
        public static final String ORDER_SHIPPED_TEMPLATE = "order_shipped";
        public static final String ORDER_DELIVERED_TEMPLATE = "order_delivered";
        public static final String ORDER_CANCELLED_TEMPLATE = "order_cancelled";

        private Notification() {}
    }

    /**
     * Date/Time formats
     */
    public static final class DateTimeFormat {
        public static final String DATE_FORMAT = "yyyy-MM-dd";
        public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
        public static final String TIME_FORMAT = "HH:mm:ss";
        public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        public static final String DISPLAY_DATE_FORMAT = "MMM dd, yyyy";
        public static final String DISPLAY_DATE_TIME_FORMAT = "MMM dd, yyyy HH:mm";

        private DateTimeFormat() {}
    }

    /**
     * Error codes and messages
     */
    public static final class ErrorCodes {
        public static final String ORDER_NOT_FOUND = "ORD_001";
        public static final String INVALID_ORDER_STATUS = "ORD_002";
        public static final String ORDER_ALREADY_CANCELLED = "ORD_003";
        public static final String CUSTOMER_NOT_FOUND = "CUST_001";
        public static final String PRODUCT_NOT_FOUND = "PROD_001";
        public static final String INSUFFICIENT_STOCK = "PROD_002";
        public static final String PAYMENT_FAILED = "PAY_001";
        public static final String VALIDATION_ERROR = "VAL_001";
        public static final String UNAUTHORIZED = "AUTH_001";
        public static final String FORBIDDEN = "AUTH_002";
        public static final String INTERNAL_ERROR = "SYS_001";

        private ErrorCodes() {}
    }

    /**
     * HTTP-related constants
     */
    public static final class Http {
        public static final String HEADER_CORRELATION_ID = "X-Correlation-ID";
        public static final String HEADER_REQUEST_ID = "X-Request-ID";
        public static final String HEADER_TRACE_ID = "X-Trace-ID";
        public static final String HEADER_API_KEY = "X-API-Key";
        public static final String HEADER_API_VERSION = "X-API-Version";
        public static final int REQUEST_TIMEOUT_SECONDS = 30;
        public static final int MAX_REQUEST_SIZE_MB = 10;

        private Http() {}
    }

    /**
     * Feature flags (can be moved to ConfigurationParameter for dynamic control)
     */
    public static final class Features {
        public static final boolean ENABLE_PAYMENT_GATEWAY = true;
        public static final boolean ENABLE_EMAIL_NOTIFICATIONS = true;
        public static final boolean ENABLE_SMS_NOTIFICATIONS = false;
        public static final boolean ENABLE_INVENTORY_RESERVATION = true;
        public static final boolean ENABLE_ORDER_TRACKING = true;
        public static final boolean ENABLE_ANALYTICS = true;
        public static final boolean ENABLE_CACHING = true;
        public static final boolean ENABLE_RATE_LIMITING = true;

        private Features() {}
    }

    /**
     * Business rules (these should eventually be moved to ConfigurationParameter)
     */
    public static final class BusinessRules {
        public static final double VIP_CUSTOMER_DISCOUNT_PERCENT = 15.0;
        public static final double WHOLESALE_CUSTOMER_DISCOUNT_PERCENT = 10.0;
        public static final double BULK_ORDER_DISCOUNT_THRESHOLD = 10; // items
        public static final double BULK_ORDER_DISCOUNT_PERCENT = 5.0;
        public static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("100.00");
        public static final double TAX_RATE_PERCENT = 10.0; // Can vary by region
        public static final BigDecimal MIN_ORDER_AMOUNT = new BigDecimal("5.00");
        public static final int LOYALTY_POINTS_PER_DOLLAR = 1;
        public static final int LOYALTY_POINTS_REDEMPTION_THRESHOLD = 100;

        private BusinessRules() {}
    }
}
