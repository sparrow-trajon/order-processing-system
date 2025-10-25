# High Level Design (HLD) - E-commerce Order Processing System

## 1. System Overview

The E-commerce Order Processing System is a backend service that manages the complete order lifecycle from creation to delivery. It provides RESTful APIs for order management and includes automated order status processing.

### Key Features
- Order creation with multiple items
- Order status tracking and management
- Automated order status progression
- Order cancellation (with business rules)
- Comprehensive order listing and filtering

---

## 2. System Architecture

### 2.1 Layered Architecture (Clean Architecture)

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │  REST APIs   │  │   Swagger    │  │   Exception  │  │
│  │ (Controllers)│  │     UI       │  │   Handlers   │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
                           │
┌─────────────────────────────────────────────────────────┐
│                     Service Layer                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │   Order      │  │   Validator  │  │   Scheduler  │  │
│  │   Service    │  │   Service    │  │   Service    │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
                           │
┌─────────────────────────────────────────────────────────┐
│                  Repository Layer                        │
│  ┌──────────────┐  ┌──────────────┐                     │
│  │   Order      │  │  OrderItem   │                     │
│  │  Repository  │  │  Repository  │                     │
│  └──────────────┘  └──────────────┘                     │
└─────────────────────────────────────────────────────────┘
                           │
┌─────────────────────────────────────────────────────────┐
│                    Persistence Layer                     │
│              JPA/Hibernate + H2 Database                 │
└─────────────────────────────────────────────────────────┘
```

### 2.2 Component Breakdown

#### **A. Presentation Layer**
- **Controllers**: Handle HTTP requests and responses
- **DTOs**: Data Transfer Objects for API contracts
- **Exception Handlers**: Global exception handling using @ControllerAdvice
- **Swagger Config**: API documentation configuration

#### **B. Service Layer**
- **OrderService**: Core business logic for order operations
- **OrderValidator**: Validation logic (Strategy Pattern)
- **OrderStatusScheduler**: Background job for status updates
- **Mapper Services**: DTO ↔ Entity conversion

#### **C. Repository Layer**
- **OrderRepository**: Data access for orders
- **OrderItemRepository**: Data access for order items
- Uses Spring Data JPA for CRUD operations

#### **D. Domain Layer**
- **Entities**: Order, OrderItem
- **Enums**: OrderStatus
- **Value Objects**: Money, Quantity (if needed)

---

## 3. Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Language | Java | 21 |
| Framework | Spring Boot | 3.2.0 |
| ORM | Spring Data JPA + Hibernate | - |
| Database | H2 (in-memory) | - |
| API Documentation | Springdoc OpenAPI (Swagger) | 2.x |
| Build Tool | Maven | - |
| Validation | Jakarta Validation API | - |
| Testing | JUnit 5, Mockito, Spring Test | - |
| Scheduler | Spring @Scheduled | - |

---

## 4. Design Patterns to be Used

### 4.1 **Singleton Pattern**
- **Usage**: Spring Beans (default scope)
- **Components**: Services, Repositories, Controllers

### 4.2 **Factory Pattern**
- **Usage**: Order creation with different item types
- **Component**: OrderFactory (if complex creation logic needed)

### 4.3 **Strategy Pattern**
- **Usage**: Order validation strategies
- **Component**: OrderValidationStrategy interface with implementations

### 4.4 **Repository Pattern**
- **Usage**: Data access abstraction
- **Component**: Spring Data JPA repositories

### 4.5 **Builder Pattern**
- **Usage**: Creating complex objects (Order, OrderItem)
- **Implementation**: Using Lombok @Builder

### 4.6 **Facade Pattern**
- **Usage**: OrderService as a facade to multiple operations
- **Component**: OrderServiceImpl

### 4.7 **Observer Pattern**
- **Usage**: Order status change notifications (future enhancement)
- **Component**: Event listeners (Spring Events)

### 4.8 **Template Method Pattern**
- **Usage**: Order processing workflow
- **Component**: Abstract order processor

### 4.9 **DTO Pattern**
- **Usage**: Data transfer between layers
- **Component**: Request/Response DTOs

### 4.10 **Dependency Injection**
- **Usage**: Throughout application
- **Implementation**: Spring IoC Container

---

## 5. SOLID Principles Application

### 5.1 **Single Responsibility Principle (SRP)**
- Each class has one reason to change
- Controllers: Handle HTTP only
- Services: Business logic only
- Repositories: Data access only
- DTOs: Data transfer only

### 5.2 **Open/Closed Principle (OCP)**
- Strategy pattern for validators (open for extension, closed for modification)
- Plugin architecture for order processors

### 5.3 **Liskov Substitution Principle (LSP)**
- Interface-based programming
- Implementations can be substituted without breaking functionality

### 5.4 **Interface Segregation Principle (ISP)**
- Small, focused interfaces
- OrderService, OrderValidator, OrderRepository interfaces

### 5.5 **Dependency Inversion Principle (DIP)**
- Depend on abstractions, not concrete implementations
- All dependencies injected via interfaces

---

## 6. API Endpoints Overview

### 6.1 Order Management APIs

| Method | Endpoint | Description | Status Codes |
|--------|----------|-------------|--------------|
| POST | `/api/v1/orders` | Create new order | 201, 400, 500 |
| GET | `/api/v1/orders/{id}` | Get order by ID | 200, 404, 500 |
| GET | `/api/v1/orders` | List all orders | 200, 500 |
| GET | `/api/v1/orders?status={status}` | List orders by status | 200, 400, 500 |
| PUT | `/api/v1/orders/{id}/status` | Update order status | 200, 400, 404, 500 |
| DELETE | `/api/v1/orders/{id}` | Cancel order | 200, 400, 404, 500 |

### 6.2 Health Check APIs

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/actuator/health` | Health check |
| GET | `/actuator/info` | Application info |

---

## 7. Data Flow

### 7.1 Create Order Flow
```
Client → Controller → Validator → Service → Repository → Database
   ↓
Response ← DTO Mapper ← Entity
```

### 7.2 Update Order Status Flow (Scheduled)
```
Scheduler (every 5 min) → Service → Repository → Query PENDING Orders
   ↓
Update to PROCESSING → Save → Database
```

### 7.3 Cancel Order Flow
```
Client → Controller → Service → Validate (PENDING?) → Repository → Database
   ↓
Response ← Success/Error
```

---

## 8. Database Schema Overview

### 8.1 Orders Table
```
orders
├── id (PK, AUTO_INCREMENT)
├── order_number (UNIQUE, NOT NULL)
├── customer_name (NOT NULL)
├── customer_email (NOT NULL)
├── total_amount (NOT NULL)
├── status (ENUM: PENDING, PROCESSING, SHIPPED, DELIVERED)
├── created_at (TIMESTAMP)
├── updated_at (TIMESTAMP)
└── version (for optimistic locking)
```

### 8.2 Order Items Table
```
order_items
├── id (PK, AUTO_INCREMENT)
├── order_id (FK → orders.id)
├── product_name (NOT NULL)
├── product_code (NOT NULL)
├── quantity (NOT NULL, > 0)
├── unit_price (NOT NULL, > 0)
├── total_price (NOT NULL)
└── created_at (TIMESTAMP)
```

**Relationship**: One-to-Many (Order → OrderItems)

---

## 9. Scheduled Jobs

### 9.1 Order Status Updater
- **Frequency**: Every 5 minutes
- **Logic**:
  - Fetch all orders with status = PENDING
  - Update status to PROCESSING
  - Save to database
- **Configuration**: `@Scheduled(fixedRate = 300000)` or Cron expression

---

## 10. Error Handling Strategy

### 10.1 Exception Types
- `OrderNotFoundException`: When order ID not found
- `InvalidOrderStatusException`: When status transition is invalid
- `OrderCancellationException`: When cancellation rules violated
- `ValidationException`: When input validation fails

### 10.2 Global Exception Handler
- Uses `@ControllerAdvice` and `@ExceptionHandler`
- Returns standardized error response:
```json
{
  "timestamp": "2025-10-24T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Order not found with ID: 123",
  "path": "/api/v1/orders/123"
}
```

---

## 11. Validation Rules

### 11.1 Order Creation
- Customer name: Required, 2-100 characters
- Customer email: Required, valid email format
- Order items: At least 1 item required
- Item quantity: Must be > 0
- Item price: Must be > 0

### 11.2 Order Cancellation
- Order must exist
- Order status must be PENDING
- Cannot cancel already processed orders

### 11.3 Status Update
- Valid status transitions only
- Cannot move backwards in status flow

---

## 12. Non-Functional Requirements

### 12.1 Performance
- API response time: < 200ms (95th percentile)
- Scheduler execution: < 5 seconds for 1000 orders

### 12.2 Scalability
- Stateless service design
- Horizontal scaling support (future)

### 12.3 Maintainability
- Clean code principles
- Comprehensive unit and integration tests
- Code coverage > 80%

### 12.4 Documentation
- Swagger UI for API exploration
- Inline code documentation
- README with setup instructions

---

## 13. Future Enhancements

1. **Authentication & Authorization**: Add Spring Security
2. **Event-Driven Architecture**: Publish order events to message queue
3. **Notification Service**: Email/SMS notifications on status changes
4. **Payment Integration**: Payment processing before order confirmation
5. **Inventory Management**: Stock validation and reservation
6. **Order History**: Maintain audit trail of all status changes
7. **Pagination**: Add pagination for large result sets
8. **Caching**: Redis cache for frequently accessed orders
9. **Rate Limiting**: API rate limiting for abuse prevention
10. **Monitoring**: Prometheus/Grafana integration

---

## 14. Deployment Architecture (Future)

```
                    ┌─────────────┐
                    │  Load       │
                    │  Balancer   │
                    └──────┬──────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
   ┌────▼────┐       ┌────▼────┐       ┌────▼────┐
   │  App    │       │  App    │       │  App    │
   │Instance1│       │Instance2│       │Instance3│
   └────┬────┘       └────┬────┘       └────┬────┘
        │                  │                  │
        └──────────────────┼──────────────────┘
                           │
                    ┌──────▼──────┐
                    │  Database   │
                    │  (RDS/PostgreSQL) │
                    └─────────────┘
```

---

## 15. Security Considerations

1. **Input Validation**: All inputs validated using Jakarta Validation
2. **SQL Injection Prevention**: Using JPA parameterized queries
3. **CORS Configuration**: Restrict allowed origins
4. **Rate Limiting**: Prevent abuse (future)
5. **HTTPS**: Enforce HTTPS in production
6. **Sensitive Data**: No sensitive data in logs

---

## 16. Monitoring & Logging

### 16.1 Logging Strategy
- **INFO**: Business operations (order created, status changed)
- **WARN**: Invalid operations, validation failures
- **ERROR**: Exceptions, system errors
- **DEBUG**: Detailed flow (development only)

### 16.2 Metrics to Track
- Total orders created
- Orders by status
- API response times
- Error rates
- Scheduler execution time

---

## Summary

This HLD provides a comprehensive overview of the Order Processing System architecture, emphasizing clean code, SOLID principles, and design patterns. The system is designed to be maintainable, scalable, and follows industry best practices for RESTful API development.
