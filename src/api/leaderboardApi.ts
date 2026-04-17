import { useQuery } from '@tanstack/react-query';
import { apiFetch } from '@/lib/apiClient';

interface LeaderboardEntry {
  id: string;
  displayName: string;
  score: number;
  solves: number;
  rank?: number;
  tier?: string;
}

const computeTier = (score: number): string => {
  if (score >= 5000) return 'Platinum';
  if (score >= 2500) return 'Gold';
  if (score >= 1000) return 'Silver';
  if (score >= 100) return 'Bronze';
  return 'Rookie';
};

export const useLeaderboard = () => {
  return useQuery<LeaderboardEntry[]>({
    queryKey: ['leaderboard'],
    queryFn: async () => {
      const data = await apiFetch('/leaderboard');
      return data.map((entry: LeaderboardEntry) => ({
        ...entry,
        tier: computeTier(entry.score)
      }));
    },
    staleTime: 5 * 60 * 1000,
  });
};

