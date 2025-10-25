package com.ordermanagement.mapper;

import com.ordermanagement.model.dto.request.CreateOrderRequest;
import com.ordermanagement.model.dto.request.OrderItemRequest;
import com.ordermanagement.model.dto.response.OrderItemResponse;
import com.ordermanagement.model.dto.response.OrderResponse;
import com.ordermanagement.model.entity.*;
import com.ordermanagement.model.valueobject.Email;
import com.ordermanagement.model.valueobject.Money;
import com.ordermanagement.model.valueobject.Quantity;
import com.ordermanagement.repository.CustomerRepository;
import com.ordermanagement.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper component for converting between entities and DTOs.
 * Provides transformation logic for Order and Item objects.
 *
 * Design Patterns:
 * - Mapper Pattern - Converts between different object representations
 * - Data Transfer Object (DTO) Pattern - Separates API contracts from domain model
 *
 * SOLID Principles:
 * - Single Responsibility: Only handles object mapping
 * - Open/Closed: Can be extended with new mapping methods without modification
 */
@Component
@RequiredArgsConstructor
public class OrderMapper {

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    /**
     * Converts CreateOrderRequest DTO to Order entity.
     * Finds or creates customer, maps items with product references.
     *
     * @param request The order creation request
     * @return Order entity with items and customer
     */
    public Order toEntity(CreateOrderRequest request) {
        if (request == null) {
            return null;
        }

        // Find or create customer
        Customer customer = findOrCreateCustomer(request.getCustomerName(), request.getCustomerEmail());

        // Build order (status will be set by service)
        Order order = Order.builder()
                .customer(customer)
                .items(new ArrayList<>())
                .build();

        // Map and add items
        if (request.getItems() != null) {
            request.getItems().forEach(itemRequest -> {
                Item item = toItemEntity(itemRequest);
                order.addItem(item);
            });
        }

        return order;
    }

    /**
     * Finds existing customer by email or creates a new one.
     */
    private Customer findOrCreateCustomer(String customerName, String customerEmail) {
        Email email = Email.of(customerEmail);

        return customerRepository.findByEmail(email.getAddress())
                .orElseGet(() -> {
                    Customer newCustomer = Customer.builder()
                            .fullName(customerName)
                            .email(email)
                            .build();
                    return customerRepository.save(newCustomer);
                });
    }

    /**
     * Converts OrderItemRequest DTO to Item entity.
     * Attempts to link to Product entity if exists.
     *
     * @param request The order item request
     * @return Item entity
     */
    public Item toItemEntity(OrderItemRequest request) {
        if (request == null) {
            return null;
        }

        // Try to find product in catalog
        Product product = productRepository.findByProductCode(request.getProductCode())
                .orElse(null);

        Item item = Item.builder()
                .product(product)
                .productCodeSnapshot(request.getProductCode())
                .productNameSnapshot(request.getProductName())
                .quantity(Quantity.of(request.getQuantity()))
                .unitPrice(Money.of(new BigDecimal(String.valueOf(request.getUnitPrice()))))
                .build();

        // Calculate pricing immediately to initialize totalPrice, tax, and finalAmount
        // This prevents NullPointerException when Order.calculateTotals() is called
        item.calculatePricing();

        return item;
    }

    /**
     * Converts Order entity to OrderResponse DTO.
     *
     * @param order The order entity
     * @return OrderResponse DTO
     */
    public OrderResponse toResponse(Order order) {
        if (order == null) {
            return null;
        }

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber() != null ? order.getOrderNumber().getValue() : null)
                .customerName(order.getCustomer() != null ? order.getCustomer().getFullName() : null)
                .customerEmail(order.getCustomer() != null && order.getCustomer().getEmail() != null
                        ? order.getCustomer().getEmail().getAddress() : null)
                .totalAmount(order.getFinalAmount() != null
                        ? order.getFinalAmount().getAmount()
                        : BigDecimal.ZERO)
                .status(order.getStatus() != null ? order.getStatus().getCode() : null)
                .items(toItemResponses(order.getItems()))
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    /**
     * Converts a list of Order entities to OrderResponse DTOs.
     *
     * @param orders List of order entities
     * @return List of OrderResponse DTOs
     */
    public List<OrderResponse> toResponses(List<Order> orders) {
        if (orders == null) {
            return new ArrayList<>();
        }

        return orders.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Converts a list of Item entities to OrderItemResponse DTOs.
     *
     * @param items List of item entities
     * @return List of OrderItemResponse DTOs
     */
    public List<OrderItemResponse> toItemResponses(List<Item> items) {
        if (items == null) {
            return new ArrayList<>();
        }

        return items.stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());
    }

    /**
     * Converts Item entity to OrderItemResponse DTO.
     *
     * @param item The item entity
     * @return OrderItemResponse DTO
     */
    public OrderItemResponse toItemResponse(Item item) {
        if (item == null) {
            return null;
        }

        return OrderItemResponse.builder()
                .id(item.getId())
                .productName(item.getProductNameSnapshot())
                .productCode(item.getProductCodeSnapshot())
                .quantity(item.getQuantity() != null ? item.getQuantity().getValue() : 0)
                .unitPrice(item.getUnitPrice() != null
                        ? item.getUnitPrice().getAmount()
                        : BigDecimal.ZERO)
                .totalPrice(item.getFinalAmount() != null
                        ? item.getFinalAmount().getAmount()
                        : BigDecimal.ZERO)
                .build();
    }
}
