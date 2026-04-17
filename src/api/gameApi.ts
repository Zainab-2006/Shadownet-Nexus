import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { apiFetch } from '../lib/apiClient';
import { GameplayState } from '@/types/gameplay';

export interface SolveChallengeRequest {
  challengeId: string;
  flag: string;
}

export const useGameState = () => useQuery<Partial<GameplayState>>({
  queryKey: ['gameState'],
  queryFn: () => apiFetch('/users/me/story-progress'),
  enabled: !!localStorage.getItem('token'),
});

export const useSolveChallenge = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({challengeId, flag}: SolveChallengeRequest) => 
      apiFetch('/submit-flag', {
        method: 'POST',
        data: JSON.stringify({challengeId, flag}),
      }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['gameState'] }),
  });
};

export const useUpdateScore = () => useMutation({
  mutationFn: async () => {
    throw new Error('Score mutation is backend-authoritative and must happen through challenge/story/team endpoints.');
  },
});

export const useUpdateMissionProgress = () => useMutation({
  mutationFn: async () => {
    throw new Error('Client-authored mission progress mutation is retired. Use backend-authored consequence endpoints.');
  },
});
