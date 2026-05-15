# Backend (Spring Boot + MySQL)
This folder contains the first backend milestone for the ZHHS project.
## What is included
- Spring Boot 3 + MyBatis-Plus + Flyway
- MySQL runtime configuration
- JWT auth with role claims
- Swagger UI (`/swagger-ui.html`)
- Public and auth APIs:
  - `POST /api/auth/register`
  - `POST /api/auth/login`
  - `GET /api/auth/profile` (requires token)
  - `GET /api/products`
  - `GET /api/products/{id}`
  - `GET /api/news`
  - `GET /api/news/{id}`
- Trade APIs (requires token):
  - `GET /api/cart`
  - `POST /api/cart/items`
  - `PUT /api/cart/items/{id}`
  - `DELETE /api/cart/items/{id}`
  - `GET /api/addresses`
  - `POST /api/addresses`
  - `PUT /api/addresses/{id}`
  - `DELETE /api/addresses/{id}`
  - `GET /api/orders`
  - `GET /api/orders/{id}`
  - `POST /api/orders`
- Admin APIs (requires `ROLE_ADMIN`):
  - `GET /api/admin/farmer-verifications`
  - `POST /api/admin/farmer-verifications/{id}/review`
  - `GET /api/admin/product-reviews`
  - `POST /api/admin/product-reviews/{id}/review`
  - `GET /api/admin/news-reviews`
  - `POST /api/admin/news-reviews/{id}/review`
  - `GET /api/admin/products`
  - `GET /api/admin/news`
  - `GET /api/admin/users`
  - `PATCH /api/admin/users/{id}`
  - `GET /api/admin/roles`
  - `PATCH /api/admin/roles/{id}`
  - `GET /api/admin/permissions`
  - `GET /api/admin/logs`
- Flyway migrations + seed data
- Integration test harness (`ApiFlowIntegrationTest`)
## Quick Start
1. Create MySQL database, for example: `zhhs_nong`.
2. Set environment variables if needed:
   - `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`
   - `JWT_SECRET`, `JWT_EXPIRE_SECONDS`
3. Run app:
```powershell
cd C:\Users\lz\Desktop\zhhsannong\n\backend
mvn spring-boot:run
```
4. Open Swagger UI:
```text
http://localhost:8080/swagger-ui.html
```
## Run tests (H2 profile)
```powershell
cd C:\Users\lz\Desktop\zhhsannong\n\backend
mvn test
```
## Notes
- The app serves under `/api`, matching front-end `baseURL` default.
- Current migrations include trade and admin demo data for local development.
- Tests run with H2 (`application-test.yml`) and replay all Flyway scripts.
- Contract freeze and milestone acceptance checklist: `backend/CONTRACT_BASELINE.md`.
- Usable acceptance guide and smoke script: `backend/USABLE_ACCEPTANCE.md`, `backend/scripts/acceptance_smoke.ps1`.
- Deployment and rollback runbook: `backend/DEPLOYMENT_RUNBOOK.md`.
- Logic gap-closure review: `backend/LOGIC_REVIEW.md`.
