# Usable Acceptance (D4)

This guide defines the acceptance baseline for the current "usable" milestone.

## 1) Prerequisites

- MySQL database exists: `zhhs_nong`
- Backend can connect with valid DB credentials
- Node.js and Maven installed

## 2) Start backend

Use environment variables if local MySQL password is not `root`.

```powershell
Set-Location "C:\Users\lz\Desktop\zhhsannong\n\backend"
$env:DB_PASSWORD = "<your-mysql-password>"
mvn spring-boot:run
```

## 3) Run smoke acceptance script

In another terminal:

```powershell
Set-Location "C:\Users\lz\Desktop\zhhsannong\n\backend"
.\scripts\acceptance_smoke.ps1
```

Optional admin validation (if you have a working admin credential):

```powershell
Set-Location "C:\Users\lz\Desktop\zhhsannong\n\backend"
.\scripts\acceptance_smoke.ps1 -AdminPhone "13800000000" -AdminPassword "<admin-password>"
```

## 4) Frontend unit tests

```powershell
Set-Location "C:\Users\lz\Desktop\zhhsannong\n"
npm run test:unit -- --run
```

## 5) Backend integration tests

```powershell
Set-Location "C:\Users\lz\Desktop\zhhsannong\n\backend"
mvn test -q
```

## 6) Expected result summary

- Customer can register/login and get profile
- Disabled user is blocked on new login
- Customer can CRUD addresses and keep one default
- Customer can add to cart and create order from cart
- Cart and address update/delete operations are user-scoped
- Empty cart checkout returns business error
- Customer can only read own orders
- Non-admin access to `/api/admin/*` returns 403 JSON
- Admin invalid payloads return 400 with `{ message, code }`
- Missing admin review resources return 404 with `{ message, code }`
- Not-found business errors return HTTP 404 with `{ message, code }`

## 7) Contract references

- `backend/CONTRACT_BASELINE.md`
- `backend/README.md`

