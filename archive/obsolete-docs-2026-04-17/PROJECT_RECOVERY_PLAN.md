# ShadowNet Nexus Recovery Plan & Master Tracker

**Status: Executing approved plan to make fully playable.**

## Confirmed Working (Verified from file analysis)
| Feature | Details | Evidence |
|---------|---------|----------|
| Operators | 24 UI + 25 backend seed, backend select/persist | roster.ts, Operators.tsx, V20.sql, GameContext |
| CTF Solo | 12 challenges, session/hint/submit/learning | CTF.tsx, V23.sql, puzzleApi |
| Team Mode | create/join/ready/start/evidence/accuse/WS/chat | Team.tsx, shadownetApi |
| GameContext | Backend sync + story consequence apply | GameContext.tsx |
| API | shadownetApi canonical, contracts match | API_CONTRACT.md |
| Build | Vite clean, real framer-motion | vite.config.ts |

## Fixed/Partial
| Feature | Current | Target |
|---------|---------|--------|
| Story | Chapters→scene/1 stub | Full choices/consequences (Phase 2) |
| Missions | Cards→team stub runtime | Full session/actions (Phase 3) |
| Docs | Overlapping MDs | Consolidated here + README |

## Verification Matrix
| Test | Expected | Status |
|------|----------|--------|
| Solo | login→CTF solve→score | ✅ |
| Story | ch1→decision→trust/evid | 🔄 Phase 2 |
| Team | create→evidence→accuse | ✅ |
| Mission | brief→runtime→complete | 🔄 Phase 3 |
| Refresh | op/story persist | ✅ |

**Progress: TODO.md tracking. After all: Cypress + PR.**

