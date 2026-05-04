@echo off
REM Shadownet Nexus - MySQL Startup Script (MySQL Only)

echo Starting Shadownet Nexus Backend with MySQL...
echo DB URL: jdbc:mysql://127.0.0.1:3305/shadownet?createDatabaseIfNotExist=true^&useSSL=false^&allowPublicKeyRetrieval=true
echo Server: http://localhost:3001
echo.

if "%DB_USERNAME%"=="" set DB_USERNAME=shadownet
if "%DB_PASSWORD%"=="" (
  echo DB_PASSWORD must be set before running this script.
  exit /b 1
)
if "%JWT_SECRET%"=="" (
  echo JWT_SECRET must be set before running this script.
  exit /b 1
)
if "%EMAIL_ENCRYPTION_KEY%"=="" (
  echo EMAIL_ENCRYPTION_KEY must be set before running this script.
  exit /b 1
)

java -Dspring.profiles.active=mysql -Dspring.datasource.url="jdbc:mysql://127.0.0.1:3305/shadownet?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true" -Dspring.datasource.username="%DB_USERNAME%" -Dspring.datasource.password="%DB_PASSWORD%" -Djwt.secret="%JWT_SECRET%" -Dapp.security.email-encryption-key="%EMAIL_ENCRYPTION_KEY%" -Dflyway.enabled=true -jar target/shadownet-nexus-1.0.0.jar

pause
