@echo off
echo Starting Shadownet Nexus Backend (Local Profile)...
set SPRING_PROFILES_ACTIVE=local
mvnw.cmd spring-boot:run
pause
