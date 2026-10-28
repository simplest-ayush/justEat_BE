# JustEat — Backend

A food ordering platform backend built with Spring Boot, supporting two roles — **Customer** and **Restaurant Owner** — covering restaurant/menu management, cart, checkout, order lifecycle tracking, and preferences/favorites.

## Tech Stack

- **Java 21**, **Spring Boot 3**
- **Spring Data JPA** + **Hibernate** (schema managed via `ddl-auto=update` — no Flyway)
- **PostgreSQL**
- **Spring Security** + **JWT** (stateless, `Authorization: Bearer <token>` header)
- **BCrypt** password hashing
- **Bean Validation** (`jakarta.validation`)
- **Lombok**
- **springdoc-openapi** (Swagger UI)
- **Spring Mail** (password reset emails)
- **Docker**

## Package Structure (layer-based)

```
com.justeat.backend
├── config/        Security, OpenAPI, CORS configuration
├── controller/     REST controllers
├── dto/            Request/response records
├── entity/         JPA entities
├── enums/          Role, OrderStatus, DietaryRestriction
├── exception/       Custom exceptions + GlobalExceptionHandler
├── repository/      Spring Data JPA repositories
├── security/         JWT service, auth filter, user details service
└── service/          Business logic
    └── impl/          Implementations
```

## Prerequisites

- Java 21
- Maven
- PostgreSQL (running locally, or via the included `compose.yaml`)

## Environment Variables

Create a `.env` file in the project root (never committed — see `.gitignore`):

```env
DB_URL=jdbc:postgresql://localhost:5432/justeat
DB_USERNAME=justeat
DB_PASSWORD=justeat

JWT_SECRET=<a long random string>
JWT_ACCESS_EXPIRATION_MS=900000

MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=<your email>
MAIL_PASSWORD=<app password>
MAIL_FROM=<your email>

FRONTEND_URL=http://localhost:5173
```

## Running Locally

```bash
# Start Postgres (if using the bundled compose file)
docker compose up -d

# Run the application
./mvnw spring-boot:run
```

The API is available at `http://localhost:8090/api/v1`.
Swagger UI: `http://localhost:8090/swagger-ui.html`

## Authentication

- `POST /auth/register` / `POST /auth/login` return an `accessToken` (short-lived JWT) and a `refreshToken` (opaque, stored server-side, revocable).
- Protected endpoints require `Authorization: Bearer <accessToken>`.
- `POST /auth/refresh` exchanges a valid refresh token for a new access token.
- `POST /auth/logout` revokes the refresh token server-side.
- `POST /auth/forget-password` / `POST /auth/reset-password` — password recovery via a hashed, single-use, time-limited token emailed to the user.

## Roles

Two roles only — `CUSTOMER` and `RESTAURANT_OWNER` (no self-serve `ADMIN`, to avoid privilege escalation via the public register endpoint).

| Capability | Customer | Restaurant Owner |
|---|---|---|
| Browse restaurants/menus | ✅ | ✅ (read-only) |
| Cart, checkout, order history | ✅ | ❌ |
| Manage own restaurants/menu | ❌ | ✅ |
| Advance order status | ❌ | ✅ (own restaurant's orders) |
| Preferences/favorites | ✅ | ❌ |

## Order Lifecycle

```
PENDING → PREPARING → READY
```
Enforced via a fixed transition map in `OrderServiceImpl` — no skipping states, no transitions out of `READY`.

## "Mostly Ordered" Items

Each `MenuItem` tracks an `orderCount`, incremented once per order it appears in (independent of quantity). Once `orderCount` exceeds a threshold, the item is automatically flagged `mostlyOrdered` — no manual toggle.

## Tests

```bash
mvn test
```
37 unit tests (JUnit 5 + Mockito) covering auth, ownership checks, the cart's single-restaurant rule, and the order status state machine.

## Docker

```bash
docker build -t justeat-backend .
```

## Deployment

Deployed to Azure App Service (container) with Azure Database for PostgreSQL. CORS `allowedOrigins` in `SecurityConfig` must include the deployed frontend's origin.

## Documentation

- `AI_USAGE.md` — where AI assistance was used during development
- Swagger UI — full endpoint documentation, available at `/swagger-ui.html`