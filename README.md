# E-Commerce Backend API

A production-grade REST API built with Spring Boot featuring multi-role RBAC
(Admin, Seller, Buyer), JWT authentication, Redis caching, and Kafka event streaming.

## Tech Stack

- Java 21 + Spring Boot 3.2
- Spring Security 6 + JWT
- PostgreSQL + Spring Data JPA
- Redis (cart, sessions, caching)
- Apache Kafka (async order events)
- Swagger UI (API documentation)
- Docker

## Features

- JWT-based authentication with role-based access control
- Seller onboarding with admin approval workflow
- Multi-seller cart and order management
- Async notifications via Kafka (order placed, seller alerts)
- Redis-based cart with 7-day TTL
- Product caching with @Cacheable and @CacheEvict
- Global exception handling with consistent API responses
- Mock payment gateway integration

## Getting Started

### Prerequisites
- Java 21
- PostgreSQL 15+
- Redis 7
- Kafka (or Docker)

### Run locally
```bash
# Start infrastructure
docker start redis kafka

# Run the app
./mvnw spring-boot:run
```

### API Documentation

Once running, open:
```
http://localhost:8080/swagger-ui.html
```

## API Overview

| Module | Endpoints |
|---|---|
| Auth | Register, Login, OTP verify |
| Seller | Apply, view profile |
| Admin | Approve/reject sellers, manage users |
| Products | CRUD (seller), browse/search (public) |
| Cart | Add, update, remove (Redis) |
| Orders | Place, track, cancel (buyer) · Process (seller) |
| Payments | Mock payment, gateway callback |

## Project Structure
```
src/main/java/com/ecommerce/ecommerce_backend/
├── config/          # Security, Redis, Kafka, Swagger config
├── controller/      # REST controllers
├── service/         # Business logic
├── repository/      # Spring Data JPA repositories
├── entity/          # JPA entities
├── dto/             # Request and response DTOs
├── security/        # JWT filter, UserPrincipal
├── kafka/           # Producers and consumers
├── exception/       # Custom exceptions + global handler
└── util/            # OTP, slug, security utilities
```