package com.ordermanagement.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration class for OpenAPI (Swagger) documentation.
 * Provides comprehensive API documentation for the Order Management System.
 *
 * Design Patterns:
 * - Configuration Pattern - Centralized configuration
 * - Builder Pattern - Fluent API for building OpenAPI objects
 *
 * SOLID Principles:
 * - Single Responsibility: Only handles API documentation configuration
 * - Open/Closed: Can be extended with additional documentation settings
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * Configures OpenAPI documentation for the application.
     *
     * @return OpenAPI configuration
     */
    @Bean
    public OpenAPI orderManagementOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Development Server")
                ));
    }

    /**
     * Creates API information metadata.
     *
     * @return API Info object
     */
    private Info apiInfo() {
        return new Info()
                .title("Order Management System API")
                .description("""
                        ## E-commerce Order Processing System

                        This API provides comprehensive order management capabilities for an e-commerce platform.

                        ### Features
                        - Create orders with multiple items
                        - Retrieve order details and history
                        - Update order status through defined lifecycle
                        - Cancel pending orders
                        - Automatic status progression (PENDING → PROCESSING every 5 minutes)

                        ### Order Status Lifecycle
                        1. **PENDING** - Order created, awaiting processing
                        2. **PROCESSING** - Order being prepared
                        3. **SHIPPED** - Order dispatched
                        4. **DELIVERED** - Order completed

                        ### Business Rules
                        - Only PENDING orders can be cancelled
                        - Status transitions are unidirectional (PENDING → PROCESSING → SHIPPED → DELIVERED)
                        - Orders require at least one item
                        - Automatic status update runs every 5 minutes

                        ### Technical Stack
                        - Java 21
                        - Spring Boot 3.2.0
                        - H2 In-Memory Database
                        - JPA/Hibernate
                        - Maven
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("Order Management Team")
                        .email("support@ordermanagement.com")
                        .url("https://ordermanagement.com"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0.html"));
    }
}
