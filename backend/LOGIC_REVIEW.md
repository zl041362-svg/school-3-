# Logic Review (Gap Closure)

This review summarizes the current backend/frontend logic after the gap-closure iterations.

## 1) Auth and identity logic

- Register/login/profile path is covered by integration tests.
- User status check is enforced on login (`active` required).
- Confirmed policy: disabling a user blocks **new login only**; existing JWT remains valid until expiration.

## 2) Authorization and access control

- Public: products/news list+detail.
- Protected: cart/addresses/orders.
- Admin-only: `/api/admin/**`.
- Security behavior is consistent:
  - unauthenticated => `401` + `{ message: "unauthorized", code: 401 }`
  - forbidden => `403` + `{ message: "forbidden", code: 403 }`

## 3) Trade domain logic

- Cart is user-scoped; cross-user update/delete is rejected.
- Address book is user-scoped; cross-user update/delete is rejected.
- Default address uniqueness is maintained; deleting default promotes another address when available.
- Checkout rule: order creation is cart-driven only; empty cart returns business error.
- Order isolation is enforced: users can read only their own orders.

## 4) Admin domain logic

- Farmer/product/news reviews are queryable and reviewable by admin.
- User status and role members update are available to admin.
- Invalid admin payloads are rejected with `400`.
- Missing moderation records return `404` with business message/code.

## 5) Error semantics

- Validation, business, unauthorized, forbidden all use `{ message, code }` payload.
- Not-found business errors use HTTP `404` and keep business `code/message`.
- Frontend `http` interceptor reads backend `message` and propagates consistently.

## 6) Order-state policy

- Confirmed policy: keep minimal state loop for this milestone.
- Current implemented creation state: `pending_shipment`.
- Future transitions (`shipped`, `completed`) are reserved for the next milestone.

## 7) Residual non-blocking risks

- Existing JWT is not revoked immediately after user disable (accepted policy).
- No dedicated order status transition endpoints in this milestone (accepted scope).
- Acceptance smoke script does not force admin credential path by default.

## 8) Conclusion

No blocking logic defects were found for the current usable scope.
The current baseline is suitable for internal trial use with documented constraints above.

