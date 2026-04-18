# Shadownet Nexus Deployment Guide

This project is currently verified with the Docker Compose stack in this repo: React frontend, Spring Boot backend, MySQL, and Redis.

## Local Verification

```powershell
cd C:\Users\zain\Downloads\shadownet-nexus\springboot
.\mvnw.cmd clean package

cd C:\Users\zain\Downloads\shadownet-nexus
npm run build
```

## Full Docker Deployment

```powershell
cd C:\Users\zain\Downloads\shadownet-nexus
docker compose up -d --build
```

Services:

- Frontend: `http://localhost:5173`
- Backend: `http://localhost:3001`
- MySQL: `127.0.0.1:3305`
- Redis: `127.0.0.1:6379`

Useful checks:

```powershell
docker compose ps
Invoke-WebRequest http://localhost:3001/actuator/health
Invoke-WebRequest http://localhost:3001/api/challenges
Invoke-WebRequest http://localhost:3001/api/operators
```

## Oracle Cloud Deployment

See `ORACLE_DEPLOY.md` for Oracle Cloud Always Free VM deployment.

## GitHub Repo

This folder is not currently a Git repository. To push it:

```powershell
git init
git add .
git commit -m "Initial clean deploy copy"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/shadownet-nexus-deploy.git
git push -u origin main
```

Review `.gitignore` before `git add .` so generated folders like `node_modules`, `dist`, `target`, database files, and logs are not committed.

## Optional Vercel Frontend

Vercel can host only the frontend. The backend must be deployed somewhere publicly reachable first.

1. Import the GitHub repo in Vercel.
2. Framework: Vite.
3. Build command: `npm run build`.
4. Output directory: `dist`.
5. Env: `VITE_API_URL=https://your-backend-domain/api`.

Do not use `http://localhost:3001/api` in Vercel; that only works on your own machine.

## Production Backend Note

The current backend migrations are MySQL migrations. Use a MySQL-compatible production database, or keep the provided Docker Compose deployment. Do not switch this app to Postgres without first porting the Flyway migrations.
