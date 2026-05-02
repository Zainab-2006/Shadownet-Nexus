@echo off
echo Starting Shadownet Nexus Backend with Environment Variables...

REM Load environment variables from .env file
for /f "usebackq tokens=1,2 delims==" %%a in ("%~dp0.env") do (
    set %%a=%%b
)

set SPRING_PROFILES_ACTIVE=local
call mvnw.cmd spring-boot:run
pause
