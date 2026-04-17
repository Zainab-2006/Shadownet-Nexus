# Shadownet Nexus Ultimate Start - PowerShell Compatible
Write-Host "SHADOWNET NEXUS - ELITE START v2.2 - PS FIXED" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

# Setup env
& .\setup.bat

# Start Backend
Start-Process cmd -ArgumentList '/k', 'cd springboot && mvn spring-boot:run' -WindowStyle Normal -WorkingDirectory $PWD

Start-Sleep -Seconds 10

# Start Frontend
npm run dev

Write-Host "Frontend: http://localhost:8080" -ForegroundColor Yellow
Write-Host "Backend: http://localhost:3001" -ForegroundColor Yellow
Write-Host "Health: http://localhost:3001/actuator/health" -ForegroundColor Yellow
