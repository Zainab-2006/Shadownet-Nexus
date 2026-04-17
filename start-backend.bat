@echo off
REM Shadownet Nexus Backend Quick Start (H2 Embedded DB - No MySQL needed)
REM Usage: Double-click or run "start-backend.bat" from project root

echo Starting Shadownet Nexus Backend...
echo Port: http://localhost:3001
echo DB: H2 embedded (auto-seeded with demo data)
echo Press Ctrl+C to stop

cd springboot
if not exist "target\shadownet-nexus-1.0.0.jar" (
    echo Building JAR first...
    call mvnw.cmd clean package -DskipTests
)

java -jar target\shadownet-nexus-1.0.0.jar

echo.
echo Backend stopped. Test at: http://localhost:3001/health
pause
