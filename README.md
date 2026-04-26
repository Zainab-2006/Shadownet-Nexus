# Shadownet Nexus

Narrative cybersecurity training / CTF platform with a React frontend and Spring Boot backend.

## Verified Runtime

- Frontend: Vite/React on `http://localhost:5173` in Docker Compose.
- Backend: Spring Boot on `http://localhost:3001`
- Database: MySQL, using Docker Compose on `127.0.0.1:3305` or native MySQL fallback on `127.0.0.1:3306`
- Maven helper: `tools\maven.bat`

## Deploy Full Stack

```powershell
cd C:\Users\zain\Downloads\shadownet-nexus
docker compose up -d --build
```

Open `http://localhost:5173`.

Useful checks:

```powershell
docker compose ps
Invoke-WebRequest http://localhost:3001/actuator/health
Invoke-WebRequest http://localhost:3001/api/challenges
```

## Deploy On Render

This repo now includes [render.yaml](/C:/Users/zain/Downloads/shadownet-nexus/render.yaml:1) for a three-service Render deployment:

- Static frontend: `shadownet-frontend`
- Spring Boot backend: `shadownet-backend`
- Private MySQL: `shadownet-mysql`

Important setup notes:

- Render should create the frontend and backend from this repo automatically via Blueprint sync.
- The MySQL service uses a persistent disk and must not be deployed on the free plan. The Blueprint sets it to `starter`.
- Do not commit secrets. Set `DB_PASSWORD`, `DATABASE_PASSWORD`, `MYSQL_PASSWORD`, and `MYSQL_ROOT_PASSWORD` in Render only.
- After Render creates the frontend URL, update the backend `CORS_ORIGINS` value if you rename the frontend service or attach a custom domain.

Recommended Render flow:

1. Push this repo to GitHub.
2. In Render, choose `New +` -> `Blueprint`.
3. Select this repository and confirm `render.yaml`.
4. Before the first deploy completes, set the secret values for the MySQL and backend password variables in the Render dashboard.
5. After deploy, open the backend health endpoint and frontend site.

Minimal post-deploy checks:

- Backend: `https://<your-backend>/actuator/health`
- Backend API: `https://<your-backend>/api/challenges`
- Frontend loads and can reach the backend without CORS errors.

## Run Backend

```powershell
cd C:\Users\zain\Downloads\shadownet-nexus\springboot
..\tools\maven.bat spring-boot:run
```

The helper checks MySQL on `3305`, falls back to native MySQL on `3306`, and uses the repo-local Maven cache.

## Run Frontend

```powershell
npm.cmd run dev
```

## Verification

```powershell
npx.cmd tsc --noEmit
npm.cmd test
npm.cmd run build
cd springboot
..\tools\maven.bat test
..\tools\maven.bat -DskipTests package
```

In this environment, Vite/Vitest and Maven tests may need to run outside the sandbox because esbuild and javac dependency-JAR access can be blocked.

Fresh DB proof was run against disposable native-MySQL schema `shadownet_fresh_v19_codex`; Flyway applied through `v19`, and the packaged app booted against that schema.

## Current Gameplay Truth

- Player-facing modes are `Solo`, `Missions`, and `Story`.
- `/story/operator/:id` is the canonical operator narrative route. `/operator/:id` is compatibility redirect only.
- Mission cell/team mechanics are runtime infrastructure behind Missions, not a separate player mode.
- Backend owns score, XP, solves, operator selection, puzzle sessions, and story progress.
- Solo training submit metadata is backend-enforced: ranked points are awarded only when the answer is correct, `trainingMode=false`, `rankedEligible=true`, `solutionRevealed=false`, and no narrator-triggered training path was used.
- Story decisions now produce backend-authored consequences: trust delta, persistent evidence, mission recommendation/unlock state, summary payload, and operator interpretation metadata.
- Client-authored trust and mission mutation routes are retired: `/api/trust/update` and `/api/missions/{id}/progress` return `410`.
- Team evidence and accusation consequences now route through backend consequence services; team create/join/ready/start state, explicit leader authority, mission association, readable member DTOs, activity log, and per-team update broadcasts are persisted.
- `/api/puzzle/*` is the canonical solo CTF session route family.
- Legacy `/api/puzzle-session/*` routes have been removed; use `/api/puzzle/*`.
- Team and mission systems are backend-backed and tested at service level. Team backend lifecycle also has a live two-user REST smoke proof. Browser-level multi-user WebSocket/Cypress validation remains the remaining proof gap.

## Maintained Documentation

The maintained project docs are:

- `API_CONTRACT.md`
- `DEPLOYMENT_TESTING.md`
- `DEPLOYMENT_CHECKLIST.md`
- `SECURITY_INDEX.md`
- `SECURITY_DEVELOPER_GUIDE.md`
- `SECURITY_IMPLEMENTATION.md`
- `GAMEPLAY_STATE_MODEL.md`
- `OPERATOR_SYSTEM.md`
- `STORY_CONSEQUENCES.md`

Historical planning/setup notes are archived in `archive/obsolete-docs-2026-04-17/`.
