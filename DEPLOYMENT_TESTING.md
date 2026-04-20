# Deployment Testing

This checklist verifies that a release candidate can be built, started, and inspected from source without relying on generated artifacts from a developer workstation.

## Prerequisites

- Node.js and npm matching the supported frontend toolchain.
- Java 17.
- Docker Desktop when using Docker Compose.
- MySQL when running the backend outside Compose.
- Required environment variables documented in `.env.example` and `springboot/.env.example`.

## Clean Frontend Verification

Run from the repository root:

```powershell
npm ci
npm.cmd test
npm.cmd run build
```

The release bundle must not include `node_modules/` or `dist/`. They should be recreated by the commands above.

## Backend Verification

Run from `springboot/`:

```powershell
.\mvnw.cmd test
.\mvnw.cmd -DskipTests package
```

The release bundle must not include `springboot/target/`. It should be recreated by Maven.

## Docker Compose Verification

Run from the repository root:

```powershell
docker compose up -d --build
docker compose ps
Invoke-WebRequest http://localhost:3001/actuator/health
Invoke-WebRequest http://localhost:3001/api/challenges
```

Confirm Flyway migrations complete on a fresh database and the frontend opens at `http://localhost:5173`.

## Security Smoke Checks

- `GET /api/challenges` does not contain `flagHash`, raw `stages`, `hints`, `explanation`, or `dockerImage`.
- `GET /api/puzzle/session/{challengeId}` does not contain stage `flagHash`, `answer`, `solution`, or `finalAnswer`.
- Repeated wrong submissions against `/api/submit-flag` and `/api/puzzle/submit` are throttled.
- `/api/puzzle-session/*` routes are not available.
- Team endpoints do not expose `traitorId`, raw evidence JSON, or ready JSON.

## Manual Gameplay Smoke Checks

- Challenge list renders for an ordinary user.
- Solo puzzle session starts, accepts wrong answers, and preserves non-ranked training behavior.
- A valid challenge completion updates server-owned score/progression.
- Story decisions persist backend-authored consequences.
- Mission runtime starts, updates objectives, and completes only when objectives are done.
- Team create, join, ready, start, evidence, and accusation flows return view DTOs only.
