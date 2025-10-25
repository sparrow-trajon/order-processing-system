package com.ordermanagement.service.impl;

import com.ordermanagement.exception.OrderCancellationException;
import com.ordermanagement.exception.OrderNotFoundException;
import com.ordermanagement.mapper.OrderMapper;
import com.ordermanagement.metrics.OrderMetrics;
import com.ordermanagement.model.dto.request.CreateOrderRequest;
import com.ordermanagement.model.dto.request.OrderItemRequest;
import com.ordermanagement.model.dto.response.OrderResponse;
import com.ordermanagement.model.entity.*;
import com.ordermanagement.model.valueobject.Email;
import com.ordermanagement.model.valueobject.Money;
import com.ordermanagement.model.valueobject.OrderNumber;
import com.ordermanagement.model.valueobject.Quantity;
import com.ordermanagement.repository.OrderRepository;
import com.ordermanagement.service.OrderPricingService;
import com.ordermanagement.service.OrderStatusService;
import com.ordermanagement.validator.OrderValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderServiceImpl.
 * Tests all business logic with mocked dependencies.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderValidator orderValidator;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private OrderMetrics orderMetrics;

    @Mock
    private OrderStatusService orderStatusService;

    @Mock
    private OrderPricingService orderPricingService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private CreateOrderRequest createOrderRequest;
    private Order order;
    private OrderResponse orderResponse;
    private OrderStatusEntity pendingStatus;
    private OrderStatusEntity processingStatus;
    private Customer customer;
    private Item item;

    @BeforeEach
    void setUp() {
        // Setup test data
        OrderItemRequest itemRequest = OrderItemRequest.builder()
                .productName("Laptop")
                .productCode("LAPTOP-001")
                .quantity(1)
                .unitPrice(new BigDecimal("1200.00"))
                .build();

        createOrderRequest = CreateOrderRequest.builder()
                .customerName("John Doe")
                .customerEmail("john.doe@example.com")
                .items(List.of(itemRequest))
                .build();

        // Create mock OrderStatusEntity
        pendingStatus = OrderStatusEntity.builder()
                .id(1L)
                .code("PENDING")
                .name("Pending")
                .description("Order is pending")
                .displayOrder(1)
                .isActive(true)
                .isFinal(false)
                .build();

        processingStatus = OrderStatusEntity.builder()
                .id(2L)
                .code("PROCESSING")
                .name("Processing")
                .description("Order is being processed")
                .displayOrder(2)
                .isActive(true)
                .isFinal(false)
                .build();

        // Create mock Customer
        customer = Customer.builder()
                .id(1L)
                .fullName("John Doe")
                .email(Email.of("john.doe@example.com"))
                .isActive(true)
                .build();

        // Create mock Item
        item = Item.builder()
                .id(1L)
                .productNameSnapshot("Laptop")
                .productCodeSnapshot("LAPTOP-001")
                .quantity(Quantity.of(1))
                .unitPrice(Money.of(new BigDecimal("1200.00")))
                .totalPrice(Money.of(new BigDecimal("1200.00")))
                .tax(Money.zero())
                .finalAmount(Money.of(new BigDecimal("1200.00")))
                .build();

        order = Order.builder()
                .id(1L)
                .orderNumber(OrderNumber.of("ORD-123456789-1234"))
                .customer(customer)
                .status(pendingStatus)
                .totalAmount(Money.of(new BigDecimal("1200.00")))
                .items(new ArrayList<>(List.of(item)))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        orderResponse = OrderResponse.builder()
                .id(1L)
                .orderNumber("ORD-123456789-1234")
                .customerName("John Doe")
                .customerEmail("john.doe@example.com")
                .status("PENDING")
                .totalAmount(new BigDecimal("1200.00"))
                .build();
    }

    @Test
    @DisplayName("Should create order successfully")
    void createOrder_Success() {
        // Arrange
        when(orderMapper.toEntity(createOrderRequest)).thenReturn(order);
        when(orderStatusService.getDefaultStatus()).thenReturn(pendingStatus);
        when(orderRepository.existsByOrderNumber(anyString())).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        // Act
        OrderResponse result = orderService.createOrder(createOrderRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCustomerName()).isEqualTo("John Doe");
        assertThat(result.getStatus()).isEqualTo("PENDING");

        verify(orderValidator).validateCreateOrderRequest(createOrderRequest);
        verify(orderStatusService).getDefaultStatus();
        verify(orderPricingService).calculateOrderPricing(any(Order.class));
        verify(orderRepository).save(any(Order.class));
        verify(orderMapper).toResponse(order);
    }

    @Test
    @DisplayName("Should throw exception when order not found by ID")
    void getOrderById_NotFound() {
        // Arrange
        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.getOrderById(1L))
                .isInstanceOf(OrderNotFoundException.class);

        verify(orderRepository).findByIdWithItems(1L);
    }

    @Test
    @DisplayName("Should get order by ID successfully")
    void getOrderById_Success() {
        // Arrange
        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        // Act
        OrderResponse result = orderService.getOrderById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getOrderNumber()).isEqualTo("ORD-123456789-1234");

        verify(orderRepository).findByIdWithItems(1L);
        verify(orderMapper).toResponse(order);
    }

    @Test
    @DisplayName("Should get all orders successfully")
    void getAllOrders_Success() {
        // Arrange
        List<Order> orders = Arrays.asList(order);
        List<OrderResponse> responses = Arrays.asList(orderResponse);

        when(orderRepository.findAllWithItems()).thenReturn(orders);
        when(orderMapper.toResponses(orders)).thenReturn(responses);

        // Act
        List<OrderResponse> result = orderService.getAllOrders();

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);

        verify(orderRepository).findAllWithItems();
        verify(orderMapper).toResponses(orders);
    }

    @Test
    @DisplayName("Should get orders by status successfully")
    void getOrdersByStatus_Success() {
        // Arrange
        List<Order> orders = Arrays.asList(order);
        List<OrderResponse> responses = Arrays.asList(orderResponse);

        when(orderStatusService.getStatusByCode("PENDING")).thenReturn(pendingStatus);
        when(orderRepository.findByStatusWithItems(pendingStatus)).thenReturn(orders);
        when(orderMapper.toResponses(orders)).thenReturn(responses);

        // Act
        List<OrderResponse> result = orderService.getOrdersByStatus("PENDING");

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);

        verify(orderStatusService).getStatusByCode("PENDING");
        verify(orderRepository).findByStatusWithItems(pendingStatus);
        verify(orderMapper).toResponses(orders);
    }

    @Test
    @DisplayName("Should throw exception when status is null")
    void getOrdersByStatus_NullStatus() {
        // Act & Assert
        assertThatThrownBy(() -> orderService.getOrdersByStatus(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Status code cannot be null or empty");

        verify(orderRepository, never()).findByStatusWithItems(any());
    }

    @Test
    @DisplayName("Should update order status successfully")
    void updateOrderStatus_Success() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        // Act
        OrderResponse result = orderService.updateOrderStatus(1L, "PROCESSING");

        // Assert
        assertThat(result).isNotNull();

        verify(orderRepository).findById(1L);
        verify(orderStatusService).transitionStatus(eq(order), eq("PROCESSING"), anyString(), anyString());
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("Should cancel order in PENDING status")
    void cancelOrder_Success() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act
        orderService.cancelOrder(1L);

        // Assert
        verify(orderRepository).findById(1L);
        verify(orderRepository).delete(order);
    }

    @Test
    @DisplayName("Should throw exception when cancelling non-PENDING order")
    void cancelOrder_NotPending() {
        // Arrange
        order.setStatus(processingStatus);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThatThrownBy(() -> orderService.cancelOrder(1L))
                .isInstanceOf(OrderCancellationException.class);

        verify(orderRepository).findById(1L);
        verify(orderRepository, never()).delete(any());
    }
}
