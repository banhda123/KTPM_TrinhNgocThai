# E-commerce Microservices System

This project implements a simple e-commerce system using microservices architecture.

## Services

1. Product Service
   - Manages product information (name, price, description, inventory)
   - Database: MariaDB
   - API endpoints: /products

2. Order Service
   - Manages orders (create, view, cancel)
   - Database: MariaDB
   - API endpoints: /orders

3. Customer Service
   - Manages customer information (name, address, contact)
   - Database: MariaDB
   - API endpoints: /customers

4. API Gateway
   - Single entry point for all client requests
   - Routes requests to appropriate services

## Architecture

- Each service has its own database (Database per Service pattern)
- Services communicate via REST APIs
- Kafka is used for asynchronous messaging
- Docker and Docker Compose for containerization

## Technology Stack

- Spring Boot for services
- MariaDB for databases
- Apache Kafka for message broker
- Docker & Docker Compose for containerization
- Spring Cloud Gateway for API Gateway 