import { apiFetch } from '../lib/apiClient';

export interface Team {
  sessionCode: string;
  missionId?: string;
  status: 'WAITING' | 'ACTIVE' | 'COMPLETE';
  members: TeamMember[];
  maxTeamSize: number;
  currentTeamSize: number;
}

export interface TeamMember {
  userId: string;
  username: string;
  isLeader: boolean;
  isReady: boolean;
}

export const teamApi = {
  createSession: async (): Promise<Team> => {
    return apiFetch('/team/create', {
      method: 'POST',
      data: JSON.stringify({}),
    });
  },

  joinSession: async (sessionCode: string): Promise<{ status: string }> => {
    return apiFetch('/team/join', {
      method: 'POST',
      data: JSON.stringify({ teamId: sessionCode }),
    });
  },

  getSession: async (sessionCode: string): Promise<Team> => {
    return apiFetch(`/team/${sessionCode}`);
  },

  setReady: async (sessionCode: string, ready: boolean): Promise<{ status: string }> => {
    return apiFetch(`/team/${sessionCode}/ready`, {
      method: 'POST',
      data: JSON.stringify({ ready }),
    });
  },

  startSession: async (sessionCode?: string): Promise<void> => {
    if (!sessionCode) {
      throw new Error('Team session code is required to start a team mission.');
    }
    return apiFetch(`/team/${sessionCode}/start`, { method: 'POST' });
  }
};
