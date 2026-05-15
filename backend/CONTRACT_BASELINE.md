# Contract Baseline (D1)

This document freezes the current frontend-backend API contract for the "usable" milestone.

## 1) Global rules

- Base path: `/api`
- Auth header: `Authorization: Bearer <token>`
- Success body: return business object directly (no outer `data` wrapper)
- List body: `{ items, total, page?, pageSize? }`
- Error body: `{ message, code }`

## 2) Auth contract

Frontend source: `src/api/modules/auth.js`

- `POST /api/auth/register`
  - request: `{ phone, password, role }`
  - response: `{ id, phone, role }`
- `POST /api/auth/login`
  - request: `{ phone, password }`
  - response: `{ token, user: { id, name, phone, role } }`
- `GET /api/auth/profile`
  - response: `{ user: { id, name, phone, role } }`

## 3) Shop contract

Frontend source: `src/api/modules/products.js`, `src/api/modules/news.js`, `src/api/modules/cart.js`, `src/api/modules/addresses.js`, `src/api/modules/orders.js`

### Products
- `GET /api/products`
  - query: `keyword?`, `category?`, `region?`, `page?`, `pageSize?`
  - response: `{ items, total, page, pageSize }`
- `GET /api/products/{id}`

### News
- `GET /api/news`
  - query: `page?`, `pageSize?`
  - response: `{ items, total, page, pageSize }`
- `GET /api/news/{id}`

### Cart (user scoped)
- `GET /api/cart`
  - response: `{ items, total }`
- `POST /api/cart/items`
  - request: `{ productId, qty }`
- `PUT /api/cart/items/{id}`
  - request: `{ qty }`
- `DELETE /api/cart/items/{id}`

### Addresses (user scoped)
- `GET /api/addresses`
  - response: `{ items, total }`
- `POST /api/addresses`
  - request: `{ receiver, phone, address, isDefault? }`
- `PUT /api/addresses/{id}`
  - request: `{ receiver, phone, address, isDefault? }`
- `DELETE /api/addresses/{id}`

### Orders (user scoped)
- `GET /api/orders`
  - query: `page?`, `pageSize?`
  - response: `{ items, total, page, pageSize }`
- `GET /api/orders/{id}`
- `POST /api/orders`
  - request: `{ receiver, phone, address, items: [{ productId, qty }] }`
  - response: order object with auto-increment numeric `id`
  - rule: checkout uses current user's cart items; when cart is empty, return business error `cart is empty`

## 4) Admin contract

Frontend source: `src/api/modules/admin.js`

- `GET /api/admin/farmer-verifications`
- `POST /api/admin/farmer-verifications/{id}/review`
  - request: `{ approved, reason }`
- `GET /api/admin/product-reviews`
- `POST /api/admin/product-reviews/{id}/review`
  - request: `{ approved, reason }`
- `GET /api/admin/news-reviews`
- `POST /api/admin/news-reviews/{id}/review`
  - request: `{ approved, reason }`
- `GET /api/admin/products`
- `GET /api/admin/news`
- `GET /api/admin/users`
- `PATCH /api/admin/users/{id}`
  - request: `{ status }`
- `GET /api/admin/roles`
- `PATCH /api/admin/roles/{id}`
  - request: `{ members }`
- `GET /api/admin/permissions`
- `GET /api/admin/logs`

## 5) Acceptance checklist (usable milestone)

Status legend: `[x]` verified, `[ ]` pending explicit verification.

### A. Auth
- [x] Register + login + profile path works for customer role.
- [x] Disabled user cannot log in.

### B. Shop core flow
- [x] Product list/detail works without login.
- [x] News list/detail works without login.
- [x] Add/update/remove cart items works for logged-in user only.
- [x] Address CRUD works for logged-in user only.
- [x] Exactly one default address per user.
- [x] Create order succeeds and clears cart.
- [x] User can list and view only their own orders.

### C. Admin
- [x] Admin can query and review farmer/product/news moderation queues.
- [x] Admin can update user status and role member count.
- [x] Non-admin gets 403 JSON: `{ message: "forbidden", code: 403 }`.

### D. Error consistency
- [x] Validation, biz error, unauthorized, forbidden all return `{ message, code }`.
- [x] Frontend can display backend error message directly.

### E. Regression
- [x] `mvn test` passes in `backend`.

## 6) Confirmed decisions

1. "Not found" domain errors use HTTP `404` and keep business `code/message` payload.
2. No strict phone format validation in this milestone.
3. Order status values remain English enums (`pending_shipment`, etc.); frontend maps to Chinese labels.
4. Disabled users are blocked on new login only; issued JWT tokens are not revoked immediately.
5. Order lifecycle in this milestone keeps the minimal closed-loop baseline; creation state is `pending_shipment`.


