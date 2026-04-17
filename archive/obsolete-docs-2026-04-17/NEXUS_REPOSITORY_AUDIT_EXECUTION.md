# NEXUS Gameplay Website Code Audit

## Current Verdict

This is a real full-stack gameplay project, not a fake shell. The backend is materially stronger than the current player experience. The highest-value fixes are contract cleanup, state authority cleanup, image/repo hygiene, and better gameplay presentation of systems that already exist.

This audit is based on the current working tree, with stale findings corrected where the source has already improved.

## Verified Corrections Against Earlier Notes

- `npm.cmd run build` succeeds when run outside the sandbox. The first failure was PowerShell execution policy for `npm.ps1`, then sandbox `spawn EPERM` for esbuild. Treat build/run as shell/sandbox fragile, not currently source-broken.
- `vite.config.ts` no longer defines `VITE_API_BASE`. Frontend config currently uses `VITE_API_URL` and `VITE_WS_URL` in `src/lib/config.ts` and `src/lib/apiClient.ts`.
- `springboot/src/main/resources/application.properties` is already reduced to a consolidation note.
- `springboot/src/main/resources/application.yml` no longer has root/root database defaults and now requires `${JWT_SECRET}` without a blank fallback.
- `WebSocketConfig.java` no longer hardcodes `setAllowedOriginPatterns("*")`; it uses configured CORS origins.
- Current `src/pages/Team.tsx` does not contain a visible hardcoded `<option value="sable">Sable</option>`; it uses `accusationTargetPool` and falls back to `hidden-hand`.
- Real image files exist under `src/assets/images`; the remaining asset issue is naming/path hygiene, especially `villian` and filenames with spaces.

## P0: Repo Hygiene And Reproducibility

| Path | Status | Issue | Exact action |
| --- | --- | --- | --- |
| `node_modules/` | Safe delete from repo | Machine-local dependency install should not be source truth | Remove from versioned archive; reinstall with `npm install` or `npm ci` |
| `dist/` | Safe delete from repo | Build output is generated; `npm.cmd run build` can recreate it | Remove from source archive; regenerate in build pipeline |
| `logs/` | Safe delete from repo | Runtime logs pollute audits and searches | Remove from source archive |
| `.m2/`, `.mysql-data/`, `.mysql-run/` | Safe delete from repo | Machine/runtime data should not ship as source | Remove; use Maven restore and Docker/local MySQL |
| `apache-maven-3.9.9/` | Safe delete from repo | Bundled toolchain increases repo size and drift | Prefer Maven wrapper or installed Maven |
| `tmpCheck.js` | Safe delete | Temporary helper artifact | Remove if unreferenced |
| `src/pages/OperatorModal` | Safe delete | Empty/path artifact in page folder | Remove if no references exist |

## P0: State Authority And API Contract

| Path | Status | Issue | Exact action |
| --- | --- | --- | --- |
| `src/context/GameContext.tsx` | Rewrite carefully | Too much responsibility: bootstrap, reducer, backend restore, cache clearing, operator normalization, leaderboard/story/mission aggregation | Split into `useSessionBootstrap`, `gameReducer`, `operatorMapper`, and a tiny cache helper. Keep backend as authority, React Query as cache, localStorage only for tiny resume values |
| `src/context/AuthContext.tsx` | Keep with cleanup | Should be the only auth/session owner | Keep token/user/session here only |
| `src/api/userApi.ts` | Keep | Best canonical user API direction | Keep `/users/me` as canonical |
| `src/api/operatorApi.ts` | Rewrite | Operator selection and presentation mapping are coupled to roster assumptions | Use `/users/me` and `/users/me/operator` consistently; keep backend operator IDs canonical |
| `src/api/gameApi.ts` | Rewrite or retire | Still points to `/users/me/story-progress`, which overlaps newer story endpoints | Decide whether this legacy user story-progress path survives; otherwise retire it |
| `src/api/shadownetApi.ts` | Rewrite | Too many domains in one file; mission type/mode and team status are normalized with assumptions | Split mission/team/runtime APIs or at least normalize DTOs through explicit contracts |
| `springboot/.../UserController.java` | Rewrite contract | Supports `/user` and `/users/me`, plus old story-progress aliases | Keep aliases only for backward compatibility; document `/users/me` as canonical |

## P0: Gameplay Contract Bugs

| Path | Status | Issue | Exact action |
| --- | --- | --- | --- |
| `src/pages/Story.tsx` + `ChapterDTO.java` | Rewrite contract | Frontend reads `subtitle` and `synopsis`; backend DTO provides `description` | Either add `subtitle`/`synopsis` to backend DTO or make UI use `description` consistently |
| `src/pages/Missions.tsx` | Rewrite | `selectedMission.type !== 'team'` treats type as mode, while mission types are themes like `cyber_warfare` | Add separate `mode: solo | team` and `missionType` fields |
| `src/api/shadownetApi.ts` + `TeamSessionService.java` | Rewrite contract | Frontend checks uppercase `ACCUSATION_*` states while backend also uses lowercase `waiting`/`active` | Create one server enum: `WAITING`, `ACTIVE`, `ACCUSATION_UNLOCKED`, `ACCUSATION_RESOLVED`, `COMPLETED` |
| `src/pages/OperatorStory.tsx` | Rewrite or demote | Uses local procedural generation and does not call authoritative backend operator selection | Convert to backend-driven operator story, or mark/remove it from the canonical game path |
| `src/types/index.ts` + `src/types/gameplay.ts` | Rewrite | Parallel gameplay type definitions create conceptual drift | Make `src/types/gameplay.ts` the canonical gameplay type source and stop duplicating reduced shapes |

## P1: Gameplay Feel And Presentation

| Path | Status | Issue | Exact action |
| --- | --- | --- | --- |
| `src/pages/CTF.tsx` | Keep with focused rewrite | Real session flow, but still feels form-like | Make wrong answers show richer coaching, teaching mode, and campaign relevance |
| `src/pages/MissionRuntime.tsx` | Rewrite presentation | Backend runtime exists, but UI reads like a checklist | Reframe as phase milestones, evidence thresholds, time pressure, narrator beats, and consequence summary |
| `src/pages/StoryScene.tsx` | Keep with narrative upgrade | Mechanics exist, emotional labels are weak | Add scene tags and narrator moments after choices/trust shifts/clue reveals |
| `src/pages/Team.tsx` | Rewrite loop | Real-time skeleton exists, but evidence is mostly count-based | Add typed evidence, objective sync, synchronized mission clock, accusation prerequisites, and saved team outcomes |
| `src/components/Hud.tsx` | Keep and expand | High-leverage mode guidance surface | Use it for narrator cue, current mode, and next recommended step |
| `src/components/RecommendedSection.tsx` | Keep with verification | Needs to reflect real recommendation data | Verify it does not invent readiness/recommendations locally |

## P1: Operator And Asset Canon

| Path | Status | Issue | Exact action |
| --- | --- | --- | --- |
| `src/data/roster.ts` | Rewrite content pass | High-visibility canon file; naming/image/metadata consistency still matters | Clean IDs, `backendOperatorId`, skills, role labels, faction, image mapping, and corrupted strings |
| `src/assets/images/...` | Keep with rename plan | Assets exist, but folder/file names are fragile: `villian`, spaces in filenames | Normalize to `villain/` and kebab-case filenames in a coordinated import update |
| `public/images/operators/README.md` | Risky | Public image strategy competes with imported asset strategy | Choose one source of truth: imported assets or public URLs |
| `springboot/.../OperatorController.java` + `GameService.java` | Rewrite contract | Backend gameplay bonuses still need to align with full 24-operator canon | Seed/recognize final operator IDs or reduce frontend roster to actual backend mechanics |

## P1: Backend Security And Config

| Path | Status | Issue | Exact action |
| --- | --- | --- | --- |
| `springboot/src/main/resources/application.yml` | Keep with validation | Improved: no root/root fallback and no blank JWT fallback | Add startup validation and profile docs so missing secrets fail clearly |
| `springboot/src/main/resources/application.properties` | Keep | Already consolidated | Leave as a pointer or remove if Spring config precedence becomes confusing |
| `SecurityConfig.java` | Keep with review | Security structure is good; correctness depends on endpoint policy | Verify public endpoints are intentionally public |
| `WebSocketConfig.java` | Keep with test | Origins now come from config | Add test/manual check for exact allowed origins in dev/prod |
| `AuthService.java` | Keep with hardening | Auth flow still needs verified-email and reset-token strictness review | Enforce verification expectations and one-time expiring reset tokens |
| `RateLimitingFilter.java` | Keep with tests | Path matching can drift | Add tests for login/register/challenge/hint/team route buckets |

## P2: Tests And Proof

| Path | Status | Issue | Exact action |
| --- | --- | --- | --- |
| `cypress/e2e/team-mode.cy.ts` | Rewrite | Needs real browser fanout proof | Verify ready state, evidence, reconnect, accusation lock, and no hardcoded suspect across clients |
| `cypress/e2e/solo-mode.cy.ts` | Rewrite | Needs teaching-mode coverage | Assert narrator onboarding, coaching after wrong answer, repeated-miss teaching mode, refresh restore |
| `cypress/e2e/story-mode.cy.ts` | Rewrite | Needs dossier clarity and persistence checks | Assert dossier framing, operator-specific flow, and choice persistence |
| `cypress/e2e/operator-flow.cy.ts` | Rewrite | Needs POV meaning proof | Assert selection persists and POV-impact copy is visible |
| `src/test/storyProgressIntegrationFixed.test.ts` | Rewrite | Still targets `/api/users/me/story-progress` | Align with final story-progress endpoint decision |
| `src/test/example.test.ts` | Safe delete or rewrite | Scaffold noise if not meaningful | Replace with real regression test or remove |

## Best Fix Order

1. Clean repo artifacts: remove dependency/build/runtime junk from source control or release zip.
2. Lock env/config truth: `.env`, `.env.example`, `src/lib/config.ts`, `apiClient.ts`, Spring config docs.
3. Canonicalize API endpoints: prefer `/users/me`, decide fate of `/users/me/story-progress`.
4. Fix frontend/backend DTO mismatches: Story chapters, mission mode/type, team status enum.
5. Refactor `GameContext.tsx` into orchestration only.
6. Decide `OperatorStory.tsx`: backend-driven real path or non-canonical sandbox.
7. Align operator canon across roster, backend IDs, and gameplay bonuses.
8. Improve gameplay feel: CTF coaching, MissionRuntime drama, StoryScene labels, Team evidence depth.
9. Add proof: Cypress multi-client team fanout and restore tests.

## Build Note

`npm.cmd run build` completed successfully in this workspace after requesting unsandboxed execution. The output still warns about a large JS chunk, so code-splitting remains a performance task, but the current source is buildable.
