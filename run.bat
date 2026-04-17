@echo off
echo Starting Shadownet Nexus full stack...

call setup.bat

echo Frontend dev server...
start "Frontend" cmd /k "npm run dev"

echo Backend Spring Boot...
start "Backend" cmd /k "cd springboot && mvn spring-boot:run"

echo URLs:
echo Frontend: http://localhost:8080
echo Backend API: http://localhost:3001
echo Health: curl http://localhost:3001/actuator/health

pause
