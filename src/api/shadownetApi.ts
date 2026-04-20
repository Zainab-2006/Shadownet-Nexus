import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { apiFetch } from '../lib/apiClient';

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

const parseJsonObject = (value?: string | Record<string, unknown>) => {
  if (!value) return {} as Record<string, unknown>;
  if (typeof value !== 'string') return value;

  try {
    return JSON.parse(value) as Record<string, unknown>;
  } catch {
    return {} as Record<string, unknown>;
  }
};

const parseJsonArray = (value?: string | string[]) => {
  if (Array.isArray(value)) return value.flat().map(String);
  if (!value) return [] as string[];

  try {
    const parsed = JSON.parse(value);
    return Array.isArray(parsed) ? parsed.flat().map(String) : [];
  } catch {
    return [] as string[];
  }
};

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

export const useLogin = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ email, password }: { email: string; password: string }) => 
      apiFetch('/login', { method: 'POST', data: JSON.stringify({ email, password }) }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['user'] }),
  });
};

export const useRegister = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ email, username, password }: { email: string; username: string; password: string }) => 
      apiFetch('/register', { method: 'POST', data: JSON.stringify({ email, username, password }) }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['user'] }),
  });
};

export const useUser = () => useQuery({
  queryKey: ['user'],
  queryFn: () => apiFetch('/users/me'),
});

export const useMissions = () => useQuery({
  queryKey: ['missions'],
  queryFn: async () => {
    const missions = await apiFetch<BackendMission[]>('/missions');
    return missions.map(normalizeMission);
  },
});

export const useMission = (missionId?: string) => useQuery({
  queryKey: ['mission', missionId],
  queryFn: async () => normalizeMission(await apiFetch<BackendMission>(`/missions/${missionId}`)),
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
    mutationFn: () => apiFetch<MissionRuntimeState>(`/missions/${missionId}/runtime/start`, { method: 'POST' }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['missionRuntime', missionId] });
      queryClient.invalidateQueries({ queryKey: ['missions'] });
      queryClient.invalidateQueries({ queryKey: ['missionProgress'] });
    },
  });
};

export const useUpdateMissionObjective = (missionId?: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ objectiveId, complete }: { objectiveId: string; complete: boolean }) =>
      apiFetch<MissionRuntimeState>(`/missions/${missionId}/runtime/objective`, {
        method: 'POST',
        data: JSON.stringify({ objectiveId, complete }),
      }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['missionRuntime', missionId] }),
  });
};

export const useCompleteMissionRuntime = (missionId?: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => apiFetch<MissionRuntimeState>(`/missions/${missionId}/runtime/complete`, { method: 'POST' }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['missionRuntime', missionId] });
      queryClient.invalidateQueries({ queryKey: ['mission', missionId] });
      queryClient.invalidateQueries({ queryKey: ['missions'] });
      queryClient.invalidateQueries({ queryKey: ['missionProgress'] });
      queryClient.invalidateQueries({ queryKey: ['userProgress'] });
      queryClient.invalidateQueries({ queryKey: ['leaderboard'] });
    },
  });
};

export const useApplyMissionAction = (missionId?: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (action: MissionAction) =>
      apiFetch(`/missions/${missionId}/action`, {
        method: 'POST',
        data: JSON.stringify({ action }),
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['mission', missionId] });
      queryClient.invalidateQueries({ queryKey: ['missions'] });
      queryClient.invalidateQueries({ queryKey: ['missionProgress'] });
    },
  });
};

export const useCreateMissionSession = () => {
  return useMutation({
    mutationFn: async () => {
      throw new Error('Standalone mission sessions are retired. Use backend-authored team creation or mission runtime endpoints.');
    },
  });
};

export const useTeamSession = (teamId: string) => useQuery({
  queryKey: ['teamSession', teamId],
  queryFn: async () => {
    const view = await apiFetch(`/team/${teamId}`);
    const enrichedMembers = await apiFetch(`/team/${teamId}/members-enriched`);
    return {
      sessionId: view.sessionId || view.teamId || teamId,
      missionId: view.missionId,
      players: enrichedMembers, // Full DTOs w/ name/portrait/role/connected
      phase: view.phase || (function mapPhase(status) {
        switch (status) {
          case "ACCUSATION_RESOLVED":
          case "ACCUSATION_UNLOCKED":
            return 'accusation';
          case "active":
            return 'active';
          default:
            return 'lobby';
        }
      })(view.status),
      evidenceCount: view.evidenceCount,
      evidence: view.evidence,
      trust: view.trust,
      accusation: view.accusation,
      activity: view.activity || [],
      accusationUnlocked: view.accusationUnlocked,
      accusationResult: view.accusationResult,
    };
  },
  enabled: !!teamId,
});

export const useCreateTeam = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (missionId?: string) =>
      apiFetch<unknown>('/team/create', {
        method: 'POST',
        data: JSON.stringify({ missionId }),
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['teamSession'] });
      queryClient.invalidateQueries({ queryKey: ['missions'] });
    },
  });
};

export const useJoinTeam = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (teamId: string) =>
      apiFetch<unknown>('/team/join', {
        method: 'POST',
        data: JSON.stringify({ teamId }),
      }),
    onSuccess: (_data, teamId) => {
      queryClient.invalidateQueries({ queryKey: ['teamSession', teamId] });
    },
  });
};

export const useAddTeamEvidence = (teamId: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (evidenceType: string = 'clue') =>
      apiFetch(`/team/${teamId}/evidence`, {
        method: 'POST',
        data: JSON.stringify({ evidenceType }),
      }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['teamSession', teamId] }),
  });
};

export const useToggleTeamReady = (teamId: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (ready: boolean) =>
      apiFetch(`/team/${teamId}/ready`, {
        method: 'POST',
        data: JSON.stringify({ ready }),
      }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['teamSession', teamId] }),
  });
};

export const useAccuseTeam = (teamId: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (accusedId: string) =>
      apiFetch(`/team/${teamId}/accuse`, {
        method: 'POST',
        data: JSON.stringify({ accusedId }),
      }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['teamSession', teamId] }),
  });
};

export const useStartTeam = (teamId: string) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => apiFetch(`/team/${teamId}/start`, { method: 'POST' }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['teamSession', teamId] }),
  });
};
