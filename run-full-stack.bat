@echo off
setlocal

if not exist ".env" (
  (
    echo JWT_SECRET=local-dev-jwt-secret-32-bytes-minimum-value
    echo EMAIL_ENCRYPTION_KEY=local-dev-email-key
    echo DB_PASSWORD=shadownet-dev-password
    echo MYSQL_ROOT_PASSWORD=root
  ) > .env
  echo Created local .env for Docker development.
)

docker compose up --build
