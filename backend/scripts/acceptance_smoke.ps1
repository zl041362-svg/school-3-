param(
  [string]$BaseUrl = "http://127.0.0.1:8080/api",
  [string]$AdminPhone = "",
  [string]$AdminPassword = ""
)

$ErrorActionPreference = "Stop"

function Write-Step {
  param([string]$Message)
  Write-Host "[STEP] $Message" -ForegroundColor Cyan
}

function Assert-Condition {
  param(
    [bool]$Condition,
    [string]$Message
  )

  if (-not $Condition) {
    throw "ASSERT FAILED: $Message"
  }
}

function Invoke-Api {
  param(
    [string]$Method,
    [string]$Path,
    [hashtable]$Headers,
    $Body
  )

  $uri = "$BaseUrl$Path"
  $params = @{
    Method = $Method
    Uri = $uri
    Headers = $Headers
  }

  if ($null -ne $Body) {
    $params.ContentType = "application/json"
    $params.Body = ($Body | ConvertTo-Json -Depth 6)
  }

  Invoke-RestMethod @params
}

try {
  $suffix = Get-Date -Format "MMddHHmmss"
  $phone = "137$suffix"

  Write-Step "Register customer $phone"
  $registerResp = Invoke-Api -Method "Post" -Path "/auth/register" -Headers @{} -Body @{
    phone = $phone
    password = "123456"
    role = "customer"
  }
  Assert-Condition -Condition ($registerResp.phone -eq $phone) -Message "register phone mismatch"

  Write-Step "Login customer"
  $loginResp = Invoke-Api -Method "Post" -Path "/auth/login" -Headers @{} -Body @{
    phone = $phone
    password = "123456"
  }
  Assert-Condition -Condition (-not [string]::IsNullOrWhiteSpace($loginResp.token)) -Message "login token missing"
  $token = $loginResp.token
  $authHeaders = @{ Authorization = "Bearer $token" }

  Write-Step "Create address"
  $addressResp = Invoke-Api -Method "Post" -Path "/addresses" -Headers $authHeaders -Body @{
    receiver = "Smoke User"
    phone = "13800000099"
    address = "Shenzhen Nanshan 99"
    isDefault = $true
  }
  $isDefault = [int]$addressResp.isDefault
  Assert-Condition -Condition ($isDefault -eq 1) -Message "default address expected"

  Write-Step "Add item to cart"
  $cartItemResp = Invoke-Api -Method "Post" -Path "/cart/items" -Headers $authHeaders -Body @{
    productId = 1
    qty = 1
  }
  Assert-Condition -Condition ($cartItemResp.productId -eq 1) -Message "cart productId expected 1"

  Write-Step "Create order from cart"
  $orderResp = Invoke-Api -Method "Post" -Path "/orders" -Headers $authHeaders -Body @{
    receiver = "Smoke User"
    phone = "13800000099"
    address = "Shenzhen Nanshan 99"
    items = @(@{ productId = 1; qty = 1 })
  }
  Assert-Condition -Condition ($orderResp.id -gt 0) -Message "order id should be numeric"
  $orderId = $orderResp.id

  Write-Step "Validate order list"
  $orderListResp = Invoke-Api -Method "Get" -Path "/orders?page=1&pageSize=10" -Headers $authHeaders -Body $null
  Assert-Condition -Condition ($orderListResp.total -ge 1) -Message "order list should not be empty"

  Write-Step "Validate order detail"
  $orderDetailResp = Invoke-Api -Method "Get" -Path "/orders/$orderId" -Headers $authHeaders -Body $null
  Assert-Condition -Condition ($orderDetailResp.id -eq $orderId) -Message "order detail id mismatch"

  if (-not [string]::IsNullOrWhiteSpace($AdminPhone) -and -not [string]::IsNullOrWhiteSpace($AdminPassword)) {
    Write-Step "Login admin"
    $adminLoginResp = Invoke-Api -Method "Post" -Path "/auth/login" -Headers @{} -Body @{
      phone = $AdminPhone
      password = $AdminPassword
    }
    $adminToken = $adminLoginResp.token
    Assert-Condition -Condition (-not [string]::IsNullOrWhiteSpace($adminToken)) -Message "admin token missing"

    Write-Step "Check admin users endpoint"
    $adminHeaders = @{ Authorization = "Bearer $adminToken" }
    $adminUsersResp = Invoke-Api -Method "Get" -Path "/admin/users?page=1&pageSize=5" -Headers $adminHeaders -Body $null
    Assert-Condition -Condition ($adminUsersResp.total -ge 1) -Message "admin users list should not be empty"
  }

  Write-Host "`n[PASS] Acceptance smoke flow passed." -ForegroundColor Green
  exit 0
}
catch {
  Write-Host "`n[FAIL] $($_.Exception.Message)" -ForegroundColor Red
  exit 1
}







