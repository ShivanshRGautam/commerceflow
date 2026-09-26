# CommerceFlow - Advanced E-commerce Backend

CommerceFlow is a secure, highly modular REST API built with **Java 21** and **Spring Boot 3.x/4.x**. It is designed using a package-by-feature monolithic architecture to cleanly manage product catalogs, inventory, user authentication, cart operations, and transactional order checkouts.

## Core Features
* **Authentication & Authorization:** Stateless security using JWT (JSON Web Tokens) with distinct `CUSTOMER` and `ADMIN` roles.
* **Transactional Checkout:** Atomic order processing that verifies stock, calculates totals, creates order snapshots, and reduces inventory in a single database transaction.
* **Concurrency Handling:** Optimistic locking (`@Version`) prevents race conditions and overselling when multiple users purchase the same item simultaneously.
* **Admin Management:** Secure endpoints for administrators to manage inventory, catalog updates, and strictly validated order status transitions (e.g., `PENDING` -> `CONFIRMED`).
* **Clean Exception Handling:** Global REST controller advice translates complex Java exceptions (like validation errors and database conflicts) into clean, readable JSON responses.
* **Interactive API Docs:** Fully integrated Swagger UI / OpenAPI 3.0 documentation.

## Tech Stack
* **Language:** Java 21
* **Framework:** Spring Boot (Web, Data JPA, Validation, Security)
* **Database:** MySQL 8.0
* **Security:** Spring Security, Nimbus OAuth2 JWT
* **Testing:** JUnit 5, Mockito
* **Documentation:** SpringDoc OpenAPI (Swagger)

## Local Setup

### Prerequisites
* Java 21 installed
* MySQL running locally (or via Docker)

### Environment Variables
Configure these variables in your IDE or system environment before running:
```env
DB_USERNAME=root
DB_PASSWORD=your_mysql_password
JWT_SECRET=your_32_character_secret_key