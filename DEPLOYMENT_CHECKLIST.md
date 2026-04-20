# Deployment Checklist

Use this as the final release gate. Do not mark a release ready until every required item is checked.

## Repository Hygiene

- [ ] `git status` is clean.
- [ ] Release archive excludes `.git/`.
- [ ] Release archive excludes `node_modules/`, `dist/`, and `springboot/target/`.
- [ ] Release archive excludes logs, editor folders, local caches, and temporary files.
- [ ] `.gitignore` covers generated outputs and local-only tooling.

## Build And Test

- [ ] Fresh `npm ci` succeeds.
- [ ] `npm.cmd test` succeeds.
- [ ] `npm.cmd run build` succeeds.
- [ ] Backend `mvn test` succeeds.
- [ ] Backend package build succeeds.
- [ ] Docker Compose starts from a clean checkout.
- [ ] Flyway migrations apply to a fresh database.

## Security

- [ ] Challenge APIs expose DTOs only.
- [ ] No response leaks `flagHash`, stage validation fields, hidden answers, hints, explanations, or Docker image names.
- [ ] Team responses expose view DTOs only and do not leak `traitorId`.
- [ ] Auth, submit, hint, team, and container-spawn endpoints are rate-limited.
- [ ] Protected routes require valid JWTs.
- [ ] Team/session/story resources enforce ownership.
- [ ] Client-authored score, trust, mission progress, and story progress mutations are retired.

## Production Configuration

- [ ] `JWT_SECRET` is set and strong.
- [ ] Database credentials are provided by environment or secret manager.
- [ ] Production logging is not DEBUG-heavy.
- [ ] Actuator health details are restricted.
- [ ] Swagger/API docs are disabled unless intentionally exposed.
- [ ] CORS is configured centrally for the deployed frontend origins.
- [ ] Secure cookies are enabled behind HTTPS.

## Gameplay Verification

- [ ] Challenge list renders and contains no secret fields in network payloads.
- [ ] Puzzle session starts and contains no hidden answer fields.
- [ ] Wrong flag throttling works.
- [ ] Valid solve updates server-owned score.
- [ ] Training/coaching solves do not award ranked points.
- [ ] Story/operator flow works end-to-end.
- [ ] Mission runtime works end-to-end.
- [ ] Team mission flow works end-to-end.

## Sign-Off

- [ ] Developer sign-off.
- [ ] QA sign-off.
- [ ] Security review sign-off.
- [ ] Deployment owner sign-off.
