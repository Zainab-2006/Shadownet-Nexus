import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { apiFetch, apiPost } from '../lib/apiClient';

import type { Operator } from '@/types/operator';
import { roster } from '@/data/roster';

export interface SelectOperatorRequest {
  operatorId: string | number;
}

export interface TrustUpdateRequest {
  targetUserId: number;
  delta: number;
}

export interface MissionProgressRequest {
  choiceId: string;
  outcome: string;
  trustDelta: number;
}

export interface OperatorConsequenceRequest {
  operatorId: string;
  missionId?: string;
  choiceId?: string;
  outcome?: string;
  action?: string;
}

export interface OperatorConsequenceResponse {
  operatorId: string;
  missionId?: string;
  choiceId?: string;
  outcome?: string;
  trustDelta: number;
  updatedTrust: number;
  targetEntity: string;
  missionChanges?: Array<{
    missionId: string;
    state: string;
    reason?: string;
    newlyChanged?: boolean;
  }>;
  consequenceFlags?: string[];
  consequenceSummary?: {
    summary?: string;
    playerConclusion?: string;
    nextOperationalRisk?: string;
  };
}

interface BackendOperatorDto {
  id: string;
  name?: string;
  role?: string;
  abilities?: string;
  unlockCost?: number;
  backstory?: string;
  unlocked?: boolean;
  selected?: boolean;
  portraitUrl?: string;
  fullImageUrl?: string;
}

const DEFAULT_STATS = {
  attack: 60,
  defense: 60,
  speed: 60,
  tech: 60,
} as const;

const LEGACY_OPERATOR_ALIASES: Record<string, string> = {
  op_analyst: 'op_elara-voss',
  op_field: 'op_marcus-webb',
  op_hacker: 'op_ciphershade',
};

const canonicalOperatorId = (id?: string) => LEGACY_OPERATOR_ALIASES[id ?? ''] || id || '';

const rosterPresentationByBackendId = new Map(
  roster.map((character) => [character.backendOperatorId || 'op_' + character.id, character])
);

const rosterPresentationByName = new Map(
  roster.map((character) => [character.name.toLowerCase(), character])
);

const normalizeSkills = (abilities?: string): string[] => {
  if (!abilities) return [];

  try {
    const parsed = JSON.parse(abilities);
    if (Array.isArray(parsed)) {
      return parsed.map((skill) => String(skill).trim()).filter(Boolean);
    }
  } catch {
    // Parse error, continue with string parsing
  }

  return abilities
    .replaceAll('[', '')
    .replaceAll(']', '')
    .replaceAll('"', '')
    .split(/[,|]/)
    .map((skill) => skill.trim())
    .filter(Boolean);
};

const inferAlignment = (role: string): 'hero' | 'villain' => {
  const value = role.toLowerCase();
  return value.includes('rogue') || value.includes('shadow') || value.includes('hostile')
    ? 'villain'
    : 'hero';
};

const resolvePresentation = (dto: BackendOperatorDto) =>
  rosterPresentationByBackendId.get(canonicalOperatorId(dto.id)) ||
  rosterPresentationByBackendId.get(dto.id) ||
  rosterPresentationByName.get((dto.name || '').toLowerCase()) ||
  null;

const inferTier = (unlockCost: number): string => {
  if (unlockCost >= 2000) return 'boss';
  if (unlockCost >= 1000) return 'elite';
  if (unlockCost >= 500) return 'operative';
  return 'support';
};

const resolveImageUrl = (dto: BackendOperatorDto): string | undefined => {
  const presentation = resolvePresentation(dto);
  const backendUrl = dto.portraitUrl?.trim();

  if (presentation?.image) return presentation.image;
  if (!backendUrl) return undefined;
  if (backendUrl.startsWith('http://') || backendUrl.startsWith('https://')) return backendUrl;
  return backendUrl;
};

const toOperator = (dto: BackendOperatorDto): Operator => {
  const presentation = resolvePresentation(dto);
  const name = dto.name?.trim() || presentation?.name || 'Unknown Operator';
  const codename = presentation?.codename || name;
  const role = dto.role?.trim() || presentation?.role || 'Operative';
  const skills = normalizeSkills(dto.abilities);
  const displaySkills = skills.length > 0 ? skills : presentation?.skills.map((skill) => skill.name) ?? [];
  const unlockCost = dto.unlockCost ?? 0;
  const bio = presentation?.background || dto.backstory?.trim() || 'No briefing available yet.';
  const imageUrl = resolveImageUrl(dto);
  const alignment = presentation?.faction || inferAlignment(role);
  const id = canonicalOperatorId(dto.id);

  return {
    id,
    name,
    codename,
    role,
    faction: presentation?.faction === 'hero' ? 'Heroes' : presentation?.faction === 'villain' ? 'Villains' : 'Shadow Network',
    tier: inferTier(unlockCost),
    alignment,
    specialty: displaySkills[0] || role,
    specialization: displaySkills[0] || role,
    personality: 'Focused and adaptive under pressure.',
    visualTheme: alignment === 'hero' ? 'primary' : 'secondary',
    abilityType: displaySkills[0] || role,
    abilityValue: unlockCost,
    unlockCost,
    unlocked: dto.unlocked ?? false,
    selected: dto.selected ?? false,
    portraitUrl: imageUrl,
    fullImageUrl: imageUrl,
    bio,
    storyline: bio,
    skills: displaySkills,
    stats: { ...DEFAULT_STATS },
    trust: 50,
  };
};

export const useOperators = () => {
  return useQuery<Operator[]>({
    queryKey: ['operators'],
    queryFn: async () => {
      const operators = await apiFetch<BackendOperatorDto[]>('/operators');
      return operators.map(toOperator);
    },
  });
};

export const useCharacters = useOperators;

export const useSelectedOperator = () => {
  return useQuery<Operator | null>({
    queryKey: ['selectedOperator'],
    queryFn: async () => {
      const user = await apiFetch<unknown>('/users/me');
      if (!user.selectedOperator) return null;
      const operators = await apiFetch<BackendOperatorDto[]>('/operators');
      return operators.map(toOperator).find((op) => op.id === user.selectedOperator) || null;
    },
    retry: false,
    enabled: !!localStorage.getItem('token'),
  });
};

export const useSelectOperator = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (operatorId: string | number) => apiPost('/operators/select', { operatorId: String(operatorId) }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['selectedOperator'] });
      queryClient.invalidateQueries({ queryKey: ['user'] });
      queryClient.invalidateQueries({ queryKey: ['operators'] });
      window.dispatchEvent(new CustomEvent('operator:changed'));
    },
  });
};

export const useUpdateTrust = () => {
  return useMutation({
    mutationFn: async () => {
      throw new Error('Client-authored trust mutation is retired. Use backend-authored consequence endpoints.');
    },
  });
};

export const useUpdateMissionProgress = () => {
  return useMutation({
    mutationFn: async () => {
      throw new Error('Client-authored mission progress mutation is retired. Use backend-authored consequence endpoints.');
    },
  });
};

export const useApplyOperatorConsequence = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ operatorId, missionId, choiceId, outcome, action }: OperatorConsequenceRequest) =>
      apiPost<OperatorConsequenceResponse>(`/operators/${operatorId}/consequence`, {
        missionId,
        choiceId,
        outcome,
        action,
      }).then((response) => response.data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['missions'] });
      queryClient.invalidateQueries({ queryKey: ['trust'] });
      queryClient.invalidateQueries({ queryKey: ['operators'] });
    },
  });
};

export const useAccuseOperator = () => {
  return useMutation({
    mutationFn: async () => {
      throw new Error('Client-authored trust accusation is retired. Use team consequence endpoints.');
    },
  });
};
