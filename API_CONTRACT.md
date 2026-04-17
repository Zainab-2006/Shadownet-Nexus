# Shadownet Nexus API Contract

Verified against code on 2026-04-12. Backend base URL: `http://localhost:3001/api`. Frontend callers must use `apiFetch` for response data or unwrap Axios responses explicitly.

## Contract Status

| Domain | Route | Method | Request | Response | Backend truth | Frontend caller | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Auth | `/api/register` | POST | `{ email, username, password }` | auth token payload | `AuthController.register` | `shadownetApi.useRegister`, `AuthContext` | exact |
| Auth | `/api/login` | POST | `{ email, password }` | auth token payload | `AuthController.login` | `shadownetApi.useLogin`, `AuthContext` | exact |
| User | `/api/users/me`, `/api/user` | GET | none | `{ id, username, displayName, email, score, xp, level, selectedOperator }` | `UserController.getUser` | `GameContext`, `userApi`, `AuthContext` | exact |
| User progression | `/api/users/me/progress` | GET | none | `{ userId, totalXp, currentLevel, totalPoints, rankPoints, challengesSolved, missionsCompleted, storyProgressPercent, solvedChallengeIds }` | `UserController.getUserProgress` | `GameContext`, `userApi` | exact |
| Selected operator | `/api/users/me/operator` | GET | none | selected operator DTO or `{ selectedOperator: null }` | `UserController.getSelectedOperator` | currently indirect through `/user` and `/operators` | partial |
| Operators | `/api/operators` | GET | none | `OperatorDto[]` with `id,name,role,abilities,unlockCost,backstory,unlocked,selected,portraitUrl,fullImageUrl` | `OperatorController.getOperators` | `operatorApi`, `GameContext` | exact |
| Operator selection | `/api/operators/select` | POST | `{ operatorId: string }` | `{ success: true, selectedOperator }` or error | `OperatorController.selectOperator` | `operatorApi.useSelectOperator`, `GameContext.selectOperator` | exact |
| Operator consequence | `/api/operators/{operatorId}/consequence` | POST | `{ missionId?, choiceId?, outcome?, action? }` | `{ operatorId, missionId, choiceId, outcome, trustDelta, updatedTrust, targetEntity, missionChanges, consequenceFlags, consequenceSummary }` | `OperatorController.applyOperatorConsequence`, `GameplayConsequenceService` | `OperatorStory`, `operatorApi.useApplyOperatorConsequence` | backend-authored |
| Challenges | `/api/challenges` | GET | none | `Challenge[]` | `ChallengeController.getChallenges` | `CTF`, `challengeApi` | partial: raw entity returned |
| Challenge submit | `/api/submit-flag` | POST | `{ challengeId: string, flag: string }` | `{ success, message?, points?, error? }` | `ChallengeController.submitFlag`, `GameService` | `challengeApi`, legacy `gameApi` | exact |
| Recommended challenges | `/api/challenges/recommended` | GET | none | `Challenge[]` | `RecommendedController.getRecommendedChallenges` | `RecommendedSection` | exact after 2026-04-12 fix |
| Puzzle session canonical | `/api/puzzle/session/{challengeId}` | GET | path `challengeId` | `{ id, currentStage, hintsUsed, challenge, completed }` | `ChallengeController.getPuzzleSession`, `CoachingService` | `puzzleApi.usePuzzleSession`, `CTF` | canonical |
| Puzzle hint canonical | `/api/puzzle/hint` | POST | `{ sessionId }` | `{ content, personalized, remainingHints }` | `ChallengeController.getPuzzleHint`, `CoachingService` | `puzzleApi.useGetHint`, `CTF` | canonical |
| Puzzle stage submit canonical | `/api/puzzle/submit` | POST | `{ sessionId, stageNumber, flag }` | `{ correct, message, nextStage?, showExplanation?, awardedPoints?, duplicate?, stale? }` | `ChallengeController.submitPuzzleStage`, `CoachingService` | `puzzleApi.useSubmitStage`, `CTF` | canonical |
| Puzzle legacy | `/api/puzzle-session/**` | GET/POST | legacy equivalents | canonical-compatible response plus deprecation metadata on POST session | `PuzzleSessionController` | none expected | deprecated compatibility |
| Story chapters | `/api/story/chapters` | GET | none | `ChapterDTO[]` | `StoryController.getChapters`, `StoryService` | `storyApi`, `Story` | exact |
| Story scene | `/api/story/scenes/{id}` | GET | path `id` | `SceneDTO` | `StoryController.getScene`, `StoryService` | `storyApi`, `Story` | exact |
| Story first scene | `/api/story/chapters/{id}/first-scene` | GET | path `id` | `SceneDTO` | `StoryController.getFirstScene`, `StoryService` | `storyApi`, `Story` | exact |
| Story progress | `/api/story/progress` | GET | none | `StoryProgressDTO` | `StoryController.getProgress`, `StoryService` | `storyApi`, `Story` | exact |
| Story decision | `/api/story/decision` | POST | `{ sceneId: number, choiceId: number }` | `{ progress,nextSceneId,nextChapterId,trustImpact,trustDelta,updatedTrust,targetEntity,evidenceGained,consequenceFlags,unlockedMissionIds,recommendedMissionIds,missionChanges,consequenceSummary,operatorInterpretation }` | `StoryController.makeDecision`, `StoryService`, `GameplayConsequenceService` | `storyApi`, `GameContext` latent state | exact for backend-authored consequence transaction; UI rendering remains partial |
| Chapter debrief | `/api/story/chapters/{id}/debrief` | GET | path `id` | `{ chapterId, playerConclusion, evidenceFound, evidenceMissed, trustOutcome, nextOperationalRisk }` | `StoryController.getChapterDebrief`, `StoryService` | latent/future UI | backend-authored; UI rendering deferred |
| Legacy story blob | `/api/user/story-progress` | GET/PUT | `{ storyProgress }` on PUT | `{ storyProgress }` | `UserController` | legacy `gameApi`, live integration test | legacy |
| Missions | `/api/missions` | GET | none | `Mission[]` | `MissionController.getMissions` | `shadownetApi.useMissions`, `Missions` | partial: raw entity normalized in frontend |
| Mission progress legacy | `/api/missions/{id}/progress` | POST | any | `410 MISSION_PROGRESS_DEPRECATED` | `MissionController.updateMissionProgress` | none expected | retired; client-authored mutation blocked |
| Mission state | `/api/missions/progress` | GET | none | `UserMissionStateDTO[]` | `MissionController.getMissionProgress`, `GameplayConsequenceService` | future mission UI | backend-authored |
| Mission action | `/api/missions/{id}/action` | POST | `{ action: "RECOMMEND" | "START" | "UNLOCK" | "COMPLETE" }` | `MissionConsequenceDTO` | `MissionController.applyMissionAction`, `GameplayConsequenceService` | future mission UI | backend-authored |
| Team create | `/api/team/create` | POST | `{ missionId? }` | `TeamSession` with `missionId`, `leaderId`, `readyMap`, `evidenceMap`, `activityLog` | `TeamController.createTeam`, `TeamSessionService` | `shadownetApi.useCreateTeam`, `teamApi`, `Missions` | backend-authored |
| Team session | `/api/team/{teamId}` | GET | path `teamId` | raw `TeamSession` | `TeamController.getTeamSession` | `shadownetApi.useTeamSession`, `Team` | membership-checked reconnect/resume read |
| Team join | `/api/team/join` | POST | `{ teamId }` | `TeamSession` | `TeamController.joinTeam`, `TeamSessionService` | `shadownetApi.useJoinTeam`, `teamApi`, `Team` | persists membership and ready state |
| Team ready | `/api/team/{teamId}/ready` | POST | `{ ready }` | `TeamSession` | `TeamController.toggleReady`, `TeamSessionService` | `shadownetApi.useToggleTeamReady`, `teamApi` | persists per-user ready state |
| Team start | `/api/team/{teamId}/start` | POST | none | `TeamSession` | `TeamController.start`, `TeamSessionService` | `Team` | explicit `leaderId` only, all-ready validation |
| Team evidence | `/api/team/{teamId}/evidence` | POST | `{ evidenceType }` | `TeamSession` | `TeamController.addEvidence`, `GameplayConsequenceService` | `shadownetApi.useAddTeamEvidence` | backend-authored team consequence |
| Team accusation | `/api/team/{teamId}/accuse` | POST | `{ accusedId }` | `TeamSession` | `TeamController.accuse`, `GameplayConsequenceService` | `shadownetApi.useAccuseTeam`, `Team` | backend-authored team consequence |
| Team realtime updates | `/topic/team/{teamId}` | STOMP subscribe | n/a | `{ type, teamId, status?, evidenceCount?, data?, timestamp }` | `TeamController`, `TeamRealtimeController` | `useWebSocket`, `Team` | per-team mutation broadcast + chat relay |
| Trust update legacy | `/api/trust/update` | POST | any | `410 TRUST_UPDATE_DEPRECATED` | `TrustController.updateTrust` | none expected | retired; client-authored mutation blocked |
| Trust accuse legacy | `/api/trust/accuse` | POST | any | `410 TRUST_ACCUSE_DEPRECATED` | `TrustController.accuse` | none expected | retired; client-authored mutation blocked |
| Leaderboard | `/api/leaderboard` | GET | none | `{ userId, displayName, score }[]` | `LeaderboardController.getLeaderboard` | `leaderboardApi`, `GameContext` | partial: shape differs from `LeaderboardEntry` type |

## Canonical Decisions

- Canonical solo CTF session route family is `/api/puzzle/*`.
- `/api/puzzle-session/*` remains only as deprecated compatibility and should not be used by new frontend code.
- Score and challenge solve state are backend-authoritative through `/api/submit-flag` and `/api/puzzle/submit`.
- Story and operator consequences are backend-authoritative through `GameplayConsequenceService`; frontend must consume returned `updatedTrust`, evidence, and mission arrays instead of calculating them locally.
- `GameContext` may cache state in `localStorage`, but cache is not the source of truth when the backend is reachable.
- Team routes now persist create/join/ready/start/evidence/accuse state and broadcast shared updates; remaining team work is live multi-user reconnect/E2E validation rather than basic backend lifecycle wiring.
