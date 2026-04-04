# Finance Dashboard — Backend

A production-grade microservices backend for a **Finance Dashboard System** built with **Spring Boot**, **Spring Security (JWT)**, **Spring Cloud (Eureka + API Gateway)**, and **Microsoft SQL Server**.

The system enforces strict **role-based access control** across all services, provides full **financial record management**, and exposes **aggregated dashboard analytics APIs** designed to feed real-time dashboards.

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────┐
│                    React Frontend                   │
│                  (localhost:3000)                   │
└────────────────────────┬────────────────────────────┘
                         │ HTTP
                         ▼
┌─────────────────────────────────────────────────────┐
│                   API Gateway :8082                  │
│  • JWT validation on every request                  │
│  • Forwards X-Auth-User-Email / X-Auth-User-Role    │
│  • Routes:  /api/auth/**  → AUTH-SERVICE            │
│             /api/users/** → USER-SERVICE            │
│             /api/finance/**→ FINANCE-SERVICE        │
└──────┬────────────────┬───────────────┬─────────────┘
       │                │               │
       ▼                ▼               ▼
┌────────────┐  ┌────────────┐  ┌──────────────────┐
│AUTH-SERVICE│  │USER-SERVICE│  │ FINANCE-SERVICE   │
│  :9095     │  │  :9098     │  │    :9096          │
│            │  │            │  │                   │
│ Register   │  │ CRUD Users │  │ CRUD Records      │
│ Login      │  │ Role Mgmt  │  │ Dashboard Summary │
│ JWT Issue  │  │ User Stats │  │ Category Totals   │
│ BCrypt     │  │            │  │ Monthly Trends    │
└─────┬──────┘  └─────┬──────┘  └────────┬──────────┘
      │               │                  │
      └───────────────┴──────────────────┘
                       │
                       ▼
         ┌─────────────────────────┐
         │    Eureka Server :8761  │
         │   (Service Registry)    │
         └─────────────────────────┘
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2.5 |
| Service Discovery | Spring Cloud Netflix Eureka |
| API Gateway | Spring Cloud Gateway |
| Security | Spring Security + JWT (jjwt 0.11.5) |
| Password Hashing | BCrypt |
| ORM | Spring Data JPA / Hibernate |
| Database | Microsoft SQL Server |
| Validation | Jakarta Bean Validation |
| Build Tool | Maven |

---

## Services

| Service | Port | Description |
|---|---|---|
| `eureka-server` | 8761 | Service registry — all services register here |
| `api-gateway` | 8082 | Single entry point — validates JWT, routes requests |
| `auth-service` | 9095 | Register, login, JWT issuance with role claims |
| `user-service` | 9098 | User CRUD, role assignment, status management |
| `finance-service` | 9096 | Financial records CRUD, dashboard analytics APIs |

---

## Roles & Permissions

| Action | VIEWER | ANALYST | ADMIN |
|---|:---:|:---:|:---:|
| View users | ✅ | ✅ | ✅ |
| Create / edit / delete users | ❌ | ❌ | ✅ |
| View financial records | ✅ | ✅ | ✅ |
| Create / edit financial records | ❌ | ✅ | ✅ |
| Delete financial records | ❌ | ❌ | ✅ |
| View dashboard summary & analytics | ❌ | ✅ | ✅ |

Access control is enforced at two levels:
1. **API Gateway** — validates the JWT token before any request reaches a service
2. **Each service** — reads the `X-Auth-User-Role` header forwarded by the gateway and enforces `@PreAuthorize` rules

---

## Prerequisites

- Java 17+
- Maven 3.8+
- Microsoft SQL Server (local or remote)
- Git

---

## Database Setup

Create three databases in SQL Server before running:

```sql
CREATE DATABASE finance_auth_db;
CREATE DATABASE finance_user_db;
CREATE DATABASE finance_records_db;
```

All tables are created automatically by Hibernate on first run (`ddl-auto=update`).

> **Note:** The default DB credentials are `sa / Password123!` on port `1433`. Update `application.properties` in each service if your setup differs.

---

## Configuration

Each service has its own `src/main/resources/application.properties`. Key settings to review:

**Database** (same pattern in all three data services):
```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=<db_name>;encrypt=true;trustServerCertificate=true
spring.datasource.username=sa
spring.datasource.password=Password123!
```

**JWT** (must be identical in `auth-service` and `api-gateway`):
```properties
jwt.secret=finance-dashboard-super-secret-key-2024-minimum-256-bits
jwt.expiration-ms=3600000
```

---

## Running the Project

Start services **in this exact order** — each depends on the one before it.

### Step 1 — Eureka Server
```bash
cd eureka-server
mvn spring-boot:run
```
Open http://localhost:8761 to confirm the dashboard loads.

### Step 2 — API Gateway
```bash
cd api-gateway
mvn spring-boot:run
```

### Step 3 — Auth Service
```bash
cd auth-service
mvn spring-boot:run
```

### Step 4 — User Service
```bash
cd user-service
mvn spring-boot:run
```

### Step 5 — Finance Service
```bash
cd finance-service
mvn spring-boot:run
```

After all services are running, verify each appears in the Eureka dashboard at http://localhost:8761.

---

## API Reference

All requests go through the API Gateway at `http://localhost:8082`. Authenticated endpoints require:
```
Authorization: Bearer <token>
```

### Auth Service — `/api/auth`

#### Register
```
POST /api/auth/auth/register
Content-Type: application/json

{
  "name": "Omkar Sakri",
  "email": "omkar@example.com",
  "password": "secret123",
  "role": "ADMIN"        ← VIEWER | ANALYST | ADMIN (default: VIEWER)
}
```

#### Login
```
POST /api/auth/auth/login
Content-Type: application/json

{
  "email": "omkar@example.com",
  "password": "secret123"
}
```
**Response:**
```json
{
  "token": "eyJhbGci...",
  "tokenType": "Bearer",
  "email": "omkar@example.com",
  "name": "Omkar Sakri",
  "role": "ADMIN",
  "status": "ACTIVE",
  "expiresIn": 3600000
}
```

---

### User Service — `/api/users`

| Method | Endpoint | Role Required | Description |
|---|---|---|---|
| GET | `/api/users/users` | VIEWER+ | List all users |
| GET | `/api/users/users/{id}` | VIEWER+ | Get user by ID |
| POST | `/api/users/users` | ADMIN | Create user |
| PUT | `/api/users/users/{id}` | ADMIN | Update user |
| DELETE | `/api/users/users/{id}` | ADMIN | Delete user |
| GET | `/api/users/users/stats` | ANALYST+ | User statistics |

---

### Finance Service — `/api/finance`

#### Financial Records

| Method | Endpoint | Role Required | Description |
|---|---|---|---|
| GET | `/api/finance/finance/records` | VIEWER+ | List records (filterable) |
| GET | `/api/finance/finance/records/{id}` | VIEWER+ | Get record by ID |
| POST | `/api/finance/finance/records` | ANALYST+ | Create record |
| PUT | `/api/finance/finance/records/{id}` | ANALYST+ | Update record |
| DELETE | `/api/finance/finance/records/{id}` | ADMIN | Delete record |

**Filter Parameters (GET /records):**
```
?type=INCOME          ← INCOME or EXPENSE
?category=Salary
?from=2024-01-01&to=2024-12-31
```

**Create/Update Record Body:**
```json
{
  "amount": 5000.00,
  "type": "INCOME",
  "category": "Salary",
  "date": "2024-03-15",
  "notes": "Monthly salary"
}
```

#### Dashboard Analytics

| Method | Endpoint | Role Required | Description |
|---|---|---|---|
| GET | `/api/finance/finance/dashboard/summary` | ANALYST+ | Full dashboard summary |
| GET | `/api/finance/finance/dashboard/category-breakdown` | ANALYST+ | Totals by category |

**Dashboard Summary Response:**
```json
{
  "totalIncome": 25000.00,
  "totalExpenses": 12500.00,
  "netBalance": 12500.00,
  "totalRecords": 47,
  "incomeCount": 20,
  "expenseCount": 27,
  "categoryTotals": {
    "Salary": 20000.00,
    "Freelance": 5000.00,
    "Rent": 8000.00
  },
  "monthlyTrends": [
    { "month": "2024-01", "income": 5000.00, "expenses": 2500.00, "net": 2500.00 },
    { "month": "2024-02", "income": 5000.00, "expenses": 3000.00, "net": 2000.00 }
  ],
  "recentTransactions": [ ... ]
}
```

---

## Design Decisions

**Why microservices?**
Each domain (auth, users, finance) scales and deploys independently. The Eureka registry + API Gateway pattern mirrors real-world FinTech architectures.

**Why does the gateway forward headers instead of passing the JWT to each service?**
Downstream services trust the gateway. Re-validating JWTs in every service is redundant and adds latency. The gateway validates once, extracts `email` and `role`, and forwards them as `X-Auth-User-Email` / `X-Auth-User-Role` headers. Each service reads these to build a Spring Security context.

**Why BCrypt for passwords?**
Plain-text or MD5 passwords are a critical security flaw. BCrypt is the industry standard — it's adaptive (configurable cost factor) and inherently salted.

**Why separate databases per service?**
True microservice isolation. Each service owns its data and schema. If the finance service goes down, user management continues unaffected.

**Why `ddl-auto=update`?**
Simplifies setup for evaluation. In production this would be `validate` with Flyway/Liquibase migrations.

---

## Project Structure

```
backend/
├── eureka-server/
│   └── src/main/java/com/financeapp/eurekaserver/
├── api-gateway/
│   └── src/main/java/com/financeapp/apigateway/
│       ├── config/         ← CorsConfig
│       ├── filter/         ← JwtAuthFilter (GlobalFilter)
│       └── security/       ← JwtUtil
├── auth-service/
│   └── src/main/java/com/financeapp/authservice/
│       ├── controller/     ← AuthController
│       ├── service/        ← AuthService
│       ├── repository/     ← UserRepository
│       ├── model/          ← User (with Role + Status enums)
│       ├── dto/            ← AuthDtos (Register/Login/Response)
│       ├── security/       ← JwtUtil, SecurityConfig
│       └── exception/      ← GlobalExceptionHandler
├── user-service/
│   └── src/main/java/com/financeapp/userservice/
│       ├── controller/     ← UserController
│       ├── service/        ← UserService
│       ├── repository/     ← UserRepository
│       ├── model/          ← User
│       ├── dto/            ← UserDtos
│       ├── security/       ← GatewayAuthFilter, SecurityConfig
│       └── exception/      ← GlobalExceptionHandler
└── finance-service/
    └── src/main/java/com/financeapp/financeservice/
        ├── controller/     ← FinanceController
        ├── service/        ← FinanceService
        ├── repository/     ← FinancialRecordRepository
        ├── model/          ← FinancialRecord
        ├── dto/            ← FinanceDtos (Record + Dashboard DTOs)
        ├── security/       ← GatewayAuthFilter, SecurityConfig
        └── exception/      ← GlobalExceptionHandler
```

---

## Assumptions

- SQL Server is running locally on port `1433` (default). Change the port in `application.properties` if needed.
- All services are started on the same machine for local development.
- The JWT secret is shared between `auth-service` and `api-gateway` via `application.properties`. In production this would come from a secrets manager (e.g., AWS Secrets Manager, HashiCorp Vault).
- `ddl-auto=update` is intentional for ease of evaluation setup.

---

*Built with Spring Boot 3.2.5 · Java 17 · Spring Cloud 2023.0.3*
