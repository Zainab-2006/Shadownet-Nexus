import { apiFetch } from '../lib/apiClient';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';

export interface PuzzleSession {
  id: string;
  currentStage: number;
  hintsUsed: number;
  challenge: any;
  completed: boolean;
}

export const getPuzzleSession = (challengeId: string): Promise<PuzzleSession> =>
  apiFetch(`/puzzle/session/${challengeId}`) as Promise<PuzzleSession>;

export interface SubmitStagePayload {
  sessionId: string;
  stageNumber: number;
  flag: string;
  trainingMode?: boolean;
  rankedEligible?: boolean;
  solutionRevealed?: boolean;
  narratorTriggered?: boolean;
}

export const submitStage = (payload: SubmitStagePayload): Promise<any> =>
  apiFetch('/puzzle/submit', {
    method: 'POST',
    body: JSON.stringify(payload)
  });

export const getHint = (sessionId: string): Promise<any> =>
  apiFetch('/puzzle/hint', {
    method: 'POST',
    body: JSON.stringify({ sessionId })
  });

export const usePuzzleSession = (challengeId: string) => {
  return useQuery({
    queryKey: ['puzzleSession', challengeId],
    queryFn: () => getPuzzleSession(challengeId),
    enabled: !!challengeId && !!localStorage.getItem('token'),
    retry: false,
  });
};

export const useSubmitStage = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload: SubmitStagePayload) =>
      submitStage(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['puzzleSession'] });
    },
  });
};

export const useGetHint = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (sessionId: string) => getHint(sessionId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['puzzleSession'] });
    },
  });
};
