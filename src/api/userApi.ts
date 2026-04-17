import { useQuery } from '@tanstack/react-query';
import { apiFetch } from '../lib/apiClient';

export interface UserProgression {
  userId?: string;
  totalXp: number;
  currentLevel: number;
  totalPoints: number;
  rankPoints: number;
  challengesSolved: number;
  missionsCompleted: number;
  storyProgressPercent: number;
  solvedChallengeIds?: string[];
}

export interface User {
  id: string;
  username: string;
  displayName: string;
  email: string;
  score: number;
  xp: number;
  level: number;
  selectedOperator?: string | null;
  createdAt?: number;
  lastLoginAt?: number;
}

const toUserProgression = (user: User): UserProgression => ({
  totalXp: user.xp ?? 0,
  currentLevel: user.level ?? 1,
  totalPoints: user.score ?? 0,
  rankPoints: user.score ?? 0,
  challengesSolved: 0,
  missionsCompleted: 0,
  storyProgressPercent: 0,
  solvedChallengeIds: [],
});

export const useUserProgress = () => {
  return useQuery<UserProgression>({
    queryKey: ['userProgress'],
    queryFn: async () => {
      try {
        return await apiFetch<UserProgression>('/users/me/progress');
      } catch {
        const user = await apiFetch<User>('/users/me');
        return toUserProgression(user);
      }
    },
    staleTime: 30 * 1000,
    enabled: !!localStorage.getItem('token'),
  });
};

export const useUserProfile = () => {
  return useQuery<User>({
    queryKey: ['userProfile'],
    queryFn: () => apiFetch('/users/me'),
    staleTime: 60 * 1000,
    enabled: !!localStorage.getItem('token'),
  });
};

export const refreshUserProgress = (queryClient: any) => {
  queryClient.invalidateQueries({ queryKey: ['userProgress'] });
};
