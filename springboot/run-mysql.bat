@echo off
REM Shadownet Nexus - MySQL Startup Script (MySQL Only)

echo Starting Shadownet Nexus Backend with MySQL...
echo DB URL: jdbc:mysql://127.0.0.1:3305/shadownet?createDatabaseIfNotExist=true^&useSSL=false^&allowPublicKeyRetrieval=true
echo Server: http://localhost:3001
echo.

java -Dspring.profiles.active=mysql -Dspring.datasource.url="jdbc:mysql://127.0.0.1:3305/shadownet?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true" -Dspring.datasource.username=root -Dspring.datasource.password=root -Djwt.secret=test -Dflyway.enabled=true -jar target/shadownet-nexus-1.0.0.jar

pause
