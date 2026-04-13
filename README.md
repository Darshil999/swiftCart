# SwiftCart Backend

Multi-vendor e-commerce platform built with Spring Boot 3, Spring Security, JWT, and PostgreSQL.

---

## Project Structure

```
src/main/java/com/swiftcart/
├── SwiftCartApplication.java
├── config/
│   └── SecurityConfig.java          # Spring Security + JWT filter wiring
├── controller/
│   ├── AuthController.java          # POST /auth/register, POST /auth/login
│   └── UserController.java          # Protected endpoints with @PreAuthorize examples
├── dto/
│   ├── request/
│   │   ├── RegisterRequest.java
│   │   └── LoginRequest.java
│   └── response/
│       ├── AuthResponse.java
│       └── ApiResponse.java
├── entity/
│   ├── User.java
│   └── Role.java                    # Enum: BUYER, SELLER, ADMIN
├── exception/
│   ├── EmailAlreadyExistsException.java
│   └── GlobalExceptionHandler.java  # @RestControllerAdvice
├── repository/
│   └── UserRepository.java
├── security/
│   ├── JwtUtil.java                 # Token generation + validation
│   ├── JwtAuthenticationFilter.java # OncePerRequestFilter
│   ├── UserDetailsImpl.java         # Spring Security UserDetails adapter
│   └── UserDetailsServiceImpl.java  # Loads user from DB by email
└── service/
    └── AuthService.java
```

---

## Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 14+ running locally

## Database Setup

```sql
CREATE DATABASE swiftcart_db;
```

Update `src/main/resources/application.yml` with your PostgreSQL credentials.

## Run the Application

```bash
mvn spring-boot:run
```

Server starts on `http://localhost:8080`

## Run Tests

```bash
mvn test
```

Tests use an in-memory H2 database — no PostgreSQL required for testing.

---

## API Reference

### POST /auth/register

Registers a new user. Returns a JWT token immediately (no separate login needed).

**Request:**
```json
{
  "name": "Alice Smith",
  "email": "alice@example.com",
  "password": "securePass1",
  "role": "BUYER"
}
```

**Role options:** `BUYER`, `SELLER`, `ADMIN` — defaults to `BUYER` if omitted.

**Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "email": "alice@example.com",
  "role": "BUYER"
}
```

**Error — duplicate email (409 Conflict):**
```json
{
  "success": false,
  "message": "Email is already registered: alice@example.com"
}
```

---

### POST /auth/login

Authenticates with email and password. Returns a JWT token.

**Request:**
```json
{
  "email": "alice@example.com",
  "password": "securePass1"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "email": "alice@example.com",
  "role": "BUYER"
}
```

**Error — wrong credentials (401 Unauthorized):**
```json
{
  "success": false,
  "message": "Invalid email or password"
}
```

---

### GET /api/users/me  *(Authenticated)*

Returns the currently authenticated user's info.

```bash
curl -H "Authorization: Bearer <token>" http://localhost:8080/api/users/me
```

**Response:**
```json
{
  "id": 1,
  "email": "alice@example.com",
  "role": "ROLE_BUYER"
}
```

---

### GET /api/users/admin-only  *(ADMIN only)*

```bash
curl -H "Authorization: Bearer <admin-token>" http://localhost:8080/api/users/admin-only
```

### GET /api/users/seller-dashboard  *(SELLER or ADMIN)*

```bash
curl -H "Authorization: Bearer <seller-token>" http://localhost:8080/api/users/seller-dashboard
```

---

## Sample cURL Requests

**Register a SELLER:**
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Bob Vendor",
    "email": "bob@example.com",
    "password": "vendorPass1",
    "role": "SELLER"
  }'
```

**Login:**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "bob@example.com",
    "password": "vendorPass1"
  }'
```

**Access protected endpoint:**
```bash
TOKEN="<paste token here>"
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/users/me
```

---

## Security Design Decisions

| Decision | Rationale |
|---|---|
| Stateless JWT | No server-side session storage needed; scales horizontally |
| BCrypt (strength 10) | Industry standard; resistant to brute force |
| `OncePerRequestFilter` | Guarantees single filter execution even in async dispatches |
| `@EnableMethodSecurity` | Fine-grained `@PreAuthorize` per method rather than URL-pattern matching |
| `ROLE_` prefix in `UserDetailsImpl` | Required by Spring Security's `hasRole()` expression |
| `ddl-auto: update` | Convenient for dev; switch to `validate` + Flyway/Liquibase in production |

---

## Next Steps (Upcoming Modules)

- **Products** — CRUD for seller listings
- **Orders** — Buyer order placement and tracking
- **Payments** — Stripe integration
- **Docker** — Containerization with docker-compose
- **Redis** — Session caching and rate limiting
