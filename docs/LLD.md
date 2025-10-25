# Low Level Design (LLD) - E-commerce Order Processing System

## Table of Contents
1. [Class Diagrams](#1-class-diagrams)
2. [Package Structure](#2-package-structure)
3. [Detailed Class Specifications](#3-detailed-class-specifications)
4. [API Specifications](#4-api-specifications)
5. [Sequence Diagrams](#5-sequence-diagrams)
6. [Database Schema Details](#6-database-schema-details)
7. [Design Pattern Implementations](#7-design-pattern-implementations)
8. [Validation Rules](#8-validation-rules)

---

## 1. Class Diagrams

### 1.1 Domain Model (Entity Layer)

```
┌─────────────────────────────────────┐
│           <<Entity>>                │
│             Order                   │
├─────────────────────────────────────┤
│ - id: Long                          │
│ - orderNumber: String               │
│ - customerName: String              │
│ - customerEmail: String             │
│ - totalAmount: BigDecimal           │
│ - status: OrderStatus               │
│ - orderItems: List<OrderItem>       │
│ - createdAt: LocalDateTime          │
│ - updatedAt: LocalDateTime          │
│ - version: Integer                  │
├─────────────────────────────────────┤
│ + addOrderItem(OrderItem): void     │
│ + removeOrderItem(OrderItem): void  │
│ + calculateTotalAmount(): void      │
│ + canBeCancelled(): boolean         │
│ + updateStatus(OrderStatus): void   │
└─────────────────────────────────────┘
                  │
                  │ 1
                  │
                  │ *
┌─────────────────────────────────────┐
│           <<Entity>>                │
│           OrderItem                 │
├─────────────────────────────────────┤
│ - id: Long                          │
│ - order: Order                      │
│ - productName: String               │
│ - productCode: String               │
│ - quantity: Integer                 │
│ - unitPrice: BigDecimal             │
│ - totalPrice: BigDecimal            │
│ - createdAt: LocalDateTime          │
├─────────────────────────────────────┤
│ + calculateTotalPrice(): void       │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│           <<Enum>>                  │
│          OrderStatus                │
├─────────────────────────────────────┤
│ PENDING                             │
│ PROCESSING                          │
│ SHIPPED                             │
│ DELIVERED                           │
├─────────────────────────────────────┤
│ + canTransitionTo(OrderStatus): boolean │
│ + getNextStatus(): OrderStatus      │
└─────────────────────────────────────┘
```

### 1.2 Service Layer

```
┌─────────────────────────────────────┐
│         <<Interface>>               │
│          OrderService               │
├─────────────────────────────────────┤
│ + createOrder(CreateOrderRequest): OrderResponse      │
│ + getOrderById(Long): OrderResponse                   │
│ + getAllOrders(): List<OrderResponse>                 │
│ + getOrdersByStatus(OrderStatus): List<OrderResponse> │
│ + updateOrderStatus(Long, OrderStatus): OrderResponse │
│ + cancelOrder(Long): void                             │
│ + processScheduledStatusUpdate(): void                │
└─────────────────────────────────────┘
                  △
                  │ implements
                  │
┌─────────────────────────────────────┐
│          <<Service>>                │
│       OrderServiceImpl              │
├─────────────────────────────────────┤
│ - orderRepository: OrderRepository  │
│ - orderMapper: OrderMapper          │
│ - orderValidator: OrderValidator    │
├─────────────────────────────────────┤
│ + createOrder(CreateOrderRequest): OrderResponse      │
│ + getOrderById(Long): OrderResponse                   │
│ + getAllOrders(): List<OrderResponse>                 │
│ + getOrdersByStatus(OrderStatus): List<OrderResponse> │
│ + updateOrderStatus(Long, OrderStatus): OrderResponse │
│ + cancelOrder(Long): void                             │
│ + processScheduledStatusUpdate(): void                │
│ - generateOrderNumber(): String                       │
└─────────────────────────────────────┘
```

### 1.3 Repository Layer

```
┌─────────────────────────────────────────────┐
│         <<Interface>>                       │
│      OrderRepository                        │
│   extends JpaRepository<Order, Long>        │
├─────────────────────────────────────────────┤
│ + findByOrderNumber(String): Optional<Order>│
│ + findByStatus(OrderStatus): List<Order>    │
│ + findByCustomerEmail(String): List<Order>  │
│ + existsByOrderNumber(String): boolean      │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│         <<Interface>>                       │
│      OrderItemRepository                    │
│   extends JpaRepository<OrderItem, Long>    │
├─────────────────────────────────────────────┤
│ + findByOrderId(Long): List<OrderItem>      │
│ + deleteByOrderId(Long): void               │
└─────────────────────────────────────────────┘
```

### 1.4 Controller Layer

```
┌─────────────────────────────────────────────┐
│       <<RestController>>                    │
│        OrderController                      │
├─────────────────────────────────────────────┤
│ - orderService: OrderService                │
├─────────────────────────────────────────────┤
│ + createOrder(CreateOrderRequest): ResponseEntity<OrderResponse>     │
│ + getOrder(Long): ResponseEntity<OrderResponse>                      │
│ + getAllOrders(OrderStatus?): ResponseEntity<List<OrderResponse>>    │
│ + updateOrderStatus(Long, UpdateStatusRequest): ResponseEntity<...>  │
│ + cancelOrder(Long): ResponseEntity<Void>                            │
└─────────────────────────────────────────────┘
```

---

## 2. Package Structure

```
com.ordermanagement
├── controller
│   └── OrderController.java
├── service
│   ├── OrderService.java (interface)
│   ├── impl
│   │   └── OrderServiceImpl.java
│   └── scheduler
│       └── OrderStatusScheduler.java
├── repository
│   ├── OrderRepository.java
│   └── OrderItemRepository.java
├── model
│   ├── entity
│   │   ├── Order.java
│   │   └── OrderItem.java
│   ├── enums
│   │   └── OrderStatus.java
│   └── dto
│       ├── request
│       │   ├── CreateOrderRequest.java
│       │   ├── OrderItemRequest.java
│       │   └── UpdateOrderStatusRequest.java
│       └── response
│           ├── OrderResponse.java
│           ├── OrderItemResponse.java
│           └── ErrorResponse.java
├── mapper
│   └── OrderMapper.java
├── validator
│   ├── OrderValidator.java (interface)
│   └── impl
│       └── OrderValidatorImpl.java
├── exception
│   ├── OrderNotFoundException.java
│   ├── InvalidOrderStatusException.java
│   ├── OrderCancellationException.java
│   └── handler
│       └── GlobalExceptionHandler.java
├── config
│   ├── SwaggerConfig.java
│   └── SchedulerConfig.java
└── OrderManagementApplication.java
```

---

## 3. Detailed Class Specifications

### 3.1 Entity Classes

#### Order.java
```java
@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String orderNumber;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "Customer name is required")
    @Size(min = 2, max = 100)
    private String customerName;

    @Column(nullable = false, length = 100)
    @Email(message = "Invalid email format")
    @NotBlank(message = "Customer email is required")
    private String customerEmail;

    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Total amount must be greater than 0")
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @NotEmpty(message = "Order must have at least one item")
    private List<OrderItem> orderItems = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Integer version; // For optimistic locking

    // Business methods
    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
        calculateTotalAmount();
    }

    public void removeOrderItem(OrderItem item) {
        orderItems.remove(item);
        item.setOrder(null);
        calculateTotalAmount();
    }

    public void calculateTotalAmount() {
        this.totalAmount = orderItems.stream()
            .map(OrderItem::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean canBeCancelled() {
        return this.status == OrderStatus.PENDING;
    }

    public void updateStatus(OrderStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new InvalidOrderStatusException(
                "Cannot transition from " + this.status + " to " + newStatus
            );
        }
        this.status = newStatus;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = OrderStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

#### OrderItem.java
```java
@Entity
@Table(name = "order_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Order order;

    @Column(nullable = false, length = 200)
    @NotBlank(message = "Product name is required")
    private String productName;

    @Column(nullable = false, length = 50)
    @NotBlank(message = "Product code is required")
    private String productCode;

    @Column(nullable = false)
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.01", message = "Unit price must be greater than 0")
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    @PreUpdate
    public void calculateTotalPrice() {
        if (quantity != null && unitPrice != null) {
            this.totalPrice = unitPrice.multiply(new BigDecimal(quantity));
        }
    }
}
```

#### OrderStatus.java (Enum)
```java
public enum OrderStatus {
    PENDING("Pending"),
    PROCESSING("Processing"),
    SHIPPED("Shipped"),
    DELIVERED("Delivered");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean canTransitionTo(OrderStatus newStatus) {
        return switch (this) {
            case PENDING -> newStatus == PROCESSING;
            case PROCESSING -> newStatus == SHIPPED;
            case SHIPPED -> newStatus == DELIVERED;
            case DELIVERED -> false; // Final state
        };
    }

    public OrderStatus getNextStatus() {
        return switch (this) {
            case PENDING -> PROCESSING;
            case PROCESSING -> SHIPPED;
            case SHIPPED -> DELIVERED;
            case DELIVERED -> DELIVERED; // Already at final state
        };
    }
}
```

### 3.2 DTO Classes

#### CreateOrderRequest.java
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    @NotBlank(message = "Customer name is required")
    @Size(min = 2, max = 100, message = "Customer name must be between 2 and 100 characters")
    private String customerName;

    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email format")
    private String customerEmail;

    @NotNull(message = "Order items are required")
    @NotEmpty(message = "Order must have at least one item")
    @Valid
    private List<OrderItemRequest> items;
}
```

#### OrderItemRequest.java
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequest {
    @NotBlank(message = "Product name is required")
    @Size(min = 1, max = 200, message = "Product name must be between 1 and 200 characters")
    private String productName;

    @NotBlank(message = "Product code is required")
    @Size(min = 1, max = 50, message = "Product code must be between 1 and 50 characters")
    private String productCode;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 10000, message = "Quantity cannot exceed 10000")
    private Integer quantity;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.01", message = "Unit price must be greater than 0")
    @DecimalMax(value = "1000000.00", message = "Unit price cannot exceed 1000000")
    private BigDecimal unitPrice;
}
```

#### OrderResponse.java
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private String customerName;
    private String customerEmail;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

#### OrderItemResponse.java
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    private Long id;
    private String productName;
    private String productCode;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}
```

#### UpdateOrderStatusRequest.java
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusRequest {
    @NotNull(message = "Status is required")
    private OrderStatus status;
}
```

#### ErrorResponse.java
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private List<FieldError> fieldErrors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldError {
        private String field;
        private String message;
        private Object rejectedValue;
    }
}
```

### 3.3 Service Classes

#### OrderService.java (Interface)
```java
public interface OrderService {
    /**
     * Create a new order
     * @param request Order creation request
     * @return Created order response
     */
    OrderResponse createOrder(CreateOrderRequest request);

    /**
     * Get order by ID
     * @param id Order ID
     * @return Order response
     * @throws OrderNotFoundException if order not found
     */
    OrderResponse getOrderById(Long id);

    /**
     * Get all orders
     * @return List of all orders
     */
    List<OrderResponse> getAllOrders();

    /**
     * Get orders by status
     * @param status Order status filter
     * @return List of orders with specified status
     */
    List<OrderResponse> getOrdersByStatus(OrderStatus status);

    /**
     * Update order status
     * @param id Order ID
     * @param newStatus New status
     * @return Updated order response
     * @throws OrderNotFoundException if order not found
     * @throws InvalidOrderStatusException if status transition is invalid
     */
    OrderResponse updateOrderStatus(Long id, OrderStatus newStatus);

    /**
     * Cancel an order (only if in PENDING status)
     * @param id Order ID
     * @throws OrderNotFoundException if order not found
     * @throws OrderCancellationException if order cannot be cancelled
     */
    void cancelOrder(Long id);

    /**
     * Process scheduled status update (PENDING → PROCESSING)
     * Called by scheduler every 5 minutes
     */
    void processScheduledStatusUpdate();
}
```

#### OrderServiceImpl.java
```java
@Service
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderValidator orderValidator;

    @Autowired
    public OrderServiceImpl(
        OrderRepository orderRepository,
        OrderMapper orderMapper,
        OrderValidator orderValidator
    ) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.orderValidator = orderValidator;
    }

    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order for customer: {}", request.getCustomerName());

        // Validate request
        orderValidator.validateCreateOrderRequest(request);

        // Map to entity
        Order order = orderMapper.toEntity(request);
        order.setOrderNumber(generateOrderNumber());
        order.setStatus(OrderStatus.PENDING);

        // Calculate total amount
        order.calculateTotalAmount();

        // Save order
        Order savedOrder = orderRepository.save(order);

        log.info("Order created successfully with order number: {}", savedOrder.getOrderNumber());

        return orderMapper.toResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        log.debug("Fetching order with ID: {}", id);

        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + id));

        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        log.debug("Fetching all orders");

        return orderRepository.findAll().stream()
            .map(orderMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        log.debug("Fetching orders with status: {}", status);

        return orderRepository.findByStatus(status).stream()
            .map(orderMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    public OrderResponse updateOrderStatus(Long id, OrderStatus newStatus) {
        log.info("Updating order {} to status: {}", id, newStatus);

        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + id));

        order.updateStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        log.info("Order {} status updated to {}", id, newStatus);

        return orderMapper.toResponse(updatedOrder);
    }

    @Override
    public void cancelOrder(Long id) {
        log.info("Cancelling order with ID: {}", id);

        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + id));

        if (!order.canBeCancelled()) {
            throw new OrderCancellationException(
                "Order with ID " + id + " cannot be cancelled. Current status: " + order.getStatus()
            );
        }

        orderRepository.delete(order);

        log.info("Order {} cancelled successfully", id);
    }

    @Override
    public void processScheduledStatusUpdate() {
        log.info("Starting scheduled status update: PENDING → PROCESSING");

        List<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING);

        log.info("Found {} pending orders to process", pendingOrders.size());

        pendingOrders.forEach(order -> {
            order.updateStatus(OrderStatus.PROCESSING);
            orderRepository.save(order);
            log.debug("Updated order {} from PENDING to PROCESSING", order.getOrderNumber());
        });

        log.info("Scheduled status update completed. {} orders updated", pendingOrders.size());
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" +
               ThreadLocalRandom.current().nextInt(1000, 9999);
    }
}
```

### 3.4 Scheduler Class

#### OrderStatusScheduler.java
```java
@Component
@Slf4j
public class OrderStatusScheduler {

    private final OrderService orderService;

    @Autowired
    public OrderStatusScheduler(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Scheduled task to update PENDING orders to PROCESSING
     * Runs every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes in milliseconds
    public void updatePendingOrdersToProcessing() {
        log.info("Executing scheduled task: updatePendingOrdersToProcessing");

        try {
            orderService.processScheduledStatusUpdate();
        } catch (Exception e) {
            log.error("Error during scheduled status update", e);
        }
    }
}
```

### 3.5 Mapper Class

#### OrderMapper.java
```java
@Component
public class OrderMapper {

    public Order toEntity(CreateOrderRequest request) {
        Order order = Order.builder()
            .customerName(request.getCustomerName())
            .customerEmail(request.getCustomerEmail())
            .status(OrderStatus.PENDING)
            .orderItems(new ArrayList<>())
            .build();

        request.getItems().forEach(itemRequest -> {
            OrderItem item = OrderItem.builder()
                .productName(itemRequest.getProductName())
                .productCode(itemRequest.getProductCode())
                .quantity(itemRequest.getQuantity())
                .unitPrice(itemRequest.getUnitPrice())
                .build();

            order.addOrderItem(item);
        });

        return order;
    }

    public OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
            .id(order.getId())
            .orderNumber(order.getOrderNumber())
            .customerName(order.getCustomerName())
            .customerEmail(order.getCustomerEmail())
            .totalAmount(order.getTotalAmount())
            .status(order.getStatus())
            .items(toItemResponses(order.getOrderItems()))
            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            .build();
    }

    private List<OrderItemResponse> toItemResponses(List<OrderItem> items) {
        return items.stream()
            .map(this::toItemResponse)
            .collect(Collectors.toList());
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
            .id(item.getId())
            .productName(item.getProductName())
            .productCode(item.getProductCode())
            .quantity(item.getQuantity())
            .unitPrice(item.getUnitPrice())
            .totalPrice(item.getTotalPrice())
            .build();
    }
}
```

---

## 4. API Specifications

### 4.1 Create Order

**Endpoint:** `POST /api/v1/orders`

**Request Body:**
```json
{
  "customerName": "John Doe",
  "customerEmail": "john.doe@example.com",
  "items": [
    {
      "productName": "Laptop",
      "productCode": "LAPTOP-001",
      "quantity": 1,
      "unitPrice": 1200.00
    },
    {
      "productName": "Mouse",
      "productCode": "MOUSE-001",
      "quantity": 2,
      "unitPrice": 25.00
    }
  ]
}
```

**Success Response (201 Created):**
```json
{
  "id": 1,
  "orderNumber": "ORD-1729766400000-5678",
  "customerName": "John Doe",
  "customerEmail": "john.doe@example.com",
  "totalAmount": 1250.00,
  "status": "PENDING",
  "items": [
    {
      "id": 1,
      "productName": "Laptop",
      "productCode": "LAPTOP-001",
      "quantity": 1,
      "unitPrice": 1200.00,
      "totalPrice": 1200.00
    },
    {
      "id": 2,
      "productName": "Mouse",
      "productCode": "MOUSE-001",
      "quantity": 2,
      "unitPrice": 25.00,
      "totalPrice": 50.00
    }
  ],
  "createdAt": "2025-10-24T10:30:00",
  "updatedAt": "2025-10-24T10:30:00"
}
```

**Error Response (400 Bad Request):**
```json
{
  "timestamp": "2025-10-24T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/orders",
  "fieldErrors": [
    {
      "field": "customerEmail",
      "message": "Invalid email format",
      "rejectedValue": "invalid-email"
    }
  ]
}
```

### 4.2 Get Order by ID

**Endpoint:** `GET /api/v1/orders/{id}`

**Success Response (200 OK):**
```json
{
  "id": 1,
  "orderNumber": "ORD-1729766400000-5678",
  "customerName": "John Doe",
  "customerEmail": "john.doe@example.com",
  "totalAmount": 1250.00,
  "status": "PROCESSING",
  "items": [
    {
      "id": 1,
      "productName": "Laptop",
      "productCode": "LAPTOP-001",
      "quantity": 1,
      "unitPrice": 1200.00,
      "totalPrice": 1200.00
    }
  ],
  "createdAt": "2025-10-24T10:30:00",
  "updatedAt": "2025-10-24T10:35:00"
}
```

**Error Response (404 Not Found):**
```json
{
  "timestamp": "2025-10-24T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Order not found with ID: 999",
  "path": "/api/v1/orders/999"
}
```

### 4.3 List All Orders

**Endpoint:** `GET /api/v1/orders`

**Success Response (200 OK):**
```json
[
  {
    "id": 1,
    "orderNumber": "ORD-1729766400000-5678",
    "customerName": "John Doe",
    "customerEmail": "john.doe@example.com",
    "totalAmount": 1250.00,
    "status": "PROCESSING",
    "items": [...],
    "createdAt": "2025-10-24T10:30:00",
    "updatedAt": "2025-10-24T10:35:00"
  },
  {
    "id": 2,
    "orderNumber": "ORD-1729766460000-1234",
    "customerName": "Jane Smith",
    "customerEmail": "jane.smith@example.com",
    "totalAmount": 599.99,
    "status": "PENDING",
    "items": [...],
    "createdAt": "2025-10-24T10:31:00",
    "updatedAt": "2025-10-24T10:31:00"
  }
]
```

### 4.4 List Orders by Status

**Endpoint:** `GET /api/v1/orders?status=PENDING`

**Success Response (200 OK):**
```json
[
  {
    "id": 2,
    "orderNumber": "ORD-1729766460000-1234",
    "customerName": "Jane Smith",
    "customerEmail": "jane.smith@example.com",
    "totalAmount": 599.99,
    "status": "PENDING",
    "items": [...],
    "createdAt": "2025-10-24T10:31:00",
    "updatedAt": "2025-10-24T10:31:00"
  }
]
```

### 4.5 Update Order Status

**Endpoint:** `PUT /api/v1/orders/{id}/status`

**Request Body:**
```json
{
  "status": "SHIPPED"
}
```

**Success Response (200 OK):**
```json
{
  "id": 1,
  "orderNumber": "ORD-1729766400000-5678",
  "customerName": "John Doe",
  "customerEmail": "john.doe@example.com",
  "totalAmount": 1250.00,
  "status": "SHIPPED",
  "items": [...],
  "createdAt": "2025-10-24T10:30:00",
  "updatedAt": "2025-10-24T10:45:00"
}
```

**Error Response (400 Bad Request):**
```json
{
  "timestamp": "2025-10-24T10:45:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Cannot transition from PENDING to SHIPPED",
  "path": "/api/v1/orders/1/status"
}
```

### 4.6 Cancel Order

**Endpoint:** `DELETE /api/v1/orders/{id}`

**Success Response (204 No Content)**

**Error Response (400 Bad Request):**
```json
{
  "timestamp": "2025-10-24T10:50:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Order with ID 1 cannot be cancelled. Current status: PROCESSING",
  "path": "/api/v1/orders/1"
}
```

---

## 5. Sequence Diagrams

### 5.1 Create Order Flow

```
Client          Controller      Service         Validator       Mapper         Repository      Database
  |                 |              |                |              |               |               |
  |--POST /orders-->|              |                |              |               |               |
  |                 |--createOrder()-->              |              |               |               |
  |                 |              |--validate()-->  |              |               |               |
  |                 |              |<--valid---------|              |               |               |
  |                 |              |--toEntity()--------------------->             |               |
  |                 |              |<--Order Entity------------------|              |               |
  |                 |              |--generateOrderNumber()          |              |               |
  |                 |              |--calculateTotal()               |              |               |
  |                 |              |--save(Order)------------------------------------>             |
  |                 |              |                                                 |--INSERT----->|
  |                 |              |<--Saved Order-----------------------------------|<--OK---------|
  |                 |              |--toResponse()------------------------->         |               |
  |                 |<--OrderResponse--|<--DTO--------------------------|            |               |
  |<--201 Created---|              |                |              |               |               |
```

### 5.2 Scheduled Status Update Flow

```
Scheduler       Service         Repository      Database
  |               |                 |               |
  |--@Scheduled-->|                 |               |
  |               |--findByStatus(PENDING)--------->|
  |               |                 |--SELECT----->|
  |               |<--List<Order>---|<--Results----|
  |               |                 |               |
  |               |--forEach(order)                 |
  |               |  |--updateStatus(PROCESSING)    |
  |               |  |--save(order)---------------->|
  |               |  |              |--UPDATE----->|
  |               |  |<--Updated----|<--OK---------|
  |               |                 |               |
  |<--Complete----|                 |               |
```

### 5.3 Cancel Order Flow

```
Client          Controller      Service         Repository      Database
  |                 |              |                 |               |
  |--DELETE /orders/{id}-->        |                 |               |
  |                 |--cancelOrder(id)-->            |               |
  |                 |              |--findById(id)------------------>|
  |                 |              |                 |--SELECT----->|
  |                 |              |<--Order---------|<--Result-----|
  |                 |              |--canBeCancelled()?              |
  |                 |              |<--true/false--                  |
  |                 |              |--delete(Order)----------------->|
  |                 |              |                 |--DELETE----->|
  |                 |<--void-------|                 |<--OK---------|
  |<--204 No Content|              |                 |               |
```

---

## 6. Database Schema Details

### 6.1 Orders Table

```sql
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    customer_name VARCHAR(100) NOT NULL,
    customer_email VARCHAR(100) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL CHECK (total_amount >= 0),
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'PROCESSING', 'SHIPPED', 'DELIVERED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version INT NOT NULL DEFAULT 0,
    INDEX idx_order_number (order_number),
    INDEX idx_status (status),
    INDEX idx_customer_email (customer_email),
    INDEX idx_created_at (created_at)
);
```

### 6.2 Order Items Table

```sql
CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    product_code VARCHAR(50) NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10, 2) NOT NULL CHECK (unit_price > 0),
    total_price DECIMAL(10, 2) NOT NULL CHECK (total_price > 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    INDEX idx_order_id (order_id),
    INDEX idx_product_code (product_code)
);
```

---

## 7. Design Pattern Implementations

### 7.1 Builder Pattern (Lombok @Builder)
```java
Order order = Order.builder()
    .orderNumber("ORD-123")
    .customerName("John Doe")
    .customerEmail("john@example.com")
    .status(OrderStatus.PENDING)
    .build();
```

### 7.2 Strategy Pattern (Validation)
```java
public interface OrderValidationStrategy {
    void validate(CreateOrderRequest request);
}

@Component
public class CustomerValidationStrategy implements OrderValidationStrategy {
    @Override
    public void validate(CreateOrderRequest request) {
        // Validate customer information
    }
}

@Component
public class ItemsValidationStrategy implements OrderValidationStrategy {
    @Override
    public void validate(CreateOrderRequest request) {
        // Validate order items
    }
}

@Component
public class OrderValidator {
    private final List<OrderValidationStrategy> strategies;

    public void validateCreateOrderRequest(CreateOrderRequest request) {
        strategies.forEach(strategy -> strategy.validate(request));
    }
}
```

### 7.3 Facade Pattern (OrderService)
```java
// OrderService acts as a facade, hiding complex subsystem interactions
@Service
public class OrderServiceImpl implements OrderService {
    // Coordinates multiple repositories, validators, and mappers
    // Client only needs to call simple methods like createOrder()
}
```

### 7.4 Template Method Pattern (Status Transition)
```java
public abstract class OrderStatusUpdater {
    public final void updateStatus(Order order, OrderStatus newStatus) {
        validateTransition(order, newStatus);
        performPreUpdateActions(order);
        order.setStatus(newStatus);
        performPostUpdateActions(order);
    }

    protected abstract void validateTransition(Order order, OrderStatus newStatus);
    protected abstract void performPreUpdateActions(Order order);
    protected abstract void performPostUpdateActions(Order order);
}
```

---

## 8. Validation Rules

### 8.1 Order Creation Validations
| Field | Rules |
|-------|-------|
| customerName | Required, 2-100 characters |
| customerEmail | Required, valid email format |
| items | Required, at least 1 item |
| item.productName | Required, 1-200 characters |
| item.productCode | Required, 1-50 characters |
| item.quantity | Required, >= 1, <= 10000 |
| item.unitPrice | Required, > 0, <= 1000000 |

### 8.2 Status Transition Validations
| Current Status | Allowed Next Status |
|----------------|---------------------|
| PENDING | PROCESSING |
| PROCESSING | SHIPPED |
| SHIPPED | DELIVERED |
| DELIVERED | None (final state) |

### 8.3 Cancellation Validations
- Order must exist
- Order status must be PENDING
- Cannot cancel orders in PROCESSING, SHIPPED, or DELIVERED status

---

## Summary

This LLD document provides detailed specifications for implementing the E-commerce Order Processing System with:
- Complete class structures with field-level details
- Comprehensive API specifications with request/response examples
- Sequence diagrams for understanding flow
- Database schema with constraints and indexes
- Design pattern implementations
- Validation rules and business logic

This design ensures adherence to SOLID principles, clean code practices, and extensive use of design patterns for maintainability and scalability.
