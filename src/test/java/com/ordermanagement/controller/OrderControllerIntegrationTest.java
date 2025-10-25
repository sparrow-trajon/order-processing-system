package com.ordermanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ordermanagement.model.dto.request.CreateOrderRequest;
import com.ordermanagement.model.dto.request.OrderItemRequest;
import com.ordermanagement.model.dto.request.UpdateOrderStatusRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for OrderController.
 * Tests the entire flow from controller to database.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Order Controller Integration Tests")
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should create order successfully")
    void createOrder_Success() throws Exception {
        // Arrange
        OrderItemRequest item = OrderItemRequest.builder()
                .productName("Laptop")
                .productCode("LAPTOP-001")
                .quantity(2)
                .unitPrice(new BigDecimal("1200.00"))
                .build();

        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerName("John Doe")
                .customerEmail("john.doe@example.com")
                .items(List.of(item))
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.orderNumber", notNullValue()))
                .andExpect(jsonPath("$.customerName", is("John Doe")))
                .andExpect(jsonPath("$.customerEmail", is("john.doe@example.com")))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.totalAmount", is(2400.00)))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].productName", is("Laptop")))
                .andExpect(jsonPath("$.items[0].quantity", is(2)))
                .andExpect(jsonPath("$.items[0].totalPrice", is(2400.00)));
    }

    @Test
    @DisplayName("Should return 400 when creating order with invalid data")
    void createOrder_InvalidData() throws Exception {
        // Arrange - Missing customer name
        OrderItemRequest item = OrderItemRequest.builder()
                .productName("Laptop")
                .productCode("LAPTOP-001")
                .quantity(1)
                .unitPrice(new BigDecimal("1200.00"))
                .build();

        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerEmail("john.doe@example.com")
                .items(List.of(item))
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.fieldErrors", notNullValue()));
    }

    @Test
    @DisplayName("Should get order by ID successfully")
    void getOrderById_Success() throws Exception {
        // Arrange - Create an order first
        OrderItemRequest item = OrderItemRequest.builder()
                .productName("Mouse")
                .productCode("MOUSE-001")
                .quantity(1)
                .unitPrice(new BigDecimal("25.00"))
                .build();

        CreateOrderRequest createRequest = CreateOrderRequest.builder()
                .customerName("Jane Smith")
                .customerEmail("jane.smith@example.com")
                .items(List.of(item))
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String response = createResult.getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(response).get("id").asLong();

        // Act & Assert
        mockMvc.perform(get("/api/v1/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(orderId.intValue())))
                .andExpect(jsonPath("$.customerName", is("Jane Smith")))
                .andExpect(jsonPath("$.totalAmount", is(25.00)));
    }

    @Test
    @DisplayName("Should return 404 when order not found")
    void getOrderById_NotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/orders/{id}", 99999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")));
    }

    @Test
    @DisplayName("Should get all orders successfully")
    void getAllOrders_Success() throws Exception {
        // Arrange - Create two orders
        OrderItemRequest item1 = OrderItemRequest.builder()
                .productName("Product1")
                .productCode("PROD-001")
                .quantity(1)
                .unitPrice(new BigDecimal("100.00"))
                .build();

        CreateOrderRequest request1 = CreateOrderRequest.builder()
                .customerName("Customer 1")
                .customerEmail("customer1@example.com")
                .items(List.of(item1))
                .build();

        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated());

        // Act & Assert
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("Should filter orders by status")
    void getOrdersByStatus_Success() throws Exception {
        // Arrange - Create an order
        OrderItemRequest item = OrderItemRequest.builder()
                .productName("Product")
                .productCode("PROD-001")
                .quantity(1)
                .unitPrice(new BigDecimal("50.00"))
                .build();

        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerName("Test User")
                .customerEmail("test@example.com")
                .items(List.of(item))
                .build();

        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Act & Assert
        mockMvc.perform(get("/api/v1/orders")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[*].status", everyItem(is("PENDING"))));
    }

    @Test
    @DisplayName("Should update order status successfully")
    void updateOrderStatus_Success() throws Exception {
        // Arrange - Create an order
        OrderItemRequest item = OrderItemRequest.builder()
                .productName("Keyboard")
                .productCode("KB-001")
                .quantity(1)
                .unitPrice(new BigDecimal("75.00"))
                .build();

        CreateOrderRequest createRequest = CreateOrderRequest.builder()
                .customerName("Test User")
                .customerEmail("test@example.com")
                .items(List.of(item))
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String response = createResult.getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(response).get("id").asLong();

        UpdateOrderStatusRequest updateRequest = UpdateOrderStatusRequest.builder()
                .status("PROCESSING")
                .build();

        // Act & Assert
        mockMvc.perform(put("/api/v1/orders/{id}/status", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("PROCESSING")));
    }

    @Test
    @DisplayName("Should cancel order in PENDING status")
    void cancelOrder_Success() throws Exception {
        // Arrange - Create an order
        OrderItemRequest item = OrderItemRequest.builder()
                .productName("Product")
                .productCode("PROD-001")
                .quantity(1)
                .unitPrice(new BigDecimal("100.00"))
                .build();

        CreateOrderRequest createRequest = CreateOrderRequest.builder()
                .customerName("Test User")
                .customerEmail("test@example.com")
                .items(List.of(item))
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String response = createResult.getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(response).get("id").asLong();

        // Act & Assert
        mockMvc.perform(delete("/api/v1/orders/{id}", orderId))
                .andExpect(status().isNoContent());

        // Verify order is deleted
        mockMvc.perform(get("/api/v1/orders/{id}", orderId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should not cancel order in PROCESSING status")
    void cancelOrder_NotPending() throws Exception {
        // Arrange - Create an order and update to PROCESSING
        OrderItemRequest item = OrderItemRequest.builder()
                .productName("Product")
                .productCode("PROD-001")
                .quantity(1)
                .unitPrice(new BigDecimal("100.00"))
                .build();

        CreateOrderRequest createRequest = CreateOrderRequest.builder()
                .customerName("Test User")
                .customerEmail("test@example.com")
                .items(List.of(item))
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String response = createResult.getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(response).get("id").asLong();

        // Update to PROCESSING
        UpdateOrderStatusRequest updateRequest = UpdateOrderStatusRequest.builder()
                .status("PROCESSING")
                .build();

        mockMvc.perform(put("/api/v1/orders/{id}/status", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        // Act & Assert - Try to cancel
        mockMvc.perform(delete("/api/v1/orders/{id}", orderId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("cannot be cancelled")));
    }
}
