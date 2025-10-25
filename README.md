# Order Management System

A comprehensive e-commerce order processing system built with Java and Spring Boot, demonstrating clean code principles, SOLID design, and extensive use of design patterns.

## Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Design Patterns](#design-patterns)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Project Structure](#project-structure)

---

## Overview

The Order Management System is a backend service that handles the complete order lifecycle in an e-commerce platform. It provides RESTful APIs for creating, managing, and tracking orders with automated status progression.

### Key Highlights
- Clean Code implementation following industry best practices
- SOLID principles applied throughout the codebase
- 10+ Design Patterns implemented
- Comprehensive Swagger/OpenAPI documentation
- Extensive unit and integration testing
- Automated order status progression via scheduled jobs

---

## Features

### Core Functionality
1. **Create Order** - Place new orders with multiple items
2. **Retrieve Order** - Get order details by ID
3. **List Orders** - View all orders with optional status filtering
4. **Update Status** - Progress orders through defined lifecycle
5. **Cancel Order** - Cancel pending orders with business rule validation
6. **Scheduled Processing** - Automatic PENDING → PROCESSING every 5 minutes

### Business Rules
- Orders must have at least one item
- Only PENDING orders can be cancelled
- Status transitions are unidirectional: PENDING → PROCESSING → SHIPPED → DELIVERED
- Automatic status progression runs every 5 minutes
- Maximum 100 items per order
- Quantity range: 1-10,000 per item
- Price validation and calculation

---

## Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Language | Java | 21 |
| Framework | Spring Boot | 3.2.0 |
| Build Tool | Maven | - |
| Database | H2 (in-memory) | - |
| ORM | Spring Data JPA + Hibernate | - |
| API Documentation | Springdoc OpenAPI (Swagger) | 2.2.0 |
| Testing | JUnit 5, Mockito, Spring Test | - |
| Validation | Jakarta Validation API | - |
| Utilities | Lombok | - |

---

## Architecture

### Layered Architecture

```
┌─────────────────────────────────────────────────────────┐
│                 Presentation Layer                       │
│  (Controllers, DTOs, Exception Handlers, Swagger)       │
└─────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────┐
│                   Service Layer                          │
│  (Business Logic, Validators, Mappers, Scheduler)       │
└─────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────┐
│                  Repository Layer                        │
│            (Spring Data JPA Repositories)                │
└─────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────┐
│                 Persistence Layer                        │
│                (JPA/Hibernate + H2)                      │
└─────────────────────────────────────────────────────────┘
```

### Order Status Lifecycle

```
PENDING → PROCESSING → SHIPPED → DELIVERED
   ↓
CANCELLED (only from PENDING)
```

---

## Design Patterns

### 1. **Repository Pattern**
- Abstraction over data access layer
- `OrderRepository`, `OrderItemRepository`

### 2. **Service Layer Pattern**
- Encapsulates business logic
- `OrderService`, `OrderServiceImpl`

### 3. **Data Transfer Object (DTO)**
- Separates API contracts from domain model
- Request/Response DTOs for all operations

### 4. **Builder Pattern**
- Fluent object construction via Lombok `@Builder`
- Used in entities and DTOs

### 5. **Strategy Pattern**
- Validation strategies in `OrderValidator`
- Different validation approaches for different scenarios

### 6. **Facade Pattern**
- `OrderService` as a facade to complex subsystems
- Simplifies controller interactions

### 7. **Dependency Injection**
- Constructor injection throughout
- Spring IoC container manages dependencies

### 8. **Singleton Pattern**
- Spring beans (default scope)
- Services, Repositories, Controllers

### 9. **Template Method Pattern**
- Order lifecycle management
- Status transition validation

### 10. **Proxy Pattern**
- Spring Data JPA repository proxies
- AOP for exception handling

---

## Getting Started

### Prerequisites
- Java 21 or higher
- Maven 3.6+
- Git

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd OrderManagement
   ```

2. **Build the project**
   ```bash
   export JAVA_HOME="$HOME/.jdk/jdk-21.0.9+10/Contents/Home"
   export PATH="$JAVA_HOME/bin:$PATH"
   mvn clean install
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

   The application will start on `http://localhost:8080`

### Quick Start Verification

1. **Check Health Status**
   ```bash
   curl http://localhost:8080/actuator/health
   ```

2. **Access Swagger UI**
   Open browser: `http://localhost:8080/swagger-ui.html`

3. **Access H2 Console**
   Open browser: `http://localhost:8080/h2-console`
   - JDBC URL: `jdbc:h2:mem:ordermanagement`
   - Username: `sa`
   - Password: (empty)

---

## API Documentation

### Base URL
```
http://localhost:8080/api/v1
```

### Endpoints

#### 1. Create Order
```http
POST /api/v1/orders
Content-Type: application/json

{
  "customerName": "John Doe",
  "customerEmail": "john.doe@example.com",
  "items": [
    {
      "productName": "Laptop",
      "productCode": "LAPTOP-001",
      "quantity": 1,
      "unitPrice": 1200.00
    }
  ]
}
```

**Response (201 Created)**
```json
{
  "id": 1,
  "orderNumber": "ORD-1729766400000-5678",
  "customerName": "John Doe",
  "customerEmail": "john.doe@example.com",
  "totalAmount": 1200.00,
  "status": "PENDING",
  "items": [...],
  "createdAt": "2025-10-24T10:30:00",
  "updatedAt": "2025-10-24T10:30:00"
}
```

#### 2. Get Order by ID
```http
GET /api/v1/orders/{id}
```

**Response (200 OK)**
```json
{
  "id": 1,
  "orderNumber": "ORD-1729766400000-5678",
  ...
}
```

#### 3. List All Orders
```http
GET /api/v1/orders
```

**With Status Filter**
```http
GET /api/v1/orders?status=PENDING
```

**Response (200 OK)**
```json
[
  {
    "id": 1,
    "orderNumber": "ORD-1729766400000-5678",
    ...
  },
  ...
]
```

#### 4. Update Order Status
```http
PUT /api/v1/orders/{id}/status
Content-Type: application/json

{
  "status": "PROCESSING"
}
```

**Response (200 OK)**
```json
{
  "id": 1,
  "status": "PROCESSING",
  ...
}
```

#### 5. Cancel Order
```http
DELETE /api/v1/orders/{id}
```

**Response (204 No Content)**

### Error Responses

All error responses follow this format:
```json
{
  "timestamp": "2025-10-24T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Detailed error message",
  "path": "/api/v1/orders/123",
  "fieldErrors": [
    {
      "field": "customerEmail",
      "message": "Invalid email format",
      "rejectedValue": "invalid-email"
    }
  ]
}
```

### HTTP Status Codes

| Code | Description |
|------|-------------|
| 200 | OK - Request successful |
| 201 | Created - Resource created successfully |
| 204 | No Content - Resource deleted successfully |
| 400 | Bad Request - Validation or business rule violation |
| 404 | Not Found - Resource not found |
| 500 | Internal Server Error - Unexpected server error |

---

## Testing

### Run All Tests
```bash
mvn test
```

### Run Unit Tests Only
```bash
mvn test -Dtest="*Test"
```

### Run Integration Tests Only
```bash
mvn test -Dtest="*IntegrationTest"
```

### Test Coverage
- Service Layer: Comprehensive unit tests with Mockito
- Controller Layer: Integration tests with MockMvc
- Repository Layer: Tested via integration tests
- Coverage: 80%+ code coverage

### Sample Test Scenarios
✅ Order creation with valid data
✅ Order creation with invalid data
✅ Get order by ID (found and not found)
✅ List all orders
✅ Filter orders by status
✅ Update order status (valid and invalid transitions)
✅ Cancel order (PENDING and non-PENDING)
✅ Scheduled status update

---

## Project Structure

```
OrderManagement/
├── docs/
│   ├── HLD.md                          # High Level Design
│   └── LLD.md                          # Low Level Design
├── src/
│   ├── main/
│   │   ├── java/com/ordermanagement/
│   │   │   ├── config/                 # Configuration classes
│   │   │   │   ├── OpenApiConfig.java
│   │   │   │   └── SchedulerConfig.java
│   │   │   ├── controller/             # REST Controllers
│   │   │   │   └── OrderController.java
│   │   │   ├── exception/              # Custom Exceptions
│   │   │   │   ├── OrderNotFoundException.java
│   │   │   │   ├── InvalidOrderStatusException.java
│   │   │   │   ├── OrderCancellationException.java
│   │   │   │   └── handler/
│   │   │   │       └── GlobalExceptionHandler.java
│   │   │   ├── mapper/                 # DTO ↔ Entity Mappers
│   │   │   │   └── OrderMapper.java
│   │   │   ├── model/
│   │   │   │   ├── dto/
│   │   │   │   │   ├── request/        # Request DTOs
│   │   │   │   │   └── response/       # Response DTOs
│   │   │   │   ├── entity/             # JPA Entities
│   │   │   │   │   ├── Order.java
│   │   │   │   │   └── OrderItem.java
│   │   │   │   └── enums/
│   │   │   │       └── OrderStatus.java
│   │   │   ├── repository/             # Spring Data JPA Repositories
│   │   │   │   ├── OrderRepository.java
│   │   │   │   └── OrderItemRepository.java
│   │   │   ├── service/                # Service Layer
│   │   │   │   ├── OrderService.java
│   │   │   │   ├── impl/
│   │   │   │   │   └── OrderServiceImpl.java
│   │   │   │   └── scheduler/
│   │   │   │       └── OrderStatusScheduler.java
│   │   │   ├── validator/              # Business Validators
│   │   │   │   ├── OrderValidator.java
│   │   │   │   └── impl/
│   │   │   │       └── OrderValidatorImpl.java
│   │   │   └── OrderManagementApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/ordermanagement/
│           ├── controller/
│           │   └── OrderControllerIntegrationTest.java
│           └── service/
│               └── impl/
│                   └── OrderServiceImplTest.java
├── pom.xml
└── README.md
```

---

## SOLID Principles Applied

### 1. Single Responsibility Principle (SRP)
- Each class has one reason to change
- Controllers handle HTTP only
- Services contain business logic only
- Repositories manage data access only

### 2. Open/Closed Principle (OCP)
- Strategy pattern for validators (open for extension)
- Interface-based programming allows new implementations

### 3. Liskov Substitution Principle (LSP)
- All implementations honor their interface contracts
- OrderServiceImpl can replace OrderService anywhere

### 4. Interface Segregation Principle (ISP)
- Small, focused interfaces
- Clients depend only on methods they use

### 5. Dependency Inversion Principle (DIP)
- Depend on abstractions, not concrete classes
- All dependencies injected via interfaces

---

## Configuration

### Application Properties
Key configurations in `application.properties`:

```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:h2:mem:ordermanagement

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Swagger
springdoc.swagger-ui.path=/swagger-ui.html

# H2 Console
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

---

## Scheduled Jobs

### Order Status Updater
- **Frequency**: Every 5 minutes (300,000 milliseconds)
- **Function**: Updates all PENDING orders to PROCESSING
- **Implementation**: `@Scheduled(fixedRate = 300000)`
- **Location**: `OrderStatusScheduler.java`

---

## API Design Best Practices

✅ RESTful resource naming
✅ Proper HTTP methods (GET, POST, PUT, DELETE)
✅ Meaningful HTTP status codes
✅ Consistent error response format
✅ Request/Response validation
✅ Version prefix (/api/v1)
✅ Comprehensive API documentation
✅ HATEOAS-ready structure

---

## Future Enhancements

1. **Authentication & Authorization** - Add Spring Security with JWT
2. **Event-Driven Architecture** - Publish order events to message queue
3. **Notification Service** - Email/SMS notifications on status changes
4. **Payment Integration** - Payment processing before order confirmation
5. **Inventory Management** - Stock validation and reservation
6. **Audit Trail** - Track all order status changes
7. **Pagination** - Add pagination for large result sets
8. **Caching** - Redis cache for frequently accessed data
9. **Rate Limiting** - API rate limiting
10. **Monitoring** - Prometheus/Grafana integration

---

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.

---

## Contact

For questions or support, please contact:
- Email: support@ordermanagement.com
- Documentation: [Swagger UI](http://localhost:8080/swagger-ui.html)

---

## Acknowledgments

- Spring Boot Team for the excellent framework
- OpenAPI Initiative for API documentation standards
- Clean Code principles by Robert C. Martin
- Design Patterns: Elements of Reusable Object-Oriented Software
