# Test Matrix - ShadowNet Playable Verification

| ID | Feature Path | Steps | Backend Routes | Expected | Status | Evidence |
|----|--------------|-------|----------------|----------|--------|----------|
| S1 | Solo CTF | 1. Login 2. Op select 3. CTF start 4. Hint 5. Submit flag 6. Score update | /user /operators/select /puzzle/session /puzzle/hint /puzzle/submit | Solve→score+learn | ✅ Code | CTF.tsx |
| ST1 | Story | 1. /story 2. Start Ch1 3. /scene/1 4. Choice 5. Trust/evid 6. Next scene/mission | /story/chapters /story/scenes/1 /story/decision | Branch+conseq | ✅ V24+StoryScene | StoryScene.tsx |
| M1 | Mission | 1. /missions 2. Brief 3. Runtime 4. Obj toggle 5. Team sync | /missions /missions/{id}/action /team | Timer+complete | ✅ MissionRuntime | MissionRuntime.tsx |
| T1 | Team | 1. Create 2. Join 3. Ready 4. Evidence 5. Accuse | /team/create /team/ready /team/evidence /team/accuse | Persist+broadcast | ✅ Team.tsx | shadownetApi |
| P1 | Persist | Refresh mid-story/team | /user/progress | State restore | ✅ GameContext | localStorage+backend |
| B1 | Build | npm run build/test | Vite | No errors | Manual |  |

**Run:** Backend (flyway V24), frontend dev. All paths playable.
**Next:** Cypress coverage → PR.

