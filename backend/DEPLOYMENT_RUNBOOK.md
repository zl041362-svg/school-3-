# Deployment Runbook (D5)

This runbook provides a minimal production-style launch and rollback process for the current milestone.

## 1) Required environment variables

Set these before startup:

- `DB_HOST`
- `DB_PORT` (default `3306`)
- `DB_NAME` (default `zhhs_nong`)
- `DB_USER`
- `DB_PASSWORD`
- `JWT_SECRET` (must be long/random)
- `JWT_EXPIRE_SECONDS` (for example `604800`)

## 2) Pre-deploy checks

### Backend tests

```powershell
Set-Location "C:\Users\lz\Desktop\zhhsannong\n\backend"
mvn test -q
```

### Frontend unit tests

```powershell
Set-Location "C:\Users\lz\Desktop\zhhsannong\n"
npm run test:unit -- --run
```

### Smoke acceptance

Backend must be running first.

```powershell
Set-Location "C:\Users\lz\Desktop\zhhsannong\n\backend"
.\scripts\acceptance_smoke.ps1
```

## 3) Start backend (example)

```powershell
Set-Location "C:\Users\lz\Desktop\zhhsannong\n\backend"
$env:DB_HOST = "127.0.0.1"
$env:DB_PORT = "3306"
$env:DB_NAME = "zhhs_nong"
$env:DB_USER = "root"
$env:DB_PASSWORD = "<your-password>"
$env:JWT_SECRET = "<your-strong-random-secret>"
$env:JWT_EXPIRE_SECONDS = "604800"
mvn spring-boot:run
```

## 4) Post-deploy checks

- Open `http://127.0.0.1:8080/swagger-ui.html`
- Verify `GET /api/products` returns `200`
- Verify customer registration/login/order flow works
- Verify non-admin call to `/api/admin/users` returns `403` with JSON body
- Verify disabled user cannot perform new login

## 5) Rollback strategy

Current schema uses Flyway forward migrations only. Rollback is app-version rollback + data restore.

1. Stop current backend process.
2. Restore MySQL backup/snapshot from pre-deploy point.
3. Start previous backend version with the same env vars.
4. Re-run smoke script and verify core flows.

## 5.1) Scope notes for this milestone

- Disable-user policy is login-gate only: existing JWT is valid until expiry.
- Order lifecycle implementation currently creates `pending_shipment`; later transitions are planned for next milestone.

## 6) Emergency stop/start

### Stop process on 8080

```powershell
$conn = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
if ($conn) { Stop-Process -Id $conn.OwningProcess -Force }
```

### Quick health probe

```powershell
Invoke-WebRequest -Uri "http://127.0.0.1:8080/api/products" -UseBasicParsing
```

