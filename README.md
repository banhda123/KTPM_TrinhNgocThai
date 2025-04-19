# E-commerce Microservices System

This project implements a simple e-commerce system using microservices architecture with fault tolerance patterns.

## Services

1. Product Service
   - Manages product information (name, price, description, inventory)
   - Database: MariaDB
   - API endpoints: /api/products

2. Payment Service
   - Processes payment transactions (create, view, refund)
   - Handles payment status tracking
   - Database: MariaDB
   - API endpoints: /api/payments

3. Inventory Service
   - Manages product inventory and stock levels
   - Updates product quantities when orders are placed
   - Database: MariaDB
   - API endpoints: /api/inventory

4. Shipping Service
   - Manages shipment creation and tracking
   - Updates shipment status (processing, shipped, delivered)
   - Database: MariaDB
   - API endpoints: /api/shipping

5. API Gateway
   - Single entry point for all client requests
   - Routes requests to appropriate services
   - Implements circuit breakers for resilience

## Architecture

- Each service has its own database (Database per Service pattern)
- Services communicate via REST APIs and asynchronous messaging
- Kafka is used for asynchronous messaging between services
- Docker and Docker Compose for containerization
- Spring Cloud Gateway for API Gateway

## Fault Tolerance Features

- Circuit Breaker Pattern: Prevents cascade failures by stopping calls to failing services
- Retry Pattern: Automatically retries failed operations with exponential backoff
- Rate Limiter Pattern: Limits the number of requests to protect services from overload
- Time Limiter Pattern: Sets timeouts for service calls to prevent hanging operations
- Fallback Mechanisms: Provides graceful degradation when services are unavailable

## Technology Stack

- Spring Boot 3.2.0 for microservices
- MariaDB 10.6 for databases
- Apache Kafka 7.0.0 for message broker
- Docker & Docker Compose for containerization
- Spring Cloud Gateway for API Gateway
- Resilience4j for fault tolerance implementation

## Running the Application

1. Clone the repository
2. Build all services: `mvn clean package -DskipTests`
3. Start the services: `docker-compose up -d`
4. Access the API Gateway at: `http://localhost:8080`

## Service Endpoints

- Product Service: http://localhost:8081/api/products
- Payment Service: http://localhost:8082/api/payments
- Inventory Service: http://localhost:8083/api/inventory
- Shipping Service: http://localhost:8084/api/shipping 