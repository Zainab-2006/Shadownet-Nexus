import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { apiFetch } from '../lib/apiClient';
import type { User } from './userApi';
import { prepareMutationContext } from './queryUtils';
import { parseJsonArray, parseJsonObject } from '@/utils/safeJson';

interface BackendMission {
  id: string;
  title?: string;
  missionType?: string;
  difficulty?: string;
  story?: string;
  meta?: Record<string, unknown>;

  objectives?: string | string[];
  timeLimitSeconds?: number;
  xpReward?: number;
  createdAt?: number;
}

export interface MissionRuntimeObjective {
  id: string;
  title: string;
  complete: boolean;
}

export interface MissionRuntimeState {
  id: string;
  missionId: string;
  status: 'active' | 'completed' | 'failed' | string;
  objectives: MissionRuntimeObjective[];
  timeLimitSeconds: number;
  timeRemaining: number;
  evidenceCount: number;
  startedAt?: number;
  endedAt?: number;
  completedObjectives: number;
  missionConsequence?: unknown;
}

type MissionAction = 'START' | 'UNLOCK' | 'COMPLETE' | 'RECOMMEND';

const normalizeMission = (mission: BackendMission) => {
  const meta = parseJsonObject(mission.meta);
  const difficulty = mission.difficulty || 'medium';
  const type = mission.missionType || 'cyber_warfare';
  const parsedObjectives = parseJsonArray(mission.objectives);
  const objectives = parsedObjectives.length > 0
    ? parsedObjectives
    : Array.isArray(meta.objectives)
      ? meta.objectives.flat().map(String)
      : [mission.story || mission.title || 'Complete the mission briefing.'];

  return {
    id: mission.id,
    name: mission.title || 'Untitled Mission',
    title: mission.title || 'Untitled Mission',
    type,
    missionType: type,
    difficulty,
    description: mission.story || meta.description || 'Mission briefing pending.',
    story: mission.story || '',
    objectives,
    timeLimitSeconds: mission.timeLimitSeconds || (typeof meta.timeLimitSeconds === 'number' ? meta.timeLimitSeconds : 3600),
    xpReward: mission.xpReward || (typeof meta.xpReward === 'number' ? meta.xpReward : 500),
    completed: Boolean(meta.completed),
    createdAt: mission.createdAt,
  };
};

export const useUser = () => useQuery({
  queryKey: ['user'],
  queryFn: () => apiFetch<User>('/users/me'),
});

export const useMissions = () => useQuery({
  queryKey: ['missions'],
  queryFn: () => apiFetch<BackendMission[]>('/missions').then((missions) => missions.map(normalizeMission)),
});

export const useMission = (missionId?: string) => useQuery({
  queryKey: ['mission', missionId],
  queryFn: () => apiFetch<BackendMission>(`/missions/${missionId}`).then(normalizeMission),
  enabled: !!missionId,
});

export const useMissionRuntime = (missionId?: string) => useQuery({
  queryKey: ['missionRuntime', missionId],
  queryFn: () => apiFetch<MissionRuntimeState>(`/missions/${missionId}/runtime`),
  enabled: !!missionId,
  refetchInterval: (query) => query.state.data?.status === 'active' ? 10000 : false,
});

export const useStartMissionRuntime = (missionId?: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationKey: ['startMissionRuntime', missionId],
    mutationFn: () => apiFetch<MissionRuntimeState>(`/missions/${missionId}/runtime/start`, { method: 'POST' }),
    onMutate: () => prepareMutationContext(queryClient, ['startMissionRuntime', missionId], 'startMissionRuntime'),
  });
};

export const useUpdateMissionObjective = (missionId?: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationKey: ['updateMissionObjective', missionId],
    mutationFn: ({ objectiveId, complete }: { objectiveId: string; complete: boolean }) =>
      apiFetch<MissionRuntimeState>(`/missions/${missionId}/runtime/objective`, {
        method: 'POST',
        data: JSON.stringify({ objectiveId, complete }),
      }),
    onMutate: () => prepareMutationContext(queryClient, ['updateMissionObjective', missionId], 'updateMissionObjective'),
  });
};

export const useCompleteMissionRuntime = (missionId?: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationKey: ['completeMissionRuntime', missionId],
    mutationFn: () => apiFetch<MissionRuntimeState>(`/missions/${missionId}/runtime/complete`, { method: 'POST' }),
    onMutate: () => prepareMutationContext(queryClient, ['completeMissionRuntime', missionId], 'completeMissionRuntime'),
  });
};

export const useApplyMissionAction = (missionId?: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationKey: ['applyMissionAction', missionId],
    mutationFn: (action: MissionAction) =>
      apiFetch(`/missions/${missionId}/action`, {
        method: 'POST',
        data: JSON.stringify({ action }),
      }),
    onMutate: () => prepareMutationContext(queryClient, ['applyMissionAction', missionId], 'applyMissionAction'),
  });
};

interface TeamSessionView {
  sessionId?: string;
  teamId?: string;
  missionId?: string;
  phase?: string;
  status?: string;
  evidenceCount?: number;
  evidence?: unknown[];
  trust?: Record<string, number>;
  accusation?: string;
  activity?: unknown[];
  accusationUnlocked?: boolean;
  accusationResult?: unknown;
}

const mapPhase = (status: string): string => {
  switch (status) {
    case "ACCUSATION_RESOLVED":
    case "ACCUSATION_UNLOCKED":
      return 'accusation';
    case "active":
      return 'active';
    default:
      return 'lobby';
  }
};

export const useTeamSession = (teamId: string) => useQuery({
  queryKey: ['teamSession', teamId],
  queryFn: () =>
    Promise.all([
      apiFetch<TeamSessionView>(`/team/${teamId}`),
      apiFetch(`/team/${teamId}/members-enriched`),
    ]).then(([view, enrichedMembers]) => {
      return {
        sessionId: view.sessionId || view.teamId || teamId,
        missionId: view.missionId,
        players: enrichedMembers, // Full DTOs w/ name/portrait/role/connected
        phase: view.phase || mapPhase(view.status),
        evidenceCount: view.evidenceCount,
        evidence: view.evidence,
        trust: view.trust,
        accusation: view.accusation,
        activity: view.activity || [],
        accusationUnlocked: view.accusationUnlocked,
        accusationResult: view.accusationResult,
      };
    }),
  enabled: !!teamId,
});

export const useCreateTeam = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationKey: ['createTeam'],
    mutationFn: (missionId?: string) =>
      apiFetch<unknown>('/team/create', {
        method: 'POST',
        data: JSON.stringify({ missionId }),
      }),
    onMutate: () => prepareMutationContext(queryClient, ['createTeam'], 'createTeam'),
  });
};

export const useJoinTeam = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationKey: ['joinTeam'],
    mutationFn: (teamId: string) =>
      apiFetch<unknown>('/team/join', {
        method: 'POST',
        data: JSON.stringify({ teamId }),
      }),
    onMutate: (teamId) =>
      prepareMutationContext(queryClient, ['joinTeam'], 'joinTeam').then((context) => ({
        ...context,
        teamId,
      })),
  });
};

export const useAddTeamEvidence = (teamId: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationKey: ['addTeamEvidence', teamId],
    mutationFn: (evidenceType: string = 'clue') =>
      apiFetch(`/team/${teamId}/evidence`, {
        method: 'POST',
        data: JSON.stringify({ evidenceType }),
      }),
    onMutate: () => prepareMutationContext(queryClient, ['addTeamEvidence', teamId], 'addTeamEvidence'),
  });
};

export const useToggleTeamReady = (teamId: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationKey: ['toggleTeamReady', teamId],
    mutationFn: (ready: boolean) =>
      apiFetch(`/team/${teamId}/ready`, {
        method: 'POST',
        data: JSON.stringify({ ready }),
      }),
    onMutate: () => prepareMutationContext(queryClient, ['toggleTeamReady', teamId], 'toggleTeamReady'),
  });
};

export const useAccuseTeam = (teamId: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationKey: ['accuseTeam', teamId],
    mutationFn: (accusedId: string) =>
      apiFetch(`/team/${teamId}/accuse`, {
        method: 'POST',
        data: JSON.stringify({ accusedId }),
      }),
    onMutate: () => prepareMutationContext(queryClient, ['accuseTeam', teamId], 'accuseTeam'),
  });
};

export const useStartTeam = (teamId: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationKey: ['startTeam', teamId],
    mutationFn: () => apiFetch(`/team/${teamId}/start`, { method: 'POST' }),
    onMutate: () => prepareMutationContext(queryClient, ['startTeam', teamId], 'startTeam'),
  });
};
