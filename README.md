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
* Transactional checkout
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

| Technology        | Purpose                           |
| ----------------- | --------------------------------- |
| Java 21           | Programming language              |
| Spring Boot       | Backend framework                 |
| Spring Security   | Authentication & authorization    |
| JWT               | Token-based authentication        |
| Spring Data JPA   | Data access                       |
| Hibernate         | ORM                               |
| MySQL             | Relational database               |
| Maven             | Build & dependency management     |
| JUnit 5           | Unit testing                      |
| Mockito           | Mocking dependencies              |
| Swagger / OpenAPI | API documentation                 |
| Docker            | Containerization                  |
| Docker Compose    | Multi-container application setup |
| Git / GitHub      | Version control                   |

---

## Architecture

CommerceFlow follows a **Package-by-Feature Modular Monolith** architecture.

The application is organized primarily around business features such as authentication, catalog, inventory, cart and orders rather than having one large package for all controllers, services and repositories.

Within each feature, the application follows a layered structure where appropriate:

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

This approach keeps related business functionality together while maintaining separation between API handling, business logic and data access.

### Why a Modular Monolith?

The project was intentionally developed as a modular monolith rather than immediately splitting everything into microservices.

This makes the system easier to:

* Develop
* Test
* Debug
* Deploy
* Understand

The feature boundaries also provide a foundation for extracting selected modules into microservices later if scalability or business requirements justify it.

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

CommerceFlow is containerized using Docker and Docker Compose.

The Docker setup uses a multi-stage build to create the application image and Docker Compose to run the application along with its database dependency.

Typical architecture:

```text
Docker Compose
│
├── CommerceFlow Application
│
└── MySQL Database
```

This makes it possible to start the complete application environment without manually installing and configuring the application runtime.

---

## Getting Started

### Prerequisites

For local development, make sure the following are installed:

* Java 21
* Maven
* MySQL
* Docker Desktop / Docker Engine

---

### Option 1 — Run with Docker Compose

This is the easiest way to start the complete application environment.

Clone the repository:

```bash
git clone <YOUR_GITHUB_REPOSITORY_URL>
```

```bash
cd commerceflow
```

Then build and start the containers:

```bash
docker compose up --build -d
```

To check running containers:

```bash
docker compose ps
```

To view application logs:

```bash
docker compose logs -f
```

To stop the application:

```bash
docker compose down
```

Docker Compose starts the CommerceFlow application together with its MySQL database according to the project's Compose configuration.

---

### Option 2 — Run Locally with Maven

If you prefer running the Spring Boot application directly:

#### 1. Clone the Repository

```bash
git clone <YOUR_GITHUB_REPOSITORY_URL>
```

```bash
cd commerceflow
```

#### 2. Configure Database

Create a MySQL database:

```sql
CREATE DATABASE commerceflow_db;
```

Configure the required environment variables.

```text
DB_PASSWORD=<your_mysql_password>
JWT_SECRET=<your_jwt_secret>
```

The application uses these environment variables for the database password and JWT signing secret.

#### 3. Run the Application

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

## Environment Variables

CommerceFlow requires the following environment variables when running outside the Docker Compose configuration:

| Variable      | Purpose                                |
| ------------- | -------------------------------------- |
| `DB_PASSWORD` | MySQL database password                |
| `JWT_SECRET`  | Secret used for JWT signing/validation |

Example:

```text
DB_PASSWORD=your_database_password
JWT_SECRET=your_secure_jwt_secret
```

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

The project is organized by business feature, with common functionality such as security and response handling kept under the `common` package.

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
* Package-by-feature modular monolith architecture
* Layered application design
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
* Docker and Docker Compose
* Git and GitHub

---

## Author

**Shivansh Gautam**

Java | Spring Boot | REST APIs | MySQL | Spring Security | Apigee | Docker | Git
