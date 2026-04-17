# Story Consequences

Verified on 2026-04-12.

## Implemented Behavior

- `POST /api/story/decision` is the canonical story consequence entrypoint.
- `StoryService.makeDecision` runs the consequence flow in one backend transaction.
- The transaction validates user, scene, choice, and current-scene transition.
- A chosen choice is saved into `StoryProgress.choicesMade`.
- Story progress advances to the choice `nextSceneId`, or to the next chapter first scene when the choice ends a chapter.
- Trust delta is calculated and applied server-side through `GameplayConsequenceService`.
- Evidence is persisted in `user_story_evidence` with a stable `evidenceCode`.
- Mission consequence state is persisted in `user_mission_state` through `GameplayConsequenceService` when an existing mission can be mapped.
- Duplicate same-choice submissions are idempotent: they return `duplicate_decision_ignored` and do not mutate trust, evidence, or mission state again.
- Re-submitting a different choice for an already decided scene is rejected.
- A selected operator can add interpretation metadata to the response.

## Decision Response Contract

`DecisionResponseDTO` now returns:

- `progress`
- `nextSceneId`
- `nextChapterId`
- `trustImpact` and `trustDelta`
- `updatedTrust`
- `targetEntity`
- `evidenceGained`
- `consequenceFlags`
- `unlockedMissionIds`
- `recommendedMissionIds`
- `missionChanges`
- `consequenceSummary`
- `operatorInterpretation`

## Persistent Models

- `user_story_evidence`: user-owned evidence discovered from story choices.
- `user_mission_state`: user-owned mission recommendation/unlock state produced by story consequences.

## Partial / Deferred

- There is not yet a dedicated chapter debrief endpoint.
- Evidence found/missed accounting is not yet complete.
- Mission recommendation/unlock is real persistence, but still uses a simple service-layer mapping against existing missions.
- The current UI stores the richer response in `GameContext`, but does not yet visibly render an evidence vault or debrief panel.
- `/api/trust/update` now returns `410 TRUST_UPDATE_DEPRECATED`; client-authored trust mutation is blocked.
- `/api/trust/accuse` now returns `410 TRUST_ACCUSE_DEPRECATED`; client-authored trust accusation is blocked.
- `/api/missions/{id}/progress` now returns `410 MISSION_PROGRESS_DEPRECATED`; client-authored mission progress mutation is blocked.
- `GET /api/story/chapters/{id}/debrief` returns found/missed evidence and trust outcome for a chapter.
