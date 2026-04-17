# Operator System

## Current Verified Architecture

Frontend owns presentation metadata:

- roster images
- hero/villain grouping
- dossier copy
- visual tags
- local character-to-backend operator mapping

Backend owns authoritative gameplay selection:

- operator ID
- unlock cost
- selected state
- persisted `User.selectedOperator`
- persisted `UserOperator.selected`

## Verified Runtime Effects

- Selecting a mapped roster character calls `/api/operators/select`.
- Backend persists selected operator and exposes it through user/operator endpoints.
- `GameService.applyOperatorBonus` currently applies small score modifiers for `op_hacker`, `op_analyst`, and `op_field`.

## Partial / Deferred

These are not complete yet:

- operator-specific story lens
- evidence interpretation bias
- mission recommendation influence
- debrief wording influence
- trust bias for decisions
- backend-authored gameplay profile fields beyond `abilities`, `role`, and `unlockCost`

The UI must remain unchanged while these are added.
