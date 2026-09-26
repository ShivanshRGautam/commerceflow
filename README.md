# 🛒 CommerceFlow - Advanced E-commerce Backend

CommerceFlow is a secure, fully functional REST API built for a modern e-commerce platform. It handles everything a real-world online store needs behind the scenes: managing products, handling user accounts securely, processing shopping carts, and safely checking out orders without accidentally overselling out-of-stock items.

This project was built to demonstrate clean architecture, robust security, and production-ready coding practices using Java and Spring Boot.

## ✨ Key Features

* **Secure User Authentication:** Users can register and log in securely. The system uses JSON Web Tokens (JWT) so users stay logged in without the server needing to remember their session.
* **Role-Based Access Control:** 
  * **Customers** can browse products, manage their personal shopping carts, and place orders.
  * **Admins** have exclusive rights to add/remove products, update inventory, and change order statuses (e.g., shipping an order).
* **Safe, Transactional Checkout:** When a user checks out, the system calculates the total, creates an order, and reduces the inventory all in one single, safe step. If anything goes wrong (like a database error), the entire checkout is canceled so no data is corrupted.
* **Prevents Overselling (Concurrency):** If two customers try to buy the very last item in stock at the exact same millisecond, the system's "optimistic locking" ensures only one person gets it, and the other gets a polite "out of stock" message.
* **Interactive API Documentation:** Includes a beautifully generated Swagger UI web page where anyone can view and test the API endpoints directly in their browser.

## 🛠️ Technology Stack

* **Language:** Java 21
* **Framework:** Spring Boot 4.1.x (Web, Data JPA, Security, Validation)
* **Database:** MySQL 8.0
* **Security:** Spring Security with Nimbus OAuth2 JWT & BCrypt Password Encoding
* **Testing:** JUnit 5 & Mockito
* **Deployment/Containerization:** Docker & Docker Compose
* **Documentation:** SpringDoc OpenAPI (Swagger)

## 🚀 How to Run the Project Locally

You can run this project either using Docker (easiest) or manually via Maven.

### Prerequisites
* Java 21 installed
* MySQL running (if not using Docker)
* Git

### Step 1: Clone the Repository
```bash
git clone [https://github.com/your-username/commerceflow.git](https://github.com/your-username/commerceflow.git)
cd commerceflow
