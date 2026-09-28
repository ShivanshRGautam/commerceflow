# CommerceFlow

A backend e-commerce application built using **Java, Spring Boot, Spring Security, JPA/Hibernate and MySQL**.

CommerceFlow provides REST APIs for managing products, categories, inventory, customers, carts and orders, along with a secure checkout workflow.

## Features

* User registration and login
* JWT-based authentication
* Role-based authorization
* Category management
* Product CRUD operations
* Product pagination
* Product soft deletion
* Inventory management
* Inventory restocking
* Optimistic locking for inventory concurrency
* Shopping cart management
* Order creation
* Order item management
* Checkout transaction
* Automatic inventory deduction during checkout
* Request validation
* Global exception handling
* Standard HTTP status codes
* Swagger/OpenAPI documentation
* JUnit and Mockito unit testing
* Docker containerization
* Git/GitHub version control

---

## Tech Stack

| Technology        | Purpose                        |
| ----------------- | ------------------------------ |
| Java 21           | Programming language           |
| Spring Boot       | Backend framework              |
| Spring Security   | Authentication & authorization |
| JWT               | Token-based authentication     |
| Spring Data JPA   | Data access                    |
| Hibernate         | ORM                            |
| MySQL             | Relational database            |
| Maven             | Build & dependency management  |
| JUnit 5           | Unit testing                   |
| Mockito           | Mocking dependencies           |
| Swagger / OpenAPI | API documentation              |
| Docker            | Containerization               |
| Git / GitHub      | Version control                |

---

## Architecture

The application follows a layered architecture:

```text
Client
   |
   v
Controller Layer
   |
   v
Service Layer
   |
   v
Repository Layer
   |
   v
MySQL Database
```

### Controller

Responsible for:

* Receiving HTTP requests
* Validating request data
* Calling service methods
* Returning HTTP responses

### Service

Contains the application's business logic.

Examples:

* Product creation
* Cart operations
* Order processing
* Checkout
* Inventory validation
* Inventory updates

### Repository

Handles database interaction using Spring Data JPA.

---

## Main Modules

```text
CommerceFlow
│
├── Authentication
│   ├── Registration
│   ├── Login
│   └── JWT
│
├── Catalog
│   ├── Category
│   └── Product
│
├── Inventory
│   └── Product Stock
│
├── Cart
│   └── Cart Items
│
└── Order
    ├── Order
    ├── Order Items
    └── Checkout
```

---

## Authentication Flow

CommerceFlow uses Spring Security with JWT authentication.

```text
User
 |
 | Login
 v
Authentication API
 |
 | Credentials validated
 v
JWT Generated
 |
 | Authorization: Bearer <token>
 v
Protected APIs
```

The JWT contains authentication information and user authorities.

Protected endpoints use the JWT to determine whether the user is authenticated and authorized to perform the requested operation.

---

## Order & Checkout Flow

The main e-commerce flow is:

```text
Customer Login
      |
      v
Add Product to Cart
      |
      v
View Cart
      |
      v
Checkout
      |
      v
Validate Cart
      |
      v
Validate Inventory
      |
      v
Create Order
      |
      v
Create Order Items
      |
      v
Decrease Inventory
      |
      v
Clear Cart
      |
      v
Transaction Commit
```

The checkout operation is handled using transaction management so that related database operations are treated as a single unit of work.

---

## Transaction Management

Checkout involves multiple database operations:

1. Validate inventory
2. Create order
3. Create order items
4. Update inventory
5. Clear cart

These operations are handled within a transaction.

If an appropriate failure occurs during the transaction, the changes can be rolled back instead of leaving the database in an inconsistent state.

---

## Inventory Concurrency

The inventory module uses JPA optimistic locking with a version field.

Example:

```java
@Version
private Long version;
```

The version value changes when the inventory entity is successfully updated.

This helps detect concurrent updates when multiple transactions try to modify the same inventory record.

Example:

```text
Transaction A
    |
Reads version = 1

Transaction B
    |
Reads version = 1

Transaction A
    |
Updates successfully
version = 2

Transaction B
    |
Attempts update with version = 1
    |
Update rejected because version changed
```

This helps protect inventory from silent concurrent overwrites.

---

## Database Relationships

The application contains relationships between major entities.

Examples:

```text
Category
   |
   └── Products

User
   |
   ├── Cart
   |
   └── Orders

Cart
   |
   └── Cart Items

Order
   |
   └── Order Items

Product
   |
   └── Inventory
```

---

## API Documentation

Swagger/OpenAPI is used to document the REST APIs.

After starting the application, Swagger UI can be accessed through the configured Swagger endpoint.

Swagger provides:

* Available endpoints
* HTTP methods
* Request parameters
* Request bodies
* Response information
* API testing from the browser

---

## Testing

Unit testing is implemented using:

* JUnit 5
* Mockito

Mockito is used to mock dependencies such as repositories so that service-layer business logic can be tested independently.

Example testing flow:

```text
JUnit Test
    |
    v
Service
    |
    v
Mock Repository
```

Important scenarios include:

* Successful operations
* Invalid input
* Missing resources
* Duplicate products
* Business validation failures
* Inventory-related scenarios

---

## Docker

The application can be containerized using Docker.

Docker helps provide a consistent runtime environment and makes the application easier to deploy.

Typical architecture:

```text
Docker
│
├── CommerceFlow Application
│
└── MySQL Database
```

---

## Getting Started

### Prerequisites

Make sure the following are installed:

* Java 21
* Maven
* MySQL
* Docker (optional)

---

### 1. Clone the Repository

```bash
git clone <YOUR_GITHUB_REPOSITORY_URL>
```

```bash
cd commerceflow
```

---

### 2. Configure Database

Create a MySQL database:

```sql
CREATE DATABASE commerceflow_db;
```

Configure the database connection in your application configuration.

Example:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/commerceflow_db
spring.datasource.username=root
spring.datasource.password=${DB_PASSWORD}
```

The database password can be supplied using the `DB_PASSWORD` environment variable.

---

### 3. Run the Application

Using Maven:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

The application will start on the configured Spring Boot port.

---

## Example API Flow

### Register

```http
POST /api/auth/register
```

### Login

```http
POST /api/auth/login
```

The login response provides a JWT token.

Use the token for protected endpoints:

```http
Authorization: Bearer <JWT_TOKEN>
```

### Product

```http
POST   /api/products
GET    /api/products
GET    /api/products/{id}
PUT    /api/products/{id}
DELETE /api/products/{id}
```

### Inventory

```http
POST /api/inventory/products/{productId}
GET  /api/inventory/products/{productId}
```

### Cart

```http
POST   /api/cart
GET    /api/cart
PUT    /api/cart/items/{itemId}
DELETE /api/cart/items/{itemId}
```

### Orders / Checkout

```http
POST /api/orders/checkout
GET  /api/orders
GET  /api/orders/{id}
```

> Exact endpoint paths may vary depending on the current controller mappings.

---

## HTTP Status Codes

The API uses standard HTTP status codes.

| Status | Meaning                         |
| ------ | ------------------------------- |
| 200    | Successful request              |
| 201    | Resource created                |
| 400    | Invalid request                 |
| 401    | Authentication required/invalid |
| 403    | Access forbidden                |
| 404    | Resource not found              |
| 409    | Resource conflict               |
| 500    | Internal server error           |

---

## Error Handling

The application uses centralized exception handling to provide consistent API error responses.

Examples include:

* Resource not found
* Duplicate product/SKU
* Invalid request data
* Insufficient inventory
* Unauthorized access
* Forbidden operations

---

## Project Structure

A simplified project structure:

```text
src
└── main
    └── java
        └── com.shivansh.commerceflow
            │
            ├── auth
            │   └── auth.dto
            │
            ├── catalog
            │   ├── category
            │   └── product
            │
            ├── inventory
            │
            ├── cart
            │
            ├── order
            │
            └── common
                ├── response
                └── security
```

---

## Future Improvements

Possible future improvements include:

* Payment gateway integration
* Email notifications
* Redis caching
* Refresh token implementation
* Integration testing
* Testcontainers
* Rate limiting
* Event-driven order processing
* Message broker such as Kafka/RabbitMQ
* API Gateway
* Microservice decomposition
* Monitoring and centralized logging

---

## Learning Outcomes

Through this project, I gained practical experience with:

* Building REST APIs using Spring Boot
* Layered backend architecture
* Spring Security and JWT
* Role-based authorization
* JPA/Hibernate relationships
* Database transactions
* Optimistic locking
* Inventory management
* DTO-based API design
* Validation and exception handling
* Unit testing with JUnit and Mockito
* API documentation with Swagger/OpenAPI
* Docker containerization
* Git and GitHub

---

## Author

**Shivansh Gautam**

Java | Spring Boot | REST APIs | MySQL | Spring Security | Apigee | Docker | Git
