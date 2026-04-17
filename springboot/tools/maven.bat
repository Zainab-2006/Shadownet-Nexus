@echo off
REM Local Maven wrapper for ShadowNet Nexus (runs backend)
setlocal
for %%I in ("%~dp0..\..") do set "ROOT_DIR=%%~fI"
set "BACKEND_DIR=%ROOT_DIR%\springboot"
set "MAVEN_HOME=%ROOT_DIR%\apache-maven-3.9.9"
set "MAVEN_OPTS=-Dmaven.repo.local=%ROOT_DIR%\.m2\repository %MAVEN_OPTS%"
set "PATH=%MAVEN_HOME%\bin;%PATH%"

REM Local development defaults. Production must pass real env vars.
if not defined JWT_SECRET set "JWT_SECRET=local-dev-jwt-secret-change-before-deploy-0123456789abcdef0123456789abcdef"
if not defined CORS_ORIGINS set "CORS_ORIGINS=http://localhost:5173,http://localhost:3000,http://localhost:8080"

echo %* | findstr /C:"spring-boot:run" >nul
if not errorlevel 1 (
  call :ensure_mysql || exit /b 1
)

cd /d "%BACKEND_DIR%"
mvn %*
exit /b %ERRORLEVEL%

:ensure_mysql
powershell -NoProfile -ExecutionPolicy Bypass -Command "if (Test-NetConnection 127.0.0.1 -Port 3305 -InformationLevel Quiet) { exit 0 } else { exit 1 }" >nul 2>nul
if not errorlevel 1 (
  if not defined DATABASE_URL set "DATABASE_URL=jdbc:mysql://127.0.0.1:3305/shadownet?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true"
  if not defined DB_USERNAME set "DB_USERNAME=shadownet"
  if not defined DB_PASSWORD set "DB_PASSWORD=root"
  echo MySQL is available on 127.0.0.1:3305; using project MySQL credentials.
  exit /b 0
)

powershell -NoProfile -ExecutionPolicy Bypass -Command "if (Test-NetConnection 127.0.0.1 -Port 3306 -InformationLevel Quiet) { exit 0 } else { exit 1 }" >nul 2>nul
if not errorlevel 1 (
  if not defined DATABASE_URL set "DATABASE_URL=jdbc:mysql://127.0.0.1:3306/shadownet?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true"
  if not defined DB_USERNAME set "DB_USERNAME=root"
  if not defined DB_PASSWORD set "DB_PASSWORD=root"
  echo MySQL is available on 127.0.0.1:3306; using native MySQL with local dev credentials.
  exit /b 0
)

echo MySQL is not listening on 127.0.0.1:3305.
echo Attempting to start the project MySQL container...
docker compose -f "%ROOT_DIR%\docker-compose-mysql.yml" up -d db
if errorlevel 1 (
  echo.
  echo Could not start MySQL with Docker. Start Docker Desktop, then run:
  echo   docker compose -f "%ROOT_DIR%\docker-compose-mysql.yml" up -d db
  echo After MySQL is healthy, rerun:
  echo   ..\tools\maven.bat spring-boot:run
  exit /b 1
)

echo Waiting for MySQL on 127.0.0.1:3305...
for /L %%I in (1,1,30) do (
  powershell -NoProfile -ExecutionPolicy Bypass -Command "if (Test-NetConnection 127.0.0.1 -Port 3305 -InformationLevel Quiet) { exit 0 } else { exit 1 }" >nul 2>nul
  if not errorlevel 1 (
    if not defined DATABASE_URL set "DATABASE_URL=jdbc:mysql://127.0.0.1:3305/shadownet?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true"
    if not defined DB_USERNAME set "DB_USERNAME=shadownet"
    if not defined DB_PASSWORD set "DB_PASSWORD=root"
    exit /b 0
  )
  timeout /t 2 /nobreak >nul
)

echo MySQL did not become available on 127.0.0.1:3305.
exit /b 1