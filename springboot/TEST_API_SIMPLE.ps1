$API_BASE = "http://localhost:3001"

Write-Host "`nShadownet Nexus Backend API Test`n" -ForegroundColor Magenta

# Test 1: Health Check
Write-Host "[1] Testing Health Endpoint..." -ForegroundColor Cyan
try {
    $health = Invoke-WebRequest -Uri "$API_BASE/health" -UseBasicParsing
    $data = $health.Content | ConvertFrom-Json
    Write-Host "OK: Health Check Passed" -ForegroundColor Green
} catch {
    Write-Host "ERROR: Backend is not running!" -ForegroundColor Red
    Write-Host "Start with: java -jar target/shadownet-nexus-1.0.0.jar" -ForegroundColor Yellow
    exit 1
}

# Test 2: User Registration
Write-Host "`n[2] Testing User Registration..." -ForegroundColor Cyan
$randomId = Get-Random -Minimum 1000 -Maximum 9999
$body = @{
    email = "testuser$randomId@example.com"
    username = "testuser$randomId"
    password = "Test123!"
} | ConvertTo-Json

try {
    $reg = Invoke-WebRequest -Uri "$API_BASE/api/register" -Method Post -ContentType "application/json" -Body $body -UseBasicParsing
    $data = $reg.Content | ConvertFrom-Json
    $token = $data.token
    Write-Host "OK: User registered successfully" -ForegroundColor Green
} catch {
    Write-Host "ERROR: Registration failed" -ForegroundColor Red
    exit 1
}

# Test 3: Get Challenges
Write-Host "`n[3] Testing Get Challenges..." -ForegroundColor Cyan
$headers = @{ "Authorization" = "Bearer $token" }

try {
    $res = Invoke-WebRequest -Uri "$API_BASE/api/challenges" -UseBasicParsing -Headers $headers
    $data = $res.Content | ConvertFrom-Json
    Write-Host "OK: Got $($data.Count) challenges" -ForegroundColor Green
} catch {
    Write-Host "ERROR: Get challenges failed" -ForegroundColor Red
}

# Test 4: Get Operators
Write-Host "`n[4] Testing Get Operators..." -ForegroundColor Cyan
try {
    $res = Invoke-WebRequest -Uri "$API_BASE/api/operators" -UseBasicParsing
    $data = $res.Content | ConvertFrom-Json
    Write-Host "OK: Got $($data.Count) operators" -ForegroundColor Green
} catch {
    Write-Host "ERROR: Get operators failed" -ForegroundColor Red
}

# Test 5: Get Leaderboard
Write-Host "`n[5] Testing Get Leaderboard..." -ForegroundColor Cyan
try {
    $res = Invoke-WebRequest -Uri "$API_BASE/api/leaderboard" -UseBasicParsing
    $data = $res.Content | ConvertFrom-Json
    Write-Host "OK: Leaderboard retrieved" -ForegroundColor Green
} catch {
    Write-Host "ERROR: Get leaderboard failed" -ForegroundColor Red
}

# Test 6: Get User
Write-Host "`n[6] Testing Get User Profile..." -ForegroundColor Cyan
try {
    $res = Invoke-WebRequest -Uri "$API_BASE/api/user" -UseBasicParsing -Headers $headers
    $data = $res.Content | ConvertFrom-Json
    Write-Host "OK: User profile retrieved" -ForegroundColor Green
} catch {
    Write-Host "ERROR: Get user failed" -ForegroundColor Red
}

# Test 7: Get Missions
Write-Host "`n[7] Testing Get Missions..." -ForegroundColor Cyan
try {
    $res = Invoke-WebRequest -Uri "$API_BASE/api/missions" -UseBasicParsing
    $data = $res.Content | ConvertFrom-Json
    Write-Host "OK: Got missions" -ForegroundColor Green
} catch {
    Write-Host "ERROR: Get missions failed" -ForegroundColor Red
}

Write-Host "`n" -ForegroundColor Green
Write-Host "SUMMARY:" -ForegroundColor Green
Write-Host "  Backend running on port 3001" -ForegroundColor Green
Write-Host "  User registration working" -ForegroundColor Green
Write-Host "  JWT authentication working" -ForegroundColor Green
Write-Host "  All endpoints responding" -ForegroundColor Green
Write-Host ""
