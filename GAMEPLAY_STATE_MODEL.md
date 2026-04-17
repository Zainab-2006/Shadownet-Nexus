# Gameplay State Model

The intended runtime rule is backend-first, cache-second.

## Backend Truth

- User identity, score, XP, level: `/api/users/me`, `/api/users/me/progress`
- Selected operator: `/api/operators/select`, `/api/operators`, `/api/users/me`
- Solved challenges: `solves` table exposed through `/api/users/me/progress`
- Active solo puzzle session: `puzzle_sessions` via `/api/puzzle/session/{challengeId}`
- Story progress: `story_progress` via `/api/story/progress` and `/api/story/decision`
- Story evidence: `user_story_evidence` through `/api/story/decision`
- Story-driven mission state: `user_mission_state` through `/api/story/decision`
- Trust: `trust_relationship` through `GameplayConsequenceService` and domain services; legacy client-authored trust update is blocked
- Leaderboard: `/api/leaderboard`

## Frontend Runtime Cache

`GameContext` stores a reload/degraded cache in:

- `shadownet_cache_user`
- `shadownet_cache_operator`
- `shadownet_cache_progression`
- `shadownet_cache_gameplay`

Cache is allowed for reload recovery and degraded mode only. It must be reconciled through `refreshUserData()` or `refreshProgression()` when backend access is available.

`GameContext` can also store latent story consequence payloads returned from the backend:

- `storyEvidence`
- `unlockedMissions`
- `recommendedMissions`
- `storyConsequenceFlags`
- `missionProgress.storyConsequenceSummary`
- `missionProgress.storyMissionChanges`
- `missionProgress.operatorInterpretation`

These fields are not frontend-authored consequence truth. They are a cache of `/api/story/decision` results.

## Consequence Authority

`GameplayConsequenceService` is the backend mutation authority for:

- story decision trust/evidence/mission consequences
- operator action trust/mission consequences
- backend-authored mission action state

Legacy client-authored mutation routes are blocked:

- `POST /api/trust/update` returns `410 TRUST_UPDATE_DEPRECATED`
- `POST /api/missions/{id}/progress` returns `410 MISSION_PROGRESS_DEPRECATED`

## Sync Modes

`GameContext.syncMode` can be:

- `initializing`
- `recoveringFromCache`
- `refreshingFromBackend`
- `ready`
- `degraded`
- `authExpired`
- `syncError`

Current implementation sets the main modes, but UI-specific degraded-state rendering is still minimal.
