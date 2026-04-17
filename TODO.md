# TypeScript Error Fix Plan for ShadowNet-Nexus

## Information Gathered
- **Primary blocker fixed**: CTF.tsx syntax error (literal \\n in interface) resolved by full file rewrite.
- **Remaining issues**:
  - `Challenge` interface missing `stages?: unknown` property (used in CTF.tsx line 155).
  - `BackendMission` in shadownetApi.ts missing `meta?: Record<string, unknown>`.
  - Axios config 'body' used instead of 'data' in API files (apiClient converts at runtime, but TS errors).
  - Property access on `unknown` types in GameContext.tsx, MissionCellPanel.tsx (teamId, phase, etc. on API responses).
  - Login/Register: response.data on unknown.
  - OperatorStory: setTab arg type mismatch.
- **Files analyzed**: CTF.tsx, shadownetApi.ts, apiClient.ts, GameContext.tsx, challengeApi.ts, types/gameplay.ts.

## Plan
1. **Extend Challenge interface** in src/api/challengeApi.ts to include `stages?: unknown; attachments?: any[]; solved?: boolean;`.
2. **Fix BackendMission** in shadownetApi.ts: add `meta?: Record<string, unknown>;`.
3. **Fix Axios calls**: Replace `body:` with `data:` in shadownetApi.ts, puzzleApi.ts, teamApi.ts, etc. (18 instances).
4. **Type guards for unknown**: In GameContext.normalizeOperator, useTeamSession, etc., add type assertions or guards.
5. **Challenge normalize in CTF.tsx**: Use optional chaining `challenge?.stages ?? []`.
6. **Test fixes**: Run vite, check no build errors, fix remaining TS.

## Dependent Files
- src/api/challengeApi.ts
- src/api/shadownetApi.ts 
- src/lib/apiClient.ts (already handles body->data)
- src/pages/CTF.tsx (add optional chaining)
- src/context/GameContext.tsx
- src/components/MissionCellPanel.tsx
- src/pages/Login.tsx, Register.tsx

## Follow-up Steps
- execute_command "npm run dev" to test build.
- Install no deps needed.
- attempt_completion once all TS errors 0.

**Approve this plan to proceed?**
