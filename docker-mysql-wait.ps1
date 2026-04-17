$maxRetries = 30
$retryCount = 0
do {
    try {
docker exec db mysqladmin ping -h localhost -u root -proot --silent
        Write-Host "MySQL is healthy!" -ForegroundColor Green
        break
    } catch {
        Write-Host "Waiting for MySQL... ($retryCount/$maxRetries)" -ForegroundColor Yellow
        Start-Sleep -Seconds 2
        $retryCount++
    }
} while ($retryCount -lt $maxRetries)

if ($retryCount -ge $maxRetries) {
    Write-Error "MySQL failed to become healthy after $maxRetries attempts"
    exit 1
}
